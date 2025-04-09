package com.example.giaothong.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.giaothong.utils.SharedPreferencesManager;

/**
 * BroadcastReceiver để khởi động lại thông báo khi thiết bị khởi động
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && 
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Thiết bị vừa khởi động lại, đang thiết lập lại thông báo nhắc nhở");
            
            // Kiểm tra xem tính năng nhắc nhở có được bật không
            SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
            if (prefsManager.isDailyReminderEnabled()) {
                try {
                    // Thiết lập lại thông báo
                    ReminderManager reminderManager = new ReminderManager(context);
                    boolean success = reminderManager.scheduleDailyReminder(
                            prefsManager.getReminderHour(),
                            prefsManager.getReminderMinute()
                    );
                    
                    if (success) {
                        Log.d(TAG, "Đã thiết lập lại thông báo nhắc nhở hàng ngày");
                    } else {
                        Log.e(TAG, "Thiết lập thông báo nhắc nhở không thành công");
                        
                        // Trên Android 12+, có thể do thiếu quyền SCHEDULE_EXACT_ALARM
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !reminderManager.canScheduleExactAlarms()) {
                            Log.e(TAG, "Không có quyền SCHEDULE_EXACT_ALARM");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi thiết lập lại thông báo: " + e.getMessage());
                }
            }
        }
    }
} 