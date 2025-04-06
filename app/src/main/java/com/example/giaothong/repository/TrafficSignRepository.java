package com.example.giaothong.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.giaothong.api.ApiClient;
import com.example.giaothong.api.ApiConfig;
import com.example.giaothong.model.BienBaoCategory;
import com.example.giaothong.model.BienBaoItem;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.DataUtils;
import com.example.giaothong.utils.OfflineImageManager;
import com.example.giaothong.utils.OfflineDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository cung cấp dữ liệu biển báo từ các nguồn khác nhau
 */
public class TrafficSignRepository {
    
    private static final String TAG = "TrafficSignRepository";
    private static TrafficSignRepository instance;
    private List<TrafficSign> trafficSigns;
    private TrafficSignRepository repository;
    private List<Consumer<List<TrafficSign>>> dataListeners;
    private OfflineImageManager offlineImageManager;
    private OfflineDataManager offlineDataManager;
    private boolean isLoadingData = false;
    
    private TrafficSignRepository(Context context) {
        // Private constructor to prevent direct instantiation
        trafficSigns = new ArrayList<>();
        repository = null; // Không tự tham chiếu đến chính nó
        dataListeners = new ArrayList<>();
        
        // Khởi tạo OfflineImageManager
        offlineImageManager = new OfflineImageManager(context);
        offlineDataManager = new OfflineDataManager(context);
        
        // Tải dữ liệu từ API
        loadTrafficSignsFromApi();
    }
    
    /**
     * Get the singleton instance
     * @return the DataManager instance
     */
    public static synchronized TrafficSignRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TrafficSignRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Lấy danh sách biển báo từ API
     * @param callback Callback được gọi khi dữ liệu sẵn sàng
     */
    public void getTrafficSigns(Consumer<List<TrafficSign>> callback) {
        // Nếu đã có dữ liệu sẵn sàng, trả về ngay
        if (isDataReady()) {
            Log.d(TAG, "Sử dụng dữ liệu có sẵn (" + trafficSigns.size() + " biển báo)");
            callback.accept(trafficSigns);
            return;
        }
        
        // Đánh dấu đang tải dữ liệu
        isLoadingData = true;
        
        // Thêm callback vào danh sách lắng nghe
        dataListeners.add(callback);
        
        // Kiểm tra kết nối internet
        boolean hasNetwork = offlineDataManager.isNetworkAvailable();
        
        // Nếu không có mạng, kiểm tra có dữ liệu offline không
        if (!hasNetwork && offlineDataManager.hasOfflineData()) {
            Log.d(TAG, "Không có kết nối mạng, sử dụng dữ liệu ngoại tuyến");
            List<TrafficSign> offlineSigns = offlineDataManager.getOfflineTrafficSigns();
            if (offlineSigns != null && !offlineSigns.isEmpty()) {
                trafficSigns = offlineSigns;
                notifyDataChanged();
                isLoadingData = false;
                return;
            }
        }
        
        // Tải dữ liệu từ API (sẽ tự động kiểm tra và sử dụng dữ liệu ngoại tuyến nếu cần)
        loadTrafficSignsFromApi();
    }
    
    /**
     * Phương thức tương thích với cách gọi cũ, sẽ cập nhật trực tiếp LiveData
     * @param trafficSigns LiveData để cập nhật kết quả
     */
    public void getTrafficSigns(MutableLiveData<List<TrafficSign>> trafficSigns) {
        getTrafficSigns(trafficSigns::setValue);
    }
    
