package com.example.giaothong.utils;

import android.content.Context;
import android.util.Log;

import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Singleton class to manage data in the application
 */
public class DataManager {
    
    private static final String TAG = "DataManager";
    private static DataManager instance;
    private List<TrafficSign> trafficSigns;
    private TrafficSignRepository repository;
    private List<Consumer<List<TrafficSign>>> dataListeners;
    private Context context;
    
    private DataManager(Context context) {
        // Private constructor to prevent direct instantiation
        this.context = context.getApplicationContext();
        trafficSigns = new ArrayList<>();
        repository = TrafficSignRepository.getInstance(this.context);
        dataListeners = new ArrayList<>();
        
        // Tải dữ liệu từ API
        loadTrafficSignsFromApi();
    }
    
    /**
     * Get the singleton instance
     * @return the DataManager instance
     */
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }
    
    /**
     * Tải dữ liệu biển báo từ API
     */
    private void loadTrafficSignsFromApi() {
        Log.d(TAG, "Bắt đầu tải dữ liệu biển báo từ API");
        repository.getTrafficSigns(signs -> {
            Log.d(TAG, "Đã nhận dữ liệu từ API: " + (signs != null ? signs.size() : 0) + " biển báo");
            trafficSigns = signs;
            
            // Thông báo cho tất cả listeners về dữ liệu mới
            notifyDataChanged();
        });
    }
    
    /**
     * Thông báo cho tất cả listeners rằng dữ liệu đã thay đổi
     */
    private void notifyDataChanged() {
        Log.d(TAG, "Thông báo cho " + dataListeners.size() + " listeners về dữ liệu mới");
        for (Consumer<List<TrafficSign>> listener : dataListeners) {
            try {
                listener.accept(trafficSigns);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi thông báo cho listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Đăng ký một listener để nhận thông báo khi dữ liệu thay đổi
     * @param listener Listener sẽ được gọi khi dữ liệu thay đổi
     */
    public void registerDataListener(Consumer<List<TrafficSign>> listener) {
        if (!dataListeners.contains(listener)) {
            dataListeners.add(listener);
            
            // Gọi listener ngay lập tức nếu đã có dữ liệu
            if (trafficSigns != null && !trafficSigns.isEmpty()) {
                listener.accept(trafficSigns);
            }
        }
    }
    
    /**
     * Hủy đăng ký một listener
     * @param listener Listener cần hủy đăng ký
     */
    public void unregisterDataListener(Consumer<List<TrafficSign>> listener) {
        dataListeners.remove(listener);
    }
    
    /**
     * Làm mới dữ liệu biển báo từ API
     */
    public void refreshTrafficSigns() {
        loadTrafficSignsFromApi();
    }
    
    /**
     * Get all traffic signs
     * @return List of all traffic signs
     */
    public List<TrafficSign> getAllTrafficSigns() {
        return trafficSigns;
    }
    
    /**
     * Update the list of traffic signs
     * @param trafficSigns The new list of traffic signs
     */
    public void setTrafficSigns(List<TrafficSign> trafficSigns) {
        this.trafficSigns = trafficSigns;
        notifyDataChanged();
    }
} 