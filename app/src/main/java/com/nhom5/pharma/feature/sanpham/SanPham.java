package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class SanPham {
    @DocumentId
    private String id;
    
    @PropertyName("tenSP")
    private String tenSanPham;
    
    @PropertyName("giavon")
    private double giaVon;
    
    @PropertyName("giaBan")
    private double giaBan;
    
    @PropertyName("hangSX")
    private String hangSanXuat;
    
    @PropertyName("nuocSX")
    private String nuocSanXuat;

    public SanPham() {}

    @PropertyName("tenSP")
    public String getTenSanPham() { return tenSanPham; }
    @PropertyName("tenSP")
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    @PropertyName("giavon")
    public double getGiaVon() { return giaVon; }
    @PropertyName("giavon")
    public void setGiaVon(double giaVon) { this.giaVon = giaVon; }

    @PropertyName("giaBan")
    public double getGiaBan() { return giaBan; }
    @PropertyName("giaBan")
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    @PropertyName("hangSX")
    public String getHangSanXuat() { return hangSanXuat; }
    @PropertyName("hangSX")
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }

    @PropertyName("nuocSX")
    public String getNuocSanXuat() { return nuocSanXuat; }
    @PropertyName("nuocSX")
    public void setNuocSanXuat(String nuocSanXuat) { this.nuocSanXuat = nuocSanXuat; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
