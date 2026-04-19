package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IgnoreExtraProperties
public class Product {
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^SP(\\d+)$");

    private String id; 
    private String maID; 
    private String tenSP;
    private double giavon;
    private double giaBan;
    private String maVach;
    private String moTa;
    private String hangSX;
    private String nuocSX;
    private String displayId;
    
    @PropertyName("trangThai")
    private Object trangThai; 
    
    @ServerTimestamp
    private Date ngayTao;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date ngayCapNhat;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaID() { return maID != null ? maID : id; }
    public void setMaID(String maID) { this.maID = maID; }

    public String getTenSP() { 
        if (tenSP != null && !tenSP.isEmpty()) return tenSP;
        return (maID != null) ? "Mã: " + maID : (id != null ? "Mã: " + id : "(Chưa có tên)");
    }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public double getGiavon() { return giavon; }
    public void setGiavon(double giavon) { this.giavon = giavon; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public String getMaVach() { return maVach != null ? maVach : ""; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getMoTa() { return moTa != null ? moTa : ""; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHangSX() { return hangSX != null ? hangSX : ""; }
    public void setHangSX(String hangSX) { this.hangSX = hangSX; }

    public String getNuocSX() { return nuocSX != null ? nuocSX : ""; }
    public void setNuocSX(String nuocSX) { this.nuocSX = nuocSX; }

    public String getDisplayId() { return displayId != null ? displayId : id; }
    public void setDisplayId(String displayId) { this.displayId = displayId; }

    @PropertyName("trangThai")
    public Object getTrangThai() { return trangThai; }

    @PropertyName("trangThai")
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }

    public boolean layTrangThaiBoolean() {
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true; 
    }

    public Date getNgayTao() { return ngayTao != null ? ngayTao : createdAt; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public static String buildNextProductId(List<DocumentSnapshot> documents) {
        long maxNumber = 0;
        int maxDigits = 3;

        for (DocumentSnapshot document : documents) {
            String id = document.getId();
            long number = extractProductIdNumber(id);
            if (number > maxNumber) {
                maxNumber = number;
                if (id.length() > 2) {
                    maxDigits = Math.max(maxDigits, id.length() - 2);
                }
            }
            
            String maID = document.getString("maID");
            if (maID != null) {
                long maIdNum = extractProductIdNumber(maID);
                if (maIdNum > maxNumber) {
                    maxNumber = maIdNum;
                    if (maID.length() > 2) {
                        maxDigits = Math.max(maxDigits, maID.length() - 2);
                    }
                }
            }
        }

        return String.format(Locale.getDefault(), "SP%0" + maxDigits + "d", maxNumber + 1);
    }

    private static long extractProductIdNumber(String id) {
        if (id == null) return -1;
        Matcher matcher = PRODUCT_ID_PATTERN.matcher(id.trim());
        if (matcher.matches()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