    /**
     * Làm mới dữ liệu biển báo từ API
     */
    public void refreshTrafficSigns() {
        // Tránh gọi nhiều request cùng lúc
        if (isLoadingData) {
            Log.d(TAG, "Đang tải dữ liệu, bỏ qua yêu cầu làm mới");
            return;
        }
        
        isLoadingData = true;
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
     * Lấy danh sách các biển báo đã ghim để ưu tiên tải
     */
    public List<TrafficSign> getPinnedTrafficSigns() {
        if (trafficSigns == null) {
            return new ArrayList<>();
        }
        
        List<TrafficSign> pinnedSigns = new ArrayList<>();
        for (TrafficSign sign : trafficSigns) {
            if (sign.isPinned()) {
                pinnedSigns.add(sign);
            }
        }
        return pinnedSigns;
    }
    
    /**
     * Ưu tiên tải hình ảnh cho các biển báo đã ghim
     */
    public void preloadPinnedImages() {
        List<TrafficSign> pinnedSigns = getPinnedTrafficSigns();
        if (!pinnedSigns.isEmpty()) {
            offlineImageManager.preloadPinnedImages(pinnedSigns);
        }
    }
    
    /**
     * Tải trước tất cả hình ảnh biển báo trong nền
     */
    public void preloadAllImages() {
        if (trafficSigns != null && !trafficSigns.isEmpty()) {
            offlineImageManager.preloadTrafficSignImages(trafficSigns);
        }
    }
    
    /**
     * Tải dữ liệu biển báo từ API
     */
    private void loadTrafficSignsFromApi() {
        Log.d(TAG, "Bắt đầu tải dữ liệu biển báo từ API");
        
        // Kiểm tra kết nối internet
        boolean hasNetwork = offlineDataManager.isNetworkAvailable();
        
        // Nếu không có mạng, kiểm tra có dữ liệu offline không
        if (!hasNetwork) {
            Log.d(TAG, "Không có kết nối mạng, kiểm tra dữ liệu ngoại tuyến");
            
            if (offlineDataManager.hasOfflineData()) {
                // Sử dụng dữ liệu offline nếu không có mạng và có dữ liệu
                Log.d(TAG, "Sử dụng dữ liệu ngoại tuyến vì không có kết nối mạng");
                List<TrafficSign> offlineSigns = offlineDataManager.getOfflineTrafficSigns();
                if (offlineSigns != null && !offlineSigns.isEmpty()) {
                    trafficSigns = offlineSigns;
                    notifyDataChanged();
                    isLoadingData = false;
                    return;
                }
            }
            
            // Nếu không có dữ liệu offline, sử dụng dữ liệu mẫu
            Log.d(TAG, "Không có dữ liệu ngoại tuyến, sử dụng dữ liệu mẫu");
            trafficSigns = DataUtils.getSampleTrafficSigns();
            notifyDataChanged();
            isLoadingData = false;
            return;
        }
        
        // Nếu có kết nối internet, tải từ API
        ApiClient.getApiService().getTrafficSigns(ApiConfig.API_KEY).enqueue(new Callback<List<BienBaoCategory>>() {
            @Override
            public void onResponse(Call<List<BienBaoCategory>> call, Response<List<BienBaoCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API trả về thành công, code: " + response.code() + ", body size: " + response.body().size());
                    List<TrafficSign> allSigns = new ArrayList<>();
                    List<BienBaoCategory> categories = response.body();
                    
                    for (BienBaoCategory category : categories) {
                        // Thêm biển báo cấm
                        if (category.getBienBaoCam() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoCam()) {
                                allSigns.add(item.toTrafficSign("bien_bao_cam", index++));
                            }
                            Log.d(TAG, "Đã thêm " + index + " biển báo cấm");
                        }
                        
                        // Thêm biển báo nguy hiểm và cảnh báo
                        if (category.getBienBaoNguyHiem() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoNguyHiem()) {
                                allSigns.add(item.toTrafficSign("bien_nguy_hiem_va_canh_bao", index++));
                            }
                            Log.d(TAG, "Đã thêm " + index + " biển báo nguy hiểm");
                        }
                        
                        // Thêm biển hiệu lệnh
                        if (category.getBienBaoHieuLenh() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoHieuLenh()) {
                                allSigns.add(item.toTrafficSign("bien_hieu_lenh", index++));
                            }
                            Log.d(TAG, "Đã thêm " + index + " biển báo hiệu lệnh");
                        }
                        
                        // Thêm biển chỉ dẫn
                        if (category.getBienBaoChiDan() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoChiDan()) {
                                allSigns.add(item.toTrafficSign("bien_chi_dan", index++));
                            }
                            Log.d(TAG, "Đã thêm " + index + " biển báo chỉ dẫn");
                        }
                        
                        // Thêm biển phụ
                        if (category.getBienBaoPhu() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoPhu()) {
                                allSigns.add(item.toTrafficSign("bien_phu", index++));
                            }
                            Log.d(TAG, "Đã thêm " + index + " biển báo phụ");
                        }
                    }
                    
