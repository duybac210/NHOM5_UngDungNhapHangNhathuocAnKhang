package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
    private String nhaCungCapId;
    private String tenNhaCungCap;
    private int trangThai;
    private int tongSoLuong;
    private double tongTien;
    private String ghiChu;

    @ServerTimestamp // Tự động chuyển đổi Timestamp từ Firebase sang Date của Java
    private Date createdAt;

    private String createdBy;
    private List<Map<String, Object>> chiTiet;

    public NhapHang() {}

    // Getter và Setter đầy đủ (Quan trọng để Firebase đọc được dữ liệu)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}