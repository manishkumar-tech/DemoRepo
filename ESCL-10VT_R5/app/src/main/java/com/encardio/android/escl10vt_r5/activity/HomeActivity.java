package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.BuildConfig;

import com.encardio.android.escl10vt_r5.bluetooth.BluetoothService;
import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author Sandeep
 */
public class HomeActivity extends AppCompatActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CODE_SETUP_SCREEN = 3;
    private static final String TAG = "ESCL Bluetooth App";
    private static final boolean D = true;
    protected PowerManager.WakeLock mWakeLock;
    protected ProgressDialog dialog;
    LocationClient mLocationClient;
    ConnectionDetector cd;
    ProgressDialog alertForConnecting;


    private TextView txt_datalogger_id;
    private ImageView img_connection_status;
    private ImageView img_scan_status;
    private String mconnectingDeviceName = null;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter;

    private Constants objConstant;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:

                            Variable.isConnected = true;
                            objConstant.wakeUpDL();
                            sendInitialCommands();
                            locationDifference();
                            if (Constants.sc == Constants.BATTERY_DEAD_STATUS) {
                                img_connection_status
                                        .setImageResource(R.drawable.circle_red);
                                img_scan_status.setImageResource(R.drawable.circle_red);
                                txt_datalogger_id.setText("--------");
                                alertForConnecting.dismiss();
                                batteryLowDialog();
                            } else {
                                try {
                                    if (Variable.loggerModel.equalsIgnoreCase("ESCL-10VTR5")) {

                                        if ((!Variable.loggerID.equals(""))
                                                && (!Variable.loggerID
                                                .equals("--------"))) {
                                            alertForConnecting.dismiss();
                                            img_connection_status
                                                    .setImageResource(R.drawable.circle_green);

                                            if (Variable.scanStatus) {
                                                img_scan_status.setImageResource(R.drawable.circle_green);
                                            } else {
                                                img_scan_status.setImageResource(R.drawable.circle_red);
                                            }

                                            txt_datalogger_id
                                                    .setText(Variable.loggerID);
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Connected to "
                                                            + mConnectedDeviceName,
                                                    Toast.LENGTH_SHORT).show();
                                            if (Constants.toastFromThread)
                                                Constants.toastFromThread = false;
                                            Constants.connectionBreak = false;

                                        } else {
                                            alertForConnecting.dismiss();
                                            img_connection_status
                                                    .setImageResource(R.drawable.circle_red);
                                            img_scan_status.setImageResource(R.drawable.circle_red);
                                            txt_datalogger_id.setText("--------");
                                            Toast.makeText(getApplicationContext(),
                                                    "Data logger not connected...",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        alertForConnecting.dismiss();
                                        img_connection_status
                                                .setImageResource(R.drawable.circle_red);
                                        img_scan_status.setImageResource(R.drawable.circle_red);
                                        txt_datalogger_id.setText("--------");
                                        Toast.makeText(getApplicationContext(),
                                                "Unable to find ESCL-10VTR5",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception ignored) {
                                    alertForConnecting.dismiss();
                                }
                                alertForConnecting.dismiss();
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:

                            initProgressDialog();
                            alertForConnecting.show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:

                            img_connection_status
                                    .setImageResource(R.drawable.circle_red);
                            img_scan_status.setImageResource(R.drawable.circle_red);
                            txt_datalogger_id.setText("--------");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    if (Constants.toastFromThread) {
                        Constants.toastFromThread = false;
                    }
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                case MESSAGE_TOAST:
                    if (msg.getData().getBoolean("CONNECTION_FAILED")) {
                        alertForConnecting.dismiss();
                        Variable.isConnected = false;
                    }
                    if (msg.getData().getBoolean("CONNECTION_LOST")) {

                        img_connection_status
                                .setImageResource(R.drawable.circle_red);
                        img_scan_status.setImageResource(R.drawable.circle_red);
                        txt_datalogger_id.setText("--------");
                        Variable.isConnected = false;
                    }
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    private short ftp_testw;
    private long currentTimemill;


    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (Environment.isExternalStorageManager()) {
                        // Permission granted. Now resume your workflow.

                    } else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                    Uri.parse("package:" + "com.encardio.android.escl10vt_r5.activity"));
                            //  startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                            activityResultLaunch.launch(intent);


                        } catch (Exception e) {

                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            //    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                            activityResultLaunch.launch(intent);

                        }
                    }
                }
            });



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);


        //# manish add
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + "com.encardio.android.escl10vt_r5.activity"));
                    //  startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                    activityResultLaunch.launch(intent);


                } catch (Exception e) {

                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    //    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                    activityResultLaunch.launch(intent);


                }
            }
        }

        if (!checkPermission()) {
            requestPermission();
        }


        txt_datalogger_id = findViewById(R.id.txt_datalogger_id);
        img_connection_status = findViewById(R.id.img_connection_status);
        img_scan_status = findViewById(R.id.img_scan_status);

        cd = new ConnectionDetector(this);

        img_connection_status.setImageResource(R.drawable.circle_red);
        img_scan_status.setImageResource(R.drawable.circle_red);
        Variable.loggerID = "";

        txt_datalogger_id.setText("--------");
        objConstant = new Constants();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, ":MyTag");
        this.mWakeLock.acquire();
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Sdcard is not mounted...", Toast.LENGTH_LONG).show();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available...",
                    Toast.LENGTH_LONG).show();
        }
        mLocationClient = new LocationClient(this, this, this);
        img_connection_status.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    public boolean onLongClick(View v) {
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        if (!manager
                                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "GPS need to be turned ON for FTP Test to function.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            final StringBuilder replyMsg = new StringBuilder();
                            final ProgressDialog dialog = ProgressDialog.show(
                                    HomeActivity.this, "FTP TEST",
                                    "FTPTEST Uploading...\n It may take more than a minute");
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    if (Variable.isConnected) {
                                        try {
                                            currentTimemill = System
                                                    .currentTimeMillis();
                                            while (((System.currentTimeMillis() - currentTimemill) / 1000) < 40) {

                                            }
                                            sendFTPTest();

                                            if (ftp_testw == Constants.OK_STATUS) {
                                                replyMsg.append("Command Successful");
                                            } else if (ftp_testw == Constants.MODEM_DISABLE_STATUS) {
                                                replyMsg.append("GPRS Modem is disabled");
                                            } else if (ftp_testw == Constants.BATTERY_DEAD_STATUS) {
                                                replyMsg.append("Battery Voltage is too low");
                                            } else if (ftp_testw == Constants.MODEM_COMM_MODE_GPRS_STATUS) {
                                                replyMsg.append("Modem is already in use for data transfer");
                                            } else if (ftp_testw == Constants.MODEM_OPERATING_MODE_OFF_STATUS) {
                                                replyMsg.append("Modem ststus is 'Permanently OFF'");
                                            } else if (ftp_testw == Constants.MODEM_POWER_ON_ERROR) {
                                                replyMsg.append("Unable to turn on modem power");
                                            } else if (ftp_testw == Constants.MODEM_SIM_UNAVAILABLE_STATUS) {
                                                replyMsg.append("No SIM card inserted");
                                            } else if (ftp_testw == Constants.MODEM_SIGNAL_ERROR) {
                                                replyMsg.append("Service provider network not found");
                                            } else if (ftp_testw == Constants.FTP_PARAMETER_NOT_SET) {
                                                replyMsg.append("Unable to set FTP parameters");
                                            } else if (ftp_testw == Constants.UNOPEN_FTP_SOCKET) {
                                                replyMsg.append("Unable to open FTP socket");
                                            } else if (ftp_testw == Constants.FTP_DATA_FAIL) {
                                                replyMsg.append("Unable to send FTP data");
                                            } else if (ftp_testw == Constants.INVALID_COMMAND_ERROR) {
                                                replyMsg.append(" Error - Command is invalid");
                                            } else {
                                                replyMsg.append("Resend..");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        replyMsg.append("Datalogger is not connected...");
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    dialog.dismiss();
                                    displayCurrentLocation(ftp_testw + " " + replyMsg);
                                    Toast.makeText(getApplicationContext(),
                                            replyMsg, Toast.LENGTH_LONG).show();
                                }

                            }.execute();
                        }
                        return false;
                    }
                });

    }


    public void click_connection(View view) {
        if (!Variable.isConnected) {
            img_connection_status
                    .setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);

            txt_datalogger_id.setText("--------");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,
                    REQUEST_ENABLE_BT);
        } else {

            if ((Constants.bluetoothService != null)
                    && (Constants.bluetoothService.getState() == BluetoothService.STATE_CONNECTED)) {
                if ((!Variable.loggerID.equals(""))
                        && (!Variable.loggerID
                        .equals("--------"))) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Already Connected to bluetooth device..."
                                    + Constants.BLUETOOTH_DEVICE,
                            Toast.LENGTH_SHORT).show();
                    img_connection_status
                            .setImageResource(R.drawable.circle_green);

                } else {

                    BluetoothAdapter mBtAdapter = BluetoothAdapter
                            .getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = mBtAdapter
                            .getBondedDevices();
                    int countEsclDevice = 0;

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().startsWith(
                                    "ESCL"))
                                countEsclDevice++;
                        }
                    }

                    if (countEsclDevice > 0) {
                        Intent serverIntent = new Intent(view
                                .getContext(),
                                DeviceListActivity.class);
                        startActivityForResult(serverIntent,
                                REQUEST_CONNECT_DEVICE);
                    } else {
                        showToast("Please pair a ESCL-10VTR5 device...");
                    }
                }
            } else {

                BluetoothAdapter mBtAdapter = BluetoothAdapter
                        .getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = mBtAdapter
                        .getBondedDevices();
                int countEsclDevice = 0;

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().startsWith(
                                "ESCL"))

                            countEsclDevice++;
                    }
                }
                if (countEsclDevice > 0) {
                    Intent serverIntent = new Intent(view
                            .getContext(),
                            DeviceListActivity.class);
                    startActivityForResult(serverIntent,
                            REQUEST_CONNECT_DEVICE);
                } else {
                    showToast("Please pair a ESCL-10VTR5 device...");
                }
            }
        }
    }

    public void click_system_information(View view) {
        Intent intent = new Intent(HomeActivity.this,
                SystemInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void click_system_configuration(View view) {
        if (Variable.isConnected) {
            if ((!Variable.loggerID.equals(""))
                    && (!Variable.loggerID
                    .equals("--------"))) {
                Intent intent = new Intent(view.getContext(),
                        SetupMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,
                        REQUEST_CODE_SETUP_SCREEN);
            } else {
                img_connection_status
                        .setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);

                txt_datalogger_id.setText("--------");
                Toast.makeText(getApplicationContext(),
                        "Datalogger is not connected...",
                        Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(),
                    "Datalogger is not connected...",
                    Toast.LENGTH_SHORT).show();
    }

    public void click_modem_configuration(View view) {
        if (Variable.isConnected) {
            if ((!Variable.loggerID.equals(""))
                    && (!Variable.loggerID
                    .equals("--------"))) {


                objConstant.wakeUpDL();
                String reply = objConstant
                        .sendCMDgetRLY("GPRSMDM,\"?\"");
                if (Constants.gotReply) {
                    short statsCode = (short) Constants.sc;
                    if (reply.trim().contains("ENABLE")
                            && statsCode == Constants.OK_STATUS) {
                        Intent intent = new Intent(view
                                .getContext(),
                                ModemSettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {

                        if (statsCode == Constants.OK_STATUS)
                            Toast.makeText(
                                    getApplicationContext(),
                                    "GPRS Modem is Disabled..",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(
                                    getApplicationContext(),
                                    Constants.responseMsg,
                                    Toast.LENGTH_LONG).show();
                    }
                } else {

                    img_connection_status
                            .setImageResource(R.drawable.circle_red);
                    img_scan_status.setImageResource(R.drawable.circle_red);
                    Variable.loggerID = "";

                    txt_datalogger_id.setText("--------");
                    showDialog();

                }
            } else {
                img_connection_status
                        .setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);

                txt_datalogger_id.setText("--------");
                showDialog();
            }
        } else
            Toast.makeText(getApplicationContext(),
                    "Bluetooth Connection lost...",
                    Toast.LENGTH_SHORT).show();
    }

    public void click_monitor_parameter(View view) {
        if (Variable.isConnected) {
            if ((!Variable.loggerID.equals(""))
                    && (!Variable.loggerID
                    .equals("--------"))) {

                Log.e("-------------", "-------------------");


                objConstant.wakeUpDL();
                String reply = objConstant.removeDQ(objConstant
                        .sendCMDgetRLY("GPRSMDM,\"?\""));
                if (!Constants.toastFromThread) {
                    short statsCode = (short) Constants.sc;
                    Intent intent = new Intent(view.getContext(),
                            MonitorTest.class);

                    if (reply.trim().contains("ENABLE")
                            && statsCode == Constants.OK_STATUS) {
                        // intent.putExtra("MODEM_EXIST", true);
                        Constants.mdmstatus = true;
                        String replyMdm = objConstant.removeDQ(objConstant
                                .sendCMDgetRLY("MDMPWR,\"?\""));
                        short statsCodeMdm = (short) Constants.sc;
                        Constants.mdmPwr = replyMdm.trim().contains("ON")
                                && statsCodeMdm == Constants.OK_STATUS;

                    } else
                        Constants.mdmstatus = false;
                    startActivity(intent);
                } else {
                    img_connection_status
                            .setImageResource(R.drawable.circle_red);
                    img_scan_status.setImageResource(R.drawable.circle_red);
                    Variable.loggerID = "";

                    txt_datalogger_id.setText("--------");
                    Toast.makeText(getApplicationContext(),
                            "Datalogger is not connected...",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                img_connection_status
                        .setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);

                txt_datalogger_id.setText("--------");
                Toast.makeText(getApplicationContext(),
                        "Datalogger is not connected...",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            img_connection_status
                    .setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);
            txt_datalogger_id.setText("--------");
            Toast.makeText(getApplicationContext(),
                    "Datalogger is not connected...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void click_scan_format(View view) {
        if (Variable.isConnected) {
            if ((!Variable.loggerID.equals(""))
                    && (!Variable.loggerID
                    .equals("--------"))) {
                Intent intent = new Intent(view.getContext(),
                        ScanAndFormatActivity.class);
                startActivity(intent);

            } else {

                img_connection_status
                        .setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);

                txt_datalogger_id.setText("--------");
                Toast.makeText(getApplicationContext(),
                        "Datalogger is not connected...",
                        Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(),
                    "Datalogger is not connected...",
                    Toast.LENGTH_SHORT).show();
    }

    public void click_view_data(View view) {
        if (!Variable.isConnected) {

            img_connection_status
                    .setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);

            txt_datalogger_id.setText("--------");
        }
        Intent intent = new Intent(view.getContext(),
                ViewFileDataTableActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void click_upload_data(View view) {
        if (!Variable.isConnected) {
            img_connection_status
                    .setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);

            txt_datalogger_id.setText("--------");
        }
        Intent intent = new Intent(view.getContext(),
                UploadFilesActivity.class);

        startActivity(intent);
    }

    public void click_shut_down_datalogger(View view) {
        if (Variable.isConnected) {

            String shutDownMsg = "";
            if (Variable.scanStatus) {
                shutDownMsg = "Do you really want to Shut Down Datalogger ?\nIt will STOP Scanning";
            } else {
                shutDownMsg = "Do you really want to Shut Down Datalogger ?";
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    HomeActivity.this)
                    .setTitle(R.string.shutdown)
                    .setIcon(R.drawable.shutdown)
                    .setMessage(shutDownMsg)
                    .setPositiveButton(
                            R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                    new asyncTask()
                                            .execute();
                                }
                            })
                    .setNegativeButton(R.string.no, null);
            dialog.show();
        } else
            Toast.makeText(getApplicationContext(),
                    "Datalogger is not connected...",
                    Toast.LENGTH_SHORT).show();
    }

    private void sendFTPTest() {

        objConstant.wakeUpDL();
        objConstant.sendCommandAndGetReplyforMonPara("TESTFTP");

        ftp_testw = (short) Constants.sc;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Tab OK to go to setting screen for turning On GPS.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(
                            final DialogInterface dialog,
                            final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        mLocationClient.connect();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkScanStatus() {
        objConstant.wakeUpDL();
        String reply = objConstant.sendCMDgetRLY("SCAN,\"?\"");
        if (Constants.sc == Constants.OK_STATUS)
            if (reply.trim().equalsIgnoreCase("ON")
                    || reply.trim().equalsIgnoreCase("START")) {
                Variable.scanStatus = true;
            }
    }

    private void stopScan() {
        objConstant.wakeUpDL();
        objConstant.sendCMDgetFullRLY("SCAN,\"STOP\"");
        if (Constants.sc == Constants.OK_STATUS) {
            Variable.scanStatus = false;
        }
    }

    public void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            mLocationClient.connect();
        }


        if (Constants.toastFromThread) {
            img_connection_status.setImageResource(R.drawable.circle_red);
            img_scan_status.setImageResource(R.drawable.circle_red);
            Variable.loggerID = "";

            txt_datalogger_id.setText("--------");
        }
        try {
            if (!mBluetoothAdapter.isEnabled()) {

                Intent enableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

            } else {
                if (Constants.bluetoothService == null) {
                    setupBluetoothService();
                }
                if (Constants.bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    if (Constants.isNewBluetoothConnection) {

                        Intent intent = new Intent();
                        intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS,
                                Constants.BLUETOOTH_ADDRESS_FROM_LIST);
                        onActivityResult(REQUEST_CONNECT_DEVICE,
                                Activity.RESULT_OK, intent);
                        Constants.isNewBluetoothConnection = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this).setTitle("Connection")
                .setIcon(R.drawable.error)
                .setMessage("Device connection lost !").setCancelable(false)
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

    private void setupBluetoothService() {

        Constants.bluetoothService = new BluetoothService(this, mHandler,
                objConstant);
    }

    private void sendInitialCommands() {

        objConstant.wakeUpDataLoggerConnection();

        Variable.loggerModel = objConstant.removeDQ(objConstant
                .sendCMDgetRLY("MODEL,\"?\""));
        if (Constants.OK_STATUS == Constants.sc) {
            Variable.fw_ver = Float.parseFloat(objConstant.removeDQ(objConstant
                    .sendCMDgetRLY("FWVER,\"?\"")).trim());
        }


        if ((Variable.loggerModel != null) && (Variable.loggerModel.equalsIgnoreCase("ESCL-11VT"))) {

            objConstant.removeDQ(objConstant.sendCMDgetRLY("MODEL,\"ESCL-10VTR5\""));
            if (Constants.OK_STATUS == Constants.sc) {
                Variable.loggerModel = "ESCL-10VTR5";
            }
        }

        try {
            if ((Variable.loggerModel != null) && (Variable.loggerModel.equalsIgnoreCase("ESCL-10VTR5"))) {

                if (Constants.OK_STATUS == Constants.sc) {
                    String scanStatus = (objConstant.removeDQ(objConstant
                            .sendCMDgetRLY("SCAN,\"?\"")));
                    Variable.scanStatus = !scanStatus.equalsIgnoreCase("STOP");
                }

                if (Constants.OK_STATUS == Constants.sc) {
                    Variable.loggerTopElevation = "" + Float.parseFloat(objConstant.removeDQ(objConstant
                            .sendCMDgetRLY("TOPELEV,\"?\"")));
                }

                try {
                    Integer.parseInt(Variable.loggerTopElevation);
                    Variable.isTopElevationInFloat = false;
                } catch (Exception e) {
                    Variable.isTopElevationInFloat = true;
                }

                if (Constants.OK_STATUS == Constants.sc) {
                    String freq = objConstant.removeDQ(objConstant
                            .sendCMDgetRLY("XVALUE,\"?\""));
                    Constants.isFrequency = freq.equalsIgnoreCase("FREQ");
                }

                if (Constants.sc == Constants.OK_STATUS) {
                    Variable.loggerID = objConstant.removeDQ(objConstant
                            .sendCMDgetRLY("DLID,\"?\""));
                    if (Constants.sc == Constants.OK_STATUS) {
                        txt_datalogger_id.setText(Variable.loggerID);
                    } else {
                        showToast("Invalid Data found...");
                        txt_datalogger_id.setText("");
                    }

                    String battv;

                    String temp1 = objConstant
                            .sendCMDgetRLY("BATTV,\"?\"");
                    if (Constants.sc == Constants.OK_STATUS) {
                        battv = temp1;
                    } else {
                        battv = "0";
                    }
                    float btvolt = Float.parseFloat(battv);
                    temp1 = objConstant.removeDQ(objConstant
                            .sendCMDgetRLY("BATTYPE,\"?\""));
                    if (Constants.sc == Constants.OK_STATUS) {
                        battv = temp1;
                    } else {
                        battv = "";
                    }
                    temp1 = objConstant.sendCMDgetRLY("BATDATE,\"?\"");
                    if (Constants.sc == Constants.OK_STATUS) {
                        Constants.batteryInstallationDate = temp1.substring(3,
                                temp1.length() - 1);
                        int temp;
                        temp = Integer.parseInt(temp1.substring(0, 1));
                        if (temp == 0) {
                            newBatteryFoundDialog();
                        }
                    }
                    switch (battv) {
                        case "ALKALINE":
                            if ((btvolt > 1.9F) && (btvolt < 2.0F)) {
                                batteryDialog("Datalogger battery is low !!",
                                        "            20% remaining.",
                                        R.drawable.battery20);
                            } else if ((btvolt > 1.7F) && (btvolt <= 1.9F)) {
                                batteryDialog("Datalogger battery is low !!",
                                        "            10% remaining.",
                                        R.drawable.battery10);
                            } else if ((btvolt > 1.6F) && (btvolt <= 1.7F)) {
                                batteryDialog("Datalogger battery is too low !!",
                                        "            5% remaining.",
                                        R.drawable.battery5);
                            } else if ((btvolt > 1.5F) && (btvolt <= 1.6F)) {
                                batteryDialog("Datalogger battery is too low !!",
                                        "            2% remaining.",
                                        R.drawable.battery2);
                            } else if (btvolt <= 1.5F) {
                                batteryDialog(
                                        "Datalogger battery is too low !!",
                                        "Please replace Datalogger battery immediately.",
                                        R.drawable.battery0);
                            }
                            break;
                        case "LITHIUM":
                            if ((btvolt > 6.9F) && (btvolt < 7.0F)) {
                                batteryDialog("Datalogger battery is low !!",
                                        "            20% remaining.",
                                        R.drawable.battery20);
                            } else if ((btvolt > 6.8F) && (btvolt <= 6.9F)) {
                                batteryDialog("Datalogger battery is low !!",
                                        "            10% remaining.",
                                        R.drawable.battery10);
                            } else if ((btvolt > 6.7F) && (btvolt <= 6.8F)) {
                                batteryDialog("Datalogger battery is too low !!",
                                        "            5% remaining.",
                                        R.drawable.battery5);
                            } else if ((btvolt > 6.6F) && (btvolt <= 6.7F)) {
                                batteryDialog("Datalogger battery is too low !!",
                                        "            2% remaining.",
                                        R.drawable.battery2);
                            } else if (btvolt <= 6.6F) {
                                batteryDialog(
                                        "Datalogger battery is too low !!",
                                        "Please replace Datalogger battery immediately.",
                                        R.drawable.battery0);
                            }
                            break;
                        case "EXTERNAL":
                            if (btvolt <= 9.0F) {
                                batteryDialog(
                                        "Datalogger battery is low !!",
                                        "Please replace Datalogger battery immediately.",
                                        R.drawable.battery0);
                            }
                            break;
                    }

                }
            } else {
                showToast("Unable to find Datalogger");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception occurred !!", Toast.LENGTH_LONG);
        }
    }

    private void newBatteryFoundDialog() {
        // Creating a custom dialog
        final Dialog mdialog = new Dialog(HomeActivity.this);
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // provide view to that custom dialog through xml
        mdialog.setContentView(R.layout.new_battery);
        mdialog.setTitle("Enter Date");

        final Button buttonDate = (Button) mdialog
                .findViewById(R.id.buttonDate);
        Button buttonOk = (Button) mdialog.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button) mdialog.findViewById(R.id.buttonCancel);
        buttonDate.setText("" + Constants.batteryInstallationDate);

        buttonDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        try {
                            buttonDate.setText(parseDate(myCalendar.getTime()
                                    .toString()));
                        } catch (Exception ignored) {
                        }
                    }
                };

                new DatePickerDialog(HomeActivity.this, datePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        buttonOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = buttonDate.getText().toString().trim();
                objConstant.wakeUpDL();
                objConstant.sendCMDgetRLY("BATDATE,\"" + date + "\"");

                if (Constants.sc == Constants.OK_STATUS) {
                    Constants.batteryInstallationDate = date;
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid date...",
                            Toast.LENGTH_LONG).show();
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


    private boolean checkPermission() {

        int bluetooth_admin = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN);
        int bluetooth = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH);
        int read_phone_state = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int wake_lock = ContextCompat.checkSelfPermission(getApplicationContext(), WAKE_LOCK);
        int vibrate = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        int write_external_storage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int internet = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int access_network_state = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
        int access_fine_location = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int access_coarse_location = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int send_sms = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);

        return bluetooth_admin == PackageManager.PERMISSION_GRANTED
                && bluetooth == PackageManager.PERMISSION_GRANTED
                && read_phone_state == PackageManager.PERMISSION_GRANTED
                && wake_lock == PackageManager.PERMISSION_GRANTED
                && vibrate == PackageManager.PERMISSION_GRANTED
                && write_external_storage == PackageManager.PERMISSION_GRANTED
                && internet == PackageManager.PERMISSION_GRANTED
                && access_network_state == PackageManager.PERMISSION_GRANTED
                && access_fine_location == PackageManager.PERMISSION_GRANTED
                && access_coarse_location == PackageManager.PERMISSION_GRANTED
                && send_sms == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{BLUETOOTH_ADMIN, BLUETOOTH, READ_PHONE_STATE,
                WAKE_LOCK, VIBRATE, WRITE_EXTERNAL_STORAGE, INTERNET, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                boolean bluetooth_admin = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean bluetooth = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean read_phone_state = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                boolean wake_lock = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                boolean vibrate = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                boolean write_external_storage = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                boolean internet = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                boolean access_network_state = grantResults[7] == PackageManager.PERMISSION_GRANTED;
                boolean access_fine_location = grantResults[8] == PackageManager.PERMISSION_GRANTED;
                boolean access_coarse_location = grantResults[9] == PackageManager.PERMISSION_GRANTED;
                boolean send_sms = grantResults[10] == PackageManager.PERMISSION_GRANTED;

                if (bluetooth_admin
                        && bluetooth
                        && read_phone_state
                        && wake_lock
                        && vibrate
                        && write_external_storage
                        && internet
                        && access_network_state
                        && access_fine_location
                        && access_coarse_location
                        && send_sms) {
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showMessageOKCancel(
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{BLUETOOTH_ADMIN, BLUETOOTH, READ_PHONE_STATE,
                                                            WAKE_LOCK, VIBRATE, WRITE_EXTERNAL_STORAGE, INTERNET, ACCESS_NETWORK_STATE,
                                                            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, SEND_SMS},
                                                    PERMISSION_REQUEST_CODE);
                                        }
                                    }
                                });
                        return;
                    }
                }
            }
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(HomeActivity.this)
                .setMessage("You need to allow access to these permissions")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
        month = Tool.pad(mnth).trim();
        day = dateArr[2].trim();
        year = dateArr[5].trim();

        return year + "/" + month + "/" + day;

    }

    private void batteryLowDialog() {

        Constants.battery_low = false;
        new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this).setIcon(R.drawable.battery0)
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK) {

                    String address = data.getExtras().getString(
                            Constants.EXTRA_DEVICE_ADDRESS);

                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mconnectingDeviceName = device.getName();
                    Constants.BLUETOOTH_ADDRESS = address;
                    Constants.BLUETOOTH_DEVICE = mconnectingDeviceName;

                    try {

                        Constants.bluetoothService = new BluetoothService(this,
                                mHandler, objConstant);
                        Constants.bluetoothService.connect(device);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case REQUEST_ENABLE_BT:

                if (resultCode == Activity.RESULT_OK) {

                    setupBluetoothService();
                    Constants.isBTenabledByApp = true;
                } else {

                    Toast.makeText(this, "Bluetooth connection failure...",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_SETUP_SCREEN:
                if (resultCode == Activity.RESULT_OK) {
//                    txt_datalogger_id = (TextView) findViewById(R.id.textViewDataloggerValue);
//
//                    txt_datalogger_id.setText(Variable.loggerID);
                }
                break;
        }
    }

    private void batteryDialog(String title, String message, int img) {
        new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this).setTitle(title).setIcon(img)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    private void locationDifference() {
        try {

            final float get_Current_Lat = Float.parseFloat(Constants.setDecimalDigits(Double.toString(getlocationser().getLatitude()), 6));// Double.toString(currentLocation.getLatitude());
            final float get_Current_Lon = Float.parseFloat(Constants.setDecimalDigits(Double.toString(getlocationser().getLongitude()), 6));// Double.toString(currentLocation.getLongitude());
            float get_Datalogger_Lat = Float.parseFloat(Variable.loggerLocation.split(",")[0]);
            float get_Datalogger_Lon = Float.parseFloat(Variable.loggerLocation.split(",")[1]);
            float diff_lat = Math.abs(get_Current_Lat - get_Datalogger_Lat);
            float diff_Lon = Math.abs(get_Current_Lon - get_Datalogger_Lon);
            if (diff_lat > .0001 || diff_Lon > .0001) {
                Log.d("Difference is ", "" + diff_lat + " , " + diff_Lon);
                new AlertDialog.Builder(HomeActivity.this).setTitle("Alert!")
                        .setMessage("Datalogger location coordinates do not match current location.\nUpdate datalogger location coordinates with current location coordinates?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    objConstant.wakeUpDL();

                                    if (Constants.sc == Constants.OK_STATUS) {
                                        String coord = "" + get_Current_Lat + "," + get_Current_Lon;
                                        objConstant.sendCMDgetRLY("DLCORD," + coord + "");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                }).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @SuppressLint("MissingPermission")
    private Location getlocationser() {
        Location location = null;
        // Getting LocationManager object
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        String provider = locationManager.getBestProvider(criteria, false);

        if (provider != null && !provider.equals("")) {

            // Get the location from the given provider
            location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 20000, 1, this);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location == null) {
                    Toast.makeText(
                            HomeActivity.this,
                            "Location Null", Toast.LENGTH_SHORT).show();
                }
            }
            if (location != null)
                onLocationChanged(location);
            else {

                Toast.makeText(HomeActivity.this, "Location can't be retrieved", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(HomeActivity.this, "No Provider Found", Toast.LENGTH_SHORT).show();
        }
        return location;


    }

    /**
     * To initialize progress bar.
     */
    private void initProgressDialog() {
        // create a dialog
        alertForConnecting = new ProgressDialog(this);
        // set the title of the dialog
        alertForConnecting.setTitle("Please wait !!!");
        // Set if the dialog can be skipped
        alertForConnecting.setCancelable(true);
        // Set if the dialog doesn't have a estimated time to be dismissed
        alertForConnecting.setMessage("Connecting to " + mconnectingDeviceName);
    }

    protected void showToast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        Log.e("Variable.logger_ID",""+Variable.loggerID);

        if (Variable.isConnected) {
            txt_datalogger_id.setText(Variable.loggerID);
            if (Variable.scanStatus) {
                img_scan_status.setImageResource(R.drawable.circle_green);
            } else {
                img_scan_status.setImageResource(R.drawable.circle_red);
            }
        } else {
            txt_datalogger_id.setText(Constants.DEFAULT_TEXT);
            img_scan_status.setImageResource(R.drawable.circle_red);
        }

        if (D)

            if (Constants.bluetoothService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't
                // started already
                if (Constants.bluetoothService.getState() == BluetoothService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    Constants.bluetoothService.start();
                }
            }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mLocationClient.disconnect();

        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
        Log.e("Stop", "Got disconnected....");
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Ask the user if they want to quit
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setIcon(R.drawable.quit)
                    .setTitle("QUIT")
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Constants.tag = 0;
                                    Constants.tag1 = 0;
                                    quitFromApp();
                                }
                            }).setNegativeButton(R.string.no, null).show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void quitFromApp() {
        if (Variable.isConnected) {

            Variable.isConnected = false;
        }

        if (Constants.bluetoothService != null) {
            Constants.bluetoothService.stop();

            Constants.bluetoothService = null;
        }
        if (Constants.isBTenabledByApp) {
            mBluetoothAdapter.disable();
        } else {
            Toast.makeText(
                    HomeActivity.this,
                    "Bluetooth is not being turned off !!\nIt was enabled by another application...",
                    Toast.LENGTH_LONG).show();
        }
        Constants.isBTenabledByApp = false;
        mBluetoothAdapter = null;

        HomeActivity.this.finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Connection Failure : " + arg0.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle arg0) {

    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    public void displayCurrentLocation(String dlid_content) {

        Date datetime = new Date();
        String msg = "";
        try {
            Location currentLocation = mLocationClient.getLastLocation();

            msg = String.format("ORG:2\r\nDL:%s\r\nSN:%s\r\nLOC:%s,%s\r\nD/T:%s\r\nEC:%s\r\nFV:%s", Variable.loggerID, Variable.loggerSerialNumber, currentLocation.getLatitude(), currentLocation.getLongitude(), new SimpleDateFormat("yyyy/MM/dd HH:mm").format(datetime), dlid_content, Variable.loggerFwVer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SharedPreferences pref = getSharedPreferences(
                    Constants.PREF_PHON_NAME, Context.MODE_PRIVATE);
            String phone = pref.getString(Constants.PREF_ADMIN_NAME, null);
            if (phone != null) {
                msg = msg + "\r\n" + Constants.dataToascii(msg);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, msg, null, null);// 9335225879
                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please add Admin Contact", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class asyncTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

        public asyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(HomeActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait.....");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            checkScanStatus();
            if (Variable.scanStatus)
                stopScan();
            objConstant.wakeUpDL();
            objConstant.sendCMDgetFullRLY("SHDN");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (Constants.sc == Constants.OK_STATUS) {
                showToast("Datalogger shutdown successfully...");

                txt_datalogger_id.setText("--------");
                img_connection_status
                        .setImageResource(R.drawable.circle_red);
                img_scan_status.setImageResource(R.drawable.circle_red);
                txt_datalogger_id.setText("--------");
                quitFromApp();
            } else if (Constants.battery_low)
                batteryLowDialog();
            else if (Constants.toastFromThread)
                showDialog();
            else
                showToast(Constants.sc
                        + ": Datalogger doesn't get shutdown...");
        }
    }
}