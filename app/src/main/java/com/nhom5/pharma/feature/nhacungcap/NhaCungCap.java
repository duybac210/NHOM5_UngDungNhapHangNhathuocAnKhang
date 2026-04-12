package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class NhaCungCap implements Serializable {
    @DocumentId
    private String id; // Mã NCC từ Document ID
    
    private String tenNCC;
    private String tenNhaCungCap;
    private String maSoThue;
    private String sdt;
    private String phone;
    private String email;
    private String diaChi;
    private String address;
    private boolean trangThai;
    private String TongDon; // Khớp với field 'TongDon' trên Firestore
    private String GiaTri;  // Khớp với field 'GiaTri' trên Firestore

    public NhaCungCap() {}

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNCC() {
        if (tenNCC != null && !tenNCC.trim().isEmpty()) {
            return tenNCC;
        }
        return tenNhaCungCap;
    }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getSdt() {
        if (sdt != null && !sdt.trim().isEmpty()) {
            return sdt;
        }
        return phone;
    }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() {
        if (diaChi != null && !diaChi.trim().isEmpty()) {
            return diaChi;
        }
        return address;
    }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getTongDon() { return TongDon; }
    public void setTongDon(String tongDon) { TongDon = tongDon; }

    public String getGiaTri() { return GiaTri; }
    public void setGiaTri(String giaTri) { GiaTri = giaTri; }
}
