package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class NhaCungCap implements Serializable {
    @DocumentId
    private String id;
    private String tenNCC;
    private String tenNhaCungCap;
    private String maSoThue;
    private String sdt;
    private String phone;
    private String email;
    private String diaChi;
    private String address;
    private Object trangThai;
    private String TongDon;
    private String GiaTri;
    private String tongDon;
    private String giaTri;
    private Object tongMua;
    private Object soLuong;

    public NhaCungCap() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNCC() {
        if (tenNCC != null && !tenNCC.trim().isEmpty()) return tenNCC;
        return tenNhaCungCap;
    }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getSdt() {
        if (sdt != null && !sdt.trim().isEmpty()) return sdt;
        return phone;
    }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() {
        if (diaChi != null && !diaChi.trim().isEmpty()) return diaChi;
        return address;
    }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTongDon() {
        if (TongDon != null && !TongDon.trim().isEmpty()) return TongDon;
        if (tongDon != null && !tongDon.trim().isEmpty()) return tongDon;
        return toNumberString(soLuong);
    }
    public void setTongDon(String tongDon) {
        this.TongDon = tongDon;
        this.tongDon = tongDon;
    }

    public String getGiaTri() {
        if (GiaTri != null && !GiaTri.trim().isEmpty()) return GiaTri;
        if (giaTri != null && !giaTri.trim().isEmpty()) return giaTri;
        return toNumberString(tongMua);
    }
    public void setGiaTri(String giaTri) {
        this.GiaTri = giaTri;
        this.giaTri = giaTri;
    }

    public Object getTongMua() { return tongMua; }
    public void setTongMua(Object tongMua) { this.tongMua = tongMua; }

    public Object getSoLuong() { return soLuong; }
    public void setSoLuong(Object soLuong) { this.soLuong = soLuong; }

    private String toNumberString(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return String.valueOf(((Number) value).longValue());
        }
        return String.valueOf(value);
    }

    @Exclude
    public boolean isTrangThai() {
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true;
    }
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }
}
