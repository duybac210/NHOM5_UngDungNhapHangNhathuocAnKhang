package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

public class NhaCungCap implements Serializable {
    @DocumentId
    private String id;
    private String tenNCC;
    
    // Dùng Object để chấp nhận cả Số và Chữ từ Firebase, chống văng App triệt để
    private Object maSoThue; 
    private Object sdt;
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

    // Hàm lấy Số điện thoại an toàn
    @Exclude
    public String fetchSdt() {
        return sdt != null ? String.valueOf(sdt) : "---";
    }

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
    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }
    
    public Object getMaSoThue() { return maSoThue; }
    public void setMaSoThue(Object maSoThue) { this.maSoThue = maSoThue; }
    
    public Object getSdt() { return sdt; }
    public void setSdt(Object sdt) { this.sdt = sdt; }

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
}
