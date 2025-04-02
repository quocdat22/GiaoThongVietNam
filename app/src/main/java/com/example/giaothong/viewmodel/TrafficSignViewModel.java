package com.example.giaothong.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel để quản lý dữ liệu biển báo
 */
public class TrafficSignViewModel extends ViewModel {
    
    // LiveData gốc (dữ liệu đầy đủ từ repository)
    private final MutableLiveData<List<TrafficSign>> originalTrafficSigns = new MutableLiveData<>();
    
    // LiveData đã lọc (được hiển thị cho người dùng)
    private final MediatorLiveData<List<TrafficSign>> filteredTrafficSigns = new MediatorLiveData<>();
    
    // Danh mục hiện tại được chọn để lọc
    private String currentCategory = "";
    
    private final TrafficSignRepository repository;
    
    public TrafficSignViewModel() {
        repository = new TrafficSignRepository();
        
        // Thêm source cho MediatorLiveData
        filteredTrafficSigns.addSource(originalTrafficSigns, trafficSigns -> {
            applyFilter(currentCategory);
        });
        
        loadTrafficSigns();
    }
    
    /**
     * Tải dữ liệu biển báo từ repository
     */
    private void loadTrafficSigns() {
        repository.getTrafficSigns(originalTrafficSigns);
    }
    
    /**
     * Lấy LiveData đối tượng chứa danh sách biển báo đã lọc
     */
    public LiveData<List<TrafficSign>> getTrafficSigns() {
        return filteredTrafficSigns;
    }
    
    /**
     * Làm mới dữ liệu biển báo
     */
    public void refreshTrafficSigns() {
        loadTrafficSigns();
    }
    
    /**
     * Thiết lập bộ lọc danh mục
     * @param category Danh mục cần lọc, để trống để hiển thị tất cả
     */
    public void setCategory(String category) {
        if (category == null) {
            category = "";
        }
        
        currentCategory = category;
        applyFilter(currentCategory);
    }
    
    /**
     * Áp dụng bộ lọc vào danh sách biển báo
     * @param category Danh mục cần lọc
     */
    private void applyFilter(String category) {
        List<TrafficSign> allSigns = originalTrafficSigns.getValue();
        
        if (allSigns == null) {
            return;
        }
        
        // Nếu không có danh mục, hiển thị tất cả
        if (category.isEmpty()) {
            filteredTrafficSigns.setValue(allSigns);
            return;
        }
        
        // Lọc theo danh mục
        List<TrafficSign> filtered = new ArrayList<>();
        for (TrafficSign sign : allSigns) {
            if (sign.getCategory().equals(category)) {
                filtered.add(sign);
            }
        }
        
        filteredTrafficSigns.setValue(filtered);
    }
} 