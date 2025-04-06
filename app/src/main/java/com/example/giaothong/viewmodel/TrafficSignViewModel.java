package com.example.giaothong.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * ViewModel cho màn hình danh sách biển báo giao thông
 * Quản lý dữ liệu và nghiệp vụ lọc, tìm kiếm
 */
public class TrafficSignViewModel extends AndroidViewModel {
    
    // LiveData chứa danh sách biển báo gốc (chưa lọc)
    private final MutableLiveData<List<TrafficSign>> originalTrafficSigns = new MutableLiveData<>();
    
    // LiveData chứa danh sách biển báo đã lọc để hiển thị
    private final MediatorLiveData<List<TrafficSign>> filteredTrafficSigns = new MediatorLiveData<>();
    
    // Danh mục đang lọc hiện tại
    private String currentCategory = "";
    
    // Từ khóa tìm kiếm hiện tại
    private String currentSearchQuery = "";
    
    // Có đang hiển thị chỉ các biển báo đã ghim không
    private boolean showOnlyPinned = false;
    
    // Set lưu trữ ID của các biển báo đã ghim
    private Set<String> pinnedSignIds = new HashSet<>();
    
    private final TrafficSignRepository repository;
    private SharedPreferencesManager prefsManager;
    
    // Khởi tạo ViewModel
    public TrafficSignViewModel(@NonNull Application application) {
        super(application);
        repository = TrafficSignRepository.getInstance(application);
        
        // Thêm source cho MediatorLiveData
        filteredTrafficSigns.addSource(originalTrafficSigns, trafficSigns -> {
            applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
        });
        
        loadTrafficSigns();
    }
    
    public void setPreferencesManager(SharedPreferencesManager manager) {
        this.prefsManager = manager;
        
        // Lấy danh sách ID đã ghim (tương thích ngược)
        pinnedSignIds = prefsManager.getPinnedTrafficSignIds();
        
        // Lấy danh sách chi tiết các biển báo đã ghim
        List<TrafficSign> pinnedSigns = prefsManager.getPinnedTrafficSignDetails();
        
        // Áp dụng trạng thái ghim lên dữ liệu gốc nếu có
        List<TrafficSign> currentSigns = originalTrafficSigns.getValue();
        if (currentSigns != null && !currentSigns.isEmpty()) {
            // Đầu tiên thử áp dụng từ danh sách chi tiết (chính xác hơn)
            for (TrafficSign currentSign : currentSigns) {
                // Mặc định không ghim
                currentSign.setPinned(false);
                
                // Kiểm tra nếu có trong danh sách ghim chi tiết
                for (TrafficSign pinnedSign : pinnedSigns) {
                    // So sánh theo tên và loại, không chỉ theo ID
                    if (currentSign.getName() != null && pinnedSign.getName() != null &&
                        currentSign.getName().equals(pinnedSign.getName()) &&
                        currentSign.getCategory() != null && pinnedSign.getCategory() != null &&
                        currentSign.getCategory().equals(pinnedSign.getCategory())) {
                        currentSign.setPinned(true);
                        break;
                    }
                }
            }
            
            // Sau đó thử áp dụng từ ID đã ghim (tương thích ngược)
            if (pinnedSigns.isEmpty() && !pinnedSignIds.isEmpty()) {
                updatePinnedStatus(currentSigns);
            }
            
            // Cập nhật danh sách
            originalTrafficSigns.setValue(currentSigns);
        }
    }
    
    /**
     * Tải dữ liệu biển báo từ repository
     */
    private void loadTrafficSigns() {
        repository.getTrafficSigns(signs -> {
            // Áp dụng trạng thái ghim lên dữ liệu mới
            if (signs != null && !pinnedSignIds.isEmpty()) {
                updatePinnedStatus(signs);
            }
            originalTrafficSigns.setValue(signs);
        });
    }
    
    /**
     * Cập nhật trạng thái ghim cho danh sách biển báo dựa trên pinnedSignIds
     */
    private void updatePinnedStatus(List<TrafficSign> signs) {
        for (TrafficSign sign : signs) {
            sign.setPinned(pinnedSignIds.contains(sign.getId()));
        }
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
        applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
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
        applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
    }
    
    /**
     * Đặt chế độ chỉ hiển thị biển báo đã ghim
     * @param showPinned true để chỉ hiển thị biển báo đã ghim, false để hiển thị tất cả
     */
    public void setShowOnlyPinned(boolean showPinned) {
        showOnlyPinned = showPinned;
        applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
    }
    
