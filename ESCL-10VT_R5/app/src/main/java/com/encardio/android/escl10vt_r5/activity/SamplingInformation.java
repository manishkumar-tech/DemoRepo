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

public class SamplingInformation extends AppCompatActivity {

    protected static final int MESSAGE_DISPLAY_DATA = 0;
    protected static final int MESSAGE_DISPLAY_ERROR = 1;
    Thread thread;
    private TextView scanStatus;
    private TextView logInterval;
    private TextView samplesAveraged;
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
                    Toast.makeText(SamplingInformation.this, "Exception occurred !!", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.sampling_information);
        objConstant = new Constants();

        try {
            scanStatus = findViewById(R.id.scanStatus);
            logInterval = findViewById(R.id.logInterval);
            samplesAveraged = findViewById(R.id.samplesAveraged);

            Variable.scanStatus = false;
            Variable.loggerLogInterval = "";
            Variable.loggerSamplesAveraged = "";

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
                Variable.scanStatus = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("SCAN,\"?\"")).trim().equals("START");
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerLogInterval = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("LOGINT,\"?\""));
            }
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerSamplesAveraged = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("AVGSAMP,\"?\""));
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
            if (Variable.scanStatus) {
                scanStatus.setText("START");
            } else {
                scanStatus.setText("STOP");
            }
            logInterval.setText(Variable.loggerLogInterval);
            samplesAveraged.setText(Variable.loggerSamplesAveraged);
        } catch (Exception e) {
            Toast.makeText(this, "Exception occurred !!", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }
}
