package com.example.giaothong.utils;

import com.example.giaothong.model.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to provide sample traffic sign data
 */
public class DataUtils {

    /**
     * Creates and returns a list of sample traffic signs
     * In a real application, this data would come from a database or API
     */
    public static List<TrafficSign> getSampleTrafficSigns() {
        List<TrafficSign> signs = new ArrayList<>();
        
        // Sample data - in a real app, these would be loaded from a database
        signs.add(new TrafficSign(
                "P101", 
                "Đường cấm", 
                "Báo đường cấm tất cả các loại phương tiện (thô sơ và cơ giới) đi lại cả hai hướng", 
                "bien_bao/p101.jpg",
                "Biển báo cấm"));
                
        signs.add(new TrafficSign(
                "P102", 
                "Cấm đi ngược chiều", 
                "Báo đường cấm tất cả các loại xe đi vào theo chiều đặt biển", 
                "bien_bao/p102.jpg", 
                "Biển báo cấm"));
                
        signs.add(new TrafficSign(
                "P103a", 
                "Cấm xe ô tô", 
                "Báo đường cấm tất cả các loại xe cơ giới kể cả môtô 3 bánh có thùng đi qua, trừ môtô 2 bánh, xe gắn máy và các xe được ưu tiên theo quy định", 
                "bien_bao/p103a.jpg", 
                "Biển báo cấm"));
                
        signs.add(new TrafficSign(
                "P104", 
                "Cấm xe máy", 
                "Báo đường cấm tất cả các loại xe máy đi qua", 
                "bien_bao/p104.jpg", 
                "Biển báo cấm"));
                
        signs.add(new TrafficSign(
                "W201", 
                "Chỗ ngoặt nguy hiểm vòng bên trái", 
                "Báo trước sắp đến một chỗ ngoặt nguy hiểm phía bên trái", 
                "bien_bao/w201.jpg", 
                "Biển báo nguy hiểm"));
                
        signs.add(new TrafficSign(
                "W202", 
                "Chỗ ngoặt nguy hiểm vòng bên phải", 
                "Báo trước sắp đến một chỗ ngoặt nguy hiểm phía bên phải", 
                "bien_bao/w202.jpg", 
                "Biển báo nguy hiểm"));
                
        signs.add(new TrafficSign(
                "I401", 
                "Bắt đầu đường ưu tiên", 
                "Báo hiệu đoạn bắt đầu đường ưu tiên", 
                "bien_bao/i401.jpg", 
                "Biển báo hiệu lệnh"));
                
        signs.add(new TrafficSign(
                "I402", 
                "Hết đường ưu tiên", 
                "Báo hiệu đoạn kết thúc đường ưu tiên", 
                "bien_bao/i402.jpg", 
                "Biển báo hiệu lệnh"));
                
        signs.add(new TrafficSign(
                "R301", 
                "Hướng đi thẳng", 
                "Báo các phương tiện chỉ được đi thẳng", 
                "bien_bao/r301.jpg", 
                "Biển báo chỉ dẫn"));
                
        signs.add(new TrafficSign(
                "R302", 
                "Hướng đi chỉ rẽ phải", 
                "Báo các phương tiện chỉ được rẽ phải", 
                "bien_bao/r302.jpg", 
                "Biển báo chỉ dẫn"));
        
        return signs;
    }
} 