package com.example.giaothong.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Quản lý lưu trữ dữ liệu sử dụng SharedPreferences
 */
public class SharedPreferencesManager {
    
    private static final String PREFS_NAME = "com.example.giaothong.preferences";
    private static final String KEY_PINNED_TRAFFIC_SIGNS = "pinned_traffic_signs";
    
    private final SharedPreferences sharedPreferences;
    
    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        editor.apply();
    }
    
    /**
     * Thêm một biển báo vào danh sách đã ghim
     * @param signId ID của biển báo cần ghim
     */
    public void addPinnedSign(String signId) {
        Set<String> pinnedSignIds = getPinnedTrafficSignIds();
        pinnedSignIds.add(signId);
        savePinnedTrafficSignIds(pinnedSignIds);
    }
    
    /**
     * Xóa một biển báo khỏi danh sách đã ghim
     * @param signId ID của biển báo cần bỏ ghim
     */
    public void removePinnedSign(String signId) {
        Set<String> pinnedSignIds = getPinnedTrafficSignIds();
        pinnedSignIds.remove(signId);
        savePinnedTrafficSignIds(pinnedSignIds);
    }
} 