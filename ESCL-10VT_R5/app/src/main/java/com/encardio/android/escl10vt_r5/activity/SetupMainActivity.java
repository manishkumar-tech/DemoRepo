package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/*
 * @author Sandeep
 */

public class SetupMainActivity extends AppCompatActivity {


    private ProgressDialog dialog;
    private Thread resetThread;

    private Constants constantSetup;
    private short statusCodeReset;


    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (resetThread != null) {
                resetThread.interrupt();
                resetThread = null;
            }
            if (msg.what == 100) {
                dialog.dismiss();
                if (Constants.toastFromThread)
                    showDialog();
                else if (Constants.battery_low)
                    battryLowDialog();
                else {
                    if (Constants.gotReply
                            && statusCodeReset == Constants.OK_STATUS) {
                        Toast.makeText(getApplicationContext(),
                                "Data logger is reset now...",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_main);

        TextView txt_loggerDateTime = findViewById(R.id.txt_loggerDateTime);
        TextView txt_loggerInfo = findViewById(R.id.txt_loggerInfo);
        TextView txt_sensorSettings = findViewById(R.id.txt_sensorSettings);
        TextView txt_resetSettings = findViewById(R.id.txt_resetSettings);
        TextView txt_ftpServerContact = findViewById(R.id.txt_ftpServerContact);
        TextView txt_barometerSettings = findViewById(R.id.txt_barometerSettings);

        constantSetup = new Constants();

        txt_loggerDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        LoggerDateTimeSettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        txt_loggerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        LoggerInfoSettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        txt_sensorSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        SensorSettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        txt_barometerSettings
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Variable.isConnected) {
                            constantSetup.wakeUpDL();
                            String reply = constantSetup.removeDQ(constantSetup
                                    .sendCMDgetRLY("BAROSENS,\"?\""));
                            if (Constants.toastFromThread)
                                showDialog();
                            else if (Constants.battery_low)
                                battryLowDialog();
                            else {
                                short statsCode = (short) Constants.sc;
                                if (reply.trim().contains("ENABLE")
                                        && statsCode == Constants.OK_STATUS) {
                                    Intent intent = new Intent(v.getContext(),
                                            BarometerSettingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                } else
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Barometer Not Mounted In This DataLogger...",
                                            Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
        txt_resetSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                warningDialog();

            }
        });

        txt_ftpServerContact.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showInputDialog();

            }
        });

    }

    private void warningDialog() {
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.warning_icon)
                .setTitle(R.string.warning)
                .setMessage(R.string.details_for_reset)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                String temp = constantSetup.removeDQ(constantSetup
                                        .sendCMDgetRLY("SCAN,\"?\""));

                                if (Constants.battery_low)
                                    battryLowDialog();
                                else if (Constants.toastFromThread)
                                    showDialog();
                                else if (Constants.sc == Constants.OK_STATUS) {
                                    if (temp.equals("STOP")) {
                                        // call dialog to authenticate user
                                        authenticateDialog();
                                    } else {

                                        String wrn_msg = "Please stop scanning before RESET."
                                                + "\n"
                                                + "Do you Wish to stop scan?";
                                        new AlertDialog.Builder(
                                                SetupMainActivity.this)
                                                .setIcon(
                                                        R.drawable.warning_icon)
                                                .setTitle(R.string.warning)
                                                .setMessage(wrn_msg)
                                                .setPositiveButton(
                                                        "Yes",
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                // TODO
                                                                // Auto-generated
                                                                // method stub
                                                                constantSetup
                                                                        .wakeUpDL();
                                                                constantSetup
                                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                                short statusCodeSS = (short) Constants.sc;

                                                                if (Constants.battery_low)
                                                                    battryLowDialog();
                                                                else if (Constants.toastFromThread)
                                                                    showDialog();
                                                                else if (statusCodeSS == Constants.OK_STATUS) {
                                                                    Variable.scanStatus = false;
                                                                    Toast.makeText(
                                                                            SetupMainActivity.this,
                                                                            "Scan Stopped",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                                    authenticateDialog();
                                                                }
                                                            }
                                                        })
                                                .setNegativeButton("NO", null)
                                                .show();
                                    }
                                }
                            }
                        }).setNegativeButton(R.string.no, null).show();
    }

    /**
     * Authentication before reset data logger
     * <p>
     * Dialog will ask for password before reset data logger. After valid
     * password user is allowed for reset.
     */

    protected void showDialog() {

        new AlertDialog.Builder(SetupMainActivity.this).setTitle("Connection")
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

    private void authenticateDialog() {
        // Creating a custom dialog
        final Dialog mdialog = new Dialog(SetupMainActivity.this);
        // provide view to that custom dialog through xml

        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.reset_factory_password_dialog);
        mdialog.setTitle(R.string.reset_datalogger);
        final EditText editTextPassword = (EditText) mdialog
                .findViewById(R.id.editTextBatteryInstDate);
        Button buttonOk = (Button) mdialog.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button) mdialog.findViewById(R.id.buttonCancel);
        buttonOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = editTextPassword.getText().toString();

                if (password.equals("4TfZ9q7X")) {
                    resetDatalogger();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Invalid password...", Toast.LENGTH_LONG).show();
                }
                mdialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mdialog.dismiss();
            }
        });
        mdialog.show();
    }


    private void resetDatalogger() {
        initProgressDialog();
        dialog.show();

        resetThread = new Thread() {
            public void run() {
                String command = "INIT";
                // command send to datalogger
                constantSetup.removeDQ(constantSetup
                        .sendCMDgetRLY(command));
                statusCodeReset = (short) Constants.sc;
                if (Constants.gotReply
                        && statusCodeReset == Constants.OK_STATUS) {
                    command = "ERASE";
                    constantSetup.sendCMDgetRLY(command);
                    statusCodeReset = (short) Constants.sc;

                }
                // sending msg
                Message msg = setTextHandler.obtainMessage(100);
                setTextHandler.sendMessage(msg);

            }
        };
        resetThread.start();
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new androidx.appcompat.app.AlertDialog.Builder(SetupMainActivity.this)
                .setIcon(R.drawable.battery0).setTitle(Constants.responseMsg)
                .setMessage("Please replace battery immediately !!!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();

    }

    private void initProgressDialog() {
        // create a dialog
        dialog = new ProgressDialog(this);
        // set the title of the dialog
        dialog.setTitle("Reset Datalogger");
        // Set if the dialog can be skipped
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            this.finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    protected void showInputDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(SetupMainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.sms_contact, null);
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetupMainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        final SharedPreferences pref = getSharedPreferences(Constants.PREF_PHON_NAME, Context.MODE_PRIVATE);

        editText.setText(pref.getString(Constants.PREF_ADMIN_NAME, ""));
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Editor edit = pref.edit();
                        edit.putString(Constants.PREF_ADMIN_NAME, editText.getText().toString());
                        edit.apply();
                        Toast.makeText(SetupMainActivity.this, "Contact updated Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(SetupMainActivity.this, "Contact not updated", Toast.LENGTH_LONG).show();
                                dialog.cancel();

                            }
                        });


        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
