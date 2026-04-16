package com.nhom5.pharma.feature.nhaphang;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom5.pharma.MainActivity;
import com.nhom5.pharma.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaoDonNhapActivity extends AppCompatActivity {

    private static final Pattern IMPORT_ID_PATTERN = Pattern.compile("^NH(\\d+)$");

    private RecyclerView recyclerView;
    private SelectedProductAdapter adapter;
    private List<SelectedProduct> selectedProducts = new ArrayList<>();
    private TextView tvTotalLabel;
    private Spinner spnStatus, spnPayment, spnSupplier;
    private Button btnAddBatch, btnSave;
    private EditText etImportDate;
    private Calendar calendar = Calendar.getInstance();
    private FirebaseFirestore db;
    private List<String> supplierIds = new ArrayList<>();
    private List<String> supplierNames = new ArrayList<>();
    private ArrayAdapter<String> supplierAdapter;
    private double currentTotal = 0;

    private final ActivityResultLauncher<Intent> pickProductLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String id = result.getData().getStringExtra("product_id");
                    String name = result.getData().getStringExtra("product_name");
                    double price = result.getData().getDoubleExtra("product_price", 0);
                    addProduct(new SelectedProduct(id, name, price, 1));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_don_nhap);
        db = FirebaseFirestore.getInstance();

        initViews();
        setupBackNavigation();
        setupRecyclerView();
        setupSpinners();
        loadSuppliersFromFirebase();
        updateAddBatchButtonVisibility();

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddProduct).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("SELECT_MODE", true);
            pickProductLauncher.launch(intent);
        });

        etImportDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveOrderWithAutoIncrementId());
        updateDateLabel();
    }

    private void setupBackNavigation() {
        findViewById(R.id.ivBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewSelectedProducts);
        tvTotalLabel = findViewById(R.id.tvTotalLabel);
        spnStatus = findViewById(R.id.spnStatus);
        spnPayment = findViewById(R.id.spnPayment);
        spnSupplier = findViewById(R.id.spnSupplier);
        btnAddBatch = findViewById(R.id.btnAddBatch);
        etImportDate = findViewById(R.id.etImportDate);
        btnSave = findViewById(R.id.btnSave);
        etImportDate.setFocusable(false);

        // Khởi tạo Adapter rỗng ngay từ đầu để tránh NullPointerException khi UI đo đạc (measure)
        supplierAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, supplierNames);
        supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSupplier.setAdapter(supplierAdapter);
    }

    private void loadSuppliersFromFirebase() {
        db.collection("NhaCungCap").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                supplierIds.clear();
                supplierNames.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("tenNhaCungCap");
                    if (name == null || name.trim().isEmpty()) {
                        name = document.getString("tenNCC");
                    }
                    if (name == null || name.trim().isEmpty()) {
                        name = document.getString("ten");
                    }
                    if (name != null) {
                        supplierIds.add(document.getId());
                        supplierNames.add(name);
                    }
                }
                if (supplierNames.isEmpty()) {
                    supplierIds.add("");
                    supplierNames.add("Chưa có nhà cung cấp");
                }
                supplierAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Không thể tải danh sách nhà cung cấp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrderWithAutoIncrementId() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        db.collection("NhapHang")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> saveOrderToFirebase(buildNextImportId(queryDocumentSnapshots.getDocuments())))
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String buildNextImportId(List<? extends DocumentSnapshot> documents) {
        long maxNumber = 0;
        int maxDigits = 4;

        for (DocumentSnapshot document : documents) {
            String[] candidates = new String[] { document.getId(), document.getString("maID") };
            for (String candidate : candidates) {
                long number = extractImportIdNumber(candidate);
                if (number < 0) {
                    continue;
                }

                maxNumber = Math.max(maxNumber, number);
                maxDigits = Math.max(maxDigits, candidate.trim().length() - 2);
            }
        }

        return String.format(Locale.getDefault(), "NH%0" + maxDigits + "d", maxNumber + 1);
    }

    private long extractImportIdNumber(String rawId) {
        if (rawId == null) {
            return -1;
        }

        Matcher matcher = IMPORT_ID_PATTERN.matcher(rawId.trim());
        if (!matcher.matches()) {
            return -1;
        }

        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void saveOrderToFirebase(String customId) {
        int supplierPos = spnSupplier.getSelectedItemPosition();
        if (supplierPos < 0 || supplierNames.isEmpty() || "Chưa có nhà cung cấp".equals(supplierNames.get(0))) {
            Toast.makeText(this, "Vui lòng chọn nhà cung cấp", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            return;
        }

        Map<String, Object> order = new HashMap<>();
        int statusValue = spnStatus.getSelectedItemPosition() == 1 ? 1 : 0;
        order.put("maNhaCungCap", supplierIds.get(supplierPos));
        order.put("tenNhaCungCap", supplierNames.get(supplierPos));
        order.put("maID", customId);
        order.put("ngayNhap", new Timestamp(calendar.getTime()));
        order.put("trangThai", statusValue);
        order.put("trangThaiText", spnStatus.getSelectedItem().toString());
        order.put("hinhThucThanhToan", spnPayment.getSelectedItem().toString());
        order.put("tongTien", currentTotal);
        order.put("createdAt", FieldValue.serverTimestamp());

        List<Map<String, Object>> details = new ArrayList<>();
        for (SelectedProduct p : selectedProducts) {
            Map<String, Object> item = new HashMap<>();
            item.put("maSanPham", p.getMaSanPham());
            item.put("tenSanPham", p.getTenSanPham());
            item.put("soLuong", p.getSoLuong());
            item.put("donGia", p.getDonGia());
            details.add(item);
        }
        order.put("chiTiet", details);

        if (customId != null) {
            db.collection("NhapHang").document(customId).set(order)
                    .addOnSuccessListener(aVoid -> onSaveSuccess())
                    .addOnFailureListener(this::onSaveFailure);
        } else {
            db.collection("NhapHang").add(order)
                    .addOnSuccessListener(documentReference -> onSaveSuccess())
                    .addOnFailureListener(this::onSaveFailure);
        }
    }

    private void onSaveSuccess() {
        Toast.makeText(this, "Lưu đơn nhập thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onSaveFailure(Exception e) {
        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        btnSave.setEnabled(true);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etImportDate.setText(sdf.format(calendar.getTime()));
    }

    private void setupSpinners() {
        String[] statusArray = {"Đã hủy", "Đã nhập kho"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusArray);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(statusAdapter);

        String[] paymentArray = {"Chưa thanh toán", "Thanh toán một phần", "Đã thanh toán"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentArray);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPayment.setAdapter(paymentAdapter);
    }

    private void setupRecyclerView() {
        adapter = new SelectedProductAdapter(selectedProducts, () -> {
            updateTotal();
            updateAddBatchButtonVisibility();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void addProduct(SelectedProduct newProduct) {
        boolean exists = false;
        for (SelectedProduct p : selectedProducts) {
            if (p.getMaSanPham().equals(newProduct.getMaSanPham())) {
                p.setSoLuong(p.getSoLuong() + 1);
                exists = true; break;
            }
        }
        if (!exists) selectedProducts.add(newProduct);
        adapter.notifyDataSetChanged();
        updateTotal();
        updateAddBatchButtonVisibility();
    }

    private void updateAddBatchButtonVisibility() {
        btnAddBatch.setVisibility(selectedProducts.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateTotal() {
        currentTotal = 0;
        for (SelectedProduct p : selectedProducts) currentTotal += p.getDonGia() * p.getSoLuong();
        tvTotalLabel.setText("TỔNG GIÁ TRỊ ĐƠN NHẬP\n" + String.format("%,.0f", currentTotal) + "đ");
    }
}