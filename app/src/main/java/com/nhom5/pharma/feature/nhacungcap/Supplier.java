package com.nhom5.pharma.feature.nhacungcap;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class Supplier {
    private String maID;
    private String tenNhaCungCap;
    private String phone;
    private String email;
    private String address;
    private int trangThai;
    @ServerTimestamp
    private Date createdAt;

    public Supplier() {}

    public Supplier(String maID, String tenNhaCungCap, String phone, String email, String address, int trangThai) {
        this.maID = maID;
        this.tenNhaCungCap = tenNhaCungCap;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.trangThai = trangThai;
    }

    public String getMaID() { return maID; }
    public void setMaID(String maID) { this.maID = maID; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public String getTen() { return tenNhaCungCap; }
    public void setTen(String ten) { this.tenNhaCungCap = ten; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
