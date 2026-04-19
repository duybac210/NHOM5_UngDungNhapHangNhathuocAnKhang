package com.nhom5.pharma.feature.nhaphang;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietNhapHangActivity extends AppCompatActivity {

    private TextView tvOrderCodeTitle, tvTrangThaiHeader, tvNguoiNhap, tvNgayNhapMeta, tvTenNhaCungCap;
    private LinearLayout llChiTiet;
    private final List<LoHang> currentLoHangs = new ArrayList<>();
    private boolean isLoHangLoaded = false;
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
        findViewById(R.id.btnLuu).setEnabled(false);
        findViewById(R.id.btnLuu).setOnClickListener(v -> syncLoHangToFirebase());
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
            if (isFinishing() || isDestroyed()) return;
            Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
            finish(); 
        });
    }

    private void loadData() {
        if (nhapHangId == null) return;
        repository.getNhapHangById(nhapHangId).addOnSuccessListener(doc -> {
            if (isFinishing() || isDestroyed()) return;
            if (doc.exists()) {
                NhapHang nhapHang = NhapHang.fromDocument(doc);
                displayNhapHangInfo(nhapHang);

                if (nhapHang.getMaNCC() != null) {
                    fetchSupplierName(nhapHang.getMaNCC());
                }

                if (nhapHang.getMaNguoiNhap() != null) {
                    fetchUserName(nhapHang.getMaNguoiNhap());
                } else {
                    tvNguoiNhap.setText("Không xác định");
                }

                fetchLoHangList(doc.getId());
            }
        });
    }

    private void displayNhapHangInfo(NhapHang nhapHang) {
        if (tvOrderCodeTitle == null) return;
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
            if (isFinishing() || isDestroyed()) return;
            if (doc.exists()) {
                String name = doc.getString("tenNCC");
                if (name == null) name = doc.getString("TenNCC");
                if (name == null) name = doc.getString("tenNhaCungCap");
                if (name == null) name = doc.getString("ten");
                tvTenNhaCungCap.setText(name != null ? name : "Không xác định");
            }
        });
    }

    private void fetchUserName(String maNguoiNhap) {
        repository.getUserById(maNguoiNhap).addOnSuccessListener(doc -> {
            if (isFinishing() || isDestroyed()) return;
            if (doc.exists()) {
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
        if (llChiTiet == null) return;
        llChiTiet.removeAllViews();
        currentLoHangs.clear();
        isLoHangLoaded = false;
        repository.getLoHangByNhapHangId(id).addOnSuccessListener(snapshot -> {
            if (isFinishing() || isDestroyed()) return;
            for (DocumentSnapshot doc : snapshot) {
                String soLoDoc = doc.getId();
                String maSpValue = firstNonEmpty(doc, "maSP", "MaSP", "maHang", "MaHang");
                double soLuongValue = firstNumber(doc, "soLuong", "SoLuong");
                double donGiaValue = firstNumber(doc, "donGiaNhap", "DonGiaNhap", "giaNhap", "GiaNhap", "donGia", "DonGia");

                LoHang loHang = new LoHang();
                loHang.setSoLo(soLoDoc);
                loHang.setMaNhapHang(firstNonEmpty(doc, "maNhapHang", "MaNhapHang"));
                loHang.setMaSP(maSpValue);
                loHang.setSoLuong(soLuongValue);
                loHang.setDonGiaNhap(donGiaValue);
                loHang.setNgayNhap(firstDate(doc, "ngayNhap", "NgayNhap", "ngayTao", "createdAt"));
                loHang.setHanSuDung(firstDate(doc, "hanSuDung", "HanSuDung", "hansudung"));
                loHang.setNgaySanXuat(firstDate(doc, "ngaySanXuat", "NgaySanXuat", "ngaySX", "NgaySX", "nsx", "NSX"));
                loHang.setNgayTao(firstDate(doc, "ngayTao", "createdAt", "NgayTao"));
                currentLoHangs.add(loHang);

                syncLegacyLoHangFields(doc, loHang);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_chi_tiet_lo_hang, llChiTiet, false);
                ((TextView)itemView.findViewById(R.id.tvSoLo)).setText(soLoDoc);

                Double sl = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "soLuong"));
                Double dg = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "donGiaNhap"));

                double soLuong = sl != null ? sl : soLuongValue;
                double donGia = dg != null ? dg : donGiaValue;
                
                ((TextView)itemView.findViewById(R.id.tvSoLuong)).setText(String.format(Locale.getDefault(), "%,.0f", soLuong));
                ((TextView)itemView.findViewById(R.id.tvDonGia)).setText(String.format(Locale.getDefault(), "%,.0fđ", donGia));
                ((TextView)itemView.findViewById(R.id.tvThanhTien)).setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * donGia));
                
                if (maSpValue != null) {
                    repository.getProductById(maSpValue).addOnSuccessListener(spDoc -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (spDoc.exists()) {
                            String tenSP = FirestoreValueParser.safeString(spDoc, "tenSP");
                            if (tenSP == null) tenSP = FirestoreValueParser.safeString(spDoc, "TenSP");
                            ((TextView)itemView.findViewById(R.id.tvTenHang)).setText(tenSP);
                        }
                    });
                }
                llChiTiet.addView(itemView);
            }
            isLoHangLoaded = true;
            findViewById(R.id.btnLuu).setEnabled(true);
        }).addOnFailureListener(e -> {
            isLoHangLoaded = false;
            findViewById(R.id.btnLuu).setEnabled(false);
            Toast.makeText(this, "Khong tai duoc lo hang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void syncLegacyLoHangFields(DocumentSnapshot doc, LoHang loHang) {
        String soLo = loHang.getSoLo();
        if (soLo == null || soLo.trim().isEmpty()) {
            return;
        }

        boolean needsCanonicalSync = !doc.contains("maSP")
                || !doc.contains("donGiaNhap")
                || !doc.contains("ngaySanXuat")
                || !doc.contains("soLo");

        if (needsCanonicalSync) {
            repository.upsertLoHang(soLo, loHang);
        }
    }

    private void syncLoHangToFirebase() {
        if (nhapHangId == null) {
            return;
        }

        if (!isLoHangLoaded) {
            Toast.makeText(this, "Du lieu lo hang chua tai xong, vui long thu lai", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.replaceLoHangByNhapHangId(nhapHangId, currentLoHangs)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Đã đồng bộ lô hàng lên Firebase", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Đồng bộ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static String firstNonEmpty(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            String value = snapshot.getString(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static double firstNumber(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Double value = snapshot.getDouble(key);
            if (value != null) {
                return value;
            }
            Object raw = snapshot.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
            if (raw instanceof String) {
                try {
                    return Double.parseDouble(((String) raw).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0d;
    }

    private static Date firstDate(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Object raw = snapshot.get(key);
            if (raw instanceof Date) {
                return (Date) raw;
            }
            if (raw instanceof com.google.firebase.Timestamp) {
                return ((com.google.firebase.Timestamp) raw).toDate();
            }
        }
        return null;
    }
}
