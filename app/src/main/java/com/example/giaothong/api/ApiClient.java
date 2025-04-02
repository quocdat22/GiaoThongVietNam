package com.example.giaothong.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client cho việc khởi tạo và sử dụng Retrofit
 */
public class ApiClient {
    
    private static Retrofit retrofit = null;
    
    /**
     * Lấy instance của Retrofit client
     * @return Retrofit client
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Lấy instance của ApiService
     * @return ApiService
     */
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
} 