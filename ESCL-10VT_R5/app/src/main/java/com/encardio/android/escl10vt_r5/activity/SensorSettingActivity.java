package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * @author Sandeep
 */

public class SensorSettingActivity extends AppCompatActivity {

    private final int MESSAGE_ADVANCE_SETTING_READING_SCREEN = 10;
    private final int MESSAGE_ADVANCE_SETTING_UPDATING_SCREEN = 20;
    private final int MESSAGE_ADVANCE_SETTING_EXCEPTION_SCREEN = 30;
    private final int SCAN_EXCEPTION = 40;
    String err_msg = "";
    String[] spinner_XParameter_value = {"FREQUENCY", "FREQUENCY SQUARED"};
    ArrayAdapter<String> arrayAdapter_XParameter;
    private Button btnUpdateAdvanceSettings;
    private Button btnBack;
    private Spinner spinner_XParameter;
    private EditText txtSensorSN;
    private EditText txtDepth;
    private EditText txtCoeffA0;
    private EditText txtCoeffA1;
    private EditText txtCoeffA2;
    private EditText txtSensorModel;
    private EditText txtSpfcGravity;
    private EditText txtMsrngRange;
    private boolean exceptionAdvanceSetting;
    private Thread advanceSettingThread;
    private ProgressDialog dialog;
    private Constants constantAdvanceSetting;
    private String sensorSN;
    private String MsrngRange;
    private String instlDepth;
    private String coeffA0;
    private String coeffA1;
    private String coeffA2;
    private String SensorModel;
    private String SpfcGravity;
    private String xParamter;
    private short statusCodeSnsSN;
    private short statusCodeMsrngRange;
    private short statusCodeCA0;
    private short statusCodeCA1;
    private short statusCodeCA2;
    private short statusCodeSensorModel;
    private short statusCodeXParameter;
    private short statusCodeSpfcGravity;
    private short statusCodeInstlDepth;
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (advanceSettingThread != null) {
                advanceSettingThread.interrupt();
                advanceSettingThread = null;
            }
            switch (msg.what) {
                case MESSAGE_ADVANCE_SETTING_READING_SCREEN: {
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_ADVANCE_SETTING_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodeSnsSN == Constants.OK_STATUS
                                || statusCodeInstlDepth == Constants.OK_STATUS
                                || statusCodeSpfcGravity == Constants.OK_STATUS
                                || statusCodeCA0 == Constants.OK_STATUS
                                || statusCodeCA1 == Constants.OK_STATUS
                                || statusCodeCA2 == Constants.OK_STATUS
                                || statusCodeMsrngRange == Constants.OK_STATUS
                                || statusCodeXParameter == Constants.OK_STATUS
                                || statusCodeSensorModel == Constants.OK_STATUS) {

                            err_msg = "Successfully updated...";

                            Toast.makeText(SensorSettingActivity.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            err_msg = "Error in updating Sensor Settings Values...";
                            Toast.makeText(SensorSettingActivity.this,
                                    Constants.sc + ": " + err_msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else
                        showDialog();
                    break;
                case MESSAGE_ADVANCE_SETTING_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (exceptionAdvanceSetting) {
                        exceptionAdvanceSetting = false;
                        Toast.makeText(SensorSettingActivity.this, err_msg,
                                Toast.LENGTH_LONG).show();
                    }
                    break;

                case SCAN_EXCEPTION:
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.connectionBreak)
                        showDialog();
                    else
                        Toast.makeText(
                                SensorSettingActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_settings);

        txtSensorSN = (EditText) findViewById(R.id.editTextSensorIdValue);
        txtDepth = (EditText) findViewById(R.id.editTextInstDepthValue);
        txtDepth.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        txtCoeffA0 = (EditText) findViewById(R.id.editTextCoeffA0Value);
        txtCoeffA1 = (EditText) findViewById(R.id.editTextCoeffA1Value);
        txtCoeffA2 = (EditText) findViewById(R.id.editTextCoeffA2Value);

        txtSensorModel = (EditText) findViewById(R.id.editTextSensorModelValue);
        txtSpfcGravity = (EditText) findViewById(R.id.editTextSpcfcGrvtyValue);
        txtMsrngRange = (EditText) findViewById(R.id.editTextMsrngRangeValue);
        spinner_XParameter = findViewById(R.id.spinner_XParameter);
        btnUpdateAdvanceSettings = (Button) findViewById(R.id.buttonUpdateSetting);
        btnBack = (Button) findViewById(R.id.buttonBack);

        arrayAdapter_XParameter = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_XParameter_value);
        arrayAdapter_XParameter.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_XParameter.setAdapter(arrayAdapter_XParameter);

        constantAdvanceSetting = new Constants();


        initProgressDialog("Loading Sensor Setting Information !!!");
        dialog.show();
        advanceSettingThread = new Thread() {
            public void run() {
                readAdvanceSettingsParameter();
                Message msg;
                if (exceptionAdvanceSetting) {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_ADVANCE_SETTING_EXCEPTION_SCREEN);
                } else {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_ADVANCE_SETTING_READING_SCREEN);
                }
                setTextHandler.sendMessage(msg);
            }
        };
        advanceSettingThread.start();
        buttonAction();
    }

    private void buttonAction() {
        btnUpdateAdvanceSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                btnUpdateAdvanceSettings.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdateAdvanceSettings.setClickable(true);
                    }
                }, 2000);
                if (constantAdvanceSetting.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {

                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(SensorSettingActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {

                                                constantAdvanceSetting
                                                        .wakeUpDL();
                                                constantAdvanceSetting
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            SensorSettingActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    updateSensor();
                                                } else {
                                                    Message msg = setTextHandler
                                                            .obtainMessage(SCAN_EXCEPTION);
                                                    setTextHandler
                                                            .sendMessage(msg);
                                                }
                                            }
                                        }).setNegativeButton("NO", null).show();

                    }
                } else {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else
                        updateSensor();
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (advanceSettingThread != null) {
                    advanceSettingThread.interrupt();
                    advanceSettingThread = null;
                }
                finish();
                // warningDialog();
            }
        });
    }

    private void updateSensor() {

        if (!isTextFieldEmpty()) {
            if (!checkValidation()) {
                initProgressDialog("Updating Sensor Setting Information !!!");
                dialog.show();
                advanceSettingThread = new Thread() {
                    public void run() {
                        // get Advance Setting parameters from data
                        // logger

                        updateSettingParameter();
                        Message msg;
                        if (exceptionAdvanceSetting) {
                            // sending msg
                            msg = setTextHandler
                                    .obtainMessage(MESSAGE_ADVANCE_SETTING_EXCEPTION_SCREEN);
                        } else {
                            // sending msg
                            msg = setTextHandler
                                    .obtainMessage(MESSAGE_ADVANCE_SETTING_UPDATING_SCREEN);
                        }
                        setTextHandler.sendMessage(msg);
                    }
                };
                advanceSettingThread.start();
            } else {
                if (exceptionAdvanceSetting) {
                    exceptionAdvanceSetting = false;
                    Toast.makeText(SensorSettingActivity.this, err_msg,
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            if (exceptionAdvanceSetting) {
                exceptionAdvanceSetting = false;
                Toast.makeText(SensorSettingActivity.this, err_msg,
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    protected boolean checkValidation() {


        try {

            float c = Float.parseFloat(txtMsrngRange.getText().toString()
                    .trim());
            if (c > 1000) {
                exceptionAdvanceSetting = true;
                err_msg = "Measuring range must be upto 1000";
                return exceptionAdvanceSetting;
            }
            if (c < 0) {
                exceptionAdvanceSetting = true;
                err_msg = " Measuring range must not be less than 0";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Measuring range must be numeric...";
            return exceptionAdvanceSetting;
        }
        try {

            float c = Float.parseFloat(txtSpfcGravity.getText().toString()
                    .trim());
            if (c > 1.200001) {
                exceptionAdvanceSetting = true;
                err_msg = "Specifi Gravity must be upto 1.2";
                return exceptionAdvanceSetting;
            }
            if (c < 0.8) {
                exceptionAdvanceSetting = true;
                err_msg = "Specifi Gravity must not be less than 0.8";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Specifi Gravity must be numeric...";

            return exceptionAdvanceSetting;
        }

        try {

            float c = Float.parseFloat(txtDepth.getText().toString().trim());
            if (c > 999) {
                exceptionAdvanceSetting = true;
                err_msg = "Installation Depth must be upto 999";
                return exceptionAdvanceSetting;
            }
            if (c < -999) {
                exceptionAdvanceSetting = true;
                err_msg = "Installation Depth must not be less than -999";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Installation Depth must be numeric...";

            return exceptionAdvanceSetting;
        }

        try {
            float c = Float.parseFloat(txtCoeffA0.getText().toString().trim());
            if (c > 1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A0 must be upto 1.0E+015";
                return exceptionAdvanceSetting;
            }
            if (c < -1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A0 must not be less than -1.0E+015";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "CoeffA0 must be numeric...";

            return exceptionAdvanceSetting;
        }

        try {
            float c = Float.parseFloat(txtCoeffA1.getText().toString().trim());
            if (c > 1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A1 must be upto 1.0E+015";
                return exceptionAdvanceSetting;
            }
            if (c < -1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A1 must not be less than -1.0E+015";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "CoeffA1 must be numeric...";

            return false;
        }
        try {
            float c = Float.parseFloat(txtCoeffA2.getText().toString().trim());
            if (c > 1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A2 must be upto 1.0E+015";
                return exceptionAdvanceSetting;
            }
            if (c < -1.0E+015) {
                exceptionAdvanceSetting = true;
                err_msg = "Coefficient A2 must not be less than -1.0E+015";
                return exceptionAdvanceSetting;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "CoeffA2 must be numeric...";

            return exceptionAdvanceSetting;
        }


        return false;
    }

    private void readAdvanceSettingsParameter() {
        try {
            if (Variable.isConnected) {
                constantAdvanceSetting.wakeUpDL();
                sensorSN = constantAdvanceSetting
                        .removeDQ(constantAdvanceSetting
                                .sendCMDgetRLY("SENSN,\"?\""));
                statusCodeSnsSN = (short) Constants.sc;
                if (Constants.sc == Constants.OK_STATUS) {
                    SensorModel = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("SNMODEL,\"?\""));
                    statusCodeSensorModel = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    instlDepth = "" + Float.parseFloat(constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("OFFSET,\"?\"")));
                    statusCodeInstlDepth = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    coeffA0 = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("COEFFA,0,\"?\""));
                    statusCodeCA0 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    coeffA1 = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("COEFFA,1,\"?\""));
                    statusCodeCA1 = (short) Constants.sc;
                }


                if (Constants.sc == Constants.OK_STATUS) {
                    coeffA2 = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("COEFFA,2,\"?\""));
                    statusCodeCA2 = (short) Constants.sc;
                }

                if (Constants.sc == Constants.OK_STATUS) {
                    xParamter = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("XVALUE,\"?\""));
                    statusCodeXParameter = (short) Constants.sc;
                }


                if (Constants.sc == Constants.OK_STATUS) {
                    MsrngRange = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("SNRANGE,\"?\""));
                    statusCodeMsrngRange = (short) Constants.sc;
                }

                if (Constants.sc == Constants.OK_STATUS) {
                    SpfcGravity = constantAdvanceSetting
                            .removeDQ(constantAdvanceSetting
                                    .sendCMDgetRLY("SPECGRV,\"?\""));
                    statusCodeSpfcGravity = (short) Constants.sc;
                }
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Error in reading Sensor Settings Values...";
            e.printStackTrace();
        }
    }

    private void displayData() {
        try {
            if (statusCodeSnsSN == Constants.OK_STATUS)
                txtSensorSN.setText(sensorSN);
            else
                txtSensorSN.setText("");
            if (statusCodeSensorModel == Constants.OK_STATUS)
                txtSensorModel.setText(SensorModel);
            if (statusCodeMsrngRange == Constants.OK_STATUS) {
                MsrngRange = Constants.setDecimalDigits(MsrngRange, 3);
                txtMsrngRange.setText(MsrngRange);
            } else
                txtMsrngRange.setText("");
            if (statusCodeSpfcGravity == Constants.OK_STATUS) {
                SpfcGravity = Constants.setDecimalDigits(SpfcGravity, 6);
                txtSpfcGravity.setText(SpfcGravity);
            } else
                txtSpfcGravity.setText("");
            if (statusCodeInstlDepth == Constants.OK_STATUS) {
                instlDepth = Constants.setDecimalDigits(instlDepth, 3);
                txtDepth.setText(instlDepth);
            } else
                txtDepth.setText("");
            if (statusCodeCA0 == Constants.OK_STATUS) {
                coeffA0 = Constants.setDecimalDigits(coeffA0.substring(2), 6);
                txtCoeffA0.setText(coeffA0);
            } else
                txtCoeffA0.setText("0.0");
            if (statusCodeCA1 == Constants.OK_STATUS) {
                coeffA1 = Constants.setDecimalDigits(coeffA1.substring(2), 6);
                txtCoeffA1.setText(coeffA1);
            } else
                txtCoeffA1.setText("0.0");
            if (statusCodeCA2 == Constants.OK_STATUS) {
                coeffA2 = Constants.setDecimalDigits(coeffA2.substring(2), 6);
                txtCoeffA2.setText(coeffA2);
            } else
                txtCoeffA2.setText("0.0");


            if (statusCodeXParameter == Constants.OK_STATUS) {
                if (xParamter.equalsIgnoreCase("FREQ")) {
                    spinner_XParameter.setSelection(arrayAdapter_XParameter.getPosition("FREQUENCY"));
                } else {
                    spinner_XParameter.setSelection(arrayAdapter_XParameter.getPosition("FREQUENCY SQUARED"));
                }
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Error in reading Sensor Settings Values...";
            e.printStackTrace();
        }
    }

    private void updateSettingParameter() {
        try {
            constantAdvanceSetting.wakeUpDL();
            constantAdvanceSetting.sendCMDgetRLY("SENSN,\""
                    + txtSensorSN.getText().toString().trim() + "\"");
            statusCodeSnsSN = (short) Constants.sc;
            if (Constants.sc == Constants.OK_STATUS) {
                constantAdvanceSetting.sendCMDgetRLY("SNMODEL,\""
                        + txtSensorModel.getText().toString().trim() + "\"");
                statusCodeSensorModel = (short) Constants.sc;
            }
            if (Constants.sc == Constants.OK_STATUS) {
                constantAdvanceSetting.sendCMDgetRLY("SNRANGE,"
                        + txtMsrngRange.getText().toString().trim());
                statusCodeMsrngRange = (short) Constants.sc;
            }
            if (Constants.sc == Constants.OK_STATUS) {
                constantAdvanceSetting.sendCMDgetRLY("SPECGRV,"
                        + txtSpfcGravity.getText().toString().trim());
                statusCodeSpfcGravity = (short) Constants.sc;
            }
            if (Constants.sc == Constants.OK_STATUS) {

                constantAdvanceSetting.sendCMDgetRLY("OFFSET,"
                        + txtDepth.getText().toString().trim());
                statusCodeInstlDepth = (short) Constants.sc;

            }
            if (Constants.sc == Constants.OK_STATUS) {

                constantAdvanceSetting.sendCMDgetRLY("COEFFA,0,"
                        + txtCoeffA0.getText().toString().trim());
                statusCodeCA0 = (short) Constants.sc;

            }
            if (Constants.sc == Constants.OK_STATUS) {

                constantAdvanceSetting.sendCMDgetRLY("COEFFA,1,"
                        + txtCoeffA1.getText().toString().trim());
                statusCodeCA1 = (short) Constants.sc;

            }
            if (Constants.sc == Constants.OK_STATUS) {

                constantAdvanceSetting.sendCMDgetRLY("COEFFA,2,"
                        + txtCoeffA2.getText().toString().trim());
                statusCodeCA2 = (short) Constants.sc;

            }


            if (Constants.sc == Constants.OK_STATUS) {
                if (spinner_XParameter.getSelectedItem().toString().trim()
                        .equalsIgnoreCase("FREQUENCY"))
                    constantAdvanceSetting
                            .sendCMDgetRLY("XVALUE,\"FREQ\"");
                else
                    constantAdvanceSetting
                            .sendCMDgetRLY("XVALUE,\"FREQ2\"");
                statusCodeXParameter = (short) Constants.sc;
            }
        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Error in updating Sensor Settings Values...";
        }
    }


    private boolean isTextFieldEmpty() {
        try {
            if (txtSensorSN.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Sensor Serial No...";
                return exceptionAdvanceSetting;
            }
            if (txtDepth.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Installation Depth...";
                return exceptionAdvanceSetting;
            }
            if (txtMsrngRange.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Measuring Range...";
                return exceptionAdvanceSetting;
            }
            if (txtSensorModel.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Sensor Model...";
                return exceptionAdvanceSetting;
            }
            if (txtSpfcGravity.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Specific Gravity...";
                return exceptionAdvanceSetting;
            }
            if (txtCoeffA0.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Coeff A0...";
                return exceptionAdvanceSetting;
            }
            if (txtCoeffA1.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Coeff A1...";
                return exceptionAdvanceSetting;
            }
            if (txtCoeffA2.getText().toString().length() == 0) {
                exceptionAdvanceSetting = true;
                err_msg = "Please provide input to Coeff A2...";
                return exceptionAdvanceSetting;
            }

        } catch (Exception e) {
            exceptionAdvanceSetting = true;
            err_msg = "Error in Sensor Text Field Values...";
            return exceptionAdvanceSetting;
        }
        return false;

    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(SensorSettingActivity.this)
                .setIcon(R.drawable.battery0).setTitle(Constants.responseMsg)
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

        new AlertDialog.Builder(SensorSettingActivity.this)
                .setIcon(R.drawable.error).setTitle("Connection")
                .setMessage("Device connection lost !").setCancelable(false)
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

    private void initProgressDialog(String msg) {

        dialog = new ProgressDialog(this);

        dialog.setTitle(msg);

        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }


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
