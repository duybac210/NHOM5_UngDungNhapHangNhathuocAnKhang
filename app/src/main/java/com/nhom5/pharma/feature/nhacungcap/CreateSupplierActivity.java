package com.nhom5.pharma.feature.nhacungcap;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.pharma.R;
import com.nhom5.pharma.util.SuccessDialogHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateSupplierActivity extends AppCompatActivity {

    private static final Pattern SUPPLIER_ID_PATTERN = Pattern.compile("^NCC(\\d+)$");

    private EditText etName, etPhone, etEmail, etAddress;
    private Button btnLuu, btnBoQua;
    private ImageView ivBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_supplier);

        db = FirebaseFirestore.getInstance();
        initViews();

        ivBack.setOnClickListener(v -> finish());
        btnBoQua.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> generateNCCIDAndSave());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        btnLuu = findViewById(R.id.btnLuu);
        btnBoQua = findViewById(R.id.btnBoQua);
        ivBack = findViewById(R.id.ivBack);

        configureVietnameseInput();
    }

    private void configureVietnameseInput() {
        int textFlags = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_VARIATION_NORMAL;
        etName.setInputType(textFlags);
        etAddress.setInputType(textFlags | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    private void generateNCCIDAndSave() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên nhà cung cấp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLuu.setEnabled(false);
        db.collection("NhaCungCap")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    saveSupplier(buildNextSupplierId(queryDocumentSnapshots.getDocuments()));
                })
                .addOnFailureListener(e -> {
                    btnLuu.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String buildNextSupplierId(List<? extends DocumentSnapshot> documents) {
        long maxNumber = 0;
        int maxDigits = 4;

        for (DocumentSnapshot document : documents) {
            String[] candidates = new String[] { document.getId(), document.getString("maID") };
            for (String candidate : candidates) {
                long number = extractSupplierNumber(candidate);
                if (number < 0) continue;

                maxNumber = Math.max(maxNumber, number);
                maxDigits = Math.max(maxDigits, candidate.trim().length() - 3);
            }
        }

        return String.format("NCC%0" + maxDigits + "d", maxNumber + 1);
    }

    private long extractSupplierNumber(String rawId) {
        if (rawId == null) return -1;

        Matcher matcher = SUPPLIER_ID_PATTERN.matcher(rawId.trim());
        if (!matcher.matches()) return -1;

        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void saveSupplier(String maID) {
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("tenNCC", etName.getText().toString().trim());
        supplier.put("sdt", etPhone.getText().toString().trim());
        supplier.put("email", etEmail.getText().toString().trim());
        supplier.put("diaChi", etAddress.getText().toString().trim());
        supplier.put("maSoThue", "");
        supplier.put("trangThai", true);
        supplier.put("soLuong", 0);
        supplier.put("tongMua", 0);
        supplier.put("ngayTao", FieldValue.serverTimestamp());
        supplier.put("ngayCapNhat", FieldValue.serverTimestamp());

        db.collection("NhaCungCap").document(maID).set(supplier)
                .addOnSuccessListener(aVoid -> {
                    SuccessDialogHelper.showSuccessDialog(this, "Thêm nhà cung cấp thành công!", () -> {
                        new Handler().postDelayed(this::finish, 1500);
                    });
                })
                .addOnFailureListener(e -> {
                    btnLuu.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
