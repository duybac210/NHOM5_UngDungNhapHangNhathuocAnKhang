package com.nhom5.pharma.feature.nhaphang;

import java.util.ArrayList;
import java.util.List;

public class SelectedProduct {
    private String maSanPham;
    private String tenSanPham;
    private double donGia;
    private int soLuong;
    private boolean hasBatch; // Theo dõi trạng thái đã có lô hay chưa
    private List<LoHang> loHangs = new ArrayList<>(); // Danh sách lô hàng của thuốc này

    public SelectedProduct(String maSanPham, String tenSanPham, double donGia, int soLuong) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.hasBatch = false;
    }

    public String getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public boolean isHasBatch() { return hasBatch; }
    public void setHasBatch(boolean hasBatch) { this.hasBatch = hasBatch; }

    public List<LoHang> getLoHangs() { return loHangs; }
    public void addLoHang(LoHang loHang) {
        this.loHangs.add(loHang);
        this.hasBatch = true;
    }
}