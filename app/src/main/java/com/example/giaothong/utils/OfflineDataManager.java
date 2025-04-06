package com.example.giaothong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.giaothong.model.TrafficSign;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp quản lý dữ liệu ngoại tuyến cho ứng dụng
 */
public class OfflineDataManager {
    private static final String TAG = "OfflineDataManager";
    private static final String PREFS_NAME = "offline_preferences";
    private static final String KEY_OFFLINE_ENABLED = "offline_enabled";
    private static final String KEY_LAST_DOWNLOAD_TIME = "last_download_time";
    private static final String OFFLINE_DATA_FILENAME = "traffic_signs_data.json";
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    
    public OfflineDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Kiểm tra xem chế độ ngoại tuyến có được bật không
     * @return true nếu chế độ ngoại tuyến được bật
     */
    public boolean isOfflineEnabled() {
        return preferences.getBoolean(KEY_OFFLINE_ENABLED, true);
    }
    
    /**
     * Bật/tắt chế độ ngoại tuyến
     * @param enabled true để bật, false để tắt
     */
    public void setOfflineEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_OFFLINE_ENABLED, enabled).apply();
        Log.d(TAG, "Chế độ ngoại tuyến: " + (enabled ? "BẬT" : "TẮT"));
    }
    
    /**
     * Lưu danh sách biển báo để sử dụng ngoại tuyến
     * @param signs Danh sách biển báo cần lưu
     * @return true nếu lưu thành công
     */
    public boolean saveTrafficSignsOffline(List<TrafficSign> signs) {
        if (signs == null || signs.isEmpty()) {
            Log.e(TAG, "Không thể lưu danh sách rỗng");
            return false;
        }
        
        try {
            // Tạo JSON từ danh sách biển báo
            String jsonData = gson.toJson(signs);
            
            // Lưu vào file
            File dataFile = new File(context.getFilesDir(), OFFLINE_DATA_FILENAME);
            FileWriter writer = new FileWriter(dataFile);
            writer.write(jsonData);
            writer.close();
            
            // Lưu thời gian tải cuối cùng
            preferences.edit().putLong(KEY_LAST_DOWNLOAD_TIME, System.currentTimeMillis()).apply();
            
            Log.d(TAG, "Đã lưu " + signs.size() + " biển báo để sử dụng ngoại tuyến");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi lưu dữ liệu ngoại tuyến", e);
            return false;
        }
    }
    
    /**
     * Lấy danh sách biển báo từ bộ nhớ ngoại tuyến
     * @return Danh sách biển báo hoặc null nếu không có
     */
    public List<TrafficSign> getOfflineTrafficSigns() {
        File dataFile = new File(context.getFilesDir(), OFFLINE_DATA_FILENAME);
        if (!dataFile.exists()) {
            Log.d(TAG, "Không tìm thấy file dữ liệu ngoại tuyến");
            return null;
        }
        
        try {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            
            // Chuyển JSON thành danh sách biển báo
            Type listType = new TypeToken<ArrayList<TrafficSign>>(){}.getType();
            List<TrafficSign> signs = gson.fromJson(content.toString(), listType);
            
            Log.d(TAG, "Đã tải " + (signs != null ? signs.size() : 0) + " biển báo từ bộ nhớ ngoại tuyến");
            return signs;
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi đọc dữ liệu ngoại tuyến", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý dữ liệu ngoại tuyến", e);
            return null;
        }
    }
    
    /**
     * Lấy thời gian tải xuống cuối cùng theo định dạng dễ đọc
     * @return Chuỗi thời gian đã định dạng
     */
    public String getLastDownloadTimeFormatted() {
        long lastDownloadTime = preferences.getLong(KEY_LAST_DOWNLOAD_TIME, 0);
        if (lastDownloadTime == 0) {
            return "Chưa tải dữ liệu";
        }
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return formatter.format(new Date(lastDownloadTime));
    }
    
    /**
     * Kiểm tra xem có dữ liệu ngoại tuyến không
     * @return true nếu có dữ liệu ngoại tuyến
     */
    public boolean hasOfflineData() {
        File dataFile = new File(context.getFilesDir(), OFFLINE_DATA_FILENAME);
        return dataFile.exists() && dataFile.length() > 0;
    }
    
    /**
     * Kiểm tra kết nối mạng
     * @return true nếu có kết nối mạng
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * Xóa dữ liệu ngoại tuyến
     * @return true nếu xóa thành công
     */
    public boolean clearOfflineData() {
        File dataFile = new File(context.getFilesDir(), OFFLINE_DATA_FILENAME);
        boolean deleted = true;
        
        if (dataFile.exists()) {
            deleted = dataFile.delete();
        }
        
        // Xóa thời gian tải
        preferences.edit().remove(KEY_LAST_DOWNLOAD_TIME).apply();
        
        Log.d(TAG, "Xóa dữ liệu ngoại tuyến: " + (deleted ? "Thành công" : "Thất bại"));
        return deleted;
    }
} 