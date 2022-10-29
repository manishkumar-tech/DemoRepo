package com.encardio.android.escl10vt_r5.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Sandeep
 */

public class SplashScreen extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent result = new Intent(SplashScreen.this, HomeActivity.class);
                startActivity(result);
                finish();
            }
        }, 3000);
    }

}