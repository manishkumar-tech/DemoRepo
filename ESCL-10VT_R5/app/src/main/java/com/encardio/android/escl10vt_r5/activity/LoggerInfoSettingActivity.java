package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.util.Locale;

/**
 * @author Sandeep
 */
public class LoggerInfoSettingActivity extends AppCompatActivity {

    private final int MESSAGE_SETUP_UPDATING_LOGGER_SCREEN = 10;
    private final int MESSAGE_SETUP_READING_SCREEN = 20;
    private final int MESSAGE_SETUP_EXCEPTION_SCREEN = 30;
    private final int SCAN_EXCEPTION = 40;
    String err_msg = "";
    String logInterval;

    String[] spinner_lattitude_value = {"NORTH", "SOUTH"};
    String[] spinner_longitude_value = {"EAST", "WEST"};
    ArrayAdapter<String> arrayAdapter_UTC_lattitude;
    ArrayAdapter<String> arrayAdapter_UTC_longitude;
    private Button btn_update;
    private Button btnBack;
    private Spinner spinner_lattitude;
    private Spinner spinner_longitude;
    private EditText txtLoggerId;
    private EditText txtTopEle;
    private EditText txtLtDegree;
    private EditText txtLtMin;
    private EditText txtLtSec;
    private EditText txtLngDegree;
    private EditText txtLngMin;
    private EditText txtLngSec;
    private EditText txtAvgSample;
    private String loggerId;
    private String avgSample;
    private String coordinate;
    private String topEl;
    private Thread thread;
    private ProgressDialog progress_dialog;
    /**
     * The set text handler.
     */
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            switch (msg.what) {
                case Constants.SHOW_MSG:
                    progress_dialog.dismiss();
                    show_toast(msg.getData().getString(Constants.SHOW_MSG_KEY));
                    break;
                case Constants.SHOW_PARAMETER:
                    progress_dialog.dismiss();
                    displayData();
                    break;
            }

        }
    };
    private Constants consts;

    public static boolean haveWhiteSpace(final String ss) {
        if (ss != null) {
            for (int i = 0; i < ss.length(); i++) {
                if (Character.isWhitespace(ss.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.logger_info_setting);

        btn_update = (Button) findViewById(R.id.btn_update);
        btnBack = (Button) findViewById(R.id.buttonBack);
        spinner_lattitude = findViewById(R.id.spinner_lattitude);
        spinner_longitude = findViewById(R.id.spinner_longitude);

        txtLoggerId = (EditText) findViewById(R.id.editTextLoggerIdValue);
        txtTopEle = (EditText) findViewById(R.id.editTextTopElevationValue);
        txtAvgSample = (EditText) findViewById(R.id.editTextAverageSampleValue);
        txtLtDegree = (EditText) findViewById(R.id.editTextDegreeValue);
        txtLtMin = (EditText) findViewById(R.id.editTextMinValue);
        txtLtSec = (EditText) findViewById(R.id.editTextSecValue);
        txtLngDegree = (EditText) findViewById(R.id.editTextDegreeValue1);
        txtLngMin = (EditText) findViewById(R.id.editTextMinValue1);
        txtLngSec = (EditText) findViewById(R.id.editTextSecValue1);
        consts = new Constants();

        arrayAdapter_UTC_lattitude = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, spinner_lattitude_value);

        arrayAdapter_UTC_longitude = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, spinner_longitude_value);

        arrayAdapter_UTC_lattitude.setDropDownViewResource(R.layout.spinner_drop_down);
        arrayAdapter_UTC_longitude.setDropDownViewResource(R.layout.spinner_drop_down);

        spinner_lattitude.setAdapter(arrayAdapter_UTC_lattitude);
        spinner_longitude.setAdapter(arrayAdapter_UTC_longitude);


        initProgressDialog("Reading logger information ...");
        progress_dialog.show();
        thread = new Thread() {
            public void run() {
                if (readParameters()) {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_PARAMETER);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SHOW_MSG_KEY, "Unable to get logger information !!");
                    msg.setData(bundle);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        thread.start();

        buttonAction();
    }

    private void datalogger_not_connected_dialog() {
        new AlertDialog.Builder(LoggerInfoSettingActivity.this)
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

    private void buttonAction() {
        btn_update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Tool.controlButtonDebouncing(btn_update);

                if (Variable.isConnected) {
                    if (Variable.scanStatus) {
                        new AlertDialog.Builder(LoggerInfoSettingActivity.this)
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
                                                    Toast.makeText(LoggerInfoSettingActivity.this,
                                                            "Scan Stopped !!", Toast.LENGTH_SHORT).show();
                                                    update_parameters();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Unable to stop scan !!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }).setNegativeButton("No", null).show();
                    } else {
                        update_parameters();
                    }
                } else {
                    datalogger_not_connected_dialog();
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void update_parameters() {
        if (checkEmptyText()) {
            initProgressDialog("Updating logger information ...");
            progress_dialog.show();
            thread = new Thread() {
                public void run() {
                    try {
                        if (consts.wakeUpDL()) {
                            String loggerid = txtLoggerId.getText().toString().trim();
                            consts.sendCMDgetRLY("DLID,\"" + loggerid + "\"");

                            if (Constants.OK_STATUS == Constants.sc) {
                                Variable.loggerID = loggerid;
                                consts.sendCMDgetRLY("AVGSAMP," + txtAvgSample.getText().toString()
                                        .trim() + "");
                            }
                            if (Constants.OK_STATUS == Constants.sc) {
                                consts.sendCMDgetRLY("TOPELEV," + txtTopEle.getText().toString().trim() + "");
                            }

                            if (Constants.OK_STATUS == Constants.sc) {
                                String coord = locCoord();
                                consts.sendCMDgetRLY("DLCORD," + coord + "");
                            }
                            if (Constants.OK_STATUS == Constants.sc) {
                                Variable.error_msg = "Logger information updated !!";
                            } else {
                                Variable.error_msg = "Unable to update logger information !!";
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

    }

    private String locCoord() {
        float lattitude = 0f;
        float longitude = 0f;
        String lattitudeDeg = "";
        String lattitudeMin = "";
        String lattitudeSec = "";
        String longitudeDeg = "";
        String longitudeMin = "";
        String longitudeSec = "";
        String value = "";
        lattitudeDeg = txtLtDegree.getText().toString().trim();
        lattitudeMin = txtLtMin.getText().toString().trim();
        lattitudeSec = txtLtSec.getText().toString().trim();
        lattitude = Float.parseFloat(lattitudeDeg)
                + (Float.parseFloat(lattitudeMin) + Float
                .parseFloat(lattitudeSec) / 60) / 60;
        lattitude = Float.parseFloat(setDecimalDigits("" + lattitude, 6));
        longitudeDeg = txtLngDegree.getText().toString().trim();
        longitudeMin = txtLngMin.getText().toString().trim();
        longitudeSec = txtLngSec.getText().toString().trim();
        longitude = Float.parseFloat(longitudeDeg)
                + (Float.parseFloat(longitudeMin) + Float
                .parseFloat(longitudeSec) / 60) / 60;
        longitude = Float.parseFloat(setDecimalDigits("" + longitude, 6));
        if (spinner_lattitude.getSelectedItem().toString().trim().equalsIgnoreCase("SOUTH")) {
            lattitude = -1 * lattitude;
        }
        if (spinner_longitude.getSelectedItem().toString().trim().equalsIgnoreCase("WEST")) {
            longitude = -1 * longitude;
        }
        value = "" + lattitude + "," + longitude;
        return value;
    }

    private boolean readParameters() {
        try {
            if (consts.wakeUpDL()) {

                loggerId = consts.removeDQ(consts
                        .sendCMDgetRLY("DLID,\"?\""));
                if (Constants.sc == Constants.OK_STATUS) {
                    avgSample = consts.removeDQ(consts
                            .sendCMDgetRLY("AVGSAMP,\"?\""));
                }
                if (Constants.sc == Constants.OK_STATUS) {

                    topEl = "" + Float.parseFloat(consts.removeDQ(consts
                            .sendCMDgetRLY("TOPELEV,\"?\"")));
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    coordinate = consts.removeDQ(consts
                            .sendCMDgetRLY("DLCORD,\"?\""));
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    logInterval = consts
                            .removeDQ(consts
                                    .sendCMDgetRLY("LOGINT,\"?\""));
                }

                return Constants.OK_STATUS == Constants.sc;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void displayData() {
        try {

            txtLoggerId.setText(loggerId);
            txtAvgSample.setText(avgSample);
            txtTopEle.setText(topEl);
            float lattitude = 0;
            float longitude = 0;
            float deg = 0;
            float min = 0;
            float sec = 0;

            String[] a = coordinate.split(",");

            lattitude = Float.parseFloat(a[0]);
            if (lattitude < 0) {
                lattitude = -1 * lattitude;
            }
            deg = lattitude / 1;
            min = ((lattitude % 1) * 60) / 1;
            sec = (((lattitude % 1) * 60) % 1) * 60;

            if (Float.parseFloat(a[0]) >= 0) {
                spinner_lattitude.setSelection(arrayAdapter_UTC_lattitude.getPosition("NORTH"));

            } else {
                spinner_lattitude.setSelection(arrayAdapter_UTC_lattitude.getPosition("SOUTH"));

            }

            txtLtDegree.setText("" + (int) deg);
            txtLtMin.setText("" + (int) min);
            txtLtSec.setText("" + (int) sec);
            if (Float.parseFloat(a[1]) >= 0) {
                spinner_longitude.setSelection(arrayAdapter_UTC_longitude.getPosition("EAST"));

            } else {
                spinner_longitude.setSelection(arrayAdapter_UTC_longitude.getPosition("WEST"));

            }

            longitude = Float.parseFloat(a[1]);
            if (longitude < 0) {
                longitude = -1 * longitude;
            }
            deg = longitude / 1;
            min = ((longitude % 1) * 60) / 1;
            sec = (((longitude % 1) * 60) % 1) * 60;

            txtLngDegree.setText("" + (int) deg);
            txtLngMin.setText("" + (int) min);
            txtLngSec.setText("" + (int) sec);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(LoggerInfoSettingActivity.this)
                .setIcon(R.drawable.ic_battery).setTitle(Constants.responseMsg)
                .setMessage("Please replace battery immediately !!!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();

    }

    protected void showDialog() {

        new AlertDialog.Builder(LoggerInfoSettingActivity.this)
                .setTitle("Connection").setMessage("Device connection lost !")
                .setCancelable(false).setIcon(R.drawable.error)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
    }

    private boolean checkEmptyText() {

        int time = Integer.parseInt(logInterval.substring(0, 3)) * 3600
                + Integer.parseInt(logInterval.substring(4, 6)) * 60
                + Integer.parseInt(logInterval.substring(7, 9));

        try {

            if (txtTopEle.getText().toString().length() == 0) {
                show_toast("Please provide input to Top Elevation...");
                return false;
            }
            if (Variable.isTopElevationInFloat) {
                try {
                    float c = Float.parseFloat(txtTopEle.getText().toString());
                    if (c > 9999) {
                        show_toast("Top Elevation must be upto 9999");
                        return false;
                    }
                    if (c < -9999) {
                        show_toast("Top Elevation must not be less than -9999");
                        return false;
                    }
                } catch (Exception e) {
                    show_toast("Top Elevation must be numeric...");
                    return false;
                }
            } else {
                try {
                    int c = Integer.parseInt(txtTopEle.getText().toString());
                    if (c > 9999) {
                        show_toast("Top Elevation must be upto 9999");
                        return false;
                    }
                    if (c < -9999) {
                        show_toast("Top Elevation must not be less than -9999");
                        return false;
                    }
                } catch (Exception e) {
                    show_toast("Top Elevation must be an Integer...");
                    return false;
                }
            }


            if (txtLoggerId.getText().toString().length() == 0
                    || haveWhiteSpace(txtLoggerId.getText().toString())) {

                show_toast("Please provide Valid Logger Id without any space...");
                return false;
            } // else if (txtAvgSample.getText().toString().length() == 0) {
            // exceptionSetup = true;
            // err_msg = "Please provide input to Average Sample...";
            // return exceptionSetup;
            // }
            else if (txtAvgSample.getText().toString().length() == 0
                    || Integer.parseInt(txtAvgSample.getText().toString()
                    .trim()) > 250
                    || Integer.parseInt(txtAvgSample.getText().toString()
                    .trim()) < 1) {

                show_toast("Please provide valid Average Sample between 1 - 250...");
                return false;
            } else if ((2 * (Integer
                    .parseInt(txtAvgSample.getText().toString()))) > time) {
                // int avg=
                // 2*Integer.parseInt(txtAvgSample.getText().toString());

                show_toast("Samples cannot be greater than (Log interval in sec.)/2.");
                return false;
            } else if (Integer
                    .parseInt(txtLtDegree.getText().toString().trim()) > 90
                    || Integer
                    .parseInt(txtLtDegree.getText().toString().trim()) < 0
                    || txtLtDegree.getText().toString().length() == 0) {
                show_toast("Please provide valid Latitude Degree between 0 - 90.");
                return false;
            } else if (Integer.parseInt(txtLtMin.getText().toString().trim()) > 59
                    || Integer.parseInt(txtLtMin.getText().toString().trim()) < 0
                    || txtLtMin.getText().toString().length() == 0) {
                show_toast("Please provide valid Latitude Minutes between 0-59");
                return false;
            } else if (Integer.parseInt(txtLtSec.getText().toString().trim()) > 59
                    || Integer.parseInt(txtLtSec.getText().toString().trim()) < 0
                    || txtLtSec.getText().toString().length() == 0) {

                show_toast("Please provide valid Latitude Seconds between 0-59");
                return false;
            } else if (Integer.parseInt(txtLngDegree.getText().toString()
                    .trim()) > 180
                    || Integer.parseInt(txtLngDegree.getText().toString()
                    .trim()) < 0
                    || txtLngDegree.getText().toString().length() == 0) {

                show_toast("Please provide valid Longitude Degree between 0-180");
                return false;
            } else if (Integer.parseInt(txtLngMin.getText().toString().trim()) > 59
                    || Integer.parseInt(txtLngMin.getText().toString().trim()) < 0
                    || txtLngMin.getText().toString().length() == 0) {

                show_toast("Please provide valid Longitude Minutes between 0-59");
                return false;
            } else if (Integer.parseInt(txtLngSec.getText().toString().trim()) > 59
                    || Integer.parseInt(txtLngSec.getText().toString().trim()) < 0
                    || txtLngSec.getText().toString().length() == 0) {

                show_toast("Please provide valid Longitude Seconds between 0-59");
                return false;
            } else if (Integer.parseInt(txtLngDegree.getText().toString()
                    .trim()) == 180
                    || Integer
                    .parseInt(txtLtDegree.getText().toString().trim()) == 90) {

                if ((Integer.parseInt(txtLngDegree.getText().toString().trim()) == 180 && (Integer
                        .parseInt(txtLngMin.getText().toString().trim()) > 0 || Integer
                        .parseInt(txtLngSec.getText().toString().trim()) > 0))
                        || (Integer.parseInt(txtLngDegree.getText().toString()
                        .trim()) < 180 && (Integer.parseInt(txtLngMin
                        .getText().toString().trim()) >= 60 || Integer
                        .parseInt(txtLngSec.getText().toString().trim()) > 60))) {

                    show_toast("Error: Longitude minutes and  Seconds  for 180 deg. should be 0");
                    return false;

                }
                if ((Integer.parseInt(txtLtDegree.getText().toString().trim()) == 90 && (Integer
                        .parseInt(txtLtMin.getText().toString().trim()) > 0 || Integer
                        .parseInt(txtLtSec.getText().toString().trim()) > 0))
                        || (Integer.parseInt(txtLtDegree.getText().toString()
                        .trim()) < 90 && (Integer.parseInt(txtLtMin
                        .getText().toString().trim()) >= 60 || Integer
                        .parseInt(txtLtSec.getText().toString().trim()) >= 60))) {
                    show_toast("Error: Latitude minutes and  Seconds  for 90 deg. should be 0");
                    return false;

                }


            }
        } catch (Exception e) {
            show_toast("Exception occurred !!");
            return false;
        }
        return true;
    }

    private void show_toast(String msg) {
        Toast.makeText(LoggerInfoSettingActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private String setDecimalDigits(String paraValue_para1, int decimalPlaces) {
        try {
            Float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) // xxxx.xxxx
                {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "f", paraValue);
                } else {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "E", paraValue);
                }
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return paraValue_para1;
    }

    /**
     * Exit from activity on back key
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * To initialize progress bar & set dialog title.
     */
    private void initProgressDialog(String msg) {
        // create a dialog
        progress_dialog = new ProgressDialog(this);
        // set the title of the dialog
        progress_dialog.setTitle(msg);
        // Set if the dialog can be skipped
        progress_dialog.setCancelable(false);
        progress_dialog.setMessage("Please Wait.....");
    }

    /**
     * Pad.
     *
     * @param decimalDigit the decimal digit
     * @return the string
     */
    public String pad(int decimalDigit) {
        try {

            if (decimalDigit < 10) {
                return "0" + decimalDigit;
            } else {
                return "" + decimalDigit;
            }
        } catch (Exception e) {
            return "";
        }

    }


}
