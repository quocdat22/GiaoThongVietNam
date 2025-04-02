package com.example.giaothong.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    // Từ khóa tìm kiếm hiện tại
    private String currentSearchQuery = "";
    
    private final TrafficSignRepository repository;
    
    public TrafficSignViewModel() {
        repository = new TrafficSignRepository();
        
        // Thêm source cho MediatorLiveData
        filteredTrafficSigns.addSource(originalTrafficSigns, trafficSigns -> {
            applyFilters(currentCategory, currentSearchQuery);
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
        applyFilters(currentCategory, currentSearchQuery);
    }
    
    /**
     * Thiết lập từ khóa tìm kiếm
     * @param query Từ khóa tìm kiếm, để trống để hiển thị tất cả
     */
    public void setSearchQuery(String query) {
        if (query == null) {
            query = "";
        }
        
        currentSearchQuery = query.toLowerCase(Locale.getDefault());
        applyFilters(currentCategory, currentSearchQuery);
    }
    
    /**
     * Áp dụng bộ lọc vào danh sách biển báo
     * @param category Danh mục cần lọc
     * @param searchQuery Từ khóa tìm kiếm
     */
    private void applyFilters(String category, String searchQuery) {
        List<TrafficSign> allSigns = originalTrafficSigns.getValue();
        
        if (allSigns == null) {
            return;
        }
        
        // Tạo danh sách kết quả
        List<TrafficSign> filtered = new ArrayList<>();
        
        // Nếu không có điều kiện lọc, hiển thị tất cả
        if (category.isEmpty() && searchQuery.isEmpty()) {
            filteredTrafficSigns.setValue(allSigns);
            return;
        }
        
        // Lọc dữ liệu
        for (TrafficSign sign : allSigns) {
            boolean matchesCategory = category.isEmpty() || sign.getCategory().equals(category);
            boolean matchesQuery = searchQuery.isEmpty() || 
                                  sign.getName().toLowerCase(Locale.getDefault()).contains(searchQuery) ||
                                  sign.getDescription().toLowerCase(Locale.getDefault()).contains(searchQuery);
            
            if (matchesCategory && matchesQuery) {
                filtered.add(sign);
            }
        }
        
        filteredTrafficSigns.setValue(filtered);
    }
} 