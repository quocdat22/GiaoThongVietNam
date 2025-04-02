package com.example.giaothong.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Đối tượng chứa danh mục biển báo
 */
public class BienBaoCategory {
    
    @SerializedName("_id")
    private String id;
    
    @SerializedName("Biển báo cấm")
    private List<BienBaoItem> bienBaoCam;
    
    @SerializedName("Biển báo nguy hiểm và cảnh báo")
    private List<BienBaoItem> bienBaoNguyHiem;
    
    @SerializedName("Biển hiệu lệnh")
    private List<BienBaoItem> bienBaoHieuLenh;
    
    @SerializedName("Biển chỉ dẫn")
    private List<BienBaoItem> bienBaoChiDan;
    
    @SerializedName("Biển phụ")
    private List<BienBaoItem> bienBaoPhu;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<BienBaoItem> getBienBaoCam() {
        return bienBaoCam;
    }

    public void setBienBaoCam(List<BienBaoItem> bienBaoCam) {
        this.bienBaoCam = bienBaoCam;
    }

    public List<BienBaoItem> getBienBaoNguyHiem() {
        return bienBaoNguyHiem;
    }

    public void setBienBaoNguyHiem(List<BienBaoItem> bienBaoNguyHiem) {
        this.bienBaoNguyHiem = bienBaoNguyHiem;
    }

    public List<BienBaoItem> getBienBaoHieuLenh() {
        return bienBaoHieuLenh;
    }

    public void setBienBaoHieuLenh(List<BienBaoItem> bienBaoHieuLenh) {
        this.bienBaoHieuLenh = bienBaoHieuLenh;
    }

    public List<BienBaoItem> getBienBaoChiDan() {
        return bienBaoChiDan;
    }

    public void setBienBaoChiDan(List<BienBaoItem> bienBaoChiDan) {
        this.bienBaoChiDan = bienBaoChiDan;
    }
    
    public List<BienBaoItem> getBienBaoPhu() {
        return bienBaoPhu;
    }

    public void setBienBaoPhu(List<BienBaoItem> bienBaoPhu) {
        this.bienBaoPhu = bienBaoPhu;
    }
} 