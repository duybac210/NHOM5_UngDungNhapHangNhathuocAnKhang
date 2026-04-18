package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LoHang {
    private String maNhapHang;
    private String maSP;
    private double soLuong;
    private double donGiaNhap;

    public LoHang() {
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
}

