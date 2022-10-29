package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * @author Sandeep
 */
public class ContactListActivity extends AppCompatActivity {

    private final int MESSAGE_CONTACT_READING_SCREEN = 10;
    private final int MESSAGE_CONTACT_UPDATING_SCREEN = 20;
    private final int MESSAGE_CONTACT_EXCEPTION_SCREEN = 30;
    private final int MESSAGE_CONTACT_MESSAGE_SENT = 40;
    private final int SCAN_EXCEPTION = 50;
    String err_msg = "";
    private Button btn_update;
    private Button btn_SendTestSMS;
    private EditText txtContactNo1;
    private EditText txtContactNo2;
    private EditText txtContactNo3;
    private EditText txtContactNo4;
    private EditText txtContactNo5;
    private EditText txtContactNo6;
    private EditText txtContactNo7;
    private EditText txtContactNo8;
    private boolean exceptionMdmSttg;
    /**
     * The system info thread.
     */
    private Thread modemSettingThread;
    /**
     * The dialog.
     */
    private ProgressDialog dialog;
    private String contact1;
    private String contact2;
    private String contact3;
    private String contact4;
    private String contact5;
    private String contact6;
    private String contact7;
    private String contact8;
    private String contactAdmin;
    private short statusCodeContact1;
    private short statusCodeContact2;
    private short statusCodeContact3;
    private short statusCodeContact4;
    private short statusCodeContact5;
    private short statusCodeContact6;
    private short statusCodeContact7;
    private short statusCodeContact8;
    private short statusCodeContactAdmin;
    /**
     * The constant scan.
     */
    private Constants constantModem;
    private int statusCodesms;
    /**
     * The set text handler.
     */

    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (modemSettingThread != null) {
                modemSettingThread.interrupt();
                modemSettingThread = null;
            }
            switch (msg.what) {
                case MESSAGE_CONTACT_READING_SCREEN: {
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_CONTACT_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else if (exceptionMdmSttg) {
                        exceptionMdmSttg = false;
                        Toast.makeText(ContactListActivity.this,
                                Constants.sc + ": " + err_msg,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case MESSAGE_CONTACT_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodeContact1 != Constants.OK_STATUS) {
                            Toast.makeText(
                                    ContactListActivity.this,
                                    statusCodeContact1
                                            + ": "
                                            + "Error In Updating Modem Setting Information !!!",
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(ContactListActivity.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();
                    } else
                        showDialog();
                    break;
                case MESSAGE_CONTACT_MESSAGE_SENT:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodesms != Constants.OK_STATUS)
                            Toast.makeText(
                                    ContactListActivity.this,
                                    statusCodesms
                                            + ": SMS Alert Disable or contact number not available...",
                                    Toast.LENGTH_LONG).show();
                        else if (Constants.sc == Constants.OK_STATUS)
                            Toast.makeText(ContactListActivity.this,
                                    "Message sent !", Toast.LENGTH_LONG).show();

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
                                ContactListActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();

                    break;

            }
            exceptionMdmSttg = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        TextView lbl_titleContactList = findViewById(R.id.lbl_titleContactList);
        txtContactNo1 = findViewById(R.id.editTextContactNo1);
        txtContactNo2 = findViewById(R.id.editTextContactNo2);
        txtContactNo3 = findViewById(R.id.editTextContactNo3);
        txtContactNo4 = findViewById(R.id.editTextContactNo4);
        txtContactNo5 = findViewById(R.id.editTextContactNo5);
        txtContactNo6 = findViewById(R.id.editTextContactNo6);
        txtContactNo7 = findViewById(R.id.editTextContactNo7);
        txtContactNo8 = findViewById(R.id.editTextContactNo8);

        btn_update = findViewById(R.id.btn_update);
        btn_SendTestSMS = findViewById(R.id.btn_SendTestSMS);

        constantModem = new Constants();
        initProgressDialog("Loading Contact Information !!!");
        dialog.show();
        modemSettingThread = new Thread() {
            public void run() {
                // get modem Setting parameters from data logger
                readContactNumbers();
                Message msg;
                if (!exceptionMdmSttg) {
                    // sending msg
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_CONTACT_READING_SCREEN);
                } else {
                    // sending msg
                    msg = setTextHandler
                            .obtainMessage(MESSAGE_CONTACT_EXCEPTION_SCREEN);
                }
                setTextHandler.sendMessage(msg);
            }
        };
        modemSettingThread.start();
        lbl_titleContactList.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                showInputDialog();
                return true;

            }
        });
        btn_SendTestSMS.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn_SendTestSMS.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btn_SendTestSMS.setClickable(true);
                    }
                }, 2000);
                constantModem.wakeUpDL();
                if (!constantModem.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        if (checkNoValidity()) {
                            initProgressDialog("Sending Message !!!");

                            dialog.show();
                            modemSettingThread = new Thread() {
                                public void run() {
                                    try {
                                        updateContact();
                                        if (exceptionMdmSttg) {
                                            // sending msg
                                            Message msg = setTextHandler
                                                    .obtainMessage(MESSAGE_CONTACT_EXCEPTION_SCREEN);
                                            setTextHandler.sendMessage(msg);
                                        } else {

                                            constantModem
                                                    .sendCommandAndGetReplyforMonPara("TESTSMS");
                                            statusCodesms = (short) Constants.sc;

                                            Message msg = setTextHandler
                                                    .obtainMessage(MESSAGE_CONTACT_MESSAGE_SENT);
                                            setTextHandler.sendMessage(msg);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            modemSettingThread.start();
                        } else {
                            Toast.makeText(ContactListActivity.this, err_msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {

                        String wrn_msg = "Please stop scan before sending message."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(ContactListActivity.this)
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
                                                constantModem
                                                        .wakeUpDL();
                                                constantModem
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;

                                                } else {
                                                    Message msg = setTextHandler
                                                            .obtainMessage(SCAN_EXCEPTION);
                                                    setTextHandler
                                                            .sendMessage(msg);
                                                }
                                            }
                                        }).setNegativeButton("NO", null).show();

                        /*
                         * Toast.makeText(ContactListActivity.this,
                         * "Please stop scan before sending message...",
                         * Toast.LENGTH_LONG).show();
                         */

                    }
                }
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_update.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btn_update.setClickable(true);
                    }
                }, 2000);
                constantModem.wakeUpDL();
                if (!constantModem.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        if (checkNoValidity()) {
                            initProgressDialog("Updating Contact Information !!!");
                            dialog.show();
                            modemSettingThread = new Thread() {
                                public void run() {
                                    try {
                                        updateContact();

                                    } catch (Exception e) {
                                        exceptionMdmSttg = true;
                                        err_msg = "Error in updating contact number...";
                                    }

                                    Message msg;
                                    if (exceptionMdmSttg) {
                                        // sending msg
                                        msg = setTextHandler
                                                .obtainMessage(MESSAGE_CONTACT_EXCEPTION_SCREEN);
                                    } else {
                                        // sending msg
                                        msg = setTextHandler
                                                .obtainMessage(MESSAGE_CONTACT_UPDATING_SCREEN);
                                    }
                                    setTextHandler.sendMessage(msg);
                                }

                            };
                            modemSettingThread.start();
                        } else {
                            Toast.makeText(ContactListActivity.this, err_msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        String wrn_msg = "Please stop scan before updating contact numbers.."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(ContactListActivity.this)
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
                                                constantModem
                                                        .wakeUpDL();
                                                constantModem
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            ContactListActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();

                                                    if (checkNoValidity()) {
                                                        initProgressDialog("Updating Contact Information !!!");
                                                        ((Dialog) dialog)
                                                                .show();
                                                        modemSettingThread = new Thread() {
                                                            public void run() {
                                                                try {
                                                                    updateContact();

                                                                } catch (Exception e) {
                                                                    exceptionMdmSttg = true;
                                                                    err_msg = "Error in updating contact number...";
                                                                }

                                                                Message msg;
                                                                if (exceptionMdmSttg) {
                                                                    // sending
                                                                    // msg
                                                                    msg = setTextHandler
                                                                            .obtainMessage(MESSAGE_CONTACT_EXCEPTION_SCREEN);
                                                                } else {
                                                                    // sending
                                                                    // msg
                                                                    msg = setTextHandler
                                                                            .obtainMessage(MESSAGE_CONTACT_UPDATING_SCREEN);
                                                                }
                                                                setTextHandler
                                                                        .sendMessage(msg);
                                                            }

                                                        };
                                                        modemSettingThread
                                                                .start();
                                                    } else {
                                                        Toast.makeText(
                                                                ContactListActivity.this,
                                                                err_msg,
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                    }

                                                } else {
                                                    Message msg = setTextHandler
                                                            .obtainMessage(SCAN_EXCEPTION);
                                                    setTextHandler
                                                            .sendMessage(msg);
                                                }
                                            }
                                        }).setNegativeButton("NO", null).show();
                    }
                }
                /*
                 * Toast.makeText( ContactListActivity.this,
                 * "Please stop scan before updating contact numbers...",
                 * Toast.LENGTH_LONG).show();
                 */
            }
        });
    }

    public void updateContact() {
        try {
            statusCodeContact1 = Constants.OK_STATUS;

            String[] contactno = new String[8];
            contactno[0] = txtContactNo1.getText().toString();
            contactno[1] = txtContactNo2.getText().toString();
            contactno[2] = txtContactNo3.getText().toString();
            contactno[3] = txtContactNo4.getText().toString();
            contactno[4] = txtContactNo5.getText().toString();
            contactno[5] = txtContactNo6.getText().toString();
            contactno[6] = txtContactNo7.getText().toString();
            contactno[7] = txtContactNo8.getText().toString();
            constantModem.wakeUpDL();

            // updating contacts in firmware through sending commands
            int j = 1;
            for (int i = 1; i <= 8; i++) {
                if ((contactno[i - 1].length() > 7)
                ) {
                    constantModem.sendCMDgetRLY("ALRTCONT," + j
                            + ",\"" + contactno[i - 1] + "\"");
                    j++;
                }
            }

            for (int i = j; i <= 8; i++) {
                constantModem.sendCMDgetRLY("ALRTCONT," + i
                        + ",\"XXXXXXXXXXXXXXX\"");
            }
        } catch (Exception e) {
            exceptionMdmSttg = true;
            err_msg = "invalid contact number";
        }
    }

    // get contacts from firmware
    private void readContactNumbers() {
        try {
            if (Variable.isConnected) {
                constantModem.wakeUpDL();
                if (Constants.sc == Constants.OK_STATUS) {
                    contactAdmin = constantModem.sendCMDgetFullRLY("ADMINCONT,1,\"?\"");
                    Log.d("AdminContact", "" + contactAdmin);
                    statusCodeContactAdmin = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {

                    contact1 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,1,\"?\"");
                    statusCodeContact1 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    contact2 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,2,\"?\"");
                    statusCodeContact2 = (short) Constants.sc;
                }
                contact3 = constantModem
                        .sendCMDgetFullRLY("ALRTCONT,3,\"?\"");
                statusCodeContact3 = (short) Constants.sc;
                if (Constants.sc == Constants.OK_STATUS) {
                    contact4 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,4,\"?\"");
                    statusCodeContact4 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    contact5 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,5,\"?\"");
                    statusCodeContact5 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    contact6 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,6,\"?\"");
                    statusCodeContact6 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    contact7 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,7,\"?\"");
                    statusCodeContact7 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    contact8 = constantModem
                            .sendCMDgetFullRLY("ALRTCONT,8,\"?\"");
                    statusCodeContact8 = (short) Constants.sc;
                }

            }
        } catch (Exception e) {
            exceptionMdmSttg = true;
            err_msg = "Error in reading Contact Numbers...";
            e.printStackTrace();
        }
    }

    /**
     * edit text validation
     */

    private boolean checkNoValidity() {
        try {
            if ((txtContactNo1.getText().toString().trim().length() > 0)
                    || (txtContactNo2.getText().toString().trim().length() > 0)
                    || (txtContactNo3.getText().toString().trim().length() > 0)
                    || (txtContactNo4.getText().toString().trim().length() > 0)
                    || (txtContactNo5.getText().toString().trim().length() > 0)
                    || (txtContactNo6.getText().toString().trim().length() > 0)
                    || (txtContactNo7.getText().toString().trim().length() > 0)
                    || (txtContactNo8.getText().toString().trim().length() > 0)) {
                if (txtContactNo1.getText().toString() != null
                        && txtContactNo1.getText().toString().trim().length() > 0) {
                    if (txtContactNo1.getText().toString().trim().length() < 8
                            || txtContactNo1.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 1 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo1.getText()
                                    .toString().trim().substring(1
                                    )));

                        } catch (Exception e) {
                            err_msg = "Contact No. 1 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo2.getText().toString() != null
                        && txtContactNo2.getText().toString().trim().length() > 0) {
                    if (txtContactNo2.getText().toString().trim().length() < 8
                            || txtContactNo2.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 2 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo2.getText()
                                    .toString().trim().substring(1
                                    )));
                        } catch (Exception e) {
                            err_msg = "Contact No. 2 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo3.getText().toString() != null
                        && txtContactNo3.getText().toString().trim().length() > 0) {
                    if (txtContactNo3.getText().toString().trim().length() < 8
                            || txtContactNo3.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 3 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo3.getText()
                                    .toString().trim().substring(1
                                    )));
                        } catch (Exception e) {
                            err_msg = "Contact No. 3 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo4.getText().toString() != null
                        && txtContactNo4.getText().toString().trim().length() > 0) {
                    if (txtContactNo4.getText().toString().trim().length() < 8
                            || txtContactNo4.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 4 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo4.getText()
                                    .toString().trim().substring(1
                                    )));

                        } catch (Exception e) {
                            err_msg = "Contact No. 4 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo5.getText().toString() != null
                        && txtContactNo5.getText().toString().trim().length() > 0) {
                    if (txtContactNo5.getText().toString().trim().length() < 8
                            || txtContactNo5.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 5 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo5.getText()
                                    .toString().trim().substring(1
                                    )));

                        } catch (Exception e) {
                            err_msg = "Contact No. 5 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo6.getText().toString() != null
                        && txtContactNo6.getText().toString().trim().length() > 0) {
                    if (txtContactNo6.getText().toString().trim().length() < 8
                            || txtContactNo6.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 6 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo6.getText()
                                    .toString().trim().substring(1
                                    )));

                        } catch (Exception e) {
                            err_msg = "Contact No. 6 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo7.getText().toString() != null
                        && txtContactNo7.getText().toString().trim().length() > 0) {
                    if (txtContactNo7.getText().toString().trim().length() < 8
                            || txtContactNo7.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 7 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo7.getText()
                                    .toString().trim().substring(1
                                    )));

                        } catch (Exception e) {
                            err_msg = "Contact No. 7 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                if (txtContactNo8.getText().toString() != null
                        && txtContactNo8.getText().toString().trim().length() > 0) {
                    if (txtContactNo8.getText().toString().trim().length() < 8
                            || txtContactNo8.getText().toString().trim()
                            .length() > 15) {
                        err_msg = "Contact No. 8 is invalid.\nPlease enter valid Contact number...";
                        return false;
                    } else {
                        try {
                            Long.parseLong((txtContactNo8.getText()
                                    .toString().trim().substring(1
                                    )));
                        } catch (Exception e) {
                            err_msg = "Contact No. 8 is invalid.\nPlease enter valid Contact number...";
                            return false;
                        }
                    }
                }
                return true;
            } else {
                err_msg = "Enter at least one contact number";
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private void displayData() {
        try {
            if (statusCodeContact1 == Constants.OK_STATUS) {
                if (contact1.contains("XXXXXXXXXXXXX")) {
                    contact1 = "";
                    txtContactNo1.setText(contact1);
                } else {

                    String tempString = constantModem
                            .removeDQ(contact1.substring(2
                            ));
                    // String tempString = contact1.split(",")[2].substring(1);

                    txtContactNo1.setText(tempString);
                }
            } else {
                contact1 = "";
                txtContactNo1.setText(contact1);
            }

            if (statusCodeContact2 == Constants.OK_STATUS) {
                if (contact2.contains("XXXXXXXXXXXXX")) {
                    contact2 = "";
                    txtContactNo2.setText(contact2);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact2.substring(2
                            ));

                    txtContactNo2.setText(tempString);
                }
            } else {
                contact2 = "";
                txtContactNo2.setText(contact2);
            }
            if (statusCodeContact3 == Constants.OK_STATUS) {
                if (contact3.contains("XXXXXXXXXXXXX")) {
                    contact3 = "";
                    txtContactNo3.setText(contact3);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact3.substring(2
                            ));

                    txtContactNo3.setText(tempString);
                }
            } else {
                contact3 = "";
                txtContactNo3.setText(contact3);
            }
            if (statusCodeContact4 == Constants.OK_STATUS) {
                if (contact4.contains("XXXXXXXXXXXXX")) {
                    contact4 = "";
                    txtContactNo4.setText(contact4);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact4.substring(2
                            ));

                    txtContactNo4.setText(tempString);
                }
            } else {
                contact4 = "";
                txtContactNo4.setText(contact4);
            }
            if (statusCodeContact5 == Constants.OK_STATUS) {
                if (contact5.contains("XXXXXXXXXXXXX")) {
                    contact5 = "";
                    txtContactNo5.setText(contact5);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact5.substring(2
                            ));

                    txtContactNo5.setText(tempString);
                }
            } else {
                contact5 = "";
                txtContactNo5.setText(contact5);
            }
            if (statusCodeContact6 == Constants.OK_STATUS) {
                if (contact6.contains("XXXXXXXXXXXXX")) {
                    contact6 = "";
                    txtContactNo6.setText(contact6);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact6.substring(2
                            ));
                    txtContactNo6.setText(tempString);
                }
            } else {
                contact6 = "";
                txtContactNo6.setText(contact6);
            }
            if (statusCodeContact7 == Constants.OK_STATUS) {
                if (contact7.contains("XXXXXXXXXXXXX")) {
                    contact7 = "";
                    txtContactNo7.setText(contact7);
                } else {
                    String tempString = constantModem
                            .removeDQ(contact7.substring(2
                            ));

                    txtContactNo7.setText(tempString);
                }
            } else {
                contact7 = "";
                txtContactNo7.setText(contact7);
            }
            if (statusCodeContact8 == Constants.OK_STATUS) {
                if (contact8.contains("XXXXXXXXXXXXX")) {
                    contact8 = "";
                    txtContactNo8.setText(contact8);
                } else {

                    String tempString = constantModem
                            .removeDQ(contact8.substring(2
                            ));

                    txtContactNo8.setText(tempString);
                }
            } else {
                contact8 = "";
                txtContactNo8.setText(contact8);
            }

        } catch (Exception e) {
            exceptionMdmSttg = true;
            err_msg = "Error in reading Contact Numbers...";
            e.printStackTrace();
        }
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(ContactListActivity.this)
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
        new androidx.appcompat.app.AlertDialog.Builder(ContactListActivity.this)
                .setTitle("Connection").setIcon(R.drawable.error)
                .setMessage("Device connection lost !").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        Intent intent = new Intent(ContactListActivity.this,
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
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

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(ContactListActivity.this);
        View promptView = layoutInflater.inflate(R.layout.sms_contact, null);
        AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(ContactListActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = promptView.findViewById(R.id.edittext);
        try {
            if (statusCodeContactAdmin == Constants.OK_STATUS) {
                if (contactAdmin.contains("XXXXXXXXXXXXX")) {
                    contactAdmin = "";
                    editText.setText(contactAdmin);
                } else {

                    String tempString = constantModem
                            .removeDQ(contactAdmin.substring(2
                            ));
                    // String tempString = contact1.split(",")[2].substring(1);

                    editText.setText(tempString);
                }
            } else {
                contactAdmin = "";
                editText.setText(contactAdmin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("Hello ", "" + editText.getText());
                        constantModem.sendCMDgetRLY("ADMINCONT,1"
                                + ",\"" + editText.getText().toString() + "\"");
                        Toast.makeText(ContactListActivity.this, "Contact updated Successfully", Toast.LENGTH_LONG).show();
						/*SharedPreferences pref = getSharedPreferences(Constants.PREF_PHON_NAME, Context.MODE_PRIVATE);
						Editor edit = pref.edit();
						edit.putString(Constants.PREF_ADMIN_NAME, editText.getText().toString());
						edit.commit();*/
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Toast.makeText(ContactListActivity.this, "Contact not updated", Toast.LENGTH_LONG).show();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}
