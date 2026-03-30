package com.nhom5.pharma.feature.nhaphang;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class NhapHang {
    private String id;
    private DocumentReference NccID;
    private boolean TrangThai;
    private double TongTien;
    
    @ServerTimestamp
    private Date NgayTao;
    
    private String createdBy;

    public NhapHang() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("NccID")
    public DocumentReference getNccID() { return NccID; }
    
    @PropertyName("NccID")
    public void setNccID(DocumentReference NccID) { this.NccID = NccID; }

    @PropertyName("TrangThai")
    public boolean getTrangThai() { return TrangThai; }
    
    @PropertyName("TrangThai")
    public void setTrangThai(boolean TrangThai) { this.TrangThai = TrangThai; }

    @PropertyName("TongTien")
    public double getTongTien() { return TongTien; }
    
    @PropertyName("TongTien")
    public void setTongTien(double TongTien) { this.TongTien = TongTien; }

    @PropertyName("NgayTao")
    public Date getNgayTao() { return NgayTao; }
    
    @PropertyName("NgayTao")
    public void setNgayTao(Date NgayTao) { this.NgayTao = NgayTao; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
