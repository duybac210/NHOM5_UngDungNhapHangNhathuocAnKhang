package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class NhaCungCap implements Serializable {
    private String id;
    private String tenNCC;
<<<<<<< HEAD
    private String tenNhaCungCap;
    private String maSoThue;
=======
    private String diaChi;
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
    private String sdt;
    private String phone;
    private String email;
<<<<<<< HEAD
    private String diaChi;
    private String address;
    private boolean trangThai;
    private String TongDon; // Khớp với field 'TongDon' trên Firestore
    private String GiaTri;  // Khớp với field 'GiaTri' trên Firestore
=======
    private String maSoThue;
    private String tongDon;
    private String giaTri;
    private Object trangThai; 
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

    public NhaCungCap() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNCC() {
        if (tenNCC != null && !tenNCC.trim().isEmpty()) {
            return tenNCC;
        }
        return tenNhaCungCap;
    }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

<<<<<<< HEAD
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }
=======
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

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

<<<<<<< HEAD
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
=======
    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getTongDon() { return tongDon; }
    public void setTongDon(String tongDon) { this.tongDon = tongDon; }
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

    public String getGiaTri() { return giaTri; }
    public void setGiaTri(String giaTri) { this.giaTri = giaTri; }

    public boolean isTrangThai() { 
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true; 
    }
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }
}
