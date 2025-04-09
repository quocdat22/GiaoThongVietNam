package com.example.giaothong.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.giaothong.MainActivity;
import com.example.giaothong.R;

import android.Manifest;

/**
 * Lớp trợ giúp để tạo và quản lý các thông báo
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "daily_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Tạo kênh thông báo (cần thiết cho Android 8.0 trở lên)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                CharSequence name = context.getString(R.string.daily_reminder);
                String description = context.getString(R.string.reminder_message);
                int importance = NotificationManager.IMPORTANCE_HIGH; // Tăng mức độ quan trọng
                
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.enableVibration(true); // Bật rung
                channel.enableLights(true); // Bật đèn thông báo
                
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Đã tạo kênh thông báo: " + CHANNEL_ID);
                } else {
                    Log.e(TAG, "NotificationManager không khả dụng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo kênh thông báo: " + e.getMessage());
            }
        }
    }

    /**
     * Hiển thị thông báo nhắc nhở học tập hàng ngày
     * @return true nếu thông báo được hiển thị thành công, false nếu không
     */
    public boolean showDailyReminderNotification() {
        try {
            // Tạo intent để mở ứng dụng khi người dùng nhấn vào thông báo
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            // Tạo PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    0, 
                    intent, 
                    PendingIntent.FLAG_IMMUTABLE
            );
            
            // Xây dựng thông báo với nội dung từ resources
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.reminder_title))
                    .setContentText(context.getString(R.string.reminder_message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Tăng mức độ ưu tiên
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 200, 500}) // Thêm rung
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Hiển thị trên màn hình khóa
            
            // Hiển thị thông báo nếu có quyền
            if (hasNotificationPermission()) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
                Log.d(TAG, "Đã hiển thị thông báo nhắc nhở");
                return true;
            } else {
                Log.e(TAG, "Không có quyền POST_NOTIFICATIONS");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị thông báo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra xem ứng dụng có quyền hiển thị thông báo hay không
     * @return true nếu có quyền, false nếu không
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Android 13+: Quyền POST_NOTIFICATIONS: " + (hasPermission ? "Đã cấp" : "Chưa cấp"));
            return hasPermission;
        }
        return true;  // Dưới Android 13 không cần quyền POST_NOTIFICATIONS
    }
} 