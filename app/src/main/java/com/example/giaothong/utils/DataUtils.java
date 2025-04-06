package com.example.giaothong.utils;

import com.example.giaothong.model.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/**
 * Cung cấp các chức năng hỗ trợ xử lý dữ liệu
 */
public class DataUtils {

    /**
     * Trả về danh sách biển báo trống khi không có dữ liệu từ API
     * Trong ứng dụng thực tế, dữ liệu nên được lấy từ API hoặc bộ nhớ offline
     */
    public static List<TrafficSign> getSampleTrafficSigns() {
        return new ArrayList<>();
    }
} 