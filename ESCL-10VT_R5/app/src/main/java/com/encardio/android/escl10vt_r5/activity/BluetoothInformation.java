package com.encardio.android.escl10vt_r5.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.bluetooth.BluetoothService;
import com.encardio.android.escl10vt_r5.constant.Constants;

public class BluetoothInformation extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_information);

        try {

            TextView bluetooth_address = findViewById(R.id.bluetooth_address);
            TextView bluetooth_device = findViewById(R.id.bluetooth_device);
            TextView bluetooth_status = findViewById(R.id.bluetooth_status);

            if (BluetoothService.mState == BluetoothService.STATE_CONNECTED) {
                bluetooth_address.setText(String.format("%s", Constants.BLUETOOTH_ADDRESS));
                bluetooth_device.setText(String.format("%s", Constants.BLUETOOTH_DEVICE));
                bluetooth_status.setText(R.string.connected);
            } else {
                bluetooth_status.setText(R.string.disconnected);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error on reading reel and probe data", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}