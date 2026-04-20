package com.nhom5.pharma.feature.sanpham;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.pharma.R;
import com.nhom5.pharma.util.SuccessDialogHelper;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Locale;
import java.util.Calendar;
import java.util.List;

public class TaoSanPhamActivity extends AppCompatActivity {

    private EditText etName, etCostPrice, etSellingPrice, etManufacturer, etCountry;
    private EditText etCreatedTime;
    private Spinner spnSupplier;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private final List<String> supplierIds = new ArrayList<>();
    private final List<String> supplierNames = new ArrayList<>();
    private ArrayAdapter<String> supplierAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_san_pham);

        db = FirebaseFirestore.getInstance();
        ProductSchemaSync.syncOnce(db);
        initViews();
        setupBackNavigation();
        loadSuppliers();
        setCreatedTimeNow();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> generateIDAndSave());
    }

    private void setupBackNavigation() {
        findViewById(R.id.ivBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initViews() {
        etName = findViewById(R.id.etProductName);
        etCreatedTime = findViewById(R.id.etCreatedTime);
        etCostPrice = findViewById(R.id.etCostPrice);
        etSellingPrice = findViewById(R.id.etSellingPrice);
        etManufacturer = findViewById(R.id.etManufacturer);
        etCountry = findViewById(R.id.etCountry);
        spnSupplier = findViewById(R.id.spnSupplier);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        supplierAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, supplierNames);
        supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSupplier.setAdapter(supplierAdapter);

        configureVietnameseInput();

        // Thời gian tạo chỉ hiển thị tự động theo ngày hiện tại, không cho chỉnh tay.
        etCreatedTime.setKeyListener(null);
        etCreatedTime.setClickable(false);
    }

    private void configureVietnameseInput() {
        int textFlags = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_VARIATION_NORMAL;
        etName.setInputType(textFlags);
        etManufacturer.setInputType(textFlags);
        etCountry.setInputType(textFlags);
    }

    private void generateIDAndSave() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (supplierIds.isEmpty() || spnSupplier.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Vui lòng chọn nhà cung cấp", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedSupplierId = supplierIds.get(spnSupplier.getSelectedItemPosition());
        if (selectedSupplierId == null || selectedSupplierId.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn nhà cung cấp hợp lệ", Toast.LENGTH_SHORT).show();
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
        double costPrice = Double.parseDouble(etCostPrice.getText().toString().trim().isEmpty() ? "0" : etCostPrice.getText().toString());
        double sellingPrice = Double.parseDouble(etSellingPrice.getText().toString().trim().isEmpty() ? "0" : etSellingPrice.getText().toString());

        Map<String, Object> product = new HashMap<>();
        product.put("maID", maID);
        product.put("maNCC", supplierIds.get(spnSupplier.getSelectedItemPosition()).trim());
        product.put("tenSP", etName.getText().toString().trim());
        product.put("giavon", costPrice);
        product.put("giaBan", sellingPrice);
        product.put("maVach", "");
        product.put("moTa", "");
        product.put("hangSX", etManufacturer.getText().toString().trim());
        product.put("nuocSX", etCountry.getText().toString().trim());
        product.put("trangThai", true);
        product.put("ngayTao", com.google.firebase.firestore.FieldValue.serverTimestamp());
        product.put("ngayCapNhat", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("SanPham").document(maID).set(product)
                .addOnSuccessListener(aVoid -> {
                    SuccessDialogHelper.showSuccessDialog(this, "Tạo sản phẩm thành công!", () -> {
                        new Handler().postDelayed(this::finish, 1500);
                    });
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setCreatedTimeNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etCreatedTime.setText(sdf.format(Calendar.getInstance().getTime()));
    }

    private void loadSuppliers() {
        db.collection("NhaCungCap").get().addOnSuccessListener(snapshot -> {
            supplierIds.clear();
            supplierNames.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                String name = doc.getString("tenNCC");
                if (name == null || name.trim().isEmpty()) {
                    name = doc.getString("tenNhaCungCap");
                }
                if (name == null || name.trim().isEmpty()) {
                    name = doc.getId();
                }
                supplierIds.add(doc.getId());
                supplierNames.add(name);
            }
            if (supplierNames.isEmpty()) {
                supplierIds.add("");
                supplierNames.add("Chưa có nhà cung cấp");
            }
            supplierAdapter.notifyDataSetChanged();
        });
    }
}
