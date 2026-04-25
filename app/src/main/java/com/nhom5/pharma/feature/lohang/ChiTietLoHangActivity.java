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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChiTietLoHangActivity extends AppCompatActivity {

    public static final String EXTRA_SO_LO = "SO_LO";

    private TextView tvSoLo;
    private TextView tvMaSanPham;
    private TextView tvNhaCungCap;
    private TextView tvHanSuDung;
    private TextView tvNgaySanXuat;
    private TextView tvTenHang;
    private TextView tvSoLuong;
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
        tvMaSanPham = findViewById(R.id.tvMaSanPham);
        tvNhaCungCap = findViewById(R.id.tvNhaCungCap);
        tvHanSuDung = findViewById(R.id.tvHanSuDung);
        tvNgaySanXuat = findViewById(R.id.tvNgaySanXuat);
        tvTenHang = findViewById(R.id.tvTenHang);
        tvSoLuong = findViewById(R.id.tvSoLuong);
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
        String maNhapHang = firstNonEmpty(doc, "maNhapHang", "MaNhapHang");
        double soLuong = firstNumber(doc, "soLuong", "SoLuong");
        Date hanSuDung = firstDate(doc, "hanSuDung", "HanSuDung", "hansudung");
        Date ngaySanXuat = firstDate(doc, "ngaySanXuat", "NgaySanXuat", "ngaySX", "NgaySX", "nsx", "NSX");

        syncLegacyLoHangFields(doc, soLo, maSP, maNhapHang, soLuong, hanSuDung, ngaySanXuat);

        tvSoLo.setText(defaultText(soLo));
        tvMaSanPham.setText(defaultText(maSP));
        tvNhaCungCap.setText("-");
        tvHanSuDung.setText(formatDate(hanSuDung));
        tvNgaySanXuat.setText(formatDate(ngaySanXuat));
        tvTenHang.setText(defaultText(maSP));
        tvSoLuong.setText(formatNumber(soLuong));
        tvThanhTien.setText("-");

        if (!TextUtils.isEmpty(maSP)) {
            repository.getProductById(maSP).addOnSuccessListener(productDoc -> {
                if (productDoc.exists()) {
                    String tenSP = firstNonEmpty(productDoc, "tenSP", "TenSP", "tenHang", "TenHang");
                    if (!TextUtils.isEmpty(tenSP)) {
                        tvTenHang.setText(tenSP);
                    }

                    // Thanh tien = soLuong * gia von (uu tien giaVon, fallback giaNhap)
                    Double giaVon = firstNumberNullable(productDoc, "giaVon", "GiaVon", "giavon", "giaNhap", "GiaNhap", "donGiaNhap", "DonGiaNhap");
                    if (giaVon != null && giaVon > 0d) {
                        tvThanhTien.setText(formatMoney(soLuong * giaVon));
                    }

                    // Uu tien lay nha cung cap tu lien ket SanPham -> maNCC, sau do truy van tenNCC.
                    String maNccFromProduct = firstNonEmpty(productDoc,
                            "maNCC", "MaNCC",
                            "maNhaCungCap", "MaNhaCungCap",
                            "maNcc", "MaNcc");
                    if (!TextUtils.isEmpty(maNccFromProduct)) {
                        repository.getSupplierById(maNccFromProduct).addOnSuccessListener(nccDoc -> {
                            if (nccDoc.exists()) {
                                String tenNCC = firstNonEmpty(nccDoc, "tenNCC", "TenNCC", "tenNhaCungCap", "TenNhaCungCap");
                                if (!TextUtils.isEmpty(tenNCC)) {
                                    tvNhaCungCap.setText(tenNCC);
                                }
                            }
                        });
                    }


                    if (ngaySanXuat == null) {
                        Date nsxFromProduct = firstDate(productDoc, "ngaySanXuat", "NgaySanXuat", "ngaySX", "NgaySX", "nsx", "NSX");
                        tvNgaySanXuat.setText(formatDate(nsxFromProduct));
                        if (nsxFromProduct != null) {
                            repository.updateLoHangNgaySanXuat(soLo, nsxFromProduct);
                        }
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(maNhapHang)) {
            repository.getNhapHangById(maNhapHang).addOnSuccessListener(nhapHangDoc -> {
                if (!nhapHangDoc.exists()) {
                    return;
                }

                String maNCC = firstNonEmpty(nhapHangDoc, "maNCC", "MaNCC");
                if (TextUtils.isEmpty(maNCC)) {
                    String tenNccLegacy = firstNonEmpty(nhapHangDoc, "tenNhaCungCap", "TenNhaCungCap");
                    if (!TextUtils.isEmpty(tenNccLegacy)) {
                        tvNhaCungCap.setText(tenNccLegacy);
                    }
                    return;
                }

                // Neu da co ten NCC tu SanPham thi khong can ghi de.
                CharSequence current = tvNhaCungCap.getText();
                if (current != null && !"-".contentEquals(current) && current.length() > 0) {
                    return;
                }

                repository.getSupplierById(maNCC).addOnSuccessListener(nccDoc -> {
                    if (!nccDoc.exists()) {
                        // Khong hien ma NCC, chi hien ten. Neu khong co ten thi giu '-'.
                        return;
                    }
                    String tenNCC = firstNonEmpty(nccDoc, "tenNCC", "TenNCC", "tenNhaCungCap", "TenNhaCungCap");
                    if (!TextUtils.isEmpty(tenNCC)) {
                        tvNhaCungCap.setText(tenNCC);
                    }
                });
            });
        }
    }

    private static String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0fđ", value);
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
        Double value = firstNumberNullable(snapshot, keys);
        return value != null ? value : 0d;
    }

    private static Double firstNumberNullable(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Number value = snapshot.getDouble(key);
            if (value != null) {
                return value.doubleValue();
            }
            Object raw = snapshot.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
            if (raw instanceof String) {
                try {
                    String normalized = normalizeNumericString((String) raw);
                    if (!TextUtils.isEmpty(normalized)) {
                        return Double.parseDouble(normalized);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private static String normalizeNumericString(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim()
                .replace(" ", "")
                .replace("đ", "")
                .replace("Đ", "")
                .replaceAll("[^0-9,.-]", "");
        if (TextUtils.isEmpty(cleaned)) {
            return null;
        }

        int commaCount = 0;
        for (int i = 0; i < cleaned.length(); i++) {
            if (cleaned.charAt(i) == ',') {
                commaCount++;
            }
        }

        if (cleaned.contains(",") && cleaned.contains(".")) {
            // Truong hop 1,234.56 -> bo dau phay ngan cach hang nghin.
            cleaned = cleaned.replace(",", "");
        } else if (commaCount > 1) {
            // Truong hop 1,234,567 -> bo tat ca dau phay.
            cleaned = cleaned.replace(",", "");
        } else if (commaCount == 1) {
            int commaIndex = cleaned.indexOf(',');
            int tailLength = cleaned.length() - commaIndex - 1;
            if (tailLength == 3) {
                // Truong hop 1,234 -> coi la ngan cach hang nghin.
                cleaned = cleaned.replace(",", "");
            } else {
                // Truong hop thap phan kieu VN 12,5 -> doi ve 12.5.
                cleaned = cleaned.replace(",", ".");
            }
        }
        return cleaned;
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
            if (raw instanceof Number) {
                return new Date(((Number) raw).longValue());
            }
        }
        return null;
    }

    private void syncLegacyLoHangFields(DocumentSnapshot doc,
                                        String soLo,
                                        String maSP,
                                        String maNhapHang,
                                        double soLuong,
                                        Date hanSuDung,
                                        Date ngaySanXuat) {
        if (TextUtils.isEmpty(soLo)) {
            return;
        }

        boolean needsCanonicalSync = !doc.contains("maSP")
                || !doc.contains("donGiaNhap")
                || !doc.contains("ngaySanXuat")
                || !doc.contains("soLo");

        if (!needsCanonicalSync) {
            return;
        }

        com.nhom5.pharma.feature.nhaphang.LoHang canonical = new com.nhom5.pharma.feature.nhaphang.LoHang();
        canonical.setSoLo(soLo);
        canonical.setMaSP(maSP);
        canonical.setMaNhapHang(maNhapHang);
        canonical.setSoLuong(soLuong);
        canonical.setHanSuDung(hanSuDung);
        canonical.setNgaySanXuat(ngaySanXuat);
        repository.upsertLoHang(soLo, canonical);
    }


    private static String defaultText(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }

    private static String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%,.0f", value);
    }


    private static String formatDate(Date value) {
        if (value == null) {
            return "-";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(value);
    }
}

