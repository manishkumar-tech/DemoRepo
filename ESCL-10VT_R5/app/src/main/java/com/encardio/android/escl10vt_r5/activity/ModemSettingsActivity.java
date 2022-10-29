package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

import org.apache.commons.net.ftp.FTPClient;

/*
 * @author Sandeep
 */


public class ModemSettingsActivity extends AppCompatActivity {

    private final int MESSAGE_FTP_CONNECTION_SCREEN = 10;
    private final int MESSAGE_MODEM_SETTING_READING_SCREEN = 20;
    private final int MESSAGE_MODEM_SETTING_EXCEPTION_SCREEN = 30;
    private final int MESSAGE_MODEM_SETTING_UPDATING_SCREEN = 40;
    private final int MESSAGE_FTP_SETTING_UPDATING_SCREEN = 50;
    private final int SCAN_EXCEPTION = 60;
    public TextView editTextURLValue;
    String err_msg = "";
    String[] spinner_operatingMode_value = {"SLEEP", "OFF"};
    ArrayAdapter<String> arrayAdapter_operatingMode;
    private Button btnScheduleUpload;
    private Button btnFTPSettings;
    private Button btnUpdateModemSetting;
    private Button btnContactList;
    private Spinner spinner_operatingMode;
    private Button btn_FTPSettingsTwoWay;
    private EditText editTextDialogURL;
    private EditText editTextDialogPort;
    private EditText editTextUser;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextApn;
    private CheckBox chkboxEnableSMS;
    private CheckBox checkBox_AutoSync;
    private EditText txt_MobileNumber;
    private boolean connectionProblem;
    private boolean exceptionMdmSttg;
    private Thread modemSettingThread;
    private ProgressDialog dialog;
    private Dialog mdialog;
    private String apn;
    private String operatingMode;
    private String enableSmsAlert;
    private String user;
    private String url;
    private String url2;
    private String port;
    private String autoRTC_Sync;
    private String dalaloggerMobileNo;
    private TextView txt_TwowayURL;
    private short statusCodeUrl;
    private short statusCodeApn;
    private short statusCodeOperatingMode;
    private short statusCodeSMSAlert;
    private Constants constantModem;
    private final Handler setTextHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (modemSettingThread != null) {
                modemSettingThread.interrupt();
                modemSettingThread = null;
            }
            switch (msg.what) {
                case MESSAGE_FTP_CONNECTION_SCREEN: {

                    dialog.dismiss();
                    mdialog.dismiss();
                    if (connectionProblem == true) {
                        connectionProblem = false;
                        new androidx.appcompat.app.AlertDialog.Builder(ModemSettingsActivity.this)
                                .setIcon(R.drawable.error)
                                .setTitle("ERROR")
                                .setMessage(Constants.sc + ": " + err_msg)
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                            }
                                        }).show();
                    }
                    break;
                }
                case MESSAGE_MODEM_SETTING_READING_SCREEN: {

                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_MODEM_SETTING_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (exceptionMdmSttg == true) {
                        exceptionMdmSttg = false;
                        Toast.makeText(ModemSettingsActivity.this,
                                Constants.sc + ": " + err_msg,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case MESSAGE_MODEM_SETTING_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodeApn != Constants.OK_STATUS
                                || statusCodeOperatingMode != Constants.OK_STATUS
                                || statusCodeSMSAlert != Constants.OK_STATUS) {
                            Toast.makeText(
                                    ModemSettingsActivity.this,
                                    Constants.sc
                                            + ": "
                                            + "Error In Updating Modem Setting Information...",
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(ModemSettingsActivity.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();
                    } else
                        showDialog();
                    break;

                case MESSAGE_FTP_SETTING_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        url = constantModem.removeDQ(constantModem
                                .sendCMDgetRLY("FTPURL,\"?\""));
                        statusCodeUrl = (short) Constants.sc;
                        port = constantModem.removeDQ(constantModem
                                .sendCMDgetRLY("FTPPORT,\"?\""));


                        displayData();
                        if (exceptionMdmSttg == true)
                            Toast.makeText(
                                    ModemSettingsActivity.this,
                                    Constants.sc + ": "
                                            + "Error in updating in FTP setting...",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(ModemSettingsActivity.this,
                                    "FTP setting updated successfully...",
                                    Toast.LENGTH_LONG).show();
                    } else
                        showDialog();
                    break;

                case SCAN_EXCEPTION:

                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.connectionBreak)
                        showDialog();
                    else
                        Toast.makeText(
                                ModemSettingsActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();

                    break;
            }
            connectionProblem = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.modem_settings);


        btnScheduleUpload = findViewById(R.id.btnUploadSchedule);
        btnFTPSettings = findViewById(R.id.btnFTPSettings);
        btnUpdateModemSetting = findViewById(R.id.btnUpdateModemSetting);
        spinner_operatingMode = findViewById(R.id.spinner_operatingMode);
        editTextApn = findViewById(R.id.editTextAPNValue);
        editTextURLValue = findViewById(R.id.editTextURLValue);
        btnContactList = findViewById(R.id.buttonContactList);
        chkboxEnableSMS = findViewById(R.id.checkBoxEnableSMSAlert);


        btn_FTPSettingsTwoWay = findViewById(R.id.btn_FTPSettingsTwoWay);
        checkBox_AutoSync = findViewById(R.id.checkBox_AutoSync);
        txt_MobileNumber = findViewById(R.id.txt_MobileNumber);
        txt_TwowayURL = findViewById(R.id.txt_TwowayURL);

        arrayAdapter_operatingMode = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, spinner_operatingMode_value);
        arrayAdapter_operatingMode.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_operatingMode.setAdapter(arrayAdapter_operatingMode);

        constantModem = new Constants();

        setActiononButtons();
        initProgressDialog("Loading Modem Setting Information !!!");
        dialog.show();
        modemSettingThread = new Thread() {
            public void run() {
                // get modem Setting parameters from data logger
                readModemSettingsParameter();
                if (exceptionMdmSttg) {
                    // sending msg
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_MODEM_SETTING_EXCEPTION_SCREEN);
                    setTextHandler.sendMessage(msg);
                } else {
                    // sending msg
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_MODEM_SETTING_READING_SCREEN);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        modemSettingThread.start();

    }

    /**
     * reading modem parameter
     */
    private void readModemSettingsParameter() {
        try {
            if (Variable.isConnected == true) {
                constantModem.wakeUpDL();
                apn = constantModem.removeDQ(constantModem
                        .sendCMDgetRLY("APN,\"?\""));

                statusCodeApn = (short) Constants.sc;
                if (Constants.sc == Constants.OK_STATUS) {
                    operatingMode = constantModem
                            .removeDQ(constantModem
                                    .sendCMDgetRLY("MDMMODE,\"?\""));

                    statusCodeOperatingMode = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    enableSmsAlert = constantModem
                            .removeDQ(constantModem
                                    .sendCMDgetRLY("SMSALRT,\"?\""));
                    statusCodeSMSAlert = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    url = constantModem.removeDQ(constantModem
                            .sendCMDgetRLY("FTPURL,\"?\""));
                    statusCodeUrl = (short) Constants.sc;
                }


                if (Constants.sc == Constants.OK_STATUS) {
                    url2 = constantModem.removeDQ(constantModem
                            .sendCMDgetRLY("FTPURL2,\"?\""));

                }
                if (Constants.sc == Constants.OK_STATUS) {
                    autoRTC_Sync = constantModem.removeDQ(constantModem
                            .sendCMDgetRLY("RTCASYNC,\"?\""));
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    dalaloggerMobileNo = constantModem.removeDQ(constantModem
                            .sendCMDgetRLY("DLMOBNO,\"?\""));
                }

            }
        } catch (Exception e) {
            // exception
            exceptionMdmSttg = true;
            err_msg = "Error in reading Modem Setting Values...";
            e.printStackTrace();
        }
    }

    private void displayData() {
        try {
            if (statusCodeUrl == Constants.OK_STATUS) {
                editTextURLValue.setText(url);
                if (!url.startsWith("XXX"))
                    Constants.url = url;
            } else
                editTextURLValue.setText("");

            txt_TwowayURL.setText(url2);


            if (statusCodeOperatingMode == Constants.OK_STATUS)

                spinner_operatingMode.setSelection(arrayAdapter_operatingMode.getPosition(operatingMode));
            else {
                operatingMode = "OFF";
                spinner_operatingMode.setSelection(arrayAdapter_operatingMode.getPosition(operatingMode));
            }
            if (statusCodeSMSAlert == Constants.OK_STATUS) {
                if (enableSmsAlert.trim().equalsIgnoreCase("ENABLE"))
                    chkboxEnableSMS.setChecked(true);
            } else
                chkboxEnableSMS.setChecked(false);
            if (statusCodeApn == Constants.OK_STATUS) {
                if (apn.equals("XXXXXXXXXXXXXXXXXXXX"))
                    editTextApn.setText("");
                else
                    editTextApn.setText(apn);
            } else
                editTextApn.setText("");


            checkBox_AutoSync.setChecked(autoRTC_Sync.trim().equalsIgnoreCase("ENABLE"));
            txt_MobileNumber.setText(dalaloggerMobileNo);


        } catch (Exception e) {
            exceptionMdmSttg = true;
            err_msg = "Error in reading Modem Setting Values...";
            e.printStackTrace();
        }
    }

    private void setActiononButtons() {
        btnScheduleUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModemSettingsActivity.this,
                        ScheduledUploadActivity.class);
                startActivity(intent);
            }
        });
        btnContactList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModemSettingsActivity.this,
                        ContactListActivity.class);
                startActivity(intent);
            }
        });

        btnUpdateModemSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnUpdateModemSetting.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdateModemSetting.setClickable(true);
                    }
                }, 2000);

                if (constantModem.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {

                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(ModemSettingsActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {

                                                constantModem
                                                        .wakeUpDL();
                                                constantModem
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            ModemSettingsActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    updateModem();
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
                        updateModem();
                    else {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    }
                }
            }
        });


        btnFTPSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnUpdateModemSetting.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdateModemSetting.setClickable(true);
                    }
                }, 2000);

                // Creating a custom dialog
                mdialog = new Dialog(ModemSettingsActivity.this);
                // provide view to that custom dialog through xml
                mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mdialog.setContentView(R.layout.ftp_setting_dialog);
                mdialog.setTitle("FTP Settings");

                editTextDialogURL = (EditText) mdialog
                        .findViewById(R.id.editTextDialogURL);
                editTextDialogPort = (EditText) mdialog
                        .findViewById(R.id.editTextDialogPort);
                editTextUser = (EditText) mdialog
                        .findViewById(R.id.editTextUser);
                editTextPassword = (EditText) mdialog
                        .findViewById(R.id.editTextBatteryInstDate);
                editTextConfirmPassword = (EditText) mdialog
                        .findViewById(R.id.editTextDialogConfirm);

                editTextDialogURL.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPURL,\"?\"")));
                editTextDialogPort.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPPORT,\"?\"")));
                editTextUser.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPUSER,\"?\"")));
                editTextPassword.setText("");
                editTextConfirmPassword.setText("");
                final Button buttonUpdateFTPSettings = (Button) mdialog
                        .findViewById(R.id.buttonUpdateFTPSettings);

                buttonUpdateFTPSettings
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                buttonUpdateFTPSettings.setClickable(false);
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        buttonUpdateFTPSettings
                                                .setClickable(true);
                                    }
                                }, 2000);
                                if (constantModem.checkScanStatus()) {
                                    if (Constants.sc != Constants.OK_STATUS) {
                                        Message msg = setTextHandler
                                                .obtainMessage(SCAN_EXCEPTION);
                                        setTextHandler.sendMessage(msg);
                                    } else {

                                        String wrn_msg = "Please stop scanning to update scan parameters.."
                                                + "\n"
                                                + "Do you Wish to stop scan?";
                                        new AlertDialog.Builder(
                                                ModemSettingsActivity.this)
                                                .setTitle(R.string.warning)
                                                .setMessage(wrn_msg)
                                                .setIcon(
                                                        R.drawable.warning_icon)
                                                .setPositiveButton(
                                                        "Yes",
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {

                                                                constantModem
                                                                        .wakeUpDL();
                                                                constantModem
                                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                                short statusCodeSS = (short) Constants.sc;
                                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                                    Variable.scanStatus = false;
                                                                    Toast.makeText(
                                                                            ModemSettingsActivity.this,
                                                                            "Scan Stopped",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();

                                                                    constantModem
                                                                            .wakeUpDL();
                                                                    if (!checkEmptyFtpSettings()) {
                                                                        initProgressDialog("Updating FTP setting");
                                                                        ((Dialog) dialog)
                                                                                .show();
                                                                        modemSettingThread = new Thread() {
                                                                            public void run() {
                                                                                try {
                                                                                    if (!checkEmptyFtpSettings()) {
                                                                                        updateFtpSetting();
                                                                                        mdialog.dismiss();
                                                                                        Message msg = setTextHandler
                                                                                                .obtainMessage(MESSAGE_FTP_SETTING_UPDATING_SCREEN);
                                                                                        setTextHandler
                                                                                                .sendMessage(msg);
                                                                                    }
                                                                                } catch (Exception e) {
                                                                                    err_msg = "Error in updating FTP settings.";
                                                                                    Toast.makeText(
                                                                                            ModemSettingsActivity.this,
                                                                                            err_msg,
                                                                                            Toast.LENGTH_LONG)
                                                                                            .show();
                                                                                }
                                                                            }

                                                                        };
                                                                        modemSettingThread
                                                                                .start();
                                                                    } else {

                                                                        Toast.makeText(
                                                                                ModemSettingsActivity.this,
                                                                                err_msg,
                                                                                Toast.LENGTH_LONG)
                                                                                .show();
                                                                    }

                                                                } else {
                                                                    Message msg = setTextHandler
                                                                            .obtainMessage(SCAN_EXCEPTION);
                                                                    setTextHandler.sendMessage(msg);
                                                                }
                                                            }
                                                        })
                                                .setNegativeButton("NO", null)
                                                .show();

                                    }
                                } else {
                                    constantModem.wakeUpDL();
                                    if (!checkEmptyFtpSettings()) {
                                        initProgressDialog("Updating FTP setting");
                                        dialog.show();
                                        modemSettingThread = new Thread() {
                                            public void run() {
                                                try {
                                                    if (!checkEmptyFtpSettings()) {
                                                        updateFtpSetting();
                                                        mdialog.dismiss();
                                                        Message msg = setTextHandler
                                                                .obtainMessage(MESSAGE_FTP_SETTING_UPDATING_SCREEN);
                                                        setTextHandler
                                                                .sendMessage(msg);
                                                    }
                                                } catch (Exception e) {
                                                    err_msg = "Error in updating FTP settings.";
                                                    Toast.makeText(
                                                            ModemSettingsActivity.this,
                                                            err_msg,
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            }

                                        };
                                        modemSettingThread.start();
                                    } else if (connectionProblem == true) {
                                        connectionProblem = false;
                                        Toast.makeText(
                                                ModemSettingsActivity.this,
                                                err_msg, Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            }
                        });
                mdialog.show();

            }
        });


        btn_FTPSettingsTwoWay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn_FTPSettingsTwoWay.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btn_FTPSettingsTwoWay.setClickable(true);
                    }
                }, 2000);

                mdialog = new Dialog(ModemSettingsActivity.this);
                mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mdialog.setContentView(R.layout.ftp_setting_dialog_twoway);
                mdialog.setTitle("Two way Communication FTP Settings");

                editTextDialogURL = (EditText) mdialog
                        .findViewById(R.id.editTextDialogURL);
                editTextDialogPort = (EditText) mdialog
                        .findViewById(R.id.editTextDialogPort);
                editTextUser = (EditText) mdialog
                        .findViewById(R.id.editTextUser);
                editTextPassword = (EditText) mdialog
                        .findViewById(R.id.editTextBatteryInstDate);
                editTextConfirmPassword = (EditText) mdialog
                        .findViewById(R.id.editTextDialogConfirm);

                editTextDialogURL.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPURL2,\"?\"")));
                editTextDialogPort.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPPORT2,\"?\"")));
                editTextUser.setText(constantModem
                        .removeDQ(constantModem
                                .sendCMDgetRLY("FTPUSER2,\"?\"")));
                editTextPassword.setText("");
                editTextConfirmPassword.setText("");


                final Button buttonUpdateFTPSettings = (Button) mdialog
                        .findViewById(R.id.buttonUpdateFTPSettings);

                buttonUpdateFTPSettings
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                buttonUpdateFTPSettings.setClickable(false);
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        buttonUpdateFTPSettings
                                                .setClickable(true);
                                    }
                                }, 2000);
                                if (constantModem.checkScanStatus()) {
                                    if (Constants.sc != Constants.OK_STATUS) {
                                        Message msg = setTextHandler
                                                .obtainMessage(SCAN_EXCEPTION);
                                        setTextHandler.sendMessage(msg);
                                    } else {

                                        String wrn_msg = "Please stop scanning to update scan parameters.."
                                                + "\n"
                                                + "Do you Wish to stop scan?";
                                        new AlertDialog.Builder(
                                                ModemSettingsActivity.this)
                                                .setTitle(R.string.warning)
                                                .setMessage(wrn_msg)
                                                .setIcon(
                                                        R.drawable.warning_icon)
                                                .setPositiveButton(
                                                        "Yes",
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {

                                                                constantModem
                                                                        .wakeUpDL();
                                                                constantModem
                                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                                short statusCodeSS = (short) Constants.sc;
                                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                                    Variable.scanStatus = false;
                                                                    Toast.makeText(
                                                                            ModemSettingsActivity.this,
                                                                            "Scan Stopped",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();

                                                                    constantModem
                                                                            .wakeUpDL();
                                                                    if (!checkEmptyFtpSettings()) {
                                                                        initProgressDialog("Updating FTP setting");
                                                                        ((Dialog) dialog)
                                                                                .show();
                                                                        modemSettingThread = new Thread() {
                                                                            public void run() {
                                                                                try {
                                                                                    if (!checkEmptyFtpSettings()) {
                                                                                        updateFtpSettingForTwoWayComm();
                                                                                        mdialog.dismiss();
                                                                                        Message msg = setTextHandler
                                                                                                .obtainMessage(MESSAGE_FTP_SETTING_UPDATING_SCREEN);
                                                                                        setTextHandler
                                                                                                .sendMessage(msg);
                                                                                    }
                                                                                } catch (Exception e) {
                                                                                    err_msg = "Error in updating FTP settings.";
                                                                                    Toast.makeText(
                                                                                            ModemSettingsActivity.this,
                                                                                            err_msg,
                                                                                            Toast.LENGTH_LONG)
                                                                                            .show();
                                                                                }
                                                                            }

                                                                        };
                                                                        modemSettingThread
                                                                                .start();
                                                                    } else {

                                                                        Toast.makeText(
                                                                                ModemSettingsActivity.this,
                                                                                err_msg,
                                                                                Toast.LENGTH_LONG)
                                                                                .show();
                                                                    }

                                                                } else {
                                                                    Message msg = setTextHandler
                                                                            .obtainMessage(SCAN_EXCEPTION);
                                                                    setTextHandler.sendMessage(msg);
                                                                }
                                                            }
                                                        })
                                                .setNegativeButton("NO", null)
                                                .show();

                                    }
                                } else {
                                    constantModem.wakeUpDL();
                                    if (!checkEmptyFtpSettings()) {
                                        initProgressDialog("Updating FTP setting");
                                        dialog.show();
                                        modemSettingThread = new Thread() {
                                            public void run() {
                                                try {
                                                    if (!checkEmptyFtpSettings()) {
                                                        updateFtpSettingForTwoWayComm();
                                                        mdialog.dismiss();
                                                        Message msg = setTextHandler
                                                                .obtainMessage(MESSAGE_FTP_SETTING_UPDATING_SCREEN);
                                                        setTextHandler
                                                                .sendMessage(msg);
                                                    }
                                                } catch (Exception e) {
                                                    err_msg = "Error in updating FTP settings.";
                                                    Toast.makeText(
                                                            ModemSettingsActivity.this,
                                                            err_msg,
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            }

                                        };
                                        modemSettingThread.start();
                                    } else if (connectionProblem == true) {
                                        connectionProblem = false;
                                        Toast.makeText(
                                                ModemSettingsActivity.this,
                                                err_msg, Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            }
                        });
                mdialog.show();

            }
        });


    }

    private void updateModem() {

        if (editTextApn.getText().toString().length() > 0) {
            initProgressDialog("Updating Modem Setting Information !!!");
            dialog.show();

            modemSettingThread = new Thread() {
                public void run() {
                    try {
                        constantModem.wakeUpDL();
                        constantModem.sendCMDgetRLY("APN,\""
                                + editTextApn.getText().toString() + "\"");
                        statusCodeApn = (short) Constants.sc;
                        if (Constants.sc == Constants.OK_STATUS) {
                            constantModem.sendCMDgetRLY("MDMMODE,\""
                                    + spinner_operatingMode.getSelectedItem().toString().trim()
                                    + "\"");
                            statusCodeOperatingMode = (short) Constants.sc;
                        }
                        if (Constants.sc == Constants.OK_STATUS) {
                            if (chkboxEnableSMS.isChecked()) {
                                constantModem
                                        .sendCMDgetRLY("SMSALRT,\"ENABLE\"");
                            } else {
                                constantModem
                                        .sendCMDgetRLY("SMSALRT,\"DISABLE\"");
                            }
                        }


                        if (Constants.sc == Constants.OK_STATUS) {
                            if (checkBox_AutoSync.isChecked()) {
                                constantModem.sendCMDgetRLY("RTCASYNC,\"ENABLE\"");
                            } else {
                                constantModem.sendCMDgetRLY("RTCASYNC,\"DISABLE\"");
                            }
                        }

                        if (Constants.sc == Constants.OK_STATUS) {
                            constantModem.sendCMDgetRLY("DLMOBNO,\"" + txt_MobileNumber.getText() + "\"");
                        }


                        Message msg = setTextHandler
                                .obtainMessage(MESSAGE_MODEM_SETTING_UPDATING_SCREEN);
                        setTextHandler.sendMessage(msg);
                    } catch (Exception e) {
                        exceptionMdmSttg = true;
                        err_msg = "Error In Updating Modem Setting Information...";
                        Message msg = setTextHandler
                                .obtainMessage(MESSAGE_MODEM_SETTING_EXCEPTION_SCREEN);
                        setTextHandler.sendMessage(msg);
                    }
                }
            };
            modemSettingThread.start();
        } else
            Toast.makeText(getApplicationContext(), "Please Enter APN",
                    Toast.LENGTH_SHORT).show();

    }


    public void updateFtpSetting() {

        constantModem.sendCMDgetRLY("FTPURL,\""
                + editTextDialogURL.getText().toString().trim() + "\"");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPPORT,"
                    + editTextDialogPort.getText().toString().trim() + "");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPUSER,\""
                    + editTextUser.getText().toString().trim() + "\"");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPPWD,\""
                    + editTextPassword.getText().toString().trim() + "\"");
    }


    public void updateFtpSettingForTwoWayComm() {

        constantModem.sendCMDgetRLY("FTPURL2,\""
                + editTextDialogURL.getText().toString().trim() + "\"");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPPORT2,"
                    + editTextDialogPort.getText().toString().trim() + "");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPUSER2,\""
                    + editTextUser.getText().toString().trim() + "\"");
        if (Constants.sc == Constants.OK_STATUS)
            constantModem.sendCMDgetRLY("FTPPWD2,\""
                    + editTextPassword.getText().toString().trim() + "\"");
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(ModemSettingsActivity.this)
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

        new AlertDialog.Builder(ModemSettingsActivity.this)
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

    private boolean checkEmptyFtpSettings() {
        try {
            if (editTextDialogURL.getText().toString() != null
                    && editTextDialogURL.getText().toString().trim().length() > 0) {
            } else {
                connectionProblem = true;
                err_msg = "URL can't be empty...";
                return true;
            }
            if (editTextDialogPort.getText().toString() != null
                    && editTextDialogPort.getText().toString().trim().length() > 0) {

            } else {
                connectionProblem = true;
                err_msg = "Port can't be empty...";
                return true;
            }
            if (editTextUser.getText().toString() != null
                    && editTextUser.getText().toString().trim().length() > 0) {
            } else {
                connectionProblem = true;
                err_msg = "FTP User can't be empty...";
                return true;
            }
            if (Constants.validateFTP_URL_AND_Password(editTextUser.getText().toString())) {

            } else {
                connectionProblem = true;
                err_msg = "The FTP User should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)";
                return true;
            }
            if (editTextPassword.getText().toString() != null
                    && editTextPassword.getText().toString().trim().length() > 0) {
            } else {
                connectionProblem = true;
                err_msg = "Password can't be empty...";
                return true;
            }

            if (Constants.validateFTP_URL_AND_Password(editTextPassword.getText().toString())) {
            } else {
                connectionProblem = true;
                err_msg = "The password should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)";
                return true;
            }

            String pass;
            String Confirmpass;
            pass = editTextPassword.getText().toString();
            Confirmpass = editTextConfirmPassword.getText().toString();
            if (!pass.equalsIgnoreCase("")) {
            } else {
                connectionProblem = true;
                err_msg = "Password can't be empty...";
                return true;
            }
            if (pass.equalsIgnoreCase(Confirmpass)) {

            } else {
                connectionProblem = true;
                err_msg = "Password and Confirm password must be same...";
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    private boolean isFTPConnectionSucceeded() {
        FTPClient con = new FTPClient();
        try {
            con.setConnectTimeout(30000); // 1 minute
            con.connect(
                    editTextDialogURL.getText().toString().trim(),
                    Integer.parseInt(editTextDialogURL.getText().toString()
                            .trim()));
        } catch (Exception e1) {
            connectionProblem = true;
            err_msg = "Unable to connect to server !!!\nPlease check Network, URL and Port...";
            return false;
        }
        try {
            if (con.login(editTextUser.getText().toString().trim(),
                    editTextPassword.getText().toString().trim()) == true) {
                con.logout();
                con.disconnect();
                return true;
            } else {
                con.logout();
                con.disconnect();
                connectionProblem = true;
                err_msg = "Userid or Password is incorrect...";
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }


}