    /**
     * Đảo trạng thái ghim của biển báo
     * @param sign Biển báo cần đảo trạng thái
     */
    public void togglePinStatus(TrafficSign sign) {
        // Đảo trạng thái ghim
        sign.togglePinned();
        
        // Cập nhật danh sách ID đã ghim
        if (sign.isPinned()) {
            pinnedSignIds.add(sign.getId());
            // Lưu thông tin chi tiết biển báo đã ghim
            if (prefsManager != null) {
                prefsManager.savePinnedTrafficSign(sign);
            }
        } else {
            pinnedSignIds.remove(sign.getId());
            // Xóa thông tin chi tiết biển báo
            if (prefsManager != null) {
                prefsManager.removePinnedTrafficSign(sign);
            }
        }
        
        // Lưu thay đổi ID vào SharedPreferences (cho khả năng tương thích ngược)
        if (prefsManager != null) {
            prefsManager.savePinnedTrafficSignIds(pinnedSignIds);
        }
        
        // Cập nhật lại danh sách gốc để áp dụng trạng thái mới
        List<TrafficSign> currentSigns = originalTrafficSigns.getValue();
        if (currentSigns != null) {
            originalTrafficSigns.setValue(currentSigns);
        }
    }
    
    /**
     * Xóa tất cả ghim
     */
    public void clearAllPins() {
        // Xóa tất cả ID đã ghim
        pinnedSignIds.clear();
        
        // Lưu thay đổi vào SharedPreferences
        if (prefsManager != null) {
            prefsManager.clearPinnedTrafficSigns();
        }
        
        // Cập nhật trạng thái ghim cho tất cả biển báo hiện tại
        List<TrafficSign> currentSigns = originalTrafficSigns.getValue();
        if (currentSigns != null) {
            for (TrafficSign sign : currentSigns) {
                sign.setPinned(false);
            }
            originalTrafficSigns.setValue(currentSigns);
        }
    }
    
    /**
     * Áp dụng bộ lọc vào danh sách biển báo
     * @param category Danh mục cần lọc
     * @param searchQuery Từ khóa tìm kiếm
     * @param onlyPinned Chỉ hiển thị đã ghim
     */
    private void applyFilters(String category, String searchQuery, boolean onlyPinned) {
        List<TrafficSign> allSigns = originalTrafficSigns.getValue();
        
        if (allSigns == null) {
            return;
        }
        
        // Tạo danh sách kết quả
        List<TrafficSign> filtered = new ArrayList<>();
        
        // Lọc dữ liệu
        for (TrafficSign sign : allSigns) {
            boolean matchesCategory = category.isEmpty() || sign.getCategory().equals(category);
            boolean matchesQuery = searchQuery.isEmpty() || 
                                  sign.getName().toLowerCase(Locale.getDefault()).contains(searchQuery) ||
                                  sign.getDescription().toLowerCase(Locale.getDefault()).contains(searchQuery);
            boolean matchesPinned = !onlyPinned || sign.isPinned();
            
            if (matchesCategory && matchesQuery && matchesPinned) {
                filtered.add(sign);
            }
        }
        
        filteredTrafficSigns.setValue(filtered);
    }
    
    /**
     * Cập nhật trạng thái ghim của một biển báo
     * @param trafficSign Biển báo cần cập nhật
     * @param isPinned Trạng thái ghim mới
     */
    public void updatePinStatus(TrafficSign trafficSign, boolean isPinned) {
        // Cập nhật trạng thái ghim cho biển báo
        trafficSign.setPinned(isPinned);
        
        // Tìm và cập nhật biển báo trong danh sách gốc
        if (originalTrafficSigns.getValue() != null) {
            boolean foundInList = false;
            for (TrafficSign sign : originalTrafficSigns.getValue()) {
                // Kiểm tra khớp theo tên và loại, không chỉ ID
                if (sign.getName().equals(trafficSign.getName()) && 
                    sign.getCategory().equals(trafficSign.getCategory())) {
                    sign.setPinned(isPinned);
                    foundInList = true;
                    break;
                }
            }
            
            // Nếu không tìm thấy biển báo có tên và loại khớp, thử tìm theo ID (tương thích ngược)
            if (!foundInList) {
                for (TrafficSign sign : originalTrafficSigns.getValue()) {
                    if (sign.getId().equals(trafficSign.getId())) {
                        sign.setPinned(isPinned);
                        break;
                    }
                }
            }
        }
        
        // Cập nhật danh sách pinnedSignIds
        String signId = trafficSign.getId();
        if (isPinned) {
            // Thêm vào danh sách nếu chưa có
            if (!pinnedSignIds.contains(signId)) {
                pinnedSignIds.add(signId);
            }
            // Lưu thông tin chi tiết biển báo
            if (prefsManager != null) {
                prefsManager.savePinnedTrafficSign(trafficSign);
            }
        } else {
            // Xóa khỏi danh sách nếu có
            pinnedSignIds.remove(signId);
            // Xóa thông tin chi tiết biển báo
            if (prefsManager != null) {
                prefsManager.removePinnedTrafficSign(trafficSign);
            }
        }
        
        // Lưu thay đổi ID vào SharedPreferences (cho khả năng tương thích ngược)
        if (prefsManager != null) {
            prefsManager.savePinnedTrafficSignIds(pinnedSignIds);
        }
        
        // Cập nhật lại danh sách hiển thị
        applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
    }
    
    /**
     * Phương thức applyFilters() không tham số, gọi phương thức applyFilters có tham số
     */
    private void applyFilters() {
        applyFilters(currentCategory, currentSearchQuery, showOnlyPinned);
    }
} 