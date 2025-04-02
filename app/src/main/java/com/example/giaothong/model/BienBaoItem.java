package com.example.giaothong.model;

import com.google.gson.annotations.SerializedName;

/**
 * Đối tượng biển báo từ API
 */
public class BienBaoItem {
    
    @SerializedName("ten_bien")
    private String tenBien;
    
    @SerializedName("mo_ta")
    private String moTa;
    
    @SerializedName("hinh_anh")
    private String hinhAnh;

    public String getTenBien() {
        return tenBien;
    }

    public void setTenBien(String tenBien) {
        this.tenBien = tenBien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    // Phương thức để chuyển đổi sang đối tượng TrafficSign
    public TrafficSign toTrafficSign(String category, int index) {
        String id = category.substring(0, 1).toUpperCase() + String.format("%03d", index);
        return new TrafficSign(id, tenBien, moTa, hinhAnh, category);
    }
} 