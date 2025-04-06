package com.example.giaothong.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.giaothong.model.TrafficSign;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp quản lý việc tải và lưu cache hình ảnh sử dụng Glide
 */
public class OfflineImageManager {
    private static final String TAG = "OfflineImageManager";
    private final Context context;
    private final RequestManager glideRequestManager;
    private final ExecutorService executorService;
    
    public OfflineImageManager(Context context) {
        this.context = context.getApplicationContext();
        this.glideRequestManager = Glide.with(this.context);
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    /**
     * Tải trước danh sách hình ảnh từ các biển báo
     * @param trafficSigns Danh sách biển báo cần tải hình
     */
    public void preloadTrafficSignImages(List<TrafficSign> trafficSigns) {
        if (trafficSigns == null || trafficSigns.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "Bắt đầu tải trước " + trafficSigns.size() + " hình ảnh biển báo");
        
        // Thực hiện tải trong background thread
        executorService.execute(() -> {
            int successCount = 0;
            int failCount = 0;
            
            for (TrafficSign sign : trafficSigns) {
                String imageUrl = sign.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    boolean success = preloadImage(imageUrl);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }
            }
            
            Log.d(TAG, "Kết quả tải hình ảnh: " + successCount + " thành công, " + failCount + " thất bại");
        });
    }
    
    /**
     * Tải trước hình ảnh biển báo đã ghim với ưu tiên cao
     * @param pinnedSigns Danh sách biển báo đã ghim
     */
    public void preloadPinnedImages(List<TrafficSign> pinnedSigns) {
        if (pinnedSigns == null || pinnedSigns.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "Bắt đầu tải trước " + pinnedSigns.size() + " hình ảnh biển báo đã ghim với ưu tiên cao");
        
        for (TrafficSign sign : pinnedSigns) {
            String imageUrl = sign.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                preloadImageWithHighPriority(imageUrl);
            }
        }
    }
    
    /**
     * Tải trước một hình ảnh và lưu vào cache
     * @param imageUrl URL của hình ảnh
     * @return true nếu đã bắt đầu tải, false nếu có lỗi
     */
    private boolean preloadImage(String imageUrl) {
        try {
            glideRequestManager
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải hình ảnh: " + imageUrl, e);
            return false;
        }
    }
    
    /**
     * Tải trước một hình ảnh với ưu tiên cao
     * @param imageUrl URL của hình ảnh
     */
    private void preloadImageWithHighPriority(String imageUrl) {
        glideRequestManager
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(com.bumptech.glide.Priority.HIGH)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.e(TAG, "Lỗi khi tải hình ảnh ưu tiên: " + imageUrl, e);
                    return false;
                }
                
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Đã tải thành công hình ảnh ưu tiên: " + imageUrl);
                    return false;
                }
            })
            .preload();
    }
    
    /**
     * Tải hình ảnh vào bộ nhớ và lưu vào cache với callback
     * @param imageUrl URL của hình ảnh
     * @param callback Callback khi tải xong
     */
    public void downloadImage(String imageUrl, ImageDownloadCallback callback) {
        glideRequestManager
            .asBitmap()
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (callback != null) {
                        callback.onImageDownloaded(resource);
                    }
                }
                
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    if (callback != null) {
                        callback.onImageDownloadFailed();
                    }
                }
                
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // Không cần xử lý
                }
            });
    }
    
    /**
     * Kiểm tra xem hình ảnh đã được cache chưa
     * @param imageUrl URL của hình ảnh
     * @return true nếu đã cache
     */
    public boolean isImageCached(String imageUrl) {
        try {
            return Glide.with(context)
                    .load(imageUrl)
                    .onlyRetrieveFromCache(true)
                    .preload() != null;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi kiểm tra cache: " + imageUrl, e);
            return false;
        }
    }
    
    /**
     * Hủy tất cả các tác vụ đang chờ
     */
    public void cancelAllTasks() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
    
    /**
     * Interface callback cho việc tải xuống hình ảnh
     */
    public interface ImageDownloadCallback {
        void onImageDownloaded(Bitmap bitmap);
        void onImageDownloadFailed();
    }
} 