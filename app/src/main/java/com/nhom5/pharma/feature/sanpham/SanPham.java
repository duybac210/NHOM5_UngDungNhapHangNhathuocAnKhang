package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentId;

public class SanPham {
    @DocumentId
    private String id;
    private String tenSanPham;
    private double giaVon;
    private double giaBan;
    private String hangSanXuat;
    private String nuocSanXuat;

    public SanPham() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public double getGiaVon() { return giaVon; }
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public String getHangSanXuat() { return hangSanXuat; }
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }

    public String getNuocSanXuat() { return nuocSanXuat; }
    public void setNuocSanXuat(String nuocSanXuat) { this.nuocSanXuat = nuocSanXuat; }
}
