package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
<<<<<<< HEAD
import com.google.firebase.firestore.ServerTimestamp;
=======
import com.google.firebase.firestore.IgnoreExtraProperties;

>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

public class NhaCungCap implements Serializable {
    @DocumentId
    private String id;
    private String tenNCC;
<<<<<<< HEAD
    
    // Sửa lỗi văng App: Dùng Object để chấp nhận cả Số và Chữ từ Firebase
    private Object maSoThue; 
    
    private String sdt;
    private String email;
    private String diaChi;
    private boolean trangThai;

    @ServerTimestamp
    private Date ngayTao;
    private Date ngayCapNhat;

    private Object soLuong; // Đây là Tổng đơn
    private Object tongMua; // Đây là Giá trị nhập (Tổng mua)

    public NhaCungCap() {}

    // Hàm lấy Mã Số Thuế an toàn không bị văng App
    @Exclude
    public String fetchMaSoThue() {
        return maSoThue != null ? String.valueOf(maSoThue) : "---";
    }
=======
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
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674

    @Exclude
    public String fetchDisplayTongDon() {
        return soLuong != null ? String.valueOf(soLuong) : "0";
    }

    @Exclude
    public String fetchDisplayGiaTri() {
        if (tongMua == null) return "0";
        try {
            double num;
            if (tongMua instanceof Number) {
                num = ((Number) tongMua).doubleValue();
            } else {
                String clean = String.valueOf(tongMua).replace(".", "").replace(",", "");
                num = Double.parseDouble(clean);
            }
            DecimalFormat df = new DecimalFormat("###,###,###");
            return df.format(num).replace(",", ".");
        } catch (Exception e) {
            return String.valueOf(tongMua);
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
<<<<<<< HEAD
    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }
    
    public Object getMaSoThue() { return maSoThue; }
    public void setMaSoThue(Object maSoThue) { this.maSoThue = maSoThue; }
    
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
    public Object getSoLuong() { return soLuong; }
    public void setSoLuong(Object soLuong) { this.soLuong = soLuong; }
    public Object getTongMua() { return tongMua; }
    public void setTongMua(Object tongMua) { this.tongMua = tongMua; }
=======

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
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
}
