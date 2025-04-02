package com.example.giaothong.api;

import com.example.giaothong.model.BienBaoCategory;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface định nghĩa các API endpoints
 */
public interface ApiService {
    
    /**
     * Lấy danh sách biển báo
     * @param apiKey API key để xác thực
     * @return Danh sách biển báo theo danh mục
     */
    @GET("layDanhSachBienBao")
    Call<List<BienBaoCategory>> getTrafficSigns(
            @Query("apiKey") String apiKey
    );
} 