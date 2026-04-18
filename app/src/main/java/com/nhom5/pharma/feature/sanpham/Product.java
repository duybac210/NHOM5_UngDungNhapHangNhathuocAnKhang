package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class Product {
    private String id; 
    private String maID; 
    private String tenSP;
    private double giavon;
    private double giaBan;
    private String maVach;
    private String moTa;
    private String hangSX;
    private String nuocSX;
    
    @PropertyName("trangThai")
    private Object trangThai; 
    
    @ServerTimestamp
    private Date ngayTao;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date ngayCapNhat;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Logic quan trọng: Nếu không có tên, trả về mã hàng để không bị trống dòng
    public String getTenSP() { 
        if (tenSP != null && !tenSP.isEmpty()) return tenSP;
        return (maID != null) ? "Mã: " + maID : (id != null ? "Mã: " + id : "(Chưa có tên)");
    }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public String getMaID() { return maID != null ? maID : id; }
    public void setMaID(String maID) { this.maID = maID; }

    public double getGiavon() { return giavon; }
    public void setGiavon(double giavon) { this.giavon = giavon; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public String getMaVach() { return maVach != null ? maVach : ""; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getMoTa() { return moTa != null ? moTa : ""; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHangSX() { return hangSX != null ? hangSX : ""; }
    public void setHangSX(String hangSX) { this.hangSX = hangSX; }

    public String getNuocSX() { return nuocSX != null ? nuocSX : ""; }
    public void setNuocSX(String nuocSX) { this.nuocSX = nuocSX; }

    @PropertyName("trangThai")
    public Object getTrangThai() { return trangThai; }

    @PropertyName("trangThai")
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }

    public boolean layTrangThaiBoolean() {
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true; 
    }

    public Date getNgayTao() { return ngayTao != null ? ngayTao : createdAt; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
