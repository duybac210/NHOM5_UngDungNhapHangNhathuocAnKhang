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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
        
        // Tắt màn hình "chồng" này đi để quay về màn hình bên dưới
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
                DocumentReference nccRef = doc.get("NccID", DocumentReference.class);
                NhapHang nhapHang = doc.toObject(NhapHang.class);
                if (nhapHang != null) {
                    nhapHang.setId(doc.getId());
                    displayNhapHangInfo(nhapHang);
                    fetchSupplierName(nccRef);
                    fetchLoHangList(doc.getId());
                }
            }
        });
    }

    private void displayNhapHangInfo(NhapHang nhapHang) {
        tvOrderCodeTitle.setText(nhapHang.getId());
        if (nhapHang.getTrangThai()) {
            tvTrangThaiHeader.setText("Đã nhập hàng");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvTrangThaiHeader.setText("Đã hủy");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#F44336"));
        }
        tvNguoiNhap.setText(nhapHang.getCreatedBy() != null ? nhapHang.getCreatedBy() : "Admin");
        if (nhapHang.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvNgayNhapMeta.setText(sdf.format(nhapHang.getNgayTao()));
        }
    }

    private void fetchSupplierName(DocumentReference nccRef) {
        if (nccRef == null) return;
        repository.getSupplierByRef(nccRef).addOnSuccessListener(doc -> {
            if (doc.exists()) tvTenNhaCungCap.setText(doc.getString("TenNCC"));
            else {
                FirebaseFirestore.getInstance().collection("Nhacungcap").document(nccRef.getId().trim()).get()
                    .addOnSuccessListener(d2 -> { if (d2.exists()) tvTenNhaCungCap.setText(d2.getString("TenNCC")); });
            }
        });
    }

    private void fetchLoHangList(String id) {
        llChiTiet.removeAllViews();
        repository.getLoHangByNhapHangId(id).addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_chi_tiet_lo_hang, llChiTiet, false);
                ((TextView)itemView.findViewById(R.id.tvSoLo)).setText(doc.getId());
                double sl = doc.getDouble("SoLuong") != null ? doc.getDouble("SoLuong") : 0;
                double dg = doc.getDouble("DonGiaNhap") != null ? doc.getDouble("DonGiaNhap") : 0;
                ((TextView)itemView.findViewById(R.id.tvSoLuong)).setText(String.format(Locale.getDefault(), "%,.0f", sl));
                ((TextView)itemView.findViewById(R.id.tvDonGia)).setText(String.format(Locale.getDefault(), "%,.0f đ", dg));
                ((TextView)itemView.findViewById(R.id.tvThanhTien)).setText(String.format(Locale.getDefault(), "%,.0f đ", sl * dg));
                
                DocumentReference spRef = doc.get("SanPhamID", DocumentReference.class);
                if (spRef != null) repository.getProductByRef(spRef).addOnSuccessListener(spDoc -> {
                    if (spDoc.exists()) ((TextView)itemView.findViewById(R.id.tvTenHang)).setText(spDoc.getString("TenSP"));
                });
                llChiTiet.addView(itemView);
            }
        });
    }
}
