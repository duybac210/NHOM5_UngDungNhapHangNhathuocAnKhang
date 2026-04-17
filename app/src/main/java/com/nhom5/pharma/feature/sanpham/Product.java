package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IgnoreExtraProperties
public class Product {
<<<<<<< HEAD
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^SP(\\d+)$");
    private static final String LEGACY_BAD_PRODUCT_ID = "SP00001";

    private String id; // Document ID (SP0001, ...)
=======
    private String id; 
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
    private String tenSP;
    private double giavon;
    private String maVach;
    private String moTa;
    private String hangSX;
    private String nuocSX;
    
    @PropertyName("trangThai")
    private Object trangThai; 
    
    @ServerTimestamp
    private Date ngayTao;

    @ServerTimestamp
    private Date ngayCapNhat;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
<<<<<<< HEAD

    public String getDisplayId() {
        return id;
    }


    public static long extractProductNumber(String rawId) {
        if (rawId == null) {
            return -1;
        }

        Matcher matcher = PRODUCT_ID_PATTERN.matcher(rawId.trim());
        if (!matcher.matches()) {
            return -1;
        }

        try {
            String numberText = matcher.group(1);
            if (numberText == null || numberText.isEmpty()) {
                return -1;
            }
            return Long.parseLong(numberText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isLegacyBadProductId(String rawId) {
        return rawId != null && LEGACY_BAD_PRODUCT_ID.equals(rawId.trim());
    }

    public static String buildNextProductId(Iterable<? extends DocumentSnapshot> documents) {
        long maxNumber = 0;
        int maxDigits = 4;

        if (documents != null) {
            for (DocumentSnapshot document : documents) {
                if (document == null) {
                    continue;
                }

                String[] candidates = new String[] { document.getId(), document.getString("maID") };
                for (String candidate : candidates) {
                    if (candidate == null || candidate.trim().isEmpty() || isLegacyBadProductId(candidate)) {
                        continue;
                    }

                    long number = extractProductNumber(candidate);
                    if (number < 0) {
                        continue;
                    }

                    maxNumber = Math.max(maxNumber, number);
                    maxDigits = Math.max(maxDigits, candidate.trim().length() - 2);
                }
            }
        }

        return String.format(Locale.getDefault(), "SP%0" + maxDigits + "d", maxNumber + 1);
    }

=======
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public double getGiavon() { return giavon; }
    public void setGiavon(double giavon) { this.giavon = giavon; }
    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getHangSX() { return hangSX; }
    public void setHangSX(String hangSX) { this.hangSX = hangSX; }
    public String getNuocSX() { return nuocSX; }
    public void setNuocSX(String nuocSX) { this.nuocSX = nuocSX; }

    @PropertyName("trangThai")
    public Object getTrangThai() { return trangThai; }

    @PropertyName("trangThai")
    public void setTrangThai(Object trangThai) { this.trangThai = trangThai; }

    // Đổi tên để không trùng lặp với getter của Firestore
    public boolean layTrangThaiBoolean() {
        if (trangThai instanceof Boolean) return (Boolean) trangThai;
        if (trangThai instanceof Number) return ((Number) trangThai).intValue() != 0;
        return true; 
    }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
