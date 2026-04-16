package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
<<<<<<< HEAD
    private String maID;
    private String maNhaCungCap;
    private String maNguoiNhap;
    private String tenNhaCungCap;
=======
    private String maNCC;
    private String maNguoiNhap;
    private Object trangThai; // Đổi sang Object để tránh lỗi Long vs Boolean từ Firestore
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
    private double tongTien;
    private Object trangThai; // Hỗ trợ int / boolean / String từ dữ liệu Firestore cũ và mới
    private Date ngayNhap;
    @ServerTimestamp
    private Date createdAt;
    private List<Map<String, Object>> chiTiet;

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

    public String getMaID() { return maID; }
    public void setMaID(String maID) { this.maID = maID; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getMaNguoiNhap() { return maNguoiNhap; }
    public void setMaNguoiNhap(String maNguoiNhap) { this.maNguoiNhap = maNguoiNhap; }

<<<<<<< HEAD
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
=======
    public boolean isTrangThai() {
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return false;
    }
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

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

    // Compatibility aliases for older screens/adapters.
    public String getMaNCC() { return maNhaCungCap; }
    public void setMaNCC(String maNCC) { this.maNhaCungCap = maNCC; }

    public String getDisplayId() {
        if (maID != null && !maID.trim().isEmpty()) {
            return maID.trim();
        }
        return id;
    }

    public Date getNgayTao() { return ngayNhap != null ? ngayNhap : createdAt; }
    public void setNgayTao(Date ngayTao) { this.createdAt = ngayTao; }

    public boolean isTrangThai() { return getTrangThaiValue() == 1; }

    public String getTrangThaiLabel() {
        if (getTrangThaiValue() == 1) {
            return "Đã nhập kho";
        }
        return "Đã hủy";
    }

    private int parseTrangThai(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1 ? 1 : 0;
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            if ("1".equals(s) || "true".equalsIgnoreCase(s) || "đã nhập".equalsIgnoreCase(s) || "đã nhập kho".equalsIgnoreCase(s)) {
                return 1;
            }
        }
        return 0;
    }
}
