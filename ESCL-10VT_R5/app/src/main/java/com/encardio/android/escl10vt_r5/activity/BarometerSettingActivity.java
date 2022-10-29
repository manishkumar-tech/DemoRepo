package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
public class BarometerSettingActivity extends AppCompatActivity {

    private final int MESSAGE_BAROMETER_UPDATING_LOGGER_SCREEN = 10;
    private final int MESSAGE_BAROMETER_READING_SCREEN = 20;
    private final int MESSAGE_BAROMETER_EXCEPTION_SCREEN = 30;
    private final int SCAN_EXCEPTION = 40;
    String err_msg = "";
    String[] spinner_BarometerCorrection_value = {"ENABLE", "DISABLE"};
    String[] spinner_BarometerUnit_value = {"hPa", "m"};

    ArrayAdapter<String> arrayAdapter_BarometerCorrection;
    ArrayAdapter<String> arrayAdapter_BarometerUnit;

    private EditText txtBarometerCoeff;
    private EditText txtBarometerOffset;
    private Spinner spinnerBarometerCorrection;
    private Spinner spinnerBarometerUnit;
    private Button btnUpdate;
    private String bCoeff;
    private String bOffset;
    private String bOption;
    private String bUnit = "1";
    private boolean exceptionBarometer;
    /**
     * The system info thread.
     */
    private Thread BarometerThread;
    /**
     * The dialog.
     */
    private ProgressDialog dialog;
    /**
     * The constant scan.
     */
    private Constants constantBarometer;
    private short statusCodebCoeff;
    private short statusCodebOffset;
    private short statusCodebOption;
    /**
     * The set text handler.
     */
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (BarometerThread != null) {
                BarometerThread.interrupt();
                BarometerThread = null;
            }
            switch (msg.what) {
                case MESSAGE_BAROMETER_UPDATING_LOGGER_SCREEN: {
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.exception)
                        Toast.makeText(
                                BarometerSettingActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();
                    else {
                        dialog.dismiss();
                        if (statusCodebCoeff != Constants.OK_STATUS
                                || statusCodebOffset != Constants.OK_STATUS
                                || statusCodebOption != Constants.OK_STATUS) {
                            err_msg = "Error in updating datalogger...";
                            Toast.makeText(
                                    BarometerSettingActivity.this,
                                    Constants.sc + ": "
                                            + "Unable to update values...",
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(BarometerSettingActivity.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();

                    }
                    break;
                }
                case MESSAGE_BAROMETER_READING_SCREEN: {

                    dialog.dismiss();

                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_BAROMETER_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (exceptionBarometer) {
                        exceptionBarometer = false;
                        Toast.makeText(
                                BarometerSettingActivity.this,
                                Constants.sc + ": "
                                        + "Unable to update values...",
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
                                BarometerSettingActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();

                    break;
            }
            exceptionBarometer = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barometer_setting);

        txtBarometerCoeff = findViewById(R.id.edittextBarometerCValue);
        txtBarometerCoeff.setEnabled(false);
        txtBarometerCoeff.setFocusable(false);
        txtBarometerCoeff.setClickable(false);
        txtBarometerOffset = findViewById(R.id.edittextBarometerOffsetValue);
        txtBarometerOffset.setEnabled(false);
        txtBarometerOffset.setFocusable(false);
        txtBarometerOffset.setClickable(false);

        btnUpdate = findViewById(R.id.btnUpdate);
        Button btnBack = findViewById(R.id.buttonBack);

        spinnerBarometerCorrection = findViewById(R.id.spinnerBarometerCorrection);
        spinnerBarometerUnit = findViewById(R.id.spinnerBarometerUnit);

        arrayAdapter_BarometerCorrection = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_BarometerCorrection_value);
        arrayAdapter_BarometerCorrection.setDropDownViewResource(R.layout.spinner_drop_down);
        spinnerBarometerCorrection.setAdapter(arrayAdapter_BarometerCorrection);

        arrayAdapter_BarometerUnit = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_BarometerUnit_value);
        arrayAdapter_BarometerUnit.setDropDownViewResource(R.layout.spinner_drop_down);
        spinnerBarometerUnit.setAdapter(arrayAdapter_BarometerUnit);

        spinnerBarometerUnit.setEnabled(false);
        if (Variable.fw_ver >= 1.21) {
            spinnerBarometerUnit.setEnabled(true);
        }

        constantBarometer = new Constants();

        initProgressDialog("Loading Barometer Information !!!");
        dialog.show();
        BarometerThread = new Thread() {
            public void run() {
                // get modem Setting parameters from data logger
                readSetupParameter();
                Message msg;
                if (!exceptionBarometer) {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_BAROMETER_READING_SCREEN);
                } else {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_BAROMETER_EXCEPTION_SCREEN);
                }
                setTextHandler.sendMessage(msg);
            }
        };
        BarometerThread.start();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdate.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdate.setClickable(true);
                    }
                }, 2000);

                if (constantBarometer.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(BarometerSettingActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                constantBarometer
                                                        .wakeUpDL();
                                                constantBarometer
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            BarometerSettingActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    barometerUpdate();
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
                    if (Constants.sc == Constants.OK_STATUS)
                        barometerUpdate();
                    else {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    }

                }

            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BarometerThread != null) {
                    BarometerThread.interrupt();
                    BarometerThread = null;
                }
                finish();
            }
        });
    }

    private void updateBarometer() {
        if (statusCodebCoeff == Constants.OK_STATUS) {
            constantBarometer.sendCMDgetRLY("BAROCALF,"
                    + txtBarometerCoeff.getText().toString().trim());
            statusCodebCoeff = (short) Constants.sc;
        }
        if (statusCodebOffset == Constants.OK_STATUS) {
            constantBarometer.sendCMDgetRLY("BAROFSET,"
                    + txtBarometerOffset.getText().toString().trim());
            statusCodebOffset = (short) Constants.sc;
        }
        if (statusCodebOption == Constants.OK_STATUS) {
            constantBarometer.sendCMDgetRLY("BAROMETR,\""
                    + spinnerBarometerCorrection.getSelectedItem().toString().trim()
                    + "\"");
            statusCodebOption = (short) Constants.sc;
        }
        if (Variable.fw_ver >= 1.21) {
            constantBarometer.sendCMDgetRLY("BAROUNIT,"
                    + spinnerBarometerUnit.getSelectedItemPosition()
                    + "");
        }


    }

    /**
     * all edit text validation check
     */

    private boolean checkValidation() {
        try {
            if (txtBarometerCoeff.getText().toString().trim().length() == 0) {
                err_msg = "Please provide input to barometer coefficient...";
                exceptionBarometer = true;
                return true;
            }
            if (txtBarometerCoeff.getText().toString().trim().length() > 12) {
                err_msg = "Please provide input to barometer coefficient of length <= 12...";
                exceptionBarometer = true;
                return true;
            }
            try {
                Float.valueOf(txtBarometerCoeff.getText().toString().trim());
            } catch (Exception e) {
                err_msg = "Please provide numeric input to barometer coefficient...";
                exceptionBarometer = true;
                return true;
            }
            if (txtBarometerOffset.getText().toString().trim().length() == 0) {
                err_msg = "Please provide input to barometer offset...";
                exceptionBarometer = true;
                return true;
            }
            if (txtBarometerOffset.getText().toString().trim().length() > 12) {
                err_msg = "Please provide input to barometer offset of length <= 12...";
                exceptionBarometer = true;
                return true;
            }

            try {
                Float.valueOf(txtBarometerOffset.getText().toString().trim());

            } catch (Exception e) {
                err_msg = "Please provide numeric input to barometer offset...";
                exceptionBarometer = true;
                return true;
            }

            if (Float.parseFloat(txtBarometerOffset.getText().toString().trim()) > 1000.0f
                    || Float.parseFloat(txtBarometerOffset.getText().toString()
                    .trim()) < -1000.0f) {
                err_msg = "Please provide input to barometer offset of between range -1000 to 1000";
                exceptionBarometer = true;
                return true;
            }
        } catch (Exception e) {
            err_msg = "Erro in updating barometer settings...";
            exceptionBarometer = true;
            return true;
        }
        return false;
    }

    private void barometerUpdate() {

        if (!checkValidation()) {
            initProgressDialog("Updating Barometer Setting !!!");
            dialog.show();
            BarometerThread = new Thread() {
                public void run() {
                    updateBarometer();
                    Message msg;
                    if (exceptionBarometer) {
                        msg = setTextHandler
                                .obtainMessage(MESSAGE_BAROMETER_EXCEPTION_SCREEN);
                    } else {
                        msg = setTextHandler
                                .obtainMessage(MESSAGE_BAROMETER_UPDATING_LOGGER_SCREEN);
                    }
                    setTextHandler.sendMessage(msg);
                }
            };
            BarometerThread.start();
        } else {
            if (exceptionBarometer) {
                exceptionBarometer = false;
                Toast.makeText(BarometerSettingActivity.this, err_msg,
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    private void readSetupParameter() {
        try {
            if (Variable.isConnected) {
                constantBarometer.wakeUpDL();
                bCoeff = constantBarometer.removeDQ(constantBarometer
                        .sendCMDgetRLY("BAROCALF,\"?\""));
                statusCodebCoeff = (short) Constants.sc;
                bOffset = constantBarometer
                        .removeDQ(constantBarometer
                                .sendCMDgetRLY("BAROFSET,\"?\""));
                statusCodebOffset = (short) Constants.sc;
                bOption = constantBarometer
                        .removeDQ(constantBarometer
                                .sendCMDgetRLY("BAROMETR,\"?\""));
                statusCodebOption = (short) Constants.sc;
                if (Variable.fw_ver >= 1.21) {
                    bUnit = constantBarometer
                            .removeDQ(constantBarometer
                                    .sendCMDgetRLY("BAROUNIT,\"?\""));
                }

            }
        } catch (Exception e) {
            exceptionBarometer = true;
            err_msg = "Error in reading Barometer Values...";
            e.printStackTrace();
        }
    }

    private void displayData() {
        try {
            if (statusCodebCoeff == Constants.OK_STATUS) {
                bCoeff = Constants.setDecimalDigits(bCoeff, 6);
                txtBarometerCoeff.setText(bCoeff);
            } else
                txtBarometerCoeff.setText("");
            if (statusCodebOffset == Constants.OK_STATUS) {
                bOffset = Constants.setDecimalDigits(bOffset, 6);
                txtBarometerOffset.setText(bOffset);
            } else
                txtBarometerOffset.setText("");
            try {
                if (statusCodebOption == Constants.OK_STATUS) {
                    if (bOption.equalsIgnoreCase("ENABLE"))

                        spinnerBarometerCorrection.setSelection(arrayAdapter_BarometerCorrection.getPosition(bOption));

                    else
                        spinnerBarometerCorrection.setSelection(arrayAdapter_BarometerCorrection.getPosition("DISABLE"));

                } else
                    spinnerBarometerCorrection.setSelection(arrayAdapter_BarometerCorrection.getPosition("DISABLE"));
            } catch (Exception e) {
                exceptionBarometer = true;
                err_msg = "Error in reading Barometer Values...";
                e.printStackTrace();
            }

            spinnerBarometerUnit.setSelection(arrayAdapter_BarometerUnit.getPosition("m"));
            if (Variable.fw_ver >= 1.21) {
                if (bUnit.equals("0")) {
                    spinnerBarometerUnit.setSelection(arrayAdapter_BarometerUnit.getPosition("hPa"));
                }

            }


        } catch (Exception e) {
            exceptionBarometer = true;
            err_msg = "Error in reading Barometer Values...";
            e.printStackTrace();
        }
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new androidx.appcompat.app.AlertDialog.Builder(BarometerSettingActivity.this)
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
        new AlertDialog.Builder(BarometerSettingActivity.this)
                .setTitle("Connection").setIcon(R.drawable.error)
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


    /**
     * To initialize progress bar & set dialog title.
     */
    private void initProgressDialog(String msg) {
        // create a dialog
        dialog = new ProgressDialog(this);
        // set the title of the dialog
        dialog.setTitle(msg);
        // Set if the dialog can be skipped
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
