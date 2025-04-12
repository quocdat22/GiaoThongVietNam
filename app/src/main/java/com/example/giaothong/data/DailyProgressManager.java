package com.example.giaothong.data;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Lớp quản lý tiến độ học tập hàng ngày của người dùng
 */
public class DailyProgressManager {
    private static final String TAG = "DailyProgressManager";
    private static final String PREF_DAILY_PROGRESS = "daily_progress";
    private static final String PREF_LAST_ACCESS_DATE = "last_access_date";
    private static final String PREF_DAILY_GOAL = "daily_goal";
    private static final int DEFAULT_DAILY_GOAL = 20; // Mục tiêu mặc định: 20 biển báo
    
    private final SharedPreferences preferences;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;
    
    public DailyProgressManager(SharedPreferences preferences) {
        this.preferences = preferences;
        this.gson = new Gson();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Kiểm tra và cập nhật ngày
        checkAndUpdateDate();
    }
    
    /**
     * Lấy ngày hiện tại dưới dạng chuỗi
     */
    private String getCurrentDateAsString() {
        return dateFormat.format(new Date());
    }
    
    /**
     * Kiểm tra và cập nhật ngày truy cập cuối cùng
     * Nếu ngày đã thay đổi, thực hiện cập nhật dữ liệu
     */
    private void checkAndUpdateDate() {
        String currentDate = getCurrentDateAsString();
        String lastAccessDate = preferences.getString(PREF_LAST_ACCESS_DATE, null);
        
        if (lastAccessDate == null || !lastAccessDate.equals(currentDate)) {
            // Lưu ngày truy cập mới
            preferences.edit().putString(PREF_LAST_ACCESS_DATE, currentDate).apply();
            
            // Bạn có thể thêm logic khác ở đây nếu cần
            // Ví dụ: reset một số thống kê hàng ngày, gửi notification, v.v.
        }
    }
    
    /**
     * Lấy mục tiêu học tập hàng ngày của người dùng
     * @return Số lượng biển báo cần học mỗi ngày
     */
    public int getDailyGoal() {
        return preferences.getInt(PREF_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }
    
    /**
     * Cập nhật mục tiêu học tập hàng ngày
     * @param goal Số lượng biển báo mục tiêu mỗi ngày
     */
    public void setDailyGoal(int goal) {
        if (goal > 0) {
            preferences.edit().putInt(PREF_DAILY_GOAL, goal).apply();
        }
    }
    
    /**
     * Lưu thông tin học tập về một biển báo đã học
     * @param signId ID của biển báo
     * @param isCorrect true nếu người dùng trả lời đúng, false nếu sai
     */
    public void trackLearnedSign(String signId, boolean isCorrect) {
        // Lấy dữ liệu hiện tại
        DailyProgress progress = getDailyProgress();
        String currentDate = getCurrentDateAsString();
        
        // Kiểm tra xem đã có dữ liệu cho ngày hiện tại chưa
        Map<String, Boolean> dailyData = progress.getProgressByDate(currentDate);
        if (dailyData == null) {
            dailyData = new HashMap<>();
            progress.addDateProgress(currentDate, dailyData);
        }
        
        // Cập nhật thông tin học tập
        dailyData.put(signId, isCorrect);
        
        // Lưu lại
        saveDailyProgress(progress);
    }
    
    /**
     * Lấy số lượng biển báo đã học hôm nay
     * @return Số lượng đã học
     */
    public int getLearnedSignsToday() {
        String currentDate = getCurrentDateAsString();
        DailyProgress progress = getDailyProgress();
        
        Map<String, Boolean> dailyData = progress.getProgressByDate(currentDate);
        if (dailyData == null) {
            return 0;
        }
        
        return dailyData.size();
    }
    
    /**
     * Tính độ chính xác học tập trong ngày
     * @return Tỷ lệ % trả lời đúng (0-100)
     */
    public int getAccuracyToday() {
        String currentDate = getCurrentDateAsString();
        DailyProgress progress = getDailyProgress();
        
        Map<String, Boolean> dailyData = progress.getProgressByDate(currentDate);
        if (dailyData == null || dailyData.isEmpty()) {
            return 0;
        }
        
        int correctCount = 0;
        for (Boolean isCorrect : dailyData.values()) {
            if (isCorrect) {
                correctCount++;
            }
        }
        
        return (correctCount * 100) / dailyData.size();
    }
    
    /**
     * Tính phần trăm hoàn thành mục tiêu hôm nay
     * @return Tỷ lệ % hoàn thành (0-100)
     */
    public int getProgressPercentToday() {
        int learned = getLearnedSignsToday();
        int goal = getDailyGoal();
        
        // Nếu đã học nhiều hơn mục tiêu, trả về 100%
        if (learned >= goal) {
            return 100;
        }
        
        return (learned * 100) / goal;
    }
    
    /**
     * Lấy ngày hiện tại dưới định dạng đọc được
     * @return Chuỗi ngày tháng theo định dạng dd/MM/yyyy
     */
    public String getFormattedCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatter.format(new Date());
    }
    
    /**
     * Lấy dữ liệu tiến độ hàng ngày từ SharedPreferences
     */
    private DailyProgress getDailyProgress() {
        String json = preferences.getString(PREF_DAILY_PROGRESS, null);
        if (json == null) {
            return new DailyProgress();
        }
        
        try {
            Type type = new TypeToken<DailyProgress>() {}.getType();
            DailyProgress progress = gson.fromJson(json, type);
            return progress != null ? progress : new DailyProgress();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing daily progress: " + e.getMessage(), e);
            return new DailyProgress();
        }
    }
    
    /**
     * Lưu dữ liệu tiến độ hàng ngày vào SharedPreferences
     */
    private void saveDailyProgress(DailyProgress progress) {
        String json = gson.toJson(progress);
        preferences.edit().putString(PREF_DAILY_PROGRESS, json).apply();
    }
    
    /**
     * Lớp đại diện cho dữ liệu tiến độ học tập hàng ngày
     */
    private static class DailyProgress {
        private final Map<String, Map<String, Boolean>> progressByDate;
        
        public DailyProgress() {
            progressByDate = new HashMap<>();
        }
        
        public Map<String, Boolean> getProgressByDate(String date) {
            return progressByDate.get(date);
        }
        
        public void addDateProgress(String date, Map<String, Boolean> progress) {
            progressByDate.put(date, progress);
        }
    }
} 