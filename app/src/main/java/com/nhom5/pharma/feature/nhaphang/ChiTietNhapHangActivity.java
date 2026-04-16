package com.nhom5.pharma.feature.nhaphang;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom5.pharma.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietNhapHangActivity extends AppCompatActivity {

    private TextView tvOrderCodeTitle, tvTrangThaiHeader, tvNguoiNhap, tvNgayNhapMeta, tvTenNhaCungCap;
    private LinearLayout llChiTiet;
    private String nhapHangId;
    private NhapHangRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nhap_hang);

        repository = NhapHangRepository.getInstance();
        nhapHangId = getIntent().getStringExtra("NHAP_HANG_ID");

        initViews();
        loadData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn nhập hàng");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());

        tvOrderCodeTitle = findViewById(R.id.tvOrderCodeTitle);
        tvTrangThaiHeader = findViewById(R.id.tvTrangThaiHeader);
        tvNguoiNhap = findViewById(R.id.tvNguoiNhap);
        tvNgayNhapMeta = findViewById(R.id.tvNgayNhapMeta);
        tvTenNhaCungCap = findViewById(R.id.tvTenNhaCungCap);
        llChiTiet = findViewById(R.id.llChiTiet);

        findViewById(R.id.btnXoa).setOnClickListener(v -> showDeleteConfirmationDialog());
        findViewById(R.id.btnLuu).setOnClickListener(v -> finish());
    }

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        Button btnBoQua = dialogView.findViewById(R.id.btnBoQua);
        Button btnXoa = dialogView.findViewById(R.id.btnXoaConfirm);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);

        tvMessage.setText("Xóa phiếu nhập hàng " + (nhapHangId != null ? nhapHangId : "") + "?");

        btnBoQua.setOnClickListener(v -> dialog.dismiss());
        btnXoa.setOnClickListener(v -> {
            dialog.dismiss();
            deleteOrder();
        });

        dialog.show();
    }

    private void deleteOrder() {
        if (nhapHangId == null) return;
        repository.deleteNhapHang(nhapHangId).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
            finish(); 
        });
    }

    private void loadData() {
        if (nhapHangId == null) return;
        repository.getNhapHangById(nhapHangId).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                NhapHang nhapHang = doc.toObject(NhapHang.class);
                if (nhapHang != null) {
                    nhapHang.setId(doc.getId());
                    if (nhapHang.getMaID() == null || nhapHang.getMaID().trim().isEmpty()) {
                        nhapHang.setMaID(doc.getId());
                    }
                    displayNhapHangInfo(nhapHang);
                    
                    if (nhapHang.getMaNCC() != null) {
                        fetchSupplierName(nhapHang.getMaNCC());
                    }
                    
                    // Truy vấn tên người dùng thay vì để cứng Admin
                    if (nhapHang.getMaNguoiNhap() != null) {
                        fetchUserName(nhapHang.getMaNguoiNhap());
                    } else {
                        tvNguoiNhap.setText("Không xác định");
                    }
                    
                    fetchLoHangList(doc.getId());
                }
            }
        });
    }

    private void displayNhapHangInfo(NhapHang nhapHang) {
        tvOrderCodeTitle.setText(nhapHang.getDisplayId());
        int trangThai = nhapHang.getTrangThaiValue();
        if (trangThai == 1) {
            tvTrangThaiHeader.setText("Đã nhập kho");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvTrangThaiHeader.setText("Đã hủy");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#F44336"));
        }
        
        if (nhapHang.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvNgayNhapMeta.setText(sdf.format(nhapHang.getNgayTao()));
        }
    }

    private void fetchSupplierName(String maNCC) {
        repository.getSupplierById(maNCC).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("tenNCC");
                if (name == null) name = doc.getString("TenNCC");
                tvTenNhaCungCap.setText(name);
            }
        });
    }

    private void fetchUserName(String maNguoiNhap) {
        repository.getUserById(maNguoiNhap).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Thử cả 2 trường hợp tên trường (tenNguoiDung hoặc hoTen)
                String name = doc.getString("tenNguoiDung");
                if (name == null) name = doc.getString("hoTen");
                if (name == null) name = "Người dùng (" + maNguoiNhap + ")";
                tvNguoiNhap.setText(name);
            } else {
                tvNguoiNhap.setText("Mã: " + maNguoiNhap);
            }
        });
    }

    private void fetchLoHangList(String id) {
        llChiTiet.removeAllViews();
        repository.getLoHangByNhapHangId(id).addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_chi_tiet_lo_hang, llChiTiet, false);
                ((TextView)itemView.findViewById(R.id.tvSoLo)).setText(doc.getId());
                
                Double sl = doc.getDouble("soLuong");
                Double dg = doc.getDouble("donGiaNhap");
                
                double soLuong = sl != null ? sl : 0;
                double donGia = dg != null ? dg : 0;
                
                ((TextView)itemView.findViewById(R.id.tvSoLuong)).setText(String.format(Locale.getDefault(), "%,.0f", soLuong));
                ((TextView)itemView.findViewById(R.id.tvDonGia)).setText(String.format(Locale.getDefault(), "%,.0fđ", donGia));
                ((TextView)itemView.findViewById(R.id.tvThanhTien)).setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * donGia));
                
                String maSP = doc.getString("maSP");
                if (maSP != null) {
                    repository.getProductById(maSP).addOnSuccessListener(spDoc -> {
                        if (spDoc.exists()) {
                            String tenSP = spDoc.getString("tenSP");
                            if (tenSP == null) tenSP = spDoc.getString("TenSP");
                            ((TextView)itemView.findViewById(R.id.tvTenHang)).setText(tenSP);
                        }
                    });
                }
                llChiTiet.addView(itemView);
            }
        });
    }
}
