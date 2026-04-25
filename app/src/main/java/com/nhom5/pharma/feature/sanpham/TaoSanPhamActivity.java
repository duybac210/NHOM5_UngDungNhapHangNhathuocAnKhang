package com.nhom5.pharma.feature.sanpham;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.pharma.R;
import com.nhom5.pharma.util.SuccessDialogHelper;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Calendar;

public class TaoSanPhamActivity extends AppCompatActivity {

    private EditText etName, etCostPrice, etSellingPrice, etManufacturer, etCountry;
    private EditText etCreatedTime;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_san_pham);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupBackNavigation();
        setCreatedTimeNow();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> generateIDAndSave());
    }

    private void setupBackNavigation() {
        View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etProductName);
        etCreatedTime = findViewById(R.id.etCreatedTime);
        etCostPrice = findViewById(R.id.etCostPrice);
        etSellingPrice = findViewById(R.id.etSellingPrice);
        etManufacturer = findViewById(R.id.etManufacturer);
        etCountry = findViewById(R.id.etCountry);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        configureVietnameseInput();

        if (etCreatedTime != null) {
            etCreatedTime.setKeyListener(null);
            etCreatedTime.setFocusable(false);
        }
    }

    private void configureVietnameseInput() {
        int textFlags = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        if (etName != null) etName.setInputType(textFlags);
        if (etManufacturer != null) etManufacturer.setInputType(textFlags);
        if (etCountry != null) etCountry.setInputType(textFlags);
    }

    private void generateIDAndSave() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        db.collection("SanPham")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String nextId = Product.buildNextProductId(queryDocumentSnapshots.getDocuments());
                    saveProduct(nextId);
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProduct(String maID) {
        String costStr = etCostPrice.getText().toString().trim();
        String sellStr = etSellingPrice.getText().toString().trim();
        double costPrice = costStr.isEmpty() ? 0 : Double.parseDouble(costStr);
        double sellingPrice = sellStr.isEmpty() ? 0 : Double.parseDouble(sellStr);

        Map<String, Object> product = new HashMap<>();
        product.put("maID", maID);
        product.put("tenSP", etName.getText().toString().trim());
        product.put("giavon", costPrice);
        product.put("giaBan", sellingPrice);
        product.put("hangSX", etManufacturer.getText().toString().trim());
        product.put("nuocSX", etCountry.getText().toString().trim());
        product.put("trangThai", true);
        product.put("ngayTao", com.google.firebase.firestore.FieldValue.serverTimestamp());
        product.put("ngayCapNhat", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("SanPham").document(maID).set(product)
                .addOnSuccessListener(aVoid -> {
                    SuccessDialogHelper.showSuccessDialog(this, "Lưu thành công!", this::finish);
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setCreatedTimeNow() {
        if (etCreatedTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etCreatedTime.setText(sdf.format(Calendar.getInstance().getTime()));
        }
    }
}
