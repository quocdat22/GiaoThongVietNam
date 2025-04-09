package com.example.giaothong.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.app.AlarmManager.AlarmClockInfo;
import android.provider.Settings;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import java.util.Calendar;

/**
 * Lớp quản lý việc lên lịch và hủy bỏ các thông báo nhắc nhở
 */
public class ReminderManager {

    private static final String TAG = "ReminderManager";
    private static final int REMINDER_REQUEST_CODE = 100;
    
    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Kiểm tra xem ứng dụng có quyền thiết lập báo thức chính xác hay không
     * @return true nếu có quyền, false nếu không
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // Trước Android 12 không cần kiểm tra
    }

    /**
     * Mở màn hình cài đặt để người dùng cấp quyền báo thức chính xác
     */
    public void openAlarmPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Lên lịch thông báo nhắc nhở hàng ngày
     * @param hour Giờ nhắc nhở (0-23)
     * @param minute Phút nhắc nhở (0-59)
     * @return true nếu thiết lập thành công, false nếu có lỗi
     */
    public boolean scheduleDailyReminder(int hour, int minute) {
        try {
            // Hủy thông báo hiện tại để tránh trùng lặp
            cancelReminder();
            
            // Tạo Intent và PendingIntent
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    REMINDER_REQUEST_CODE, 
                    intent, 
                    PendingIntent.FLAG_IMMUTABLE
            );
            
            // Thiết lập thời gian nhắc nhở
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            // Nếu thời gian đã qua, đặt cho ngày mai
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            long triggerTime = calendar.getTimeInMillis();
            
            // Đảm bảo Receiver sẽ hoạt động sau khi khởi động lại thiết bị
            ComponentName receiver = new ComponentName(context, BootReceiver.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            
            // Thiết lập báo thức dựa trên phiên bản Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                if (canScheduleExactAlarms()) {
                    // Sử dụng setExactAndAllowWhileIdle nếu có quyền
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    // Sử dụng setInexactRepeating nếu không có quyền chính xác
                    alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                    Log.w(TAG, "Không có quyền báo thức chính xác, đang sử dụng báo thức không chính xác");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6-11
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // Android 4.4-5.1
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else { // Trước Android 4.4
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
            
            // Lưu log thời gian thông báo
            Log.d(TAG, "Đã lên lịch thông báo nhắc nhở vào " + hour + ":" + 
                    (minute < 10 ? "0" + minute : minute) + 
                    ", thời gian còn lại: " + 
                    ((triggerTime - System.currentTimeMillis()) / (1000 * 60)) + " phút");
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thiết lập thông báo nhắc nhở: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hủy bỏ thông báo nhắc nhở
     */
    public void cancelReminder() {
        try {
            // Tạo Intent và PendingIntent giống như khi lên lịch
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    REMINDER_REQUEST_CODE, 
                    intent, 
                    PendingIntent.FLAG_IMMUTABLE
            );
            
            // Hủy bỏ thông báo
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            
            Log.d(TAG, "Đã hủy bỏ thông báo nhắc nhở");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hủy thông báo nhắc nhở: " + e.getMessage());
        }
    }
} 