                    // Cập nhật dữ liệu mới
                    if (!allSigns.isEmpty()) {
                        Log.d(TAG, "Đã nhận " + allSigns.size() + " biển báo từ API");
                        trafficSigns = allSigns;
                    } else {
                        // Nếu không có dữ liệu từ API, sử dụng dữ liệu mẫu
                        Log.d(TAG, "Không có dữ liệu từ API, sử dụng dữ liệu mẫu");
                        trafficSigns = DataUtils.getSampleTrafficSigns();
                    }
                    
                    // Tải trước hình ảnh cho các biển báo đã ghim
                    preloadPinnedImages();
                    
                    // Tải trước tất cả hình ảnh trong nền
                    preloadAllImages();
                    
                    // Thông báo cho tất cả listeners về dữ liệu mới
                    notifyDataChanged();
                    
                    // Đánh dấu đã tải xong
                    isLoadingData = false;
                } else {
                    // Nếu API gặp lỗi, sử dụng dữ liệu mẫu
                    Log.e(TAG, "Lỗi API: " + response.code() + " - " + response.message());
                    trafficSigns = DataUtils.getSampleTrafficSigns();
                    
                    // Thông báo cho tất cả listeners về dữ liệu mới
                    notifyDataChanged();
                    
                    // Đánh dấu đã tải xong
                    isLoadingData = false;
                }
            }
            
            @Override
            public void onFailure(Call<List<BienBaoCategory>> call, Throwable t) {
                Log.e(TAG, "Gọi API thất bại", t);
                
                // Kiểm tra có dữ liệu ngoại tuyến không
                if (offlineDataManager.hasOfflineData()) {
                    List<TrafficSign> offlineSigns = offlineDataManager.getOfflineTrafficSigns();
                    if (offlineSigns != null && !offlineSigns.isEmpty()) {
                        Log.d(TAG, "Sử dụng dữ liệu offline sau khi API thất bại: " + offlineSigns.size() + " biển báo");
                        trafficSigns = offlineSigns;
                        notifyDataChanged();
                        isLoadingData = false;
                        return;
                    }
                }
                
                // Nếu không có dữ liệu offline, sử dụng dữ liệu mẫu
                trafficSigns = DataUtils.getSampleTrafficSigns();
                
                // Thông báo cho tất cả listeners về dữ liệu mới
                notifyDataChanged();
                
                // Đánh dấu đã tải xong
                isLoadingData = false;
            }
        });
    }
    
    /**
     * Kiểm tra dữ liệu đã sẵn sàng chưa
     * @return true nếu dữ liệu đã được tải
     */
    public boolean isDataReady() {
        return trafficSigns != null && !trafficSigns.isEmpty() && !isLoadingData;
    }
    
    /**
     * Thông báo cho tất cả listener về thay đổi dữ liệu
     */
    private void notifyDataChanged() {
        if (dataListeners != null && trafficSigns != null) {
            for (Consumer<List<TrafficSign>> listener : dataListeners) {
                if (listener != null) {
                    listener.accept(trafficSigns);
                }
            }
        }
    }
    
    /**
     * Đăng ký một listener để nhận thông báo khi dữ liệu thay đổi
     * @param listener listener cần đăng ký
     */
    public void registerDataListener(Consumer<List<TrafficSign>> listener) {
        if (dataListeners != null && listener != null) {
            dataListeners.add(listener);
            
            // Nếu đã có dữ liệu, gửi ngay cho listener mới
            if (trafficSigns != null && !trafficSigns.isEmpty()) {
                listener.accept(trafficSigns);
            }
        }
    }
    
    /**
     * Hủy đăng ký một listener
     * @param listener listener cần hủy đăng ký
     */
    public void unregisterDataListener(Consumer<List<TrafficSign>> listener) {
        if (dataListeners != null && listener != null) {
            dataListeners.remove(listener);
        }
    }
    
    /**
     * Tải và lưu dữ liệu để sử dụng ngoại tuyến
     * @param callback Callback khi tải xong
     */
    public void downloadDataForOffline(Consumer<Boolean> callback) {
        Log.d(TAG, "Bắt đầu tải dữ liệu biển báo cho sử dụng ngoại tuyến");
        
        if (!offlineDataManager.isNetworkAvailable()) {
            Log.e(TAG, "Không thể tải dữ liệu ngoại tuyến: Không có kết nối mạng");
            callback.accept(false);
            return;
        }
        
        // Gọi API để lấy dữ liệu mới nhất
        ApiClient.getApiService().getTrafficSigns(ApiConfig.API_KEY).enqueue(new Callback<List<BienBaoCategory>>() {
            @Override
            public void onResponse(Call<List<BienBaoCategory>> call, Response<List<BienBaoCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API trả về thành công dữ liệu để lưu ngoại tuyến");
                    
                    List<TrafficSign> allSigns = new ArrayList<>();
                    
                    // Xử lý dữ liệu từ API
                    for (BienBaoCategory category : response.body()) {
                        // Thêm biển báo cấm
                        if (category.getBienBaoCam() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoCam()) {
                                allSigns.add(item.toTrafficSign("bien_bao_cam", index++));
                            }
                        }
                        
                        // Thêm biển báo nguy hiểm và cảnh báo
                        if (category.getBienBaoNguyHiem() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoNguyHiem()) {
                                allSigns.add(item.toTrafficSign("bien_nguy_hiem_va_canh_bao", index++));
                            }
                        }
                        
                        // Thêm biển hiệu lệnh
                        if (category.getBienBaoHieuLenh() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoHieuLenh()) {
                                allSigns.add(item.toTrafficSign("bien_hieu_lenh", index++));
                            }
                        }
                        
                        // Thêm biển chỉ dẫn
                        if (category.getBienBaoChiDan() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoChiDan()) {
                                allSigns.add(item.toTrafficSign("bien_chi_dan", index++));
                            }
                        }
                        
                        // Thêm biển phụ
                        if (category.getBienBaoPhu() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoPhu()) {
                                allSigns.add(item.toTrafficSign("bien_phu", index++));
                            }
                        }
                    }
                    
                    if (!allSigns.isEmpty()) {
                        Log.d(TAG, "Lưu " + allSigns.size() + " biển báo vào bộ nhớ ngoại tuyến");
                        boolean success = offlineDataManager.saveTrafficSignsOffline(allSigns);
                        
                        if (success) {
                            // Tải trước hình ảnh cho việc sử dụng ngoại tuyến
                            offlineImageManager.preloadTrafficSignImages(allSigns);
                            callback.accept(true);
                        } else {
                            Log.e(TAG, "Lỗi khi lưu dữ liệu ngoại tuyến");
                            callback.accept(false);
                        }
                    } else {
                        Log.e(TAG, "API trả về danh sách rỗng");
                        callback.accept(false);
                    }
                } else {
                    Log.e(TAG, "Lỗi API khi tải dữ liệu ngoại tuyến: " + 
                            (response.code() + " - " + response.message()));
                    callback.accept(false);
                }
            }
            
            @Override
            public void onFailure(Call<List<BienBaoCategory>> call, Throwable t) {
                Log.e(TAG, "Gọi API thất bại khi tải dữ liệu ngoại tuyến", t);
                callback.accept(false);
            }
        });
    }
    
    /**
     * Lấy OfflineDataManager
     * @return OfflineDataManager instance
     */
    public OfflineDataManager getOfflineDataManager() {
        return offlineDataManager;
    }
    
    // ... rest of the code ...
} 