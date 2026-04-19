package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
    private String maID;
    private String maNhaCungCap;
    private String maNguoiNhap;
    private String tenNhaCungCap;
    private double tongTien;
    private Object trangThai;
    private Date ngayNhap;
    @ServerTimestamp
    private Date createdAt;
    private List<Map<String, Object>> chiTiet;

    public NhapHang() {}

    public static NhapHang fromDocument(DocumentSnapshot doc) {
        NhapHang item = new NhapHang();
        item.setId(doc.getId());
        item.setMaID(FirestoreValueParser.safeString(doc, "maID"));
        item.setMaNCC(FirestoreValueParser.safeString(doc, "maNCC"));
        if (item.getMaNCC() == null) item.setMaNhaCungCap(FirestoreValueParser.safeString(doc, "maNhaCungCap"));
        item.setMaNguoiNhap(FirestoreValueParser.safeString(doc, "maNguoiNhap"));
        item.setTenNhaCungCap(FirestoreValueParser.safeString(doc, "tenNhaCungCap"));
        item.setNgayTao(doc.getDate("ngayTao"));
        item.setNgayNhap(doc.getDate("ngayNhap"));
        item.setCreatedAt(doc.getDate("createdAt"));

        Double tongTienRaw = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "tongTien"));
        item.setTongTien(tongTienRaw != null ? tongTienRaw.doubleValue() : 0d);
        item.setTrangThai(FirestoreValueParser.safeRaw(doc, "trangThai"));
        return item;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaID() { return maID; }
    public void setMaID(String maID) { this.maID = maID; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getMaNguoiNhap() { return maNguoiNhap; }
    public void setMaNguoiNhap(String maNguoiNhap) { this.maNguoiNhap = maNguoiNhap; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public int getTrangThaiValue() { return parseTrangThai(trangThai); }
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }

    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Map<String, Object>> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<Map<String, Object>> chiTiet) { this.chiTiet = chiTiet; }

    public String getMaNCC() { return maNhaCungCap; }
    public void setMaNCC(String maNCC) { this.maNhaCungCap = maNCC; }

    public String getDisplayId() {
        if (maID != null && !maID.trim().isEmpty()) return maID.trim();
        return id;
    }

    public Date getNgayTao() { return ngayNhap != null ? ngayNhap : createdAt; }
    public void setNgayTao(Date ngayTao) { this.createdAt = ngayTao; }

    @Exclude
    public boolean isTrangThai() { return getTrangThaiValue() == 1; }

    public String getTrangThaiLabel() {
        return getTrangThaiValue() == 1 ? "Đã nhập kho" : "Đã hủy";
    }

    private int parseTrangThai(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue() == 1 ? 1 : 0;
        if (value instanceof Boolean) return (Boolean) value ? 1 : 0;
        if (value instanceof String) {
            String s = ((String) value).trim();
            if ("1".equals(s) || "true".equalsIgnoreCase(s) || "đã nhập".equalsIgnoreCase(s) || "đã nhập kho".equalsIgnoreCase(s)) {
                return 1;
            }
        }
        return 0;
    }
}
