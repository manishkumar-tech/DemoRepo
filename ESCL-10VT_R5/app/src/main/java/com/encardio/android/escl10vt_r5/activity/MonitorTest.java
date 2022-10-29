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

public class MonitorTest extends AppCompatActivity {

    private final int MESSAGE_MONITOR_READING_SCREEN = 10;
    private final int MESSAGE_MONITOR_UPDATING_SCREEN = 20;
    private final int MESSAGE_MONITOR_EXCEPTION_SCREEN = 30;
    private final int MESSAGE_ENABLE_MODEM = 40;
    private final int SCAN_EXCEPTION = 50;
    private final int MESSAGE_TURN_OFF_EXCEPTION_OCCURED_SCREEN = 42;
    private final int MESSAGE_TURN_OFF_SCREEN = 43;
    String[] spinner_DecimalPlace_value = {"0", "1", "2", "3", "4"};
    ArrayAdapter<String> arrayAdapter_DecimalPlace;
    private Spinner spinnerDecimalPlace;
    private Button btnUpdate;
    private EditText txtMonitorInterval;
    private Constants constantMonitor;
    private Thread monitorThread;
    private String dcml;
    private String err_msg;
    private boolean exceptionMonitor;
    private short statusCodeDcmlPlc;
    private ProgressDialog dialog;
    private short statusCodeMS;
    private boolean quitfromApp, expInGetScanInfo;
    private String error_msg;
    /**
     * The set text handler
     */
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (monitorThread != null) {
                monitorThread.interrupt();
                monitorThread = null;
            }
            switch (msg.what) {
                case MESSAGE_MONITOR_READING_SCREEN: {
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showdialogs();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_ENABLE_MODEM: {
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        try {
                            if (Constants.sc == Constants.MODEM_SIM_UNAVAILABLE_STATUS)

                                Toast.makeText(getApplicationContext(),
                                        Constants.responseMsg, Toast.LENGTH_LONG).show();

                            else if (Constants.sc == Constants.OK_STATUS) {
                                // isModemEnable = true;
                                Constants.mdmPwr = true;
                                Toast.makeText(getApplicationContext(),
                                        "Modem Status : ON..", Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        Constants.sc
                                                + ": Unable to turn on modem.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    Constants.sc + ": "
                                            + "Unable to turn on modem.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        initProgressDialog("Loading Monitor Information !!!");
                        dialog.show();
                        monitorThread = new Thread() {
                            public void run() {
                                readMonitor();
                                Message msg;
                                if (exceptionMonitor) {
                                    msg = setTextHandler
                                            .obtainMessage(MESSAGE_MONITOR_EXCEPTION_SCREEN);
                                } else {
                                    msg = setTextHandler
                                            .obtainMessage(MESSAGE_MONITOR_READING_SCREEN);
                                }
                                setTextHandler.sendMessage(msg);
                            }
                        };
                        monitorThread.start();
                    } else
                        showdialogs();
                    break;
                }

                case MESSAGE_TURN_OFF_SCREEN: {
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        Toast.makeText(getApplicationContext(),
                                "Modem Status : OFF..", Toast.LENGTH_SHORT).show();
                        if (quitfromApp)
                            MonitorTest.this.finish();
                    } else
                        showdialogs();

                    break;
                }

                case MESSAGE_TURN_OFF_EXCEPTION_OCCURED_SCREEN:
                    dialog.dismiss();
                    if (expInGetScanInfo) {
                        expInGetScanInfo = false;
                        Toast.makeText(getApplicationContext(),
                                Constants.sc + ": " + error_msg,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (quitfromApp)
                        MonitorTest.this.finish();

                    break;

                case MESSAGE_MONITOR_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodeDcmlPlc != Constants.OK_STATUS ) {
                            err_msg = "Error in updating Monitor Values...";
                            Toast.makeText(MonitorTest.this,
                                    Constants.sc + ": " + err_msg,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Constants.decimalPlaces = Integer.parseInt(spinnerDecimalPlace.getSelectedItem().toString().trim());
                            Constants.monitorInterval = Integer
                                    .parseInt(txtMonitorInterval.getText()
                                            .toString().trim());
                            txtMonitorInterval.setText(String
                                    .valueOf(Constants.monitorInterval));
                            Toast.makeText(MonitorTest.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();
                        }
                    } else
                        showdialogs();
                    break;
                case MESSAGE_MONITOR_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (exceptionMonitor) {
                        exceptionMonitor = false;
                        Toast.makeText(MonitorTest.this,
                                Constants.sc + ": " + err_msg,
                                Toast.LENGTH_LONG).show();
                    }
                    break;

                case SCAN_EXCEPTION:

                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.connectionBreak)
                        showdialogs();
                    else
                        Toast.makeText(
                                MonitorTest.this,
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
        setContentView(R.layout.monitor);

        Button btnMonitor = findViewById(R.id.buttonParameter);
        spinnerDecimalPlace = findViewById(R.id.spinnerDecimalPlace);
        Button btnBack = findViewById(R.id.buttonBack);
        btnUpdate = findViewById(R.id.buttonUpdateSetting);
        txtMonitorInterval = findViewById(R.id.edittextMonitorIntervalValue);

        arrayAdapter_DecimalPlace = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_DecimalPlace_value);
        arrayAdapter_DecimalPlace.setDropDownViewResource(R.layout.spinner_drop_down);
        spinnerDecimalPlace.setAdapter(arrayAdapter_DecimalPlace);


        constantMonitor = new Constants();
        // if (getIntent().getBooleanExtra("MODEM_EXIST", false))
        if (Constants.mdmstatus) { // if modem
            // exists
            // isModemEnable = getIntent().getBooleanExtra("MODEM_STATUS",
            // false);
            // if (!isModemEnable)
            if (!Constants.mdmPwr) {
                // dialog to turn on modem if modem turn off
                new AlertDialog.Builder(this)
                        .setTitle("MODEM")
                        .setIcon(R.drawable.modem_on)
                        .setMessage("Do you want to turn ON Modem?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog1, int which) {
                                        initProgressDialog("Turning ON modem...");
                                        dialog.show();

                                        monitorThread = new Thread() {
                                            public void run() {
                                                switchOnModem();
                                                // sending msg
                                                Message msg = setTextHandler
                                                        .obtainMessage(MESSAGE_ENABLE_MODEM);
                                                setTextHandler.sendMessage(msg);

                                            }
                                        };
                                        monitorThread.start();
                                    }

                                })
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog1, int which) {
                                        loadMonitorInfo();
                                        // dialog1.dismiss();
                                    }
                                }).show();
            } else {
                loadMonitorInfo();
            }
        } else {
            loadMonitorInfo();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // check thread is running or not
                if (monitorThread != null) {
                    monitorThread.interrupt();
                    monitorThread = null;
                }
                // if (isModemEnable)
                if (Constants.mdmPwr) {
                    // dialog to turn off modem if modem is turn on

                    showDialog();

                } else
                    // finish activity
                    finish();

            }
        });


        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                btnUpdate.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdate.setClickable(true);
                    }
                }, 2000);

                if (constantMonitor.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);

                    } else {
                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(MonitorTest.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {

                                                // stub
                                                constantMonitor
                                                        .wakeUpDL();
                                                constantMonitor
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            MonitorTest.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    monitorUpdate();
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
                        monitorUpdate();
                }

            }
        });

        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(),
                        ParameterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void monitorUpdate() {

        if (!isValidAllValues()) {
            initProgressDialog("Updating Monitor Information !!!");
            dialog.show();
            monitorThread = new Thread() {
                public void run() {
                    updateMonitor();
                    Message msg;
                    if (exceptionMonitor) {
                        msg = setTextHandler
                                .obtainMessage(MESSAGE_MONITOR_EXCEPTION_SCREEN);
                    } else {
                        msg = setTextHandler
                                .obtainMessage(MESSAGE_MONITOR_UPDATING_SCREEN);

                    }
                    setTextHandler.sendMessage(msg);
                }
            };
            monitorThread.start();
        } else {
            if (exceptionMonitor) {
                exceptionMonitor = false;
                Toast.makeText(MonitorTest.this, err_msg, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    protected void showDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(MonitorTest.this)
                .setIcon(R.drawable.shutdown)
                .setTitle("MODEM")
                .setMessage("Do you want to turn OFF Modem?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog1,
                                                int which) {

                                initProgressDialog("Turning OFF modem...");
                                dialog.show();

                                monitorThread = new Thread() {
                                    public void run() {
                                        switchOFFModem();
                                        Constants.modemTurnOFFTimer = System
                                                .currentTimeMillis();
                                        quitfromApp = true;
                                        if (statusCodeMS != Constants.OK_STATUS) {
                                            expInGetScanInfo = true;
                                            error_msg = "Error in turn off modem...";
                                            // sending msg
                                            Message msg = setTextHandler
                                                    .obtainMessage(MESSAGE_TURN_OFF_EXCEPTION_OCCURED_SCREEN);
                                            setTextHandler.sendMessage(msg);
                                        } else {
                                            // sending msg
                                            Message msg = setTextHandler
                                                    .obtainMessage(MESSAGE_TURN_OFF_SCREEN);
                                            setTextHandler.sendMessage(msg);
                                        }
                                    }
                                };
                                monitorThread.start();
                            }

                        })
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog1,
                                                int which) {
                                finish();

                            }
                        }).show();
    }

    private void updateMonitor() {
        try {
            if (Variable.isConnected) {
                /* Command for setting parameter unit like Hz,Mwc etc. */
                constantMonitor.wakeUpDL();

                constantMonitor.sendCMDgetRLY("PARADCML,"
                        + spinnerDecimalPlace.getSelectedItem().toString().trim());
                statusCodeDcmlPlc = (short) Constants.sc;

                Constants.monitorInterval = Integer.parseInt(txtMonitorInterval
                        .getText().toString().trim());

            }
        } catch (Exception e) {
            err_msg = "Error in updating values...";
            exceptionMonitor = true;
            e.printStackTrace();
        }
    }

    private boolean isValidAllValues() {
        try {

            if (Integer
                    .parseInt(txtMonitorInterval.getText().toString().trim()) < 2
                    || Integer.parseInt(txtMonitorInterval.getText().toString()
                    .trim()) >= 256) {
                err_msg = "Please provide valid monitor interval 2 - 255 seconds...";
                exceptionMonitor = true;
                return true;
            }

        } catch (Exception e) {
            err_msg = "Error in updating values...";
            exceptionMonitor = true;
            return true;
        }
        return false;
    }


    public void switchOFFModem() {

        constantMonitor.wakeUpDL();

        constantMonitor.send("MDMPWR,\"OFF\"");
        try {
            long currentTimemill = System.currentTimeMillis();
            while (!Constants.gotReply) {
                if (System.currentTimeMillis() - currentTimemill >= 50000) {
                    Constants.toastFromThread = true;
                    break;
                }

            }
        } catch (Exception ignored) {

        }
        statusCodeMS = (short) Constants.sc;

    }

    private void switchOnModem() {

        constantMonitor.wakeUpDL();
        constantMonitor.send("MDMPWR,\"ON\"");
        long currentTimemill = System.currentTimeMillis();
        while (!Constants.gotReply) {
            if (System.currentTimeMillis() - currentTimemill >= 70000) {
                Constants.toastFromThread = true;
                break;
            }
        }
        statusCodeMS = (short) Constants.sc;
        // waitForTurnOnModem();

    }


    private void initProgressDialog(String msg) {
        // create a dialog
        dialog = new ProgressDialog(this);
        // set the title of the dialog
        dialog.setTitle(msg);
        // Set if the dialog can be skipped
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    private void loadMonitorInfo() {
        initProgressDialog("Loading Monitor Information !!!");
        dialog.show();
        monitorThread = new Thread() {
            public void run() {
                readMonitor();
                Message msg;
                if (!exceptionMonitor) {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_MONITOR_READING_SCREEN);
                } else {
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_MONITOR_EXCEPTION_SCREEN);
                }
                setTextHandler.sendMessage(msg);
            }
        };
        monitorThread.start();
    }

    private void readMonitor() {
        try {
            if (Variable.isConnected) {
                constantMonitor.wakeUpDL();

                dcml = constantMonitor.removeDQ(constantMonitor
                        .sendCMDgetRLY("PARADCML,\"?\""));
                statusCodeDcmlPlc = (short) Constants.sc;

            }
        } catch (Exception e) {
            err_msg = "Error in reading Monitor Parameter values...";
            exceptionMonitor = true;
            e.printStackTrace();
        }

    }

    private void displayData() {

        if (statusCodeDcmlPlc == Constants.OK_STATUS) {

            spinnerDecimalPlace.setSelection(arrayAdapter_DecimalPlace.getPosition(dcml));
            Constants.decimalPlaces = Integer.parseInt(dcml);
        } else
            spinnerDecimalPlace.setSelection(arrayAdapter_DecimalPlace.getPosition(Constants.decimalPlaces + ""));

        txtMonitorInterval.setText(String.valueOf(Constants.monitorInterval));
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(MonitorTest.this).setIcon(R.drawable.battery0)
                .setTitle(Constants.responseMsg)
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

    protected void showdialogs() {
        new androidx.appcompat.app.AlertDialog.Builder(MonitorTest.this).setTitle("Connection")
                .setIcon(R.drawable.error)
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
     * if back button pressed
     */
    @Override
    public void onBackPressed() {
        // your code.
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
        // if (isModemEnable)
        if (Constants.mdmPwr) {
            // dialog to turn off modem
            showDialog();
        } else
            // finish activity
            finish();
    }
}
