package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
    private String maNCC;
    private String maNguoiNhap; // Thêm trường này
    private boolean trangThai;
    private double tongTien;
    private String ghiChu;
    
    @ServerTimestamp
    private Date ngayTao;
    private Date ngayNhap;
    private Date ngayCapNhat;

    public NhapHang() {}

    public static NhapHang fromDocument(DocumentSnapshot doc) {
        NhapHang item = new NhapHang();
        item.setId(doc.getId());
        item.setMaNCC(doc.getString("maNCC"));
        item.setMaNguoiNhap(doc.getString("maNguoiNhap"));
        item.setGhiChu(doc.getString("ghiChu"));
        item.setNgayTao(doc.getDate("ngayTao"));
        item.setNgayNhap(doc.getDate("ngayNhap"));
        item.setNgayCapNhat(doc.getDate("ngayCapNhat"));

        Number tongTienRaw = doc.getDouble("tongTien");
        if (tongTienRaw == null) {
            Object value = doc.get("tongTien");
            if (value instanceof Number) {
                tongTienRaw = (Number) value;
            }
        }
        item.setTongTien(tongTienRaw != null ? tongTienRaw.doubleValue() : 0d);
        item.setTrangThai(parseTrangThai(doc.get("trangThai")));
        return item;
    }

    private static boolean parseTrangThai(Object rawValue) {
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue() == 1;
        }
        if (rawValue instanceof String) {
            String value = ((String) rawValue).trim();
            if ("1".equals(value)) return true;
            if ("0".equals(value)) return false;
            return "true".equalsIgnoreCase(value);
        }
        return false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getMaNguoiNhap() { return maNguoiNhap; }
    public void setMaNguoiNhap(String maNguoiNhap) { this.maNguoiNhap = maNguoiNhap; }

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
