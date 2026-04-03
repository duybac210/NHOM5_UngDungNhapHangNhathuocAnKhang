package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class Product {
    private String id; // Document ID (SP0001, ...)
    private String tenSP;
    private double giavon;
    private String maVach;
    private String moTa;
    private String hangSX;
    private String nuocSX;
    private boolean trangThai;
    
    @ServerTimestamp
    private Date ngayTao;

    @ServerTimestamp
    private Date ngayCapNhat;

    public Product() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public double getGiavon() { return giavon; }
    public void setGiavon(double giavon) { this.giavon = giavon; }

    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHangSX() { return hangSX; }
    public void setHangSX(String hangSX) { this.hangSX = hangSX; }

    public String getNuocSX() { return nuocSX; }
    public void setNuocSX(String nuocSX) { this.nuocSX = nuocSX; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
