package com.example.giaothong.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.giaothong.api.ApiClient;
import com.example.giaothong.api.ApiConfig;
import com.example.giaothong.model.BienBaoCategory;
import com.example.giaothong.model.BienBaoItem;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository cung cấp dữ liệu biển báo từ các nguồn khác nhau
 */
public class TrafficSignRepository {
    
    private static final String TAG = "TrafficSignRepository";
    
    /**
     * Lấy danh sách biển báo từ API
     * @param trafficSigns LiveData để cập nhật kết quả
     */
    public void getTrafficSigns(MutableLiveData<List<TrafficSign>> trafficSigns) {
        // Đầu tiên đặt dữ liệu mẫu để có thể hiển thị ngay
        trafficSigns.setValue(DataUtils.getSampleTrafficSigns());
        
        // Sau đó gọi API để lấy dữ liệu thực
        ApiClient.getApiService().getTrafficSigns(ApiConfig.API_KEY).enqueue(new Callback<List<BienBaoCategory>>() {
            @Override
            public void onResponse(Call<List<BienBaoCategory>> call, Response<List<BienBaoCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficSign> allSigns = new ArrayList<>();
                    List<BienBaoCategory> categories = response.body();
                    
                    for (BienBaoCategory category : categories) {
                        // Thêm biển báo cấm
                        if (category.getBienBaoCam() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoCam()) {
                                allSigns.add(item.toTrafficSign("Biển báo cấm", index++));
                            }
                        }
                        
                        // Thêm biển báo nguy hiểm và cảnh báo
                        if (category.getBienBaoNguyHiem() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoNguyHiem()) {
                                allSigns.add(item.toTrafficSign("Biển báo nguy hiểm và cảnh báo", index++));
                            }
                        }
                        
                        // Thêm biển hiệu lệnh
                        if (category.getBienBaoHieuLenh() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoHieuLenh()) {
                                allSigns.add(item.toTrafficSign("Biển hiệu lệnh", index++));
                            }
                        }
                        
                        // Thêm biển chỉ dẫn
                        if (category.getBienBaoChiDan() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoChiDan()) {
                                allSigns.add(item.toTrafficSign("Biển chỉ dẫn", index++));
                            }
                        }
                        
                        // Thêm biển phụ
                        if (category.getBienBaoPhu() != null) {
                            int index = 0;
                            for (BienBaoItem item : category.getBienBaoPhu()) {
                                allSigns.add(item.toTrafficSign("Biển phụ", index++));
                            }
                        }
                    }
                    
                    // Cập nhật LiveData với kết quả
                    if (!allSigns.isEmpty()) {
                        trafficSigns.setValue(allSigns);
                        Log.d(TAG, "Đã tải " + allSigns.size() + " biển báo từ API");
                    }
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<BienBaoCategory>> call, Throwable t) {
                Log.e(TAG, "Gọi API thất bại", t);
            }
        });
    }
} 