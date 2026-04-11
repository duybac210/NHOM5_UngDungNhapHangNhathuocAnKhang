package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class NhaCungCap implements Serializable {
    @DocumentId
    private String id;
    private String tenNCC;
    private String maSoThue;
    private String sdt;
    private String email;
    private String diaChi;
    private boolean trangThai;

    @ServerTimestamp
    private Date ngayTao;
    private Date ngayCapNhat;

    private Object TongDon; 
    private Object GiaTri;  
    private Object soLuong; 
    private Object tongMua; 

    public NhaCungCap() {}

    // Getters và Setters cho ngày tháng
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public String getDisplayTongDon() {
        if (soLuong != null) return String.valueOf(soLuong);
        if (TongDon != null) return String.valueOf(TongDon);
        return "0";
    }

    public String getDisplayGiaTri() {
        if (tongMua != null) return String.valueOf(tongMua);
        if (GiaTri != null) return String.valueOf(GiaTri);
        return "0";
    }

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

    @PropertyName("TongDon") public Object getTongDon() { return TongDon; }
    @PropertyName("TongDon") public void setTongDon(Object TongDon) { this.TongDon = TongDon; }
    @PropertyName("GiaTri") public Object getGiaTri() { return GiaTri; }
    @PropertyName("GiaTri") public void setGiaTri(Object GiaTri) { this.GiaTri = GiaTri; }
    @PropertyName("soLuong") public Object getSoLuong() { return soLuong; }
    @PropertyName("soLuong") public void setSoLuong(Object soLuong) { this.soLuong = soLuong; }
    @PropertyName("tongMua") public Object getTongMua() { return tongMua; }
    @PropertyName("tongMua") public void setTongMua(Object tongMua) { this.tongMua = tongMua; }
}
