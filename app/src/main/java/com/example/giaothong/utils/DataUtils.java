package com.example.giaothong.utils;

import android.util.Log;
import com.example.giaothong.model.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/**
 * Cung cấp các chức năng hỗ trợ xử lý dữ liệu
 */
public class DataUtils {

    /**
     * Trả về danh sách biển báo mẫu khi không có dữ liệu từ API
     * Trong ứng dụng thực tế, dữ liệu nên được lấy từ API hoặc bộ nhớ offline
     */
    public static List<TrafficSign> getSampleTrafficSigns() {
        Log.d("DataUtils", "Creating sample traffic signs with descriptions");
        
        List<TrafficSign> sampleSigns = new ArrayList<>();
        
        // Thêm một số biển báo mẫu với mô tả
        sampleSigns.add(new TrafficSign(
            "bien_bao_cam_cam_di_thang", 
            "Biển cấm đi thẳng", 
            "Biển báo cấm các phương tiện đi thẳng, phải rẽ sang hướng khác tại nơi có biển báo.",
            "signs/p_cam_di_thang.png", 
            "bien_bao_cam"));
            
        sampleSigns.add(new TrafficSign(
            "bien_bao_cam_cam_re_trai", 
            "Biển cấm rẽ trái", 
            "Biển báo cấm các phương tiện rẽ trái ở những vị trí đường giao nhau.",
            "signs/p_cam_re_trai.png", 
            "bien_bao_cam"));
            
        sampleSigns.add(new TrafficSign(
            "bien_nguy_hiem_va_canh_bao_khuc_duong_gap_gheng", 
            "Biển báo khúc đường gập ghềnh", 
            "Biển cảnh báo đoạn đường có mặt đường gập ghềnh, lồi lõm, ổ gà, sống trâu.",
            "signs/w_duong_gap_ghenh.png", 
            "bien_nguy_hiem_va_canh_bao"));
            
        sampleSigns.add(new TrafficSign(
            "bien_chi_dan_ben_xe_buyt", 
            "Biển báo bến xe buýt", 
            "Biển chỉ dẫn nơi dừng xe buýt cho hành khách lên xuống.",
            "signs/i_ben_xe_buyt.png", 
            "bien_chi_dan"));
            
        sampleSigns.add(new TrafficSign(
            "bien_hieu_lenh_huong_phai_di", 
            "Biển hiệu lệnh hướng phải đi", 
            "Biển hiệu lệnh bắt buộc các phương tiện phải đi theo hướng mũi tên chỉ.",
            "signs/r_phai_di_theo_huong_phai.png", 
            "bien_hieu_lenh"));
            
        Log.d("DataUtils", "Created " + sampleSigns.size() + " sample traffic signs");
        return sampleSigns;
    }
} 