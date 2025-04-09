package com.example.giaothong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.giaothong.model.TrafficSign;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Quản lý lưu trữ dữ liệu sử dụng SharedPreferences
 */
public class SharedPreferencesManager {
    
    private static final String TAG = "SharedPrefsManager";
    private static final String PREFS_NAME = "com.example.giaothong.preferences";
    private static final String KEY_PINNED_TRAFFIC_SIGNS = "pinned_traffic_signs";
    private static final String KEY_PINNED_TRAFFIC_SIGNS_DETAILS = "pinned_traffic_signs_details";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Lưu danh sách ID biển báo đã ghim
     * @param pinnedIds Set chứa các ID của biển báo đã ghim
     */
    public void savePinnedTrafficSignIds(Set<String> pinnedIds) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_PINNED_TRAFFIC_SIGNS, pinnedIds);
        editor.apply();
    }
    
    /**
     * Lấy danh sách ID biển báo đã ghim
     * @return Set chứa các ID của biển báo đã ghim
     */
    public Set<String> getPinnedTrafficSignIds() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_PINNED_TRAFFIC_SIGNS, new HashSet<>()));
    }
    
    /**
     * Xóa tất cả biển báo ghim
     */
    public void clearPinnedTrafficSigns() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PINNED_TRAFFIC_SIGNS);
        editor.remove(KEY_PINNED_TRAFFIC_SIGNS_DETAILS);
        editor.apply();
    }
    
    /**
     * Lưu thông tin chi tiết về biển báo đã ghim
     * @param pinnedSign Biển báo đã ghim
     */
    public void savePinnedTrafficSign(TrafficSign pinnedSign) {
        if (pinnedSign == null) return;
        
        // Lấy danh sách biển báo đã ghim hiện tại
        List<TrafficSign> pinnedSigns = getPinnedTrafficSignDetails();
        
        // Kiểm tra nếu biển báo đã tồn tại trong danh sách
        boolean exists = false;
        for (int i = 0; i < pinnedSigns.size(); i++) {
            TrafficSign sign = pinnedSigns.get(i);
            // So sánh theo tên và loại biển báo, không chỉ theo ID
            if (sign.getName() != null && sign.getName().equals(pinnedSign.getName()) &&
                sign.getCategory() != null && sign.getCategory().equals(pinnedSign.getCategory())) {
                // Cập nhật lại biển báo có sẵn
                pinnedSigns.set(i, pinnedSign);
                exists = true;
                break;
            }
        }
        
        // Nếu biển báo chưa tồn tại, thêm vào danh sách
        if (!exists) {
            pinnedSigns.add(pinnedSign);
        }
        
        // Lưu danh sách cập nhật vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(pinnedSigns);
        editor.putString(KEY_PINNED_TRAFFIC_SIGNS_DETAILS, json);
        editor.apply();
        
        // Đồng thời cập nhật Set ID cũ để đảm bảo tính tương thích
        Set<String> pinnedIds = getPinnedTrafficSignIds();
        pinnedIds.add(pinnedSign.getId());
        savePinnedTrafficSignIds(pinnedIds);
        
        Log.d(TAG, "Đã lưu biển báo đã ghim: " + pinnedSign.getName() + " (ID: " + pinnedSign.getId() + ")");
    }
    
    /**
     * Xóa thông tin chi tiết về biển báo đã ghim
     * @param unpinnedSign Biển báo cần bỏ ghim
     */
    public void removePinnedTrafficSign(TrafficSign unpinnedSign) {
        if (unpinnedSign == null) return;
        
        // Lấy danh sách biển báo đã ghim hiện tại
        List<TrafficSign> pinnedSigns = getPinnedTrafficSignDetails();
        
        // Kiểm tra và xóa biển báo khỏi danh sách
        boolean removed = false;
        for (int i = 0; i < pinnedSigns.size(); i++) {
            TrafficSign sign = pinnedSigns.get(i);
            // So sánh theo tên và loại biển báo, không chỉ theo ID
            if (sign.getName() != null && sign.getName().equals(unpinnedSign.getName()) &&
                sign.getCategory() != null && sign.getCategory().equals(unpinnedSign.getCategory())) {
                pinnedSigns.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            // Lưu danh sách cập nhật vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String json = gson.toJson(pinnedSigns);
            editor.putString(KEY_PINNED_TRAFFIC_SIGNS_DETAILS, json);
            editor.apply();
            
            // Đồng thời cập nhật Set ID cũ để đảm bảo tính tương thích
            Set<String> pinnedIds = getPinnedTrafficSignIds();
            pinnedIds.remove(unpinnedSign.getId());
            savePinnedTrafficSignIds(pinnedIds);
            
            Log.d(TAG, "Đã xóa biển báo ghim: " + unpinnedSign.getName() + " (ID: " + unpinnedSign.getId() + ")");
        }
    }
    
    /**
     * Lấy danh sách chi tiết các biển báo đã ghim
     * @return Danh sách các biển báo đã ghim
     */
    public List<TrafficSign> getPinnedTrafficSignDetails() {
        String json = sharedPreferences.getString(KEY_PINNED_TRAFFIC_SIGNS_DETAILS, "");
        
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<TrafficSign>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đọc danh sách biển báo đã ghim: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Thêm một biển báo vào danh sách đã ghim (phương thức cũ, dùng ID)
     * @param signId ID của biển báo cần ghim
     */
    public void addPinnedSign(String signId) {
        Set<String> pinnedSignIds = getPinnedTrafficSignIds();
        pinnedSignIds.add(signId);
        savePinnedTrafficSignIds(pinnedSignIds);
    }
    
    /**
     * Xóa một biển báo khỏi danh sách đã ghim (phương thức cũ, dùng ID)
     * @param signId ID của biển báo cần bỏ ghim
     */
    public void removePinnedSign(String signId) {
        Set<String> pinnedSignIds = getPinnedTrafficSignIds();
        pinnedSignIds.remove(signId);
        savePinnedTrafficSignIds(pinnedSignIds);
    }
    
    /**
     * Lưu trạng thái chế độ tối
     * @param isDarkMode true nếu đang ở chế độ tối, false nếu đang ở chế độ sáng
     */
    public void setDarkMode(boolean isDarkMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
    }
    
    /**
     * Lấy trạng thái chế độ tối
     * @return true nếu đang ở chế độ tối, false nếu đang ở chế độ sáng
     */
    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Lưu trạng thái bật/tắt nhắc nhở học hàng ngày
     * @param isEnabled true nếu đã bật, false nếu đã tắt
     */
    public void setDailyReminderEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DAILY_REMINDER_ENABLED, isEnabled);
        editor.apply();
    }
    
    /**
     * Lấy trạng thái bật/tắt nhắc nhở học hàng ngày
     * @return true nếu đã bật, false nếu đã tắt
     */
    public boolean isDailyReminderEnabled() {
        return sharedPreferences.getBoolean(KEY_DAILY_REMINDER_ENABLED, false);
    }
    
    /**
     * Lưu thời gian nhắc nhở hàng ngày
     * @param hour Giờ (0-23)
     * @param minute Phút (0-59)
     */
    public void setReminderTime(int hour, int minute) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_REMINDER_HOUR, hour);
        editor.putInt(KEY_REMINDER_MINUTE, minute);
        editor.apply();
    }
    
    /**
     * Lấy giờ nhắc nhở
     * @return Giờ nhắc nhở (0-23), mặc định là 20 (8 giờ tối)
     */
    public int getReminderHour() {
        return sharedPreferences.getInt(KEY_REMINDER_HOUR, 20);
    }
    
    /**
     * Lấy phút nhắc nhở
     * @return Phút nhắc nhở (0-59), mặc định là 0
     */
    public int getReminderMinute() {
        return sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0);
    }
} 