package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * @author Sandeep
 */
public class AlarmEventActivity extends AppCompatActivity {

    String[] spinner_alarmLevel_value = {"LOW", "HIGH", "NONE"};
    ArrayAdapter<String> arrayAdapter_alarmLevel_1;
    ArrayAdapter<String> arrayAdapter_alarmLevel_2;
    ArrayAdapter<String> arrayAdapter_alarmLevel_3;
    private Button btnUpdateAlarm;
    private Button btnBack;
    private CheckBox chkBoxEventLog1;
    private CheckBox chkBoxEventLog2;
    private CheckBox chkBoxEventLog3;
    private EditText txtAlarmLvl1;
    private EditText txtAlarmLvl2;
    private EditText txtAlarmLvl3;
    private EditText txtHour;
    private EditText txtMinute;
    private EditText txtSecond;
    private Spinner spinner_alarmLevel_1;
    private Spinner spinner_alarmLevel_2;
    private Spinner spinner_alarmLevel_3;
    private String alarm1;
    private String alarm2;
    private String alarm3;
    private String eventLogInterval;
    private Thread thread;
    private ProgressDialog progress_dialog;
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Constants.SHOW_MSG:
                    progress_dialog.dismiss();
                    show_toast(msg.getData().getString(Constants.SHOW_MSG_KEY));
                    break;
                case Constants.SHOW_PARAMETER:
                    progress_dialog.dismiss();
                    displayData();
                    break;
            }

        }
    };
    private Constants consts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_event_logs);

        btnUpdateAlarm = findViewById(R.id.buttonUpdateSmsAlert);
        btnBack = findViewById(R.id.buttonBack);
        txtAlarmLvl1 = findViewById(R.id.editTextAlarmLevel);
        txtAlarmLvl2 = findViewById(R.id.editTextAlarmLevel2);
        txtAlarmLvl3 = findViewById(R.id.editTextAlarmLevel3);
        txtHour = findViewById(R.id.editTextHour);
        txtMinute = findViewById(R.id.editTextMinute);
        txtSecond = findViewById(R.id.editTextSecond);
        spinner_alarmLevel_1 = findViewById(R.id.spinner_alarmLevel_1);
        spinner_alarmLevel_2 = findViewById(R.id.spinner_alarmLevel_2);
        spinner_alarmLevel_3 = findViewById(R.id.spinner_alarmLevel_3);
        chkBoxEventLog1 = findViewById(R.id.checkBoxEventLog1);
        chkBoxEventLog2 = findViewById(R.id.checkBoxEventLog2);
        chkBoxEventLog3 = findViewById(R.id.checkBoxEventLog3);

        arrayAdapter_alarmLevel_1 = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_alarmLevel_value);
        arrayAdapter_alarmLevel_1.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_alarmLevel_1.setAdapter(arrayAdapter_alarmLevel_1);

        arrayAdapter_alarmLevel_2 = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_alarmLevel_value);
        arrayAdapter_alarmLevel_2.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_alarmLevel_2.setAdapter(arrayAdapter_alarmLevel_2);

        arrayAdapter_alarmLevel_3 = new ArrayAdapter<>(this,
                R.layout.spinner_drop_down_selected, spinner_alarmLevel_value);
        arrayAdapter_alarmLevel_3.setDropDownViewResource(R.layout.spinner_drop_down);
        spinner_alarmLevel_3.setAdapter(arrayAdapter_alarmLevel_3);

        consts = new Constants();

        initProgressDialog("Reading alarm & event log information !!");
        progress_dialog.show();
        thread = new Thread() {
            public void run() {
                if (readParameters()) {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_PARAMETER);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SHOW_MSG_KEY, "Unable to get alarm & event log information");
                    msg.setData(bundle);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        thread.start();

        buttonAction();
    }

    private void datalogger_not_connected_dialog() {
        new AlertDialog.Builder(AlarmEventActivity.this)
                .setTitle("Logger Not Connected !!")
                .setIcon(R.drawable.info)
                .setMessage(
                        "Logger is not connected. Please connect the Logger.")
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                            }
                        }).show();
    }

    private void show_toast(String msg) {
        Toast.makeText(AlarmEventActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void buttonAction() {

        btnUpdateAlarm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Tool.controlButtonDebouncing(btnUpdateAlarm);

                if (Variable.isConnected) {
                    if (Variable.scanStatus) {
                        new AlertDialog.Builder(AlarmEventActivity.this)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.scan_stop_msg)
                                .setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (consts.scanStart_Stop("STOP")) {
                                                    show_toast("Scan Stopped");
                                                    alarmUpdate();
                                                } else {
                                                    show_toast("Unable to stop scan !!");
                                                }
                                            }
                                        }).setNegativeButton("No", null).show();
                    } else {
                        alarmUpdate();
                    }
                } else {
                    datalogger_not_connected_dialog();
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void alarmUpdate() {

        if (checkEmptyText() && checkTime()) {

            initProgressDialog("Updating alarm & event log information !!");
            progress_dialog.show();
            thread = new Thread() {
                public void run() {

                    try {

                        String alarmLevel1 = txtAlarmLvl1.getText().toString().trim();
                        String alarmLevel2 = txtAlarmLvl2.getText().toString().trim();
                        String alarmLevel3 = txtAlarmLvl3.getText().toString().trim();
                        String level1 = spinner_alarmLevel_1.getSelectedItem().toString().trim();
                        String level2 = spinner_alarmLevel_2.getSelectedItem().toString().trim();
                        String level3 = spinner_alarmLevel_3.getSelectedItem().toString().trim();

                        int lvl1 = -1;
                        int lvl2 = -1;
                        int lvl3 = -1;
                        try {
                            if (level1.equalsIgnoreCase("HIGH"))
                                lvl1 = 1;
                            else if (level1.equalsIgnoreCase("LOW"))
                                lvl1 = 0;
                            else if (level1.equalsIgnoreCase("NONE"))
                                lvl1 = 2;

                            if (level2.equalsIgnoreCase("HIGH"))
                                lvl2 = 1;
                            else if (level2.equalsIgnoreCase("LOW"))
                                lvl2 = 0;
                            else if (level2.equalsIgnoreCase("NONE"))
                                lvl2 = 2;

                            if (level3.equalsIgnoreCase("HIGH"))
                                lvl3 = 1;
                            else if (level3.equalsIgnoreCase("LOW"))
                                lvl3 = 0;
                            else if (level3.equalsIgnoreCase("NONE"))
                                lvl3 = 2;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String log1, log2, log3;
                        if (chkBoxEventLog1.isChecked()) {
                            log1 = "1";
                        } else {
                            log1 = "0";
                        }
                        if (chkBoxEventLog2.isChecked()) {
                            log2 = "1";
                        } else {
                            log2 = "0";
                        }
                        if (chkBoxEventLog3.isChecked()) {
                            log3 = "1";
                        } else {
                            log3 = "0";
                        }

                        try {
                            if (consts.wakeUpDL()) {
                                consts.sendCMDgetRLY("ALARM,1,"
                                        + Float.valueOf(alarmLevel1) + "," + lvl1
                                        + "," + log1);

                                if (Constants.OK_STATUS == Constants.sc) {
                                    consts.sendCMDgetRLY("ALARM,2,"
                                            + Float.valueOf(alarmLevel2) + ","
                                            + lvl2 + "," + log2);
                                }
                                if (Constants.OK_STATUS == Constants.sc) {
                                    consts.sendCMDgetRLY("ALARM,3,"
                                            + Float.valueOf(alarmLevel3) + ","
                                            + lvl3 + "," + log3);
                                    String hour = Tool.pad(txtHour.getText().toString().trim(), 3);
                                    String min = Tool.pad(Integer.parseInt(txtMinute
                                            .getText().toString().trim()));
                                    String sec = Tool.pad(Integer.parseInt(txtSecond
                                            .getText().toString().trim()));

                                    eventLogInterval = consts
                                            .sendCMDgetRLY("EVLOGINT,\""
                                                    + hour + ":" + min + ":" + sec
                                                    + "\"");

                                }
                                if (Constants.OK_STATUS == Constants.sc) {
                                    Variable.error_msg = "Alarm & Event Log information updated !!";
                                } else {
                                    Variable.error_msg = "Unable to update Alarm & Event Log information !!";
                                }
                            }
                        } catch (Exception e) {
                            Variable.error_msg = "Exception occurred !!";
                        }
                    } catch (Exception e) {
                        Variable.error_msg = "Exception occurred !!";
                    }
                    Message msg = setTextHandler.obtainMessage(Constants.SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SHOW_MSG_KEY, Variable.error_msg);
                    msg.setData(bundle);
                    setTextHandler.sendMessage(msg);

                }
            };
            thread.start();
        }
    }

    private boolean readParameters() {
        try {
            if (consts.wakeUpDL()) {
                alarm1 = consts.sendCMDgetRLY("ALARM,1,\"?\"");
                if (Constants.OK_STATUS == Constants.sc) {
                    alarm2 = consts.sendCMDgetRLY("ALARM,2,\"?\"");
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    alarm3 = consts.sendCMDgetRLY("ALARM,3,\"?\"");
                }
                if (Constants.OK_STATUS == Constants.sc) {
                    eventLogInterval = consts.removeDQ(consts.sendCMDgetRLY("EVLOGINT,\"?\""));
                }
                return Constants.OK_STATUS == Constants.sc;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void displayData() {
        try {
            try {

                String[] alarm = alarm1.split(",");
                alarm[0] = Constants.setDecimalDigits(alarm[0], 3);
                txtAlarmLvl1.setText(alarm[0]);
                if (alarm[1].trim().equalsIgnoreCase("0"))
                    spinner_alarmLevel_1.setSelection(arrayAdapter_alarmLevel_1.getPosition("LOW"));
                else if (alarm[1].trim().equalsIgnoreCase("1"))
                    spinner_alarmLevel_1.setSelection(arrayAdapter_alarmLevel_1.getPosition("HIGH"));
                else if (alarm[1].trim().equalsIgnoreCase("2"))
                    spinner_alarmLevel_1.setSelection(arrayAdapter_alarmLevel_1.getPosition("NONE"));
                chkBoxEventLog1.setChecked(alarm[2].trim().equalsIgnoreCase("1"));

            } catch (Exception ignored) {
            }
            try {

                String[] alarm = alarm2.split(",");
                alarm[0] = Constants.setDecimalDigits(alarm[0], 3);
                txtAlarmLvl2.setText(alarm[0]);
                if (alarm[1].trim().equalsIgnoreCase("0"))
                    spinner_alarmLevel_2.setSelection(arrayAdapter_alarmLevel_2.getPosition("LOW"));
                else if (alarm[1].trim().equalsIgnoreCase("1"))
                    spinner_alarmLevel_2.setSelection(arrayAdapter_alarmLevel_2.getPosition("HIGH"));
                else if (alarm[1].trim().equalsIgnoreCase("2"))
                    spinner_alarmLevel_2.setSelection(arrayAdapter_alarmLevel_2.getPosition("NONE"));

                chkBoxEventLog2.setChecked(alarm[2].trim().equalsIgnoreCase("1"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

                String[] alarm = alarm3.split(",");
                alarm[0] = Constants.setDecimalDigits(alarm[0], 3);
                txtAlarmLvl3.setText(alarm[0]);
                if (alarm[1].trim().equalsIgnoreCase("0"))
                    spinner_alarmLevel_3.setSelection(arrayAdapter_alarmLevel_3.getPosition("LOW"));
                else if (alarm[1].trim().equalsIgnoreCase("1"))
                    spinner_alarmLevel_3.setSelection(arrayAdapter_alarmLevel_3.getPosition("HIGH"));
                else if (alarm[1].trim().equalsIgnoreCase("2"))
                    spinner_alarmLevel_3.setSelection(arrayAdapter_alarmLevel_3.getPosition("NONE"));

                chkBoxEventLog3.setChecked(alarm[2].trim().equalsIgnoreCase("1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

                String time = eventLogInterval.substring(0,
                        eventLogInterval.indexOf(":"));
                txtHour.setText(Tool.pad(time, 3));
                time = eventLogInterval.substring(
                        eventLogInterval.indexOf(":") + 1,
                        eventLogInterval.lastIndexOf(":"));
                txtMinute.setText(Tool.pad(Integer.parseInt(time)));
                time = eventLogInterval.substring(eventLogInterval
                        .lastIndexOf(":") + 1);
                txtSecond.setText(Tool.pad(Integer.parseInt(time)));

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkEmptyText() {
        try {
            if (txtAlarmLvl1.getText().toString().isEmpty()) {
                show_toast("Please provide input to Alarm 1...");
                return false;
            } else if (txtAlarmLvl2.getText().toString().isEmpty()) {
                show_toast("Please provide input to Alarm 3...");
                return false;
            } else if (txtAlarmLvl3.getText().toString().isEmpty()) {
                show_toast("Please provide input to Alarm 3...");
                return false;
            } else if (txtHour.getText().toString().isEmpty()) {
                show_toast("Please provide input to Hour...");
                return false;
            } else if (txtMinute.getText().toString().isEmpty()) {
                show_toast("Please provide input to Minute...");
                return false;
            } else if (txtSecond.getText().toString().isEmpty()) {
                show_toast("Please provide input to Second...");
                return false;
            }

        } catch (Exception e) {
            show_toast("Exception occurred !!");
            return false;
        }
        return true;
    }

    private boolean checkTime() {
        try {
            int hour = Integer.parseInt(txtHour.getText().toString().trim());
            int minute = Integer
                    .parseInt(txtMinute.getText().toString().trim());
            int second = Integer
                    .parseInt(txtSecond.getText().toString().trim());
            if (hour <= 168 && hour >= 0) {
                txtHour.setText(Tool.pad("" + hour, 3));
                txtMinute.setText(Tool.pad(minute));
                if (hour == 168) // for 7 days
                {
                    if (minute > 0) {
                        show_toast("Event Log interval can not be\ngreater than 168 hours...");
                        return false;
                    } else if (second > 0) {
                        show_toast("Event Log interval can not be\ngreater than 168 hours...");
                        return false;
                    }
                } else if (minute > 59 || minute < 0) {
                    show_toast("minutes must be between 0 - 59");
                    return false;
                } else if (second > 59) {
                    show_toast("seconds can not be\ngreater than 59");
                    return false;
                } else if (hour == 0 && minute == 0 && second < 5) {
                    show_toast("seconds can not be\nless than 5");
                    return false;

                }
            } else {
                show_toast("Event Log interval must be \nbetween 0 to 168 hours...");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            show_toast("Exception occurred !!");
            return false;
        }

        try {
            Float.valueOf(txtAlarmLvl1.getText().toString().trim());
        } catch (Exception e) {
            show_toast("Alarm level-1 must be numeric...");
            return false;
        }

        try {
            Float.parseFloat(txtAlarmLvl2.getText().toString().trim());
        } catch (Exception e) {
            show_toast("Alarm level-2 must be numeric...");
            return false;
        }

        try {
            Float.parseFloat(txtAlarmLvl3.getText().toString().trim());
        } catch (Exception e) {
            show_toast("Alarm level-3 must be numeric...");
            return false;
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initProgressDialog(String msg) {
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setTitle("Please wait !!");
        progress_dialog.setCancelable(false);
        progress_dialog.setMessage(msg);
    }

}