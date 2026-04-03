package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class NhaCungCap implements Serializable {
    @DocumentId
    private String id; // Mã NCC từ Document ID
    
    private String tenNCC;
    private String maSoThue;
    private String sdt;
    private String email;
    private String diaChi;
    private boolean trangThai;
    private String TongDon; // Khớp với field 'TongDon' trên Firestore
    private String GiaTri;  // Khớp với field 'GiaTri' trên Firestore

    public NhaCungCap() {}

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getTongDon() { return TongDon; }
    public void setTongDon(String tongDon) { TongDon = tongDon; }

    public String getGiaTri() { return GiaTri; }
    public void setGiaTri(String giaTri) { GiaTri = giaTri; }
}
