package com.example.giaothong.utils;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * Cấu hình Glide cho ứng dụng để cải thiện hiệu suất và hỗ trợ offline
 */
@GlideModule
public class AppGlideModuleCustom extends AppGlideModule {
    private static final int MEMORY_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Cấu hình bộ nhớ cache
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE));
        
        // Cấu hình bộ nhớ đĩa cache
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        
        // Cấu hình chất lượng hình ảnh
        builder.setDefaultRequestOptions(
            new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Tiết kiệm bộ nhớ hơn ARGB_8888
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache cả dữ liệu gốc và biến đổi
                .centerCrop()
        );
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        return false; // Tắt phân tích Manifest để tối ưu hóa hiệu suất
    }
} 