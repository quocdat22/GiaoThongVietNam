package com.example.giaothong;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DISPLAY_TIME = 1000; // Hiển thị splash screen trong 1 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Chờ trong thời gian ngắn rồi chuyển đến MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(this::proceedToMainActivity, SPLASH_DISPLAY_TIME);
    }
    
    private void proceedToMainActivity() {
        if (isFinishing()) return;
        
        Log.d(TAG, "Starting MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}