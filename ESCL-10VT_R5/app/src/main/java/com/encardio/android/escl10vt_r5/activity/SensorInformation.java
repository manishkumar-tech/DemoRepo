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
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

public class SensorInformation extends AppCompatActivity {

    protected static final int MESSAGE_DISPLAY_DATA = 0;
    protected static final int MESSAGE_DISPLAY_ERROR = 1;
    Thread thread;
    private TextView sensorModel;
    private TextView sensorSerialNumber;
    private TextView measuringRange;
    private TextView specificGravity;
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
                    Toast.makeText(SensorInformation.this, "Exception occurred !!", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.sensor_information);
        objConstant = new Constants();
        try {
            sensorModel = (TextView) findViewById(R.id.sensorModel);
            sensorSerialNumber = (TextView) findViewById(R.id.sensorSerialNumber);
            measuringRange = (TextView) findViewById(R.id.measuringRange);
            specificGravity = (TextView) findViewById(R.id.specificGravity);

            Variable.loggerSensorModel = "";
            Variable.loggerSensorSerialNumber = "";
            Variable.loggerMeasuringRange = "";
            Variable.loggerSpecificGravity = "";

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
                Variable.loggerSensorModel = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SNMODEL,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerSensorSerialNumber = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SENSN,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerMeasuringRange = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SNRANGE,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerSpecificGravity = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SPECGRV,\"?\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MESSAGE_DISPLAY_ERROR;
        }
        return MESSAGE_DISPLAY_DATA;
    }

    @SuppressLint("ShowToast")
    private void displayData() {
        try {
            sensorModel.setText(Variable.loggerSensorModel);
            sensorSerialNumber.setText(Variable.loggerSensorSerialNumber);
            measuringRange.setText(Constants.setDecimalDigits(Variable.loggerMeasuringRange));
            specificGravity.setText(Constants.setDecimalDigits(Variable.loggerSpecificGravity));
        } catch (Exception e) {
            Toast.makeText(this, "Exception occurred !!", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }
}
