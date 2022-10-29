package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

/*
 * @author Sandeep
 */

public class ScheduledUploadActivity extends AppCompatActivity {

    private Button btn_update, back_button;
    private EditText txt_UploadIntervalHH;
    private EditText txt_UploadIntervalMM;
    private EditText txt_UploadStartTimeHH;
    private EditText txt_UploadStartTimeMM;
    private String interval;
    private String startTime;

    private Thread thread;
    private ProgressDialog progress_dialog;
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SHOW_MSG:
                    progress_dialog.dismiss();
                    Toast.makeText(ScheduledUploadActivity.this,
                            msg.getData().getString(Constants.SHOW_MSG_KEY), Toast.LENGTH_LONG).show();
                    break;
                case Constants.SHOW_PARAMETER:
                    progress_dialog.dismiss();
                    displayData();
                    break;
            }
        }
    };
    private Constants consts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_upload_new);

        txt_UploadIntervalHH = findViewById(R.id.txt_UploadIntervalHH);
        txt_UploadIntervalMM = findViewById(R.id.txt_UploadIntervalMM);
        txt_UploadStartTimeHH = findViewById(R.id.txt_UploadStartTimeHH);
        txt_UploadStartTimeMM = findViewById(R.id.txt_UploadStartTimeMM);
        btn_update = findViewById(R.id.btn_update);
        back_button = findViewById(R.id.buttonBack11);

        consts = new Constants();

        initProgressDialog("Reading schedule upload information...");
        progress_dialog.show();
        thread = new Thread() {
            public void run() {
                if (readParameters()) {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_PARAMETER);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SHOW_MSG_KEY, "Unable to get schedule upload information");
                    msg.setData(bundle);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        thread.start();

        back_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btn_update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Tool.controlButtonDebouncing(btn_update);


                if (Variable.isConnected) {
                    if (Variable.scanStatus) {
                        new AlertDialog.Builder(ScheduledUploadActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.scan_stop_msg)
                                .setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (consts.scanStart_Stop("STOP")) {
                                                    Toast.makeText(ScheduledUploadActivity.this,
                                                            "Scan Stopped !!", Toast.LENGTH_SHORT).show();
                                                    updateSchedule();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Unable to stop scan !!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }).setNegativeButton("No", null).show();
                    } else {
                        updateSchedule();
                    }
                } else {
                    datalogger_not_connected_dialog();
                }

            }
        });

    }

    private void datalogger_not_connected_dialog() {
        new AlertDialog.Builder(ScheduledUploadActivity.this)
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

    private void updateSchedule() {


        String tmpStr = txt_UploadIntervalHH.getText().toString();
        int tmpInt = 0;

        if (TextUtils.isEmpty(tmpStr)) {
            txt_UploadIntervalHH.setError("Hour can't be empty");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        try {
            tmpInt = Integer.parseInt(tmpStr);

            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);

        } catch (Exception e) {
            txt_UploadIntervalHH.setError("Hour should be integer");
            return;
        }
        if (tmpInt > 168) {
            txt_UploadIntervalHH.setError("Hour should be less than or equals 168 hour");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }

        tmpStr = txt_UploadIntervalMM.getText().toString();

        if (TextUtils.isEmpty(tmpStr)) {
            txt_UploadIntervalMM.setError("Minute can't be empty");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        try {
            tmpInt = Integer.parseInt(tmpStr);

            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);

        } catch (Exception e) {
            txt_UploadIntervalMM.setError("Minute should be integer");
            return;
        }
        if (tmpInt > 59) {
            txt_UploadIntervalMM.setError("Minute should be less than or equals 59 minute");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }

        int totalMinutes = (60 * Integer.parseInt(txt_UploadIntervalHH.getText().toString())) + Integer.parseInt(txt_UploadIntervalMM.getText().toString());

        if (totalMinutes > 10080) {
            txt_UploadIntervalHH.setError("Interval should be less than or equals 168 hour");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        if (totalMinutes == 10080) {
            if (tmpInt > 0) {
                txt_UploadIntervalHH.setError("Interval should be less than or equals 168 hour");
                return;
            } else {
                txt_UploadIntervalHH.setError(null);
                txt_UploadIntervalMM.setError(null);
            }
        }

        if (totalMinutes < 5) {

            if (tmpInt < 4) {
                txt_UploadIntervalMM.setError("Interval should be greater than or equals 5 Minutes");
                return;
            } else {
                txt_UploadIntervalHH.setError(null);
                txt_UploadIntervalMM.setError(null);
            }
        }
        tmpStr = txt_UploadStartTimeHH.getText().toString();

        if (TextUtils.isEmpty(tmpStr)) {
            txt_UploadStartTimeHH.setError("Hour can't be empty");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        try {
            tmpInt = Integer.parseInt(tmpStr);

            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);

        } catch (Exception e) {
            txt_UploadStartTimeHH.setError("Hour should be integer");
            return;
        }
        if (tmpInt > 23) {
            txt_UploadStartTimeHH.setError("Hour should be less than or equals 23 hour");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        tmpStr = txt_UploadStartTimeMM.getText().toString();

        if (TextUtils.isEmpty(tmpStr)) {
            txt_UploadStartTimeMM.setError("Minute can't be empty");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }
        try {
            tmpInt = Integer.parseInt(tmpStr);

            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);

        } catch (Exception e) {
            txt_UploadStartTimeMM.setError("Minute should be integer");
            return;
        }
        if (tmpInt > 59) {
            txt_UploadStartTimeMM.setError("Minute should be less than or equals 59 minute");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }

        totalMinutes = (60 * Integer.parseInt(txt_UploadStartTimeHH.getText().toString())) + Integer.parseInt(txt_UploadStartTimeMM.getText().toString());

        if (totalMinutes >= 1440) {
            txt_UploadStartTimeHH.setError("Scan Start Time should be less than 24 hour");
            return;
        } else {
            txt_UploadIntervalHH.setError(null);
            txt_UploadIntervalMM.setError(null);
        }

        initProgressDialog("Updating upload time information ...");
        progress_dialog.show();
        thread = new Thread() {
            public void run() {

                try {
                    if (consts.wakeUpDL()) {
                        String interval = Tool.pad(txt_UploadIntervalHH.getText().toString().trim(), 3) + ":"
                                + Tool.pad(txt_UploadIntervalMM.getText().toString().trim(), 2) + ":00";

                        String startTime = Tool.pad(txt_UploadStartTimeHH.getText().toString().trim(), 2) + ":"
                                + Tool.pad(txt_UploadStartTimeMM.getText().toString().trim(), 2) + ":00";

                        consts.sendCMDgetRLY("ULDINT,\"" + interval + "\"");

                        if (Constants.OK_STATUS == Constants.sc) {
                            consts.sendCMDgetRLY("ULDTIME,\"" + startTime + "\"");
                        }

                        if (Constants.OK_STATUS == Constants.sc) {
                            Variable.error_msg = "Upload time information updated !!";
                        } else {
                            Variable.error_msg = "Unable to update upload time information !!";
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
        };
        thread.start();
    }


    private boolean readParameters() {
        try {
            if (consts.wakeUpDL()) {
                interval = consts.removeDQ(consts.sendCMDgetRLY("ULDINT,\"?\""));
                if (Constants.OK_STATUS == Constants.sc) {
                    startTime = consts.removeDQ(consts.sendCMDgetRLY("ULDTIME,\"?\""));
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void displayData() {
        try {
            String uploadInterval[] = interval.split(":");
            txt_UploadIntervalHH.setText(uploadInterval[0]);
            txt_UploadIntervalMM.setText(uploadInterval[1]);
            String uploadStartTime[] = startTime.trim().split(":");
            txt_UploadStartTimeHH.setText(uploadStartTime[0]);
            txt_UploadStartTimeMM.setText(uploadStartTime[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProgressDialog(String msg) {
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setTitle("Please wait !!");
        progress_dialog.setCancelable(false);
        progress_dialog.setMessage(msg);
    }

}
