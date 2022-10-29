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

public class LoggerInformation extends AppCompatActivity {

    protected static final int MESSAGE_DISPLAY_DATA = 0;
    protected static final int MESSAGE_DISPLAY_ERROR = 1;
    Thread thread;
    private TextView model;
    private TextView serialNumber;
    private TextView loggerID;
    private TextView instDepth;
    private TextView location;
    private TextView topElevation;
    private TextView fwver;
    private TextView fwRevDate;
    private TextView connectionStatus;
    private TextView scanStatus;
    private TextView logInterval;
    private TextView scanStartTime;
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
                    Toast.makeText(LoggerInformation.this, "Exception occurred !!", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.logger_information);
        objConstant = new Constants();

        try {
            model = (TextView) findViewById(R.id.model);
            serialNumber = (TextView) findViewById(R.id.serialNumber);
            loggerID = (TextView) findViewById(R.id.loggerID);
            instDepth = (TextView) findViewById(R.id.instDepth);
            location = (TextView) findViewById(R.id.location);
            topElevation = (TextView) findViewById(R.id.topElevation);
            fwver = (TextView) findViewById(R.id.fwver);
            fwRevDate = (TextView) findViewById(R.id.fwRevDate);
            connectionStatus = (TextView) findViewById(R.id.connectionStatus);
            scanStatus = (TextView) findViewById(R.id.scanStatus);
            logInterval = (TextView) findViewById(R.id.logInterval);
            scanStartTime = (TextView) findViewById(R.id.scanStartTime);

            Variable.loggerModel = "";
            Variable.loggerSerialNumber = "";
            Variable.loggerID = "";
            Variable.loggerInstDepth = "";
            Variable.loggerLocation = "";
            Variable.loggerTopElevation = "";
            Variable.loggerFwVer = "";
            Variable.loggerFwRevDate = "";
            Variable.scanStatus = false;
            Variable.loggerLogInterval = "";
            Variable.loggerScanStartTime = "";

            initProgressDialog("Please wait !!");
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
            connectionStatus.setText(R.string.disconnected);
            e.printStackTrace();
        }
    }

    private void initProgressDialog(String msg) {
        dialog = new ProgressDialog(this);
        dialog.setTitle(msg);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    private int readParameter() {
        try {

            objConstant.wakeUpDataLoggerConnection();

            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerModel = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("MODEL,\"?\""));

            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerSerialNumber = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SN,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerID = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("DLID,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerInstDepth = "" + Float.parseFloat(objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("OFFSET,\"?\"")));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerLocation = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("DLCORD,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerTopElevation = "" + Float.parseFloat(objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("TOPELEV,\"?\"")));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerFwVer = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("FWVER,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerFwRevDate = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("FWREVDT,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.scanStatus = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SCAN,\"?\"")).trim().equals("START");
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerLogInterval = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("LOGINT,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerScanStartTime = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SSTIME,\"?\""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return MESSAGE_DISPLAY_ERROR;
        }
        return MESSAGE_DISPLAY_DATA;
    }

    private void displayData() {
        try {
            model.setText(Variable.loggerModel);
            serialNumber.setText(Variable.loggerSerialNumber);
            loggerID.setText(Variable.loggerID);
            instDepth.setText(Variable.loggerInstDepth);
            location.setText(Variable.loggerLocation);
            topElevation.setText(Variable.loggerTopElevation);
            fwver.setText(Variable.loggerFwVer);
            fwRevDate.setText(Variable.loggerFwRevDate);
            connectionStatus.setText(R.string.connected);

            if (Variable.scanStatus) {
                scanStatus.setText("START");
            } else {
                scanStatus.setText("STOP");
            }

            logInterval.setText(Variable.loggerLogInterval);
            scanStartTime.setText(Variable.loggerScanStartTime);
        } catch (Exception e) {
            Toast.makeText(this, "Exception occurred !!", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }
}
