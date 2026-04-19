package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class LoHang {
    private String soLo;
    private String maNhapHang;
    private String maSP;
    private double soLuong;
    private double donGiaNhap;
    private Date ngayNhap;
    private Date hanSuDung;
    private Date ngaySanXuat;

    @ServerTimestamp
    private Date ngayTao;

    public LoHang() {
    }

    public String getSoLo() {
        return soLo;
    }

    public void setSoLo(String soLo) {
        this.soLo = soLo;
    }

    public String getMaNhapHang() {
        return maNhapHang;
    }

    public void setMaNhapHang(String maNhapHang) {
        this.maNhapHang = maNhapHang;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public double getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(double soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(double donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public Date getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Date ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public Date getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(Date hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public Date getNgaySanXuat() {
        return ngaySanXuat;
    }

    public void setNgaySanXuat(Date ngaySanXuat) {
        this.ngaySanXuat = ngaySanXuat;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> data = new HashMap<>();
        if (soLo != null && !soLo.trim().isEmpty()) {
            data.put("soLo", soLo.trim());
        }
        if (maNhapHang != null && !maNhapHang.trim().isEmpty()) {
            data.put("maNhapHang", maNhapHang.trim());
        }
        if (maSP != null && !maSP.trim().isEmpty()) {
            data.put("maSP", maSP.trim());
        }
        data.put("soLuong", soLuong);
        data.put("donGiaNhap", donGiaNhap);
        if (ngayNhap != null) {
            data.put("ngayNhap", ngayNhap);
        }
        if (hanSuDung != null) {
            data.put("hanSuDung", hanSuDung);
        }
        if (ngaySanXuat != null) {
            data.put("ngaySanXuat", ngaySanXuat);
        }
        if (ngayTao != null) {
            data.put("ngayTao", ngayTao);
        }
        return data;
    }
}

