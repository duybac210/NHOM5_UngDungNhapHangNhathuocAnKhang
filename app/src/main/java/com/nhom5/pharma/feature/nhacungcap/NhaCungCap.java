package com.nhom5.pharma.feature.nhacungcap;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.text.DecimalFormat;
import java.util.Date;

public class NhaCungCap implements Parcelable {
    @DocumentId
    private String id;
    private String tenNCC;
    private Object maSoThue; 
    private Object sdt;
    private String email;
    private String diaChi;
    private boolean trangThai;

    @ServerTimestamp
    private Date ngayTao;
    private Date ngayCapNhat;

    private Object soLuong; 
    private Object tongMua; 

    public NhaCungCap() {}

    // --- Parcelable Implementation ---
    protected NhaCungCap(Parcel in) {
        id = in.readString();
        tenNCC = in.readString();
        email = in.readString();
        diaChi = in.readString();
        trangThai = in.readByte() != 0;
        long tmpNgayTao = in.readLong();
        ngayTao = tmpNgayTao == -1 ? null : new Date(tmpNgayTao);
        long tmpNgayCapNhat = in.readLong();
        ngayCapNhat = tmpNgayCapNhat == -1 ? null : new Date(tmpNgayCapNhat);
        
        // Đọc Object an toàn
        maSoThue = in.readValue(getClass().getClassLoader());
        sdt = in.readValue(getClass().getClassLoader());
        soLuong = in.readValue(getClass().getClassLoader());
        tongMua = in.readValue(getClass().getClassLoader());
    }

    public static final Creator<NhaCungCap> CREATOR = new Creator<NhaCungCap>() {
        @Override
        public NhaCungCap createFromParcel(Parcel in) {
            return new NhaCungCap(in);
        }

        @Override
        public NhaCungCap[] newArray(int size) {
            return new NhaCungCap[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(tenNCC);
        dest.writeString(email);
        dest.writeString(diaChi);
        dest.writeByte((byte) (trangThai ? 1 : 0));
        dest.writeLong(ngayTao != null ? ngayTao.getTime() : -1);
        dest.writeLong(ngayCapNhat != null ? ngayCapNhat.getTime() : -1);
        
        dest.writeValue(maSoThue);
        dest.writeValue(sdt);
        dest.writeValue(soLuong);
        dest.writeValue(tongMua);
    }
    // --- End Parcelable ---

    @Exclude
    public String fetchMaSoThue() {
        return maSoThue != null ? String.valueOf(maSoThue) : "---";
    }

    @Exclude
    public String fetchSdt() {
        return sdt != null ? String.valueOf(sdt) : "---";
    }

    @Exclude
    public String fetchDisplayTongDon() {
        return soLuong != null ? String.valueOf(soLuong) : "0";
    }

    @Exclude
    public String fetchDisplayGiaTri() {
        if (tongMua == null) return "0";
        try {
            double num;
            if (tongMua instanceof Number) {
                num = ((Number) tongMua).doubleValue();
            } else {
                String clean = String.valueOf(tongMua).replace(".", "").replace(",", "");
                num = Double.parseDouble(clean);
            }
            DecimalFormat df = new DecimalFormat("###,###,###");
            return df.format(num).replace(",", ".");
        } catch (Exception e) {
            return String.valueOf(tongMua);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }
    public Object getMaSoThue() { return maSoThue; }
    public void setMaSoThue(Object maSoThue) { this.maSoThue = maSoThue; }
    public Object getSdt() { return sdt; }
    public void setSdt(Object sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
    public Object getSoLuong() { return soLuong; }
    public void setSoLuong(Object soLuong) { this.soLuong = soLuong; }
    public Object getTongMua() { return tongMua; }
    public void setTongMua(Object tongMua) { this.tongMua = tongMua; }
}
