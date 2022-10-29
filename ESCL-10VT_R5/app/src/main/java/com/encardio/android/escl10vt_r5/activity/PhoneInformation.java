package com.encardio.android.escl10vt_r5.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;

public class PhoneInformation extends AppCompatActivity {
    int width = 0;
    int height = 0;
    Context context;
    IntentFilter intentfilter;
    int volageValue;
    float fullVoltage;
    Double decimalVoltage;
    DecimalFormat decimalformat;
    private String SimOperator = "<Sim Operator Name>";
    private TextView sim_operator1;
    private TextView display_resolution;
    private TextView battery_voltage;
    private TextView battery_charge;
    private TextView battype;
    private TextView batt_temp;
    private final BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            volageValue = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            fullVoltage = (float) (volageValue * 0.001);
            decimalformat = new DecimalFormat("#.###");
            decimalVoltage = Double.valueOf(decimalformat.format(fullVoltage));

            battery_voltage.setText(decimalVoltage + " V");
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int percentage = (int) ((level * 100) / (float) scale);
            battery_charge.setText(percentage + " %");

            battype.setText(intent.getExtras().getString(
                    BatteryManager.EXTRA_TECHNOLOGY));
            batt_temp
                    .setText(""
                            + (intent.getIntExtra(
                            BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f)
                            + " °C");
        }
    };
    private TextView mobile_brand;
    private TextView mobile_model;
    private TextView android_version;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_phone);
        try {
            intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            battype = findViewById(R.id.battype);
            batt_temp = findViewById(R.id.batt_temp);
            battery_voltage = findViewById(R.id.battery_voltage);
            battery_charge = findViewById(R.id.battery_charge);

            mobile_brand = findViewById(R.id.mobile_brand);
            mobile_model = findViewById(R.id.mobile_model);
            android_version = findViewById(R.id.android_version);

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            SimOperator = telephonyManager.getSimOperatorName();

            display_resolution = findViewById(R.id.display_resolution);

            sim_operator1 = findViewById(R.id.sim_operator1);
            try {
                SimOperator = Character.toUpperCase(SimOperator.charAt(0))
                        + SimOperator.substring(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sim_operator1.setText(SimOperator);
            getScreenWidthAndHeight(PhoneInformation.this);
            display_resolution.setText("" + width + " x " + height + " Pixels");
            String brand = android.os.Build.MANUFACTURER;
            brand = Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
            mobile_brand.setText(brand);
            mobile_model.setText(android.os.Build.MODEL);
            android_version.setText(android.os.Build.VERSION.RELEASE);
            PhoneInformation.this.registerReceiver(broadcastreceiver,
                    intentfilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getScreenWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay(); // gives default display object.
        width = display.getWidth();
        height = display.getHeight();
    }
}