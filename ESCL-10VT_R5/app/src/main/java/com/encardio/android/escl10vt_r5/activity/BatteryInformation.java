package com.encardio.android.escl10vt_r5.activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * Created by sandeepk on 18/02/2019.
 */

public class BatteryInformation extends AppCompatActivity {

    protected static final int MESSAGE_DISPLAY_DATA = 0;
    protected static final int MESSAGE_DISPLAY_ERROR = 1;
    Thread thread;
    float btvolt;
    String battv;
    private TextView batteryType;
    private TextView batteryVoltage;
    private TextView installationDate;
    private TextView batteryHealth;
    private ProgressDialog dialog;
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            switch (msg.what) {
                case MESSAGE_DISPLAY_DATA: {
                    displayData();
                    dialog.dismiss();
                    break;
                }
                case MESSAGE_DISPLAY_ERROR: {
                    Toast.makeText(BatteryInformation.this, "Exception occurred !!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    break;
                }
            }
        }
    };
    private Constants objConstant;
    private int returnValue;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_information);
        objConstant = new Constants();

        try {

            batteryType = findViewById(R.id.batteryType);
            batteryVoltage = findViewById(R.id.batteryVoltage);
            installationDate = findViewById(R.id.installationDate);
            batteryHealth = findViewById(R.id.batteryHealth);


            initProgressDialog();
            dialog.show();

            thread = new Thread() {
                public void run() {
                    returnValue = readParameter();
                    Message msg = setTextHandler
                            .obtainMessage(returnValue);
                    setTextHandler.sendMessage(msg);
                }
            };
            thread.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait !!");
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    private int readParameter() {
        try {

            objConstant.wakeUpDataLoggerConnection();

            if (Constants.OK_STATUS == Constants.sc) {
                Variable.batteryType = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("BATTYPE,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.batteryVoltage = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("BATTV,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.batteryInstallationDate = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("BATDATE,\"?\""));
                Variable.batteryInstallationDate = Variable.batteryInstallationDate.substring(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return MESSAGE_DISPLAY_ERROR;
        }
        return MESSAGE_DISPLAY_DATA;
    }

    @SuppressLint({"SetTextI18n", "ShowToast"})
    private void displayData() {
        try {

            batteryType.setText(Variable.batteryType);
            batteryVoltage.setText(String.format("%s V", Variable.batteryVoltage));
            installationDate.setText(Variable.batteryInstallationDate);

            btvolt = Float.parseFloat(Variable.batteryVoltage);
            battv = Variable.batteryType;


            switch (battv) {
                case "ALKALINE":
                    if ((btvolt > 1.9F) && (btvolt < 2.0F)) {
                        batteryHealth.setText("20% remaining.");
                    } else if ((btvolt > 1.7F) && (btvolt <= 1.9F)) {
                        batteryHealth.setText("10% remaining.");
                    } else if ((btvolt > 1.6F) && (btvolt <= 1.7F)) {
                        batteryHealth.setText("5% remaining.");
                    } else if ((btvolt > 1.5F) && (btvolt <= 1.6F)) {
                        batteryHealth.setText("2% remaining.");
                    } else if (btvolt <= 1.5F) {
                        batteryHealth.setText("Recharge battery immediately");

                    }
                    break;
                case "LITHIUM":
                    if (btvolt > 7.00f) {
                        batteryHealth.setText("Good");
                    } else if ((btvolt > 6.9F) && (btvolt < 7.0F)) {
                        batteryHealth.setText("20% remaining.");
                    } else if ((btvolt > 6.8F) && (btvolt <= 6.9F)) {
                        batteryHealth.setText("10% remaining.");
                    } else if ((btvolt > 6.7F) && (btvolt <= 6.8F)) {
                        batteryHealth.setText("5% remaining.");
                    } else if ((btvolt > 6.6F) && (btvolt <= 6.7F)) {
                        batteryHealth.setText("2% remaining.");
                    } else if (btvolt <= 6.6F) {
                        batteryHealth.setText("Recharge battery immediately");
                    }
                    break;
                case "EXTERNAL":
                    if (btvolt <= 9.0F) {
                        batteryHealth.setText("Recharge battery immediately");
                    }
                    break;
            }

        } catch (Exception e) {
            Toast.makeText(this, "Exception occurred !!", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }
}
