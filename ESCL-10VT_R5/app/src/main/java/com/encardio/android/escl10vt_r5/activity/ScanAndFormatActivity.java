package com.encardio.android.escl10vt_r5.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;


/**
 * @author Sandeep
 */
public class ScanAndFormatActivity extends AppCompatActivity {

    protected static final int UPDATE_SCAN_STATUS_VALUES = 3;
    protected static final int REFRESH_RECORDS = 4;

    String check_avg;

    boolean isParaErrorValueNumeric = false;
    boolean isTempErrorValueNumeric = false;
    String[] spinner_memoryFullAction_value = {"OVERWRITE", "STOP"};
    ArrayAdapter<String> arrayAdapter_memoryFullAction;
    String[] spinner_paraErrorValueOption_value = {"DISABLE", "ENABLE"};
    ArrayAdapter<String> arrayAdapter_paraErrorValueOption;
    private Constants consts;
    private EditText editTextHour;
    private EditText editTextMinute;

    private EditText editTextHourScanStartTime;
    private EditText editTextMinuteScanStartTime;
    private ImageView iv_refresh_scan_status;
    private TextView textViewNoOfRec;
    private Button buttonUpdateInScan;
    private Button buttonAlarmEventLog;
    private Spinner spinner_memoryFullAction;
    private Button buttonStartStopScan;
    private Button buttonLoggerMemory;
    private Spinner spinner_paraErrorValueOption;
    private EditText txt_ParaErrorValue;
    private EditText txt_TempErrorValue;
    private ImageView imageViewScanStatus;
    private ProgressDialog progress_dialog;
    private Thread thread;
    private TextView txt_TotalRecordFromLastDownload;
    private TextView txt_TotalRecordFromLastUpload;

    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SHOW_MSG:
                    progress_dialog.dismiss();
                    show_toast(msg.getData().getString(Constants.SHOW_MSG_KEY));
                    break;
                case Constants.SHOW_PARAMETER:
                    progress_dialog.dismiss();
                    displayData();
                    break;
                case REFRESH_RECORDS:
                    progress_dialog.dismiss();
                    refresh_records();
                    break;
                case UPDATE_SCAN_STATUS_VALUES:
                    if (Variable.scanStatus) {
                        buttonStartStopScan.setText("STOP SCAN");
                        imageViewScanStatus.setImageResource(R.drawable.circle_green);
                    } else {
                        buttonStartStopScan.setText("START SCAN");
                        imageViewScanStatus.setImageResource(R.drawable.circle_red);
                    }
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_and_format);

        editTextHour = findViewById(R.id.editTextHour);
        editTextMinute = findViewById(R.id.editTextMinute);

        editTextHourScanStartTime = findViewById(R.id.editTextHourScanStartTime);
        editTextMinuteScanStartTime = findViewById(R.id.editTextMinuteScanStartTime);
        textViewNoOfRec = findViewById(R.id.textViewNoOfRec);
        buttonStartStopScan = findViewById(R.id.buttonStartStopScan);
        buttonLoggerMemory = findViewById(R.id.buttonLoggerMemory);
        buttonUpdateInScan = findViewById(R.id.buttonUpdateInScan);
        spinner_memoryFullAction = findViewById(R.id.spinner_memoryFullAction);
        spinner_paraErrorValueOption = findViewById(R.id.spinner_paraErrorValueOption);
        buttonAlarmEventLog = findViewById(R.id.btnAlarmEventLog);
        imageViewScanStatus = findViewById(R.id.imageViewScanStatus);
        txt_ParaErrorValue = findViewById(R.id.txt_ParaErrorValue);
        txt_TempErrorValue = findViewById(R.id.txt_TempErrorValue);
        txt_TotalRecordFromLastDownload = findViewById(R.id.txt_TotalRecordFromLastDownload);
        txt_TotalRecordFromLastUpload = findViewById(R.id.txt_TotalRecordFromLastUpload);
        iv_refresh_scan_status = findViewById(R.id.iv_refresh_scan_status);

        arrayAdapter_memoryFullAction = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, spinner_memoryFullAction_value);
        arrayAdapter_memoryFullAction.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_memoryFullAction.setAdapter(arrayAdapter_memoryFullAction);

        arrayAdapter_paraErrorValueOption = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, spinner_paraErrorValueOption_value);
        arrayAdapter_paraErrorValueOption.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_paraErrorValueOption.setAdapter(arrayAdapter_paraErrorValueOption);

        consts = new Constants();

        initProgressDialog("Reading Scan Information !!");
        progress_dialog.show();
        thread = new Thread() {
            public void run() {

                if (readParameters()) {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_PARAMETER);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SHOW_MSG_KEY, "Unable to get Scan Information");
                    msg.setData(bundle);
                    setTextHandler.sendMessage(msg);
                }
            }

        };
        thread.start();

        buttonStartStopScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Tool.controlButtonDebouncing(buttonStartStopScan);
                try {
                    if (Variable.isConnected) {
                        if (consts.wakeUpDL()) {
                            if (consts.removeDQ(consts.sendCMDgetRLY("COMMODE,\"?\"")).equals("GPRS")) {
                                Toast.makeText(ScanAndFormatActivity.this, "Datalogger is busy in uploading data to Remote FTP server. Please retry after few minutes!!", Toast.LENGTH_LONG).show();
                            } else {
                                scan_start_stop();
                            }
                        }
                    } else {
                        datalogger_not_connected_dialog();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Exception occurred " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonUpdateInScan.setOnClickListener(v -> {
            Tool.controlButtonDebouncing(buttonUpdateInScan);

            if (Variable.isConnected) {

                if (validateScan()) {
                    if (Variable.scanStatus) {
                        new AlertDialog.Builder(ScanAndFormatActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.scan_stop_msg)
                                .setPositiveButton("yes",
                                        (dialog, which) -> {

                                            initProgressDialog("Updating scan information ...");
                                            progress_dialog.show();

                                            thread = new Thread() {
                                                public void run() {
                                                    if (consts.scanStart_Stop("STOP")) {
                                                        Message msg = setTextHandler.obtainMessage(UPDATE_SCAN_STATUS_VALUES);
                                                        setTextHandler.sendMessage(msg);
                                                        scanUpdate();
                                                    } else {
                                                        Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString(Constants.SHOW_MSG_KEY, "Unable to stop scan !!");
                                                        msg.setData(bundle);
                                                        setTextHandler.sendMessage(msg);
                                                    }
                                                }
                                            };
                                            thread.start();
                                        }).setNegativeButton("No", null).show();
                    } else {
                        initProgressDialog("Updating scan information...");
                        progress_dialog.show();
                        thread = new Thread() {
                            public void run() {

                                scanUpdate();
                            }
                        };
                        thread.start();
                    }
                }
            } else {
                datalogger_not_connected_dialog();
            }
        });
        buttonLoggerMemory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Tool.controlButtonDebouncing(buttonLoggerMemory);
                if (Variable.isConnected) {
                    Intent intent = new Intent(v.getContext(), CSVFileHeaderActivity.class);
                    startActivity(intent);
                } else {
                    datalogger_not_connected_dialog();
                }
            }
        });
        buttonAlarmEventLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tool.controlButtonDebouncing(buttonAlarmEventLog);
                if (Variable.isConnected) {
                    Intent intent = new Intent(v.getContext(), AlarmEventActivity.class);
                    startActivity(intent);
                } else {
                    datalogger_not_connected_dialog();
                }
            }
        });

        iv_refresh_scan_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tool.controlImageViewDebouncing(iv_refresh_scan_status);
                if (Variable.isConnected) {
                    initProgressDialog("Refreshing Scan Record Information !!");
                    progress_dialog.show();
                    thread = new Thread() {
                        public void run() {
                            if (refresh_scan_status()) {
                                Message msg = setTextHandler.obtainMessage(REFRESH_RECORDS);
                                setTextHandler.sendMessage(msg);
                            } else {
                                Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.SHOW_MSG_KEY, "Unable to refresh Scan Record Information");
                                msg.setData(bundle);
                                setTextHandler.sendMessage(msg);
                            }
                        }

                    };
                    thread.start();
                } else {
                    datalogger_not_connected_dialog();
                }
            }
        });
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (Variable.scanStatus) {
            buttonStartStopScan.setText("STOP SCAN");
            imageViewScanStatus.setImageResource(R.drawable.circle_green);
        } else {
            buttonStartStopScan.setText("START SCAN");
            imageViewScanStatus.setImageResource(R.drawable.circle_red);
        }

        textViewNoOfRec.setText(Variable.totalNumberOfRecord);
        txt_TotalRecordFromLastDownload.setText(Variable.totalNumberOfRecordFromLastDownload);
        txt_TotalRecordFromLastUpload.setText(Variable.totalNumberOfRecordFromLastUpload);

    }

    private void show_toast(String msg) {
        Toast.makeText(ScanAndFormatActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void datalogger_not_connected_dialog() {
        new AlertDialog.Builder(ScanAndFormatActivity.this)
                .setTitle("Logger Not Connected !!")
                .setIcon(R.drawable.info)
                .setMessage(
                        "Logger is not connected. Please connect the Logger.")
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                            }
                        }).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void scanUpdate() {


        try {
            if (consts.wakeUpDL()) {
                String logInterval = Tool.pad(editTextHour.getText().toString().trim(), 3) + ":"
                        + Tool.pad(editTextMinute.getText().toString().trim(), 2) + ":00";

                String nextScanStartTime = Tool.pad(editTextHourScanStartTime.getText().toString().trim(), 2) + ":"
                        + Tool.pad(editTextMinuteScanStartTime.getText().toString().trim(), 2) + ":00";


                consts.sendCMDgetRLY("LOGINT,\"" + logInterval + "\"");

                if (Constants.OK_STATUS == Constants.sc) {
                    consts.sendCMDgetRLY("SSTIME,\"" + nextScanStartTime + "\"");
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.memoryFullAction = spinner_memoryFullAction.getSelectedItem().toString().trim();
                    if (Variable.memoryFullAction.equalsIgnoreCase("OVERWRITE")) {
                        consts.sendCMDgetRLY("MEMFULL,\"" + "OVERWR" + "\"");
                    } else {
                        consts.sendCMDgetRLY("MEMFULL,\"" + "STOP" + "\"");
                    }
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    consts.sendCMDgetRLY("MESERR,\"" + spinner_paraErrorValueOption.getSelectedItem().toString().trim() + "\"");
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    if (isParaErrorValueNumeric) {
                        consts.sendCMDgetRLY("PARAERRC,\"" + txt_ParaErrorValue.getText() + "\"");
                    } else {
                        consts.sendCMDgetRLY("PARAERRC,\"\"" + txt_ParaErrorValue.getText() + "\"\"");
                    }
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    if (isTempErrorValueNumeric) {
                        consts.sendCMDgetRLY("TEMPERRC,\"" + txt_TempErrorValue.getText() + "\"");
                    } else {
                        consts.sendCMDgetRLY("TEMPERRC,\"\"" + txt_TempErrorValue.getText() + "\"\"");
                    }
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.error_msg = "Scan information updated !!";
                } else {
                    Variable.error_msg = "Unable to update scan information !!";
                }
            }
        } catch (Exception e) {
            Variable.error_msg = "Exception occurred !!";
        }
        Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SHOW_MSG_KEY, Variable.error_msg);
        msg.setData(bundle);
        setTextHandler.sendMessage(msg);

    }


    private boolean readParameters() {

        try {
            if (consts.wakeUpDL()) {

                Variable.logInterval = consts.removeDQ(consts
                        .sendCMDgetRLY("LOGINT,\"?\""));

                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.scanStartTime = consts.removeDQ(consts
                            .sendCMDgetRLY("SSTIME,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.memoryFullAction = consts.removeDQ(consts
                            .sendCMDgetRLY("MEMFULL,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.paraErrorOption = consts.removeDQ(consts
                            .sendCMDgetRLY("MESERR,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.paraErrorValue = consts.removeDQ(consts
                            .sendCMDgetRLY("PARAERRC,\"?\""));
                    if (Variable.paraErrorValue.contains("\"")) {
                        Variable.paraErrorValue = consts.removeDQ(Variable.paraErrorValue);
                    }
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.tempErrorValue = consts.removeDQ(consts
                            .sendCMDgetRLY("TEMPERRC,\"?\""));
                    if (Variable.tempErrorValue.contains("\"")) {
                        Variable.tempErrorValue = consts.removeDQ(Variable.tempErrorValue);
                    }
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.totalNumberOfRecord = consts.removeDQ(consts
                            .sendCMDgetRLY("NOOFREC,0,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.totalNumberOfRecordFromLastDownload = consts.removeDQ(consts
                            .sendCMDgetRLY("NOOFREC,1,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.totalNumberOfRecordFromLastUpload = consts.removeDQ(consts
                            .sendCMDgetRLY("NOOFREC,2,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.tmp_str = consts.removeDQ(consts
                            .sendCMDgetRLY("SCAN,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.scanStatus = Variable.tmp_str.equals("START");
                    check_avg = consts.removeDQ(consts.sendCMDgetRLY("AVGSAMP,\"?\""));
                }
                return Constants.OK_STATUS == Constants.sc;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean refresh_scan_status() {

        try {
            if (consts.wakeUpDL()) {

                Variable.totalNumberOfRecord = consts.removeDQ(consts
                        .sendCMDgetRLY("NOOFREC,0,\"?\""));

                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.totalNumberOfRecordFromLastDownload = consts.removeDQ(consts
                            .sendCMDgetRLY("NOOFREC,1,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.totalNumberOfRecordFromLastUpload = consts.removeDQ(consts
                            .sendCMDgetRLY("NOOFREC,2,\"?\""));
                }
                return Constants.OK_STATUS == Constants.sc;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void refresh_records() {
        txt_TotalRecordFromLastDownload.setText(Variable.totalNumberOfRecordFromLastDownload);
        txt_TotalRecordFromLastUpload.setText(Variable.totalNumberOfRecordFromLastUpload);
        textViewNoOfRec.setText(Variable.totalNumberOfRecord);
    }

    private void displayData() {
        try {

            String[] scanInterval = Variable.logInterval.split(":");
            editTextHour.setText(scanInterval[0]);
            editTextMinute.setText(scanInterval[1]);

            String[] scanStartTime = Variable.scanStartTime.split(":");
            editTextHourScanStartTime.setText(scanStartTime[0]);
            editTextMinuteScanStartTime.setText(scanStartTime[1]);

            if (Variable.memoryFullAction.equals("OVERWR")) {
                spinner_memoryFullAction.setSelection(arrayAdapter_memoryFullAction.getPosition("OVERWRITE"));
            } else {
                spinner_memoryFullAction.setSelection(arrayAdapter_memoryFullAction.getPosition("STOP"));
            }

            spinner_paraErrorValueOption.setSelection(arrayAdapter_paraErrorValueOption.getPosition(Variable.paraErrorOption));

            txt_ParaErrorValue.setText(Variable.paraErrorValue);
            txt_TempErrorValue.setText(Variable.tempErrorValue);
            txt_TotalRecordFromLastDownload.setText(Variable.totalNumberOfRecordFromLastDownload);
            txt_TotalRecordFromLastUpload.setText(Variable.totalNumberOfRecordFromLastUpload);
            textViewNoOfRec.setText(Variable.totalNumberOfRecord);

            if (Variable.scanStatus) {
                buttonStartStopScan.setText("STOP SCAN");
                imageViewScanStatus.setImageResource(R.drawable.circle_green);
            } else {
                buttonStartStopScan.setText("START SCAN");
                imageViewScanStatus.setImageResource(R.drawable.circle_red);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean validateScan() {
        try {

            String tmpStr = editTextHour.getText().toString();
            int tmpInt = 0;

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Hour can't be empty");
                return false;
            }
            try {
                tmpInt = Integer.parseInt(tmpStr);
            } catch (Exception e) {

                show_toast("Hour should be integer");
                return false;
            }
            if (tmpInt > 168) {
                show_toast("Hour should be less than or equals 168 hour");
                return false;
            }

            tmpStr = editTextMinute.getText().toString();

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Minute can't be empty");
                return false;
            }
            try {
                tmpInt = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                show_toast("Minute should be integer");
                return false;
            }
            if (tmpInt > 59) {
                show_toast("Minute should be less than or equals 59 minute");
                return false;
            }


            int totalMinutes = (60 * Integer.parseInt(editTextHour.getText().toString())) + Integer.parseInt(editTextMinute.getText().toString());
//            if (totalMinutes >= 4) {
//                if (tmpInt > 0) {
//                    show_toast("Second should be zero if scan interval is greater or equals 4 minutes");
//                    return false;
//                }
//            }

            if (totalMinutes > 10080) {
                show_toast("Scan Interval should be less than or equals 168 hour");
                return false;
            }

//            if (totalMinutes == 10080) {
//                if (tmpInt > 0) {
//                    show_toast("Scan Interval should be less than or equals 168 hour");
//                    return false;
//                }
//            }

//            if (totalMinutes < 1) {
//
//                if (tmpInt < 4) {
//                    show_toast("Scan Interval should be greater than or equals 5 second");
//                    return false;
//                }
//            }

            String logInterval = Tool.pad(editTextHour.getText().toString().trim(), 3) + ":"
                    + Tool.pad(editTextMinute.getText().toString().trim(), 2) + ":00";

            if (!((Integer.parseInt(check_avg) * 2) < Tool.HHHMMSSTosecond(logInterval))) {
                show_toast("Scan Interval can not be less than 2x (Samples Averaged)");
                return false;
            }

            tmpStr = editTextHourScanStartTime.getText().toString();

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Hour can't be empty");
                return false;
            }
            try {
                tmpInt = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                show_toast("Hour should be integer");
                return false;
            }
            if (tmpInt > 23) {
                show_toast("Hour should be less than or equals 23 hour");
                return false;
            }

            tmpStr = editTextMinuteScanStartTime.getText().toString();

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Minute can't be empty");
                return false;
            }
            try {
                tmpInt = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                show_toast("Minute should be integer");
                return false;
            }
            if (tmpInt > 59) {
                show_toast("Minute should be less than or equals 59 minute");
                return false;
            }

            totalMinutes = (60 * Integer.parseInt(editTextHourScanStartTime.getText().toString())) + Integer.parseInt(editTextMinuteScanStartTime.getText().toString());

            if (totalMinutes >= 1440) {
                show_toast("Scan Start Time should be less than 24 hour");
                return false;
            }


            tmpStr = txt_ParaErrorValue.getText().toString();

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Can't be empty");
                return false;
            }

            try {
                Float.parseFloat(tmpStr);
                isParaErrorValueNumeric = true;
            } catch (Exception e) {
                isParaErrorValueNumeric = false;
            }

            int len = tmpStr.length();
            if (isParaErrorValueNumeric) {
                if (len > 12) {
                    show_toast("Para Error Value must be upto 12 digits");
                    return false;
                }
            } else if (len > 10) {
                show_toast("Para Error Value must be upto 10 digits");
                return false;
            }

            tmpStr = txt_TempErrorValue.getText().toString();

            if (TextUtils.isEmpty(tmpStr)) {
                show_toast("Can't be empty");
                return false;
            }

            try {
                Float.parseFloat(tmpStr);
                isTempErrorValueNumeric = true;
            } catch (Exception e) {
                isTempErrorValueNumeric = false;
            }

            len = tmpStr.length();
            if (isTempErrorValueNumeric) {
                if (len > 12) {
                    show_toast("Temp Error Value must be upto 12 digits");
                    return false;
                }
            } else if (len > 10) {
                show_toast("Temp Error Value must be upto 10 digits");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void initProgressDialog(String msg) {
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setTitle("Please wait !!");
        progress_dialog.setCancelable(false);
        progress_dialog.setMessage(msg);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (Variable.isConnected) {
                if (!Variable.scanStatus) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setIcon(R.drawable.start)
                            .setTitle(R.string.quit)
                            .setMessage(
                                    "Scanning has been stopped. \nDo you want to resume scanning?")
                            .setPositiveButton(R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog1, int which) {

                                            initProgressDialog("Updating Scan ...");
                                            progress_dialog.show();

                                            thread = new Thread() {
                                                public void run() {
                                                    scanUpdate();
                                                    consts.scanStart_Stop("START");
                                                    ScanAndFormatActivity.this.finish();
                                                }
                                            };
                                            thread.start();
                                        }
                                    })
                            .setNegativeButton(R.string.no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            ScanAndFormatActivity.this.finish();
                                        }
                                    }).show();
                } else {
                    ScanAndFormatActivity.this.finish();
                }
            } else {
                ScanAndFormatActivity.this.finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }


    private void scan_start_stop() {

        if (Variable.scanStatus) {
            initProgressDialog("Stopping scan");
        } else {
            initProgressDialog("Starting scan");
        }

        progress_dialog.show();
        thread = new Thread() {
            public void run() {
                Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                Bundle bundle = new Bundle();
                try {
                    if (Variable.scanStatus) {
                        consts.scanStart_Stop("STOP");
                    } else {
                        consts.scanStart_Stop("START");
                    }


                    if (Constants.OK_STATUS == Constants.sc) {
                        if (Variable.scanStatus) {
                            bundle.putString(Constants.SHOW_MSG_KEY, "Scan started...");
                        } else {
                            bundle.putString(Constants.SHOW_MSG_KEY, "Scan stopped...");
                        }

                        Message msg1 = setTextHandler.obtainMessage(UPDATE_SCAN_STATUS_VALUES);
                        setTextHandler.sendMessage(msg1);

                    } else {
                        if (Variable.scanStatus) {
                            bundle.putString(Constants.SHOW_MSG_KEY, "Error in Stoping Scan...");
                        } else {
                            bundle.putString(Constants.SHOW_MSG_KEY, "Error in Starting Scan...");
                        }
                    }
                } catch (Exception e) {
                    bundle.putString(Constants.SHOW_MSG_KEY, "Exception occurred !!");
                }
                msg.setData(bundle);
                setTextHandler.sendMessage(msg);

            }
        };
        thread.start();
    }
}