package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * @author Sandeep
 */

public class LoggerDateTimeSettingActivity extends AppCompatActivity {
    private static String timeOffset;
    private final int MESSAGE_SETUP_UPDATING_DATE_TIME_SCREEN = 10;
    private final int MESSAGE_SETUP_READING_SCREEN = 20;
    private final int MESSAGE_SETUP_EXCEPTION_SCREEN = 30;
    private final int MESSAGE_SETUP_SYNC_WITH_PHONE = 40;
    private final int SCAN_EXCEPTION = 50;
    private final Handler repeatUpdateHandler = new Handler();
    String[] utc_time_values = {"00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
            "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00",
            "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30",
            "10:00", "10:30", "11:00", "11:30", "12:00"};
    String[] utc_time_sign = {"+", "-"};
    ImageView minplus, secplus, hourplus, hourminus, minminus, secminus;
    Context context = LoggerDateTimeSettingActivity.this;
    ArrayAdapter<String> arrayAdapter_UTC_values;
    ArrayAdapter<String> arrayAdapter_UTC_sign;
    String err_msg = "";
    String wrn_msg = "";
    Calendar myCalendar;
    int hour, minute, second;
    TextView actualtime;
    TextView hourvalue;
    TextView minvalue;
    TextView secvalue;
    String data_setup;
    private TextView txt_logger_date;
    private ImageView img_v_logger_date;
    private TextView txt_logger_time;
    private ImageView img_v_logger_time;
    private Spinner spinner_UTC_offset_sign;
    private Spinner spinner_UTC_offset_value;
    private Button btn_update_DT;
    private Button buttonSyncWithMobile;
    private Button buttonBackSetup1;
    private String dateStr;
    private String timeStr;
    private String timeOffsetStr;
    private boolean exceptionSetup;
    private Thread setupThread;
    private ProgressDialog dialog;
    private PopupWindow SelectPopupWindow;
    private Constants constantSetup;
    private short statusCodeDate;
    private short statusCodeTime;
    private short statusCodeTimeOffset;
    /**
     * The set text handler.
     */
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (setupThread != null) {
                setupThread.interrupt();
                setupThread = null;
            }
            switch (msg.what) {
                case MESSAGE_SETUP_UPDATING_DATE_TIME_SCREEN: {
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (statusCodeDate != Constants.OK_STATUS
                                || statusCodeTime != Constants.OK_STATUS
                                || statusCodeTimeOffset != Constants.OK_STATUS) {
                            err_msg = "Error in uploading time...";
                            Toast.makeText(LoggerDateTimeSettingActivity.this,
                                    Constants.sc + ": " + err_msg,
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(LoggerDateTimeSettingActivity.this,
                                    "Successfully updated...", Toast.LENGTH_LONG)
                                    .show();
                    } else
                        showDialog();
                    break;
                }
                case MESSAGE_SETUP_READING_SCREEN: {
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.toastFromThread)
                        showDialog();
                    else
                        displayData();
                    break;
                }
                case MESSAGE_SETUP_SYNC_WITH_PHONE: {
                    txt_logger_date.setText(dateStr);
                    data_setup = dateStr;
                    txt_logger_time.setText(timeStr);
                    break;
                }
                case MESSAGE_SETUP_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (exceptionSetup == true) {
                        exceptionSetup = false;
                        Toast.makeText(LoggerDateTimeSettingActivity.this, err_msg,
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
                                LoggerDateTimeSettingActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();

                    break;

                default:

            }
        }
    };
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logger_date_time_setting);

        txt_logger_date = findViewById(R.id.txt_logger_date);
        img_v_logger_date = findViewById(R.id.img_v_logger_date);
        txt_logger_time = findViewById(R.id.txt_logger_time);
        img_v_logger_time = findViewById(R.id.img_v_logger_time);
        spinner_UTC_offset_sign = findViewById(R.id.spinner_UTC_offset_sign);
        spinner_UTC_offset_value = findViewById(R.id.spinner_UTC_offset_value);
        btn_update_DT = findViewById(R.id.btn_update_DT);
        buttonSyncWithMobile = findViewById(R.id.buttonSyncWithMobile);
        buttonBackSetup1 = findViewById(R.id.buttonBackSetup1);


        spinner_UTC_offset_value.setPrompt("Choose");
        spinner_UTC_offset_sign.setPrompt("Choose");

        arrayAdapter_UTC_values = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, utc_time_values);

        arrayAdapter_UTC_sign = new ArrayAdapter<String>(this,
                R.layout.spinner_drop_down_selected, utc_time_sign);

        arrayAdapter_UTC_values.setDropDownViewResource(R.layout.spinner_drop_down);
        arrayAdapter_UTC_sign.setDropDownViewResource(R.layout.spinner_drop_down);

        spinner_UTC_offset_value.setAdapter(arrayAdapter_UTC_values);
        spinner_UTC_offset_sign.setAdapter(arrayAdapter_UTC_sign);


        constantSetup = new Constants();

        myCalendar = Calendar.getInstance();
        buttonAction();

        initProgressDialog("Loading Setup Information !!!");
        dialog.show();
        setupThread = new Thread() {
            public void run() {
                readSetupParameter();
                if (exceptionSetup) {
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_SETUP_EXCEPTION_SCREEN);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_SETUP_READING_SCREEN);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        setupThread.start();

    }

    private void buttonAction() {


        img_v_logger_date.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();

                DatePickerDialog datePickerDialog = new DatePickerDialog(LoggerDateTimeSettingActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                txt_logger_date.setText("" + year + "/" + Tool.pad(monthOfYear + 1) + "/" + Tool.pad(dayOfMonth));

                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        img_v_logger_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                showTimePickerDialog();
            }
        });
        buttonBackSetup1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (setupThread != null) {
                    setupThread.interrupt();
                    setupThread = null;
                }
                finish();
            }
        });
        buttonSyncWithMobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                scanCheck();
                if (constantSetup.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {

                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";

                        new AlertDialog.Builder(context)
                                .setIcon(R.drawable.warning_icon)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)

                                .setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {

                                                constantSetup
                                                        .wakeUpDL();
                                                constantSetup
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            LoggerDateTimeSettingActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                    updateTime();
                                                }
                                            }
                                        }).setNegativeButton("No", null).show();

                    }
                } else {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else
                        updateTime();
                }

            }

        });

        btn_update_DT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_update_DT.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btn_update_DT.setClickable(true);
                    }
                }, 2000);
                if (constantSetup.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        String wrn_msg = "Please stop scanning before updation."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(
                                LoggerDateTimeSettingActivity.this)
                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setIcon(R.drawable.warning_icon)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                constantSetup
                                                        .wakeUpDL();
                                                constantSetup
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            LoggerDateTimeSettingActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();

                                                    if (!check_validity()) {

                                                        initProgressDialog("Updating Setup Date Time Information !!!");
                                                        ((Dialog) dialog)
                                                                .show();
                                                        callForUpdate();
                                                    } else {
                                                        if (exceptionSetup == true) {
                                                            exceptionSetup = false;
                                                            Toast.makeText(
                                                                    LoggerDateTimeSettingActivity.this,
                                                                    err_msg,
                                                                    Toast.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                    }

                                                }
                                            }
                                        }).setNegativeButton("NO", null).show();
                    }
                } else {

                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        if (!check_validity()) {

                            initProgressDialog("Updating Setup Date Time Information !!!");
                            dialog.show();
                            callForUpdate();
                        } else {
                            if (exceptionSetup == true) {
                                exceptionSetup = false;
                                Toast.makeText(
                                        LoggerDateTimeSettingActivity.this,
                                        err_msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

            }
        });
    }

    private void updateTime() {

//        setupThread = new Thread() {
//            public void run() {
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String yr = pad(year);
        String mnth = pad(month + 1);
        String dy = pad(day);
        String hr = pad(hour);
        String mnt = pad(minute);
        String sc = pad(second);
        dateStr = yr + "/" + mnth + "/" + dy;
        timeStr = hr + ":" + mnt + ":" + sc;


        txt_logger_date.setText(dateStr);
        data_setup = dateStr;
        txt_logger_time.setText(timeStr);

        timeOffsetStr = Tool.getUTC_Offset();
        timeOffset = timeOffsetStr.substring(1).trim();

        spinner_UTC_offset_sign.setSelection(arrayAdapter_UTC_sign.getPosition(timeOffsetStr.substring(0, 1)));

        spinner_UTC_offset_value.setSelection(arrayAdapter_UTC_values.getPosition(timeOffset));


//                Message msg = setTextHandler
//                        .obtainMessage(MESSAGE_SETUP_SYNC_WITH_PHONE);
//                setTextHandler.sendMessage(msg);
//            }
//        };
//        setupThread.start();
        callForUpdate();

    }

    private boolean check_validity() {


        int data_check = Integer.parseInt(data_setup.substring(0, 4));

        try {
            if (data_check < 2000 || data_check > 2099) {
                exceptionSetup = true;
                err_msg = "Choose Valid Year between 2000 to 2099";
                return exceptionSetup;
            }
        } catch (Exception e) {
            exceptionSetup = true;
            err_msg = "Choose Valid Year between 2000 to 2099...";
            return exceptionSetup;
        }

        return false;
    }

    public void scanCheck() {

        constantSetup.wakeUpDL();
        String reply = constantSetup.removeDQ(constantSetup
                .sendCMDgetRLY("SCAN,\"?\""));
        short statsCode = (short) Constants.sc;
        if (statsCode == Constants.OK_STATUS) {
            Variable.scanStatus = reply.trim().equalsIgnoreCase("START");
        } else
            Variable.scanStatus = false;
    }

    private void callForUpdate() {

        setupThread = new Thread() {
            public void run() {
                try {
                    String date = txt_logger_date.getText().toString().trim();
                    String time = txt_logger_time.getText().toString().trim();
                    timeOffset = spinner_UTC_offset_value.getSelectedItem().toString().trim();
                    String sign = spinner_UTC_offset_sign.getSelectedItem().toString().trim();
                    constantSetup.wakeUpDL();
                    date = date.replace("-", "/");
                    constantSetup.sendCMDgetRLY("DATE,\"" + date
                            + "\"");
                    statusCodeDate = (short) Constants.sc;
                    if (Constants.sc == Constants.OK_STATUS) {
                        timeStr = constantSetup
                                .sendCMDgetRLY("TIME,\"" + time + "\"");
                        statusCodeTime = (short) Constants.sc;
                    }
                    if (Constants.sc == Constants.OK_STATUS) {
                        timeOffsetStr = constantSetup
                                .sendCMDgetRLY("UTCOFF,\"" + sign
                                        + timeOffset + "\"");
                        statusCodeTimeOffset = (short) Constants.sc;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionSetup = true;
                    err_msg = "Error in uploading time...";
                }
                if (exceptionSetup) {
                    // sending msg
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_SETUP_EXCEPTION_SCREEN);
                    setTextHandler.sendMessage(msg);
                } else {
                    // sending msg
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_SETUP_UPDATING_DATE_TIME_SCREEN);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        setupThread.start();

    }

    private void readSetupParameter() {
        try {
            if (Variable.isConnected) {
                constantSetup.wakeUpDL();
                dateStr = constantSetup.removeDQ(constantSetup
                        .sendCMDgetRLY("DATE,\"?\""));
                data_setup = dateStr;
                statusCodeDate = (short) Constants.sc;
                if (Constants.sc == Constants.OK_STATUS) {
                    timeStr = constantSetup.removeDQ(constantSetup
                            .sendCMDgetRLY("TIME,\"?\""));
                    statusCodeTime = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    timeOffsetStr = constantSetup
                            .removeDQ(constantSetup
                                    .sendCMDgetRLY("UTCOFF,\"?\""));
                    statusCodeTimeOffset = (short) Constants.sc;
                }
            }
        } catch (Exception e) {
            exceptionSetup = true;
            err_msg = "Error in reading Setup Values...";
            e.printStackTrace();
        }
    }

    private void displayData() {
        try {
            if (statusCodeDate == Constants.OK_STATUS) {

                txt_logger_date.setText(dateStr);
            } else {
                dateStr = "";

                txt_logger_date.setText(dateStr);
            }
            if (statusCodeTime == Constants.OK_STATUS)
                txt_logger_time.setText(timeStr);
            else {
                timeStr = "00:00:00";
                txt_logger_time.setText(timeStr);
            }
            if (statusCodeTimeOffset == Constants.OK_STATUS) {


                spinner_UTC_offset_sign.setSelection(arrayAdapter_UTC_sign.getPosition(timeOffsetStr.substring(0, 1)));
                timeOffset = timeOffsetStr.substring(1).trim();
                spinner_UTC_offset_value.setSelection(arrayAdapter_UTC_values.getPosition(timeOffset));

            } else {
                timeOffsetStr = "+00:00";
                spinner_UTC_offset_sign.setSelection(arrayAdapter_UTC_sign.getPosition(timeOffsetStr.substring(0, 1)));
                spinner_UTC_offset_value.setSelection(arrayAdapter_UTC_values.getPosition(timeOffset));
            }
        } catch (Exception e) {
            exceptionSetup = true;
            err_msg = "Error in reading Setup Values...";
            e.printStackTrace();
        }
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(LoggerDateTimeSettingActivity.this)
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
        new AlertDialog.Builder(LoggerDateTimeSettingActivity.this)
                .setTitle("Connection").setMessage("Device connection lost !")
                .setCancelable(false).setIcon(R.drawable.error)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
    }


    private String parseTime(String time) {
        String[] timeArr = time.split(" ");
        return timeArr[3].trim();
    }

    private String parseDate(String date) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] dateArr = date.split(" ");
        int mnth = -1;
        String month = "";
        String day = "";
        String year = "";
        for (int i = 0; i < 12; i++) {
            if (months[i].equalsIgnoreCase(dateArr[1])) {
                mnth = i + 1;
                break;
            }
        }
        month = pad(mnth).trim();
        day = dateArr[2].trim();
        year = dateArr[5].trim();

        return year + "-" + month + "-" + day;

    }

    private String parseTimeOffset(String time) {
        String[] timeArr = time.split(" ");
        return timeArr[3].substring(0, 5).trim();
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

    /**
     * Exit from activity on back key
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (setupThread != null) {
                setupThread.interrupt();
                setupThread = null;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Pad.
     *
     * @param decimalDigit the decimal digit
     * @return the string
     */
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

    public void showTimePickerDialog() {
        final Dialog dialog = new Dialog(LoggerDateTimeSettingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_time_picker);
        actualtime = (TextView) dialog.findViewById(R.id.actualtime);
        hourplus = dialog.findViewById(R.id.plus1);
        minplus = dialog.findViewById(R.id.plus2);
        secplus = dialog.findViewById(R.id.plus3);
        hourminus = dialog.findViewById(R.id.minus1);
        minminus = dialog.findViewById(R.id.minus2);
        secminus = dialog.findViewById(R.id.minus3);
        hourvalue = dialog.findViewById(R.id.hours);
        minvalue = dialog.findViewById(R.id.minutes);
        secvalue = dialog.findViewById(R.id.seconds);
        Button set = dialog.findViewById(R.id.set);
        Button cancel = dialog.findViewById(R.id.cancel);
        dialog.show();
        Calendar calendar = new GregorianCalendar();
        // String am_pm;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        String curhour = String.valueOf(hour);
        String curmin = String.valueOf(minute);
        String cursec = String.valueOf(seconds);
        if (curhour.length() == 1) {
            curhour = "0" + curhour;
        }
        if (curmin.length() == 1) {
            curmin = "0" + curmin;
        }
        if (cursec.length() == 1) {
            cursec = "0" + cursec;
        }

        actualtime.setText(curhour + ":" + curmin + ":" + cursec);
        hourvalue.setText(curhour);
        minvalue.setText(curmin);
        secvalue.setText(cursec);

        cancel.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        set.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String tmphour = hourvalue.getText().toString();
                String tmpmin = minvalue.getText().toString();
                String tmpsec = secvalue.getText().toString();

                actualtime.setText(tmphour + ":" + tmpmin + ":" + tmpsec);

                dialog.dismiss();
                txt_logger_time.setText(tmphour + ":" + tmpmin + ":" + tmpsec);
            }
        });
        hourplus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                hourvalue.requestFocus();
                increment_hr();
            }
        });
        minplus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                minvalue.requestFocus();
                increment_min();
            }
        });
        secplus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                secvalue.requestFocus();
                increment_sec();
            }
        });
        hourminus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                hourvalue.requestFocus();
                decrement_hr();
            }
        });
        minminus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                minvalue.requestFocus();
                decrement_min();
            }
        });
        secminus.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                secvalue.requestFocus();
                decrement_sec();
            }
        });
        hourplus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                hourvalue.requestFocus();
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        hourplus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                hourvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });

        hourminus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                hourvalue.requestFocus();
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        hourminus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                hourvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement) {
                    mAutoDecrement = false;
                }

                return false;
            }
        });
        minplus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                minvalue.requestFocus();
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        minplus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                minvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });
        minminus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                minvalue.requestFocus();
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        minminus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                minvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement) {
                    mAutoDecrement = false;
                }

                return false;
            }
        });
        secplus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                secvalue.requestFocus();
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        secplus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                secvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });
        secminus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                secvalue.requestFocus();
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return false;
            }
        });

        secminus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                secvalue.requestFocus();
                if ((event.getAction() == MotionEvent.ACTION_UP || event
                        .getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement) {
                    mAutoDecrement = false;
                }

                return false;
            }
        });

    }

    public void decrement_hr() {
        String tmphour = hourvalue.getText().toString();
        int curdisplayhr = Integer.parseInt(tmphour);
        int finaldisplayhr = 0;

        if (curdisplayhr == 0) {
            finaldisplayhr = 23;
        } else if (curdisplayhr > 1 || curdisplayhr < 24) {
            finaldisplayhr = curdisplayhr - 1;
        }
        String finalhr = String.valueOf(finaldisplayhr);
        if (finalhr.length() == 1) {
            finalhr = "0" + finalhr;
        }


        hourvalue.setText(finalhr);

        String tmpmin = minvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(finalhr + ":" + tmpmin + ":" + tmpsec);

    }

    public void increment_hr() {
        String tmphour = hourvalue.getText().toString();
        int curdisplayhr = Integer.parseInt(tmphour);
        int finaldisplayhr = 0;
        if (curdisplayhr < 23) {
            finaldisplayhr = curdisplayhr + 1;
        } else if (curdisplayhr == 24) {
            finaldisplayhr = 00;
        }
        String finalhr = String.valueOf(finaldisplayhr);
        if (finalhr.length() == 1) {
            finalhr = "0" + finalhr;
        }
        // finaldisplayhr++;
        hourvalue.setText(finalhr);

        String tmpmin = minvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(finalhr + ":" + tmpmin + ":" + tmpsec);

    }

    public void increment_min() {
        String tmpmin = minvalue.getText().toString();
        int curdisplaymin = Integer.parseInt(tmpmin);
        int finaldisplaymin = 0;
        if (curdisplaymin < 59) {
            finaldisplaymin = curdisplaymin + 1;
        } else if (curdisplaymin == 59) {
            finaldisplaymin = 0;
        }
        String finalmin = String.valueOf(finaldisplaymin);
        if (finalmin.length() == 1) {
            finalmin = "0" + finalmin;
        }
        minvalue.setText(finalmin);
        String tmphour = hourvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(tmphour + ":" + finalmin + ":" + tmpsec);

    }

    public void decrement_min() {
        String tmpmin = minvalue.getText().toString();
        int curdisplaymin = Integer.parseInt(tmpmin);
        int finaldisplaymin = 0;
        if (curdisplaymin == 0) {
            finaldisplaymin = 59;
        } else {
            finaldisplaymin = curdisplaymin - 1;
        }

        String finalmin = String.valueOf(finaldisplaymin);
        if (finalmin.length() == 1) {
            finalmin = "0" + finalmin;
        }
        minvalue.setText(finalmin);
        String tmphour = hourvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(tmphour + ":" + finalmin + ":" + tmpsec);

    }

    public void increment_sec() {
        String tmpsec = secvalue.getText().toString();
        int curdisplaysec = Integer.parseInt(tmpsec);
        int finaldisplaysec = 0;
        if (curdisplaysec < 59) {
            finaldisplaysec = curdisplaysec + 1;
        } else if (curdisplaysec == 59) {
            finaldisplaysec = 0;
        }
        String finalsec = String.valueOf(finaldisplaysec);
        if (finalsec.length() == 1) {
            finalsec = "0" + finalsec;
        }
        secvalue.setText(finalsec);
        String tmphour = hourvalue.getText().toString();
        String tmpmin = minvalue.getText().toString();

        actualtime.setText(tmphour + ":" + tmpmin + ":" + finalsec);

    }

    public void decrement_sec() {

        String tmpsec = secvalue.getText().toString();
        int curdisplaysec = Integer.parseInt(tmpsec);
        int finaldisplaysec;
        if (curdisplaysec == 0) {
            finaldisplaysec = 59;
        } else {
            finaldisplaysec = curdisplaysec - 1;
        }
        String finalsec = String.valueOf(finaldisplaysec);
        if (finalsec.length() == 1) {
            finalsec = "0" + finalsec;
        }
        secvalue.setText(finalsec);
        String tmphour = hourvalue.getText().toString();
        String tmpmin = minvalue.getText().toString();

        actualtime.setText(tmphour + ":" + tmpmin + ":" + finalsec);

    }

    class RptUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                if (minplus.isPressed()) {
                    increment_min();
                } else if (secplus.isPressed()) {
                    increment_sec();
                } else if (hourplus.isPressed()) {
                    increment_hr();
                }
                repeatUpdateHandler.postDelayed(new RptUpdater(), 100);
            } else if (mAutoDecrement) {
                if (hourminus.isPressed()) {
                    decrement_hr();
                } else if (minminus.isPressed()) {
                    decrement_min();
                } else if (secminus.isPressed()) {
                    decrement_sec();
                }
                repeatUpdateHandler.postDelayed(new RptUpdater(), 100);
            }
        }
    }
}
