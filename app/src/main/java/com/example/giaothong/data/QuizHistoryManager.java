package com.example.giaothong.data;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.giaothong.model.Quiz;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp quản lý lịch sử các bài trắc nghiệm đã làm
 */
public class QuizHistoryManager {
    private static final String TAG = "QuizHistoryManager";
    private static final String PREF_QUIZ_HISTORY = "quiz_history";
    private static final int MAX_HISTORY_SIZE = 20;
    
    private final SharedPreferences preferences;
    private final Gson gson;
    
    public QuizHistoryManager(SharedPreferences preferences) {
        this.preferences = preferences;
        this.gson = new Gson();
    }
    
    /**
     * Lưu bài trắc nghiệm vào lịch sử
     * @param quiz Bài trắc nghiệm đã hoàn thành
     */
    public void saveQuiz(Quiz quiz) {
        if (quiz == null || !quiz.isCompleted()) {
            return;
        }
        
        // Lấy lịch sử hiện tại
        List<Quiz> quizHistory = getQuizHistory();
        
        // Kiểm tra xem đã tồn tại chưa (dựa trên ID)
        for (int i = 0; i < quizHistory.size(); i++) {
            if (quizHistory.get(i).getId().equals(quiz.getId())) {
                quizHistory.remove(i);
                break;
            }
        }
        
        // Thêm vào đầu danh sách
        quizHistory.add(0, quiz);
        
        // Giới hạn kích thước
        if (quizHistory.size() > MAX_HISTORY_SIZE) {
            quizHistory = quizHistory.subList(0, MAX_HISTORY_SIZE);
        }
        
        // Lưu lại
        saveQuizHistory(quizHistory);
    }
    
    /**
     * Lấy danh sách lịch sử bài trắc nghiệm đã làm
     * @return Danh sách lịch sử
     */
    public List<Quiz> getQuizHistory() {
        String json = preferences.getString(PREF_QUIZ_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<Quiz>>() {}.getType();
            List<Quiz> quizHistory = gson.fromJson(json, type);
            return quizHistory != null ? quizHistory : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing quiz history: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Xóa một bài trắc nghiệm khỏi lịch sử
     * @param quizId ID của bài trắc nghiệm cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    public boolean removeQuiz(String quizId) {
        // Lấy lịch sử hiện tại
        List<Quiz> quizHistory = getQuizHistory();
        
        // Tìm và xóa
        boolean found = false;
        for (int i = 0; i < quizHistory.size(); i++) {
            if (quizHistory.get(i).getId().equals(quizId)) {
                quizHistory.remove(i);
                found = true;
                break;
            }
        }
        
        // Nếu tìm thấy, lưu lại danh sách mới
        if (found) {
            saveQuizHistory(quizHistory);
        }
        
        return found;
    }
    
    /**
     * Xóa toàn bộ lịch sử
     */
    public void clearHistory() {
        preferences.edit().remove(PREF_QUIZ_HISTORY).apply();
    }
    
    /**
     * Lưu danh sách lịch sử vào SharedPreferences
     * @param quizHistory Danh sách cần lưu
     */
    private void saveQuizHistory(List<Quiz> quizHistory) {
        String json = gson.toJson(quizHistory);
        preferences.edit().putString(PREF_QUIZ_HISTORY, json).apply();
    }
} 