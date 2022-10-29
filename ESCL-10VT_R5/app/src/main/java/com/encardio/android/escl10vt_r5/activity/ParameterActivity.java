package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * @author Sandeep
 */
public class ParameterActivity extends AppCompatActivity {

    protected final int MESSAGE_WAITING_SCREEN = 1;
    protected final int MESSAGE_SET_PARAMETER = 2;
    protected final int MESSAGE_SET_MODEM = 3;

    protected ProgressDialog dialog;
    String reply = "";
    String signal;
    String bitError;

    private TextView tv_Single_RSSI;
    private TextView tv_Single_dBm;
    private TextView tv_Single_PW;
    private TextView tv_barometer;

    private TextView lbl_ModemIMEI;
    private TextView txt_ModemIMEI;
    private ImageView img_ModemType;
    private ImageView iv_upload_status;


    private TextView txt_date;
    private TextView txt_time;
    private TextView textViewFrequency;
    private TextView textParaValue_para;
    private TextView textViewTemp_para;
    private TextView textViewAdcCounts;
    private TextView textViewMdmBitErr;
    private TextView textViewBaroLvl;
    private TextView textViewBaroCnts;
    private ImageView imgModemSignal;
    private ImageView imageBitErr;
    private Constants constant1;
    private boolean monitorCommand = true;
    private boolean isBackPressed = false;
    private Thread monitorThread;
    private short statusCodeParameter;

