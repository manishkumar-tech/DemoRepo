package com.encardio.android.escl10vt_r5.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.bluetooth.BluetoothService;

/**
 * @author Sandeep Gives the system information like Inclinometer, callibration,
 * Bluetooth and phone information
 */
public class SystemInfoActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_info);

        findViewById(R.id.btn_LoggerInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (BluetoothService.mState == BluetoothService.STATE_CONNECTED) {
                            Intent serverIntent = new Intent(v.getContext(),
                                    LoggerInformation.class);
                            startActivity(serverIntent);
                        } else {
                            showDialog();
                        }
                    }
                });

        findViewById(R.id.btn_SensorInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (BluetoothService.mState == BluetoothService.STATE_CONNECTED) {
                            Intent serverIntent = new Intent(v.getContext(),
                                    SensorInformation.class);
                            startActivity(serverIntent);
                        } else {
                            showDialog();
                        }
                    }
                });

        findViewById(R.id.btn_SamplingInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (BluetoothService.mState == BluetoothService.STATE_CONNECTED) {
                            Intent serverIntent = new Intent(v.getContext(),
                                    SamplingInformation.class);
                            startActivity(serverIntent);
                        } else {
                            showDialog();
                        }
                    }
                });

        findViewById(R.id.btn_BatteryInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (BluetoothService.mState == BluetoothService.STATE_CONNECTED) {
                            Intent serverIntent = new Intent(v.getContext(),
                                    BatteryInformation.class);
                            startActivity(serverIntent);
                        } else {
                            showDialog();
                        }
                    }
                });

        findViewById(R.id.btn_BluetoothInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        Intent serverIntent = new Intent(v.getContext(),
                                BluetoothInformation.class);
                        startActivity(serverIntent);

                    }
                });

        findViewById(R.id.btn_PhoneInformation).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent serverIntent = new Intent(v.getContext(),
                                PhoneInformation.class);
                        startActivity(serverIntent);
                    }
                });

    }

    private void showDialog() {
        new AlertDialog.Builder(SystemInfoActivity.this)
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
}
