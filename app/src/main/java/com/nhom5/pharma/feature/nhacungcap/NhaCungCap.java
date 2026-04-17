package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class NhaCungCap implements Serializable {
    private String id;
    private String tenNCC;
    private String diaChi;
    private String sdt;
    private String email;
    private String maSoThue;
    private String tongDon;
    private String giaTri;
    private Object trangThai; 

    public NhaCungCap() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getTongDon() { return tongDon; }
    public void setTongDon(String tongDon) { this.tongDon = tongDon; }

    public String getGiaTri() { return giaTri; }
    public void setGiaTri(String giaTri) { this.giaTri = giaTri; }

    public boolean isTrangThai() { 
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true; 
    }
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }
}
