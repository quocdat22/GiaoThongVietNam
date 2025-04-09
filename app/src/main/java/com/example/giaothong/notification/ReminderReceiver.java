package com.example.giaothong.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.giaothong.utils.SharedPreferencesManager;

/**
 * BroadcastReceiver để nhận và xử lý các yêu cầu thông báo nhắc nhở
 */
public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Đã nhận được yêu cầu hiển thị thông báo nhắc nhở");
        
        try {
            // Hiển thị thông báo nhắc nhở
            NotificationHelper notificationHelper = new NotificationHelper(context);
            boolean notificationShown = notificationHelper.showDailyReminderNotification();
            
            if (notificationShown) {
                Log.d(TAG, "Đã hiển thị thông báo nhắc nhở thành công");
                
                // Lập lịch cho thông báo tiếp theo (vào ngày hôm sau)
                SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
                if (prefsManager.isDailyReminderEnabled()) {
                    ReminderManager reminderManager = new ReminderManager(context);
                    reminderManager.scheduleDailyReminder(
                            prefsManager.getReminderHour(),
                            prefsManager.getReminderMinute()
                    );
                    Log.d(TAG, "Đã lập lịch cho thông báo tiếp theo");
                }
            } else {
                Log.e(TAG, "Không thể hiển thị thông báo nhắc nhở");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý thông báo nhắc nhở: " + e.getMessage());
        }
    }
} 