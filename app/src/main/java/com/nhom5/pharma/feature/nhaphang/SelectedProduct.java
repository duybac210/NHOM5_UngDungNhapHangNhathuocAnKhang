package com.nhom5.pharma.feature.nhaphang;

public class SelectedProduct {
    private String maSanPham;
    private String tenSanPham;
    private double donGia;
    private int soLuong;

    public SelectedProduct(String maSanPham, String tenSanPham, double donGia, int soLuong) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donGia = donGia;
        this.soLuong = soLuong;
    }

    public String getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public double getDonGia() { return donGia; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}