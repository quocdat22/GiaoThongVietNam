package com.example.giaothong.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lớp quản lý lịch sử tìm kiếm sử dụng SharedPreferences
 */
public class SearchHistoryManager {
    private static final String PREFS_NAME = "search_history_prefs";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final int MAX_HISTORY_ITEMS = 10;
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public SearchHistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Lấy danh sách lịch sử tìm kiếm
     * @return Danh sách lịch sử tìm kiếm
     */
    public List<String> getSearchHistory() {
        String json = prefs.getString(KEY_SEARCH_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> history = gson.fromJson(json, type);
        
        return history != null ? history : new ArrayList<>();
    }
    
    /**
     * Thêm từ khóa vào lịch sử tìm kiếm
     * @param query Từ khóa tìm kiếm
     */
    public void addSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        // Chuẩn hóa từ khóa
        query = query.trim();
        
        // Lấy lịch sử hiện tại
        List<String> history = getSearchHistory();
        
        // Sử dụng LinkedHashSet để loại bỏ trùng lặp nhưng giữ thứ tự
        LinkedHashSet<String> historySet = new LinkedHashSet<>(history);
        
        // Nếu từ khóa đã tồn tại, xóa nó để thêm lại vào đầu danh sách
        historySet.remove(query);
        
        // Thêm từ khóa vào đầu danh sách
        List<String> newHistory = new ArrayList<>();
        newHistory.add(query);
        newHistory.addAll(historySet);
        
        // Giới hạn số lượng từ khóa
        if (newHistory.size() > MAX_HISTORY_ITEMS) {
            newHistory = newHistory.subList(0, MAX_HISTORY_ITEMS);
        }
        
        // Lưu lại lịch sử
        String json = gson.toJson(newHistory);
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply();
    }
    
    /**
     * Xóa một từ khóa khỏi lịch sử tìm kiếm
     * @param query Từ khóa cần xóa
     */
    public void removeSearchQuery(String query) {
        List<String> history = getSearchHistory();
        if (history.remove(query)) {
            String json = gson.toJson(history);
            prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply();
        }
    }
    
    /**
     * Xóa toàn bộ lịch sử tìm kiếm
     */
    public void clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply();
    }
} 