    private TextView txt_Uncompensated;
    String barometer_unit = "m";

    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @SuppressLint({"HandlerLeak", "UseCompatLoadingForDrawables", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            if (monitorThread != null) {
                monitorThread.interrupt();
                monitorThread = null;
            }
            switch (msg.what) {
                case MESSAGE_WAITING_SCREEN:
                    dialog.dismiss();
                    break;

                case MESSAGE_SET_MODEM:


                    int code = msg.getData().getInt("code");

                    if (Constants.OK_STATUS == code) {
                        String modemType = msg.getData().getString("data");
                        String imei = msg.getData().getString("imei");
                        if (modemType.equals("EHS6")) {
                            img_ModemType.setImageDrawable(getResources().getDrawable(R.drawable.g3));
                        } else {
                            img_ModemType.setImageDrawable(getResources().getDrawable(R.drawable.g4));
                        }

                        lbl_ModemIMEI.setText("Modem IMEI");
                        txt_ModemIMEI.setText(imei);

                        try {
                            tv_barometer.setText("Barometer Level (m)");
                            if (Variable.fw_ver >= 1.20f) {
                                tv_barometer.setText("Barometer Level (" + barometer_unit + ")");
                            }
                        } catch (Exception ignored) {

                        }

                    } else {
                        lbl_ModemIMEI.setText("");
                        txt_ModemIMEI.setText("");
                        img_ModemType.setImageDrawable(getResources().getDrawable(R.drawable.g0));
                    }


                    break;
                case MESSAGE_SET_PARAMETER:

                    dialog.dismiss();

                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.toastFromThread) {
                        showDialog();
                        monitorCommand = false;
                    } else {
                        String reply = msg.getData().getString("data");

                        try {


                            String[] replyArr = reply.split(",");
                            String date = replyArr[0].substring(1, 11);
                            String time = replyArr[1].substring(1, 9);
                            String cycles = replyArr[2];
                            String frequency = replyArr[3];
                            String parameter = replyArr[4];
                            String adcCount = replyArr[5];
                            String tmp = replyArr[6];
                            String baroCnt = replyArr[7];
                            String baroLvl = replyArr[8];
                            String unocompensated = replyArr[9];
                            signal = replyArr[10];
                            bitError = replyArr[11];


                            if (statusCodeParameter == Constants.OK_STATUS) {
                                txt_date.setText(date);
                                txt_time.setText(time);
                                parameter = Constants.setDecimalDigits(parameter);


                                textParaValue_para.setText(parameter);
                                textViewFrequency.setText(frequency);
                                textViewTemp_para.setText(tmp.trim());// + "C"
                                textViewAdcCounts.setText(adcCount);
                                textViewBaroLvl.setText(baroLvl); // + " hPa"
                                textViewBaroCnts.setText(baroCnt);

                                txt_Uncompensated.setText(Constants.setDecimalDigits(unocompensated));

                                setTextHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        int signalstrength = Integer
                                                .parseInt(signal);
                                        if (signalstrength == 99)
                                            signalstrength = 0;
                                        if (signalstrength >= 21) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_5));
                                        } else if (signalstrength >= 18) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_4));
                                        } else if (signalstrength >= 15) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_3));
                                        } else if (signalstrength >= 12) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_2));
                                        } else if (signalstrength >= 8) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_1));
                                        } else if (signalstrength < 8) {
                                            imgModemSignal
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.signal_0));
                                        }
                                        imgModemSignal.refreshDrawableState();

                                        if (signalstrength == 0) {
                                            tv_Single_RSSI.setText("-----");
                                            tv_Single_dBm.setText("-----");
                                            tv_Single_PW.setText("-----");
                                        } else {
                                            tv_Single_RSSI.setText("" + signalstrength + " (RSSI)");
                                            tv_Single_dBm.setText("" + ((signalstrength * 2) - 113) + " dBm");
                                            tv_Single_PW.setText("" + calculateSignalPower(((signalstrength * 2) - 113)));
                                        }


                                        int birErrorRate = Integer
                                                .parseInt(bitError);
                                        if ((birErrorRate == 0)
                                                || (birErrorRate == 99)) {
                                            textViewMdmBitErr
                                                    .setText("Bit Error Rate : (0)");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_0));
                                        } else if (birErrorRate == 1) {
                                            textViewMdmBitErr
                                                    .setText("Bit Error Rate : (1)");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_1));
                                        } else if (birErrorRate == 2) {
                                            textViewMdmBitErr
                                                    .setText("Bit Error Rate : (2)");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_2));
                                        } else if (birErrorRate == 3) {
                                            textViewMdmBitErr
                                                    .setText("Bit Error Rate : (3)");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_3));
                                        } else if ((birErrorRate == 4)
                                                || (birErrorRate == 5)) {
                                            textViewMdmBitErr.setText("Bit Error Rate : ("
                                                    + birErrorRate
                                                    + ")");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_4));
                                        } else if (birErrorRate >= 6) {
                                            textViewMdmBitErr.setText("Bit Error Rate : ("
                                                    + birErrorRate
                                                    + ")");
                                            imageBitErr
                                                    .setImageDrawable(getResources()
                                                            .getDrawable(
                                                                    R.drawable.error_5));
                                        }
                                        imageBitErr.refreshDrawableState();

                                    }
                                });
                            } else {
                                txt_date.setText("");
                                txt_time.setText("");
                                textParaValue_para.setText("");
                                textViewFrequency.setText("");
                                textViewTemp_para.setText("");
                                textViewAdcCounts.setText("");
                                textViewBaroLvl.setText("");
                                textViewBaroCnts.setText("");
                                tv_Single_RSSI.setText("-----");
                                tv_Single_dBm.setText("-----");
                                tv_Single_PW.setText("-----");
                                textViewMdmBitErr.setText("Bit Error Rate :");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

            }
        }
    };

    @SuppressLint("SetTextI18n")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_paramtr);

        txt_date = findViewById(R.id.txt_date);
        txt_time = findViewById(R.id.txt_time);
        textViewFrequency = findViewById(R.id.textViewFrequency);
        textParaValue_para = findViewById(R.id.textParameter_para_value);
        textViewTemp_para = findViewById(R.id.textViewTemp_para);
        textViewAdcCounts = findViewById(R.id.textViewADCCountValue);

        iv_upload_status = findViewById(R.id.iv_upload_status);
        tv_barometer = findViewById(R.id.tv_barometer);

        textViewMdmBitErr = findViewById(R.id.textViewMdmBitErr);
        TextView textViewModelStatus = findViewById(R.id.textModemValue);
        textViewBaroLvl = findViewById(R.id.textViewBaroLvl);
        textViewBaroCnts = findViewById(R.id.textViewBaroCnts);
        imgModemSignal = findViewById(R.id.imageViewMdmSgnl);
        imageBitErr = findViewById(R.id.imageViewSgnlErr);

        lbl_ModemIMEI = findViewById(R.id.lbl_ModemIMEI);
        txt_ModemIMEI = findViewById(R.id.txt_ModemIMEI);
        img_ModemType = findViewById(R.id.img_ModemType);
        txt_Uncompensated = findViewById(R.id.txt_Uncompensated);

        tv_Single_RSSI = findViewById(R.id.tv_Single_RSSI);
        tv_Single_dBm = findViewById(R.id.tv_Single_dBm);
        tv_Single_PW = findViewById(R.id.tv_Single_PW);

        txt_date.setText("----/--/--");
        txt_time.setText("--:--:--");
        textParaValue_para.setText("0.00000");
        textViewFrequency.setText("0000.00");
        textViewTemp_para.setText("00.0");
        constant1 = new Constants();

        if (Constants.mdmstatus) {
            if (!Constants.mdmPwr) {
                textViewModelStatus.setText("MODEM STATUS (OFF)");
                iv_upload_status.setImageResource(R.drawable.circle_red);
            } else {
                textViewModelStatus.setText("MODEM STATUS (ON)");
                iv_upload_status.setImageResource(R.drawable.circle_green);
            }
        } else {
            textViewModelStatus.setText("MODEM STATUS");
            iv_upload_status.setImageResource(R.drawable.circle_red);
        }


        displayParameter();
    }

    private void displayParameter() {

        initProgressDialog();
        dialog.show();
        monitorThread = new Thread() {
            public void run() {
                try {
                    constant1.wakeUpDL();

                    try {
                        barometer_unit = "m";
                        if (Variable.fw_ver >= 1.21) {
                            if (constant1.removeDQ(constant1.sendCMDgetRLY("BAROUNIT,\"?\"")).equals("0")) {
                                barometer_unit = "hPa";
                            }
                        }
                    } catch (Exception ignored) {
                    }


                    String modemType = constant1.removeDQ(constant1.sendCMDgetRLY("MDMTYPE,\"?\""));
                    String imei = constant1.removeDQ(constant1.sendCMDgetRLY("MDMIMEI,\"?\""));

                    Message msgg = setTextHandler
                            .obtainMessage(MESSAGE_SET_MODEM);
                    Bundle bundlee = new Bundle();
                    bundlee.putString("data", modemType);
                    bundlee.putInt("code", Constants.sc);
                    bundlee.putString("imei", imei);
                    msgg.setData(bundlee);
                    setTextHandler.sendMessage(msgg);


                    long currtime;
                    while (monitorCommand) {
                        String parameterReply = null;
                        try {
                            currtime = System.currentTimeMillis();
                            if (Constants.gotReply) {

                                if (Constants.monitorInterval >= 60) {
                                    constant1.wakeUpDL();
                                }

                                reply = constant1.sendCommandAndGetReplyforMonPara("MONPARA,\"?\"");
                                statusCodeParameter = (short) Constants.sc;

                                if (reply.length() > 0
                                        && statusCodeParameter == Constants.OK_STATUS) {
                                    parameterReply = reply;
                                    reply = "";
                                } else {
                                    parameterReply = "\"2000-01-01\",\"00:00:00\",0,0.00,0.000000E+00,0,-99.9,0,0,99,99";
                                    reply = "";
                                }
                            }
                            Message msg = setTextHandler
                                    .obtainMessage(MESSAGE_SET_PARAMETER);
                            Bundle bundle = new Bundle();
                            bundle.putString("data", parameterReply);
                            msg.setData(bundle);
                            setTextHandler.sendMessage(msg);
                            while ((System.currentTimeMillis() - currtime) <= (Constants.monitorInterval * 1000))
                                ;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        monitorThread.start();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isBackPressed) // this condition is used when user press exit
            // button and back button simultaneously.
            {
                try {
                    isBackPressed = true;
                    monitorCommand = false;
                    if (monitorThread != null) {
                        monitorThread.interrupt();
                        monitorThread = null;
                    }
                    ParameterActivity.this.finish();
                    constant1 = null;
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(ParameterActivity.this)
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

    protected void showDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(ParameterActivity.this).setTitle("Connection")
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


    private void initProgressDialog() {
        dialog = new ProgressDialog(ParameterActivity.this);
        dialog.setTitle("Reading Parameters Data");
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    private String calculateSignalPower(int dBm) {

        String watt = "";
        float power = (dBm - 30) / 10.0f;
        double value_in_watt = Math.pow(10, power);

        if (value_in_watt < 0.000000000001) {  // pW
            watt = "" + (int) (value_in_watt * Math.pow(10, 15)) + " fW";
        } else if (value_in_watt < 0.000000001) {  // pW
            watt = "" + (int) (value_in_watt * Math.pow(10, 12)) + " pW";
        } else if (value_in_watt < 0.000001) {  // nW
            watt = "" + (int) (value_in_watt * Math.pow(10, 9)) + " nW";
        } else if (value_in_watt < 0.001) {  // uW
            watt = "" + (int) (value_in_watt * Math.pow(10, 6)) + " µW";
        } else if (value_in_watt < 1) {  // mW
            watt = "" + (int) (value_in_watt * Math.pow(10, 3)) + " mW";
        } else if (value_in_watt < 1000) {  // W
            watt = "" + (int) (value_in_watt) + " W";
        } else if (value_in_watt < 1000000) {  // kW
            watt = "" + (int) (value_in_watt * Math.pow(10, -3)) + " kW";
        } else if (value_in_watt < 1000000000) {  // MW
            watt = "" + (int) (value_in_watt * Math.pow(10, -6)) + " MW";
        }

        return watt;
    }

}