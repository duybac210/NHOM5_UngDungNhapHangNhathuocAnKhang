package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
    private String maNCC;
    private boolean trangThai;
    private double tongTien;
    private String ghiChu;
    
    @ServerTimestamp
    private Date ngayTao;
    private Date ngayNhap;
    private Date ngayCapNhat;

    public NhapHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
