package com.nhom5.pharma.feature.lohang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.nhaphang.NhapHangRepository;

import java.util.Locale;

public class ChiTietLoHangActivity extends AppCompatActivity {

    public static final String EXTRA_SO_LO = "SO_LO";

    private TextView tvSoLo;
    private TextView tvTenHang;
    private TextView tvSoLuong;
    private TextView tvDonGia;
    private TextView tvThanhTien;

    private NhapHangRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_lo_hang);

        repository = NhapHangRepository.getInstance();

        initViews();

        String soLo = getIntent().getStringExtra(EXTRA_SO_LO);
        if (TextUtils.isEmpty(soLo)) {
            Toast.makeText(this, "Khong co ma lo hang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadLoHangDetail(soLo.trim());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết lô hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSoLo = findViewById(R.id.tvSoLo);
        tvTenHang = findViewById(R.id.tvTenHang);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        tvDonGia = findViewById(R.id.tvDonGia);
        tvThanhTien = findViewById(R.id.tvThanhTien);
    }

    private void loadLoHangDetail(String soLo) {
        repository.getLoHangById(soLo).addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Khong tim thay lo hang", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            bindLoHang(doc);
        });
    }

    private void bindLoHang(DocumentSnapshot doc) {
        String soLo = doc.getId();
        String maSP = firstNonEmpty(doc, "maSP", "MaSP", "maHang", "MaHang");
        double soLuong = firstNumber(doc, "soLuong", "SoLuong");
        double donGiaNhap = firstNumber(doc, "donGiaNhap", "DonGiaNhap", "giaNhap", "GiaNhap");

        tvSoLo.setText(defaultText(soLo));
        tvTenHang.setText(defaultText(maSP));
        tvSoLuong.setText(formatNumber(soLuong));
        tvDonGia.setText(formatMoney(donGiaNhap));
        tvThanhTien.setText(formatMoney(soLuong * donGiaNhap));

        if (!TextUtils.isEmpty(maSP)) {
            repository.getProductById(maSP).addOnSuccessListener(productDoc -> {
                if (productDoc.exists()) {
                    String tenSP = firstNonEmpty(productDoc, "tenSP", "TenSP", "tenHang", "TenHang");
                    if (!TextUtils.isEmpty(tenSP)) {
                        tvTenHang.setText(tenSP);
                    }
                }
            });
        }
    }

    private static String firstNonEmpty(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            String value = snapshot.getString(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private static double firstNumber(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Number value = snapshot.getDouble(key);
            if (value != null) {
                return value.doubleValue();
            }
            Object raw = snapshot.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
        }
        return 0d;
    }

    private static String defaultText(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }

    private static String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%,.0f", value);
    }

    private static String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0fđ", value);
    }
}

