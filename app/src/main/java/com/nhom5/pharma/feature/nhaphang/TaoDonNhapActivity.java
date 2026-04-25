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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom5.pharma.MainActivity;
import com.nhom5.pharma.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaoDonNhapActivity extends AppCompatActivity {

    private static final Pattern IMPORT_ID_PATTERN = Pattern.compile("^NH(\\d+)$");
    private static final String DEFAULT_MA_NGUOI_NHAP = "USER003";

    private RecyclerView recyclerView;
    private SelectedProductAdapter adapter;
    private List<SelectedProduct> selectedProducts = new ArrayList<>();
    private TextView tvTotalLabel;
    private Spinner spnStatus, spnPayment, spnSupplier;
    private Button btnAddBatch, btnSave;
    private EditText etImportDate;
    private Calendar calendar = Calendar.getInstance();
    private FirebaseFirestore db;
    private NhapHangRepository repository;
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

    // Launcher cho màn hình Thêm Lô Hàng
    private final ActivityResultLauncher<Intent> addBatchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String productId = result.getData().getStringExtra("product_id");
                    long mfgDate = result.getData().getLongExtra("mfg_date", 0);
                    long expDate = result.getData().getLongExtra("exp_date", 0);
                    double price = result.getData().getDoubleExtra("price", 0);
                    double quantity = result.getData().getDoubleExtra("quantity", 0);

                    // Cập nhật trạng thái lô cho thuốc tương ứng
                    for (SelectedProduct p : selectedProducts) {
                        if (p.getMaSanPham().equals(productId)) {
                            LoHang loHang = new LoHang();
                            loHang.setMaSP(productId);
                            loHang.setNgaySanXuat(new java.util.Date(mfgDate));
                            loHang.setHanSuDung(new java.util.Date(expDate));
                            loHang.setDonGiaNhap(price);
                            loHang.setSoLuong(quantity);
                            p.addLoHang(loHang);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_don_nhap);
        db = FirebaseFirestore.getInstance();
        repository = NhapHangRepository.getInstance();

        initViews();
        setupBackNavigation();
        setupRecyclerView();
        setupSpinners();
        loadSuppliersFromFirebase();
        updateAddBatchButtonVisibility();

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddProduct).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_SELECT_MODE, true);
            intent.putExtra(MainActivity.EXTRA_START_TAB, 1);
            pickProductLauncher.launch(intent);
        });

        btnAddBatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemLoHangActivity.class);
            
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            for (SelectedProduct p : selectedProducts) {
                names.add(p.getTenSanPham());
                ids.add(p.getMaSanPham());
            }
            intent.putStringArrayListExtra("SELECTED_PRODUCT_NAMES", names);
            intent.putStringArrayListExtra("SELECTED_PRODUCT_IDS", ids);
            
            addBatchLauncher.launch(intent);
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
                    String name = document.getString("tenNCC");
                    if (name == null || name.trim().isEmpty()) {
                        name = document.getString("tenNhaCungCap");
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
        Set<Long> usedNumbers = new HashSet<>();
        for (DocumentSnapshot document : documents) {
            String maIdField = document.getString("maID");
            String[] candidates = new String[] { maIdField, document.getId() };
            for (String candidate : candidates) {
                long number = extractImportIdNumber(candidate);
                if (number > 0) usedNumbers.add(number);
            }
        }
        long nextNumber = 1;
        while (usedNumbers.contains(nextNumber)) nextNumber++;
        return String.format(Locale.getDefault(), "NH%04d", nextNumber);
    }

    private long extractImportIdNumber(String rawId) {
        if (rawId == null) return -1;
        Matcher matcher = IMPORT_ID_PATTERN.matcher(rawId.trim());
        if (!matcher.matches()) return -1;
        try { return Long.parseLong(matcher.group(1)); } 
        catch (NumberFormatException e) { return -1; }
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
        String supplierId = supplierIds.get(supplierPos);

        order.put("maNCC", supplierId);
        order.put("maID", customId);
        order.put("maNguoiNhap", DEFAULT_MA_NGUOI_NHAP);
        order.put("ngayNhap", new Timestamp(calendar.getTime()));
        order.put("ngayTao", new Timestamp(calendar.getTime()));
        order.put("ngayCapNhat", FieldValue.serverTimestamp());
        order.put("trangThai", statusValue);
        order.put("trangThaiText", statusValue == 1 ? "Đã nhập kho" : "Đã hủy");
        order.put("tongTien", currentTotal);

        WriteBatch batch = db.batch();
        DocumentReference orderRef = db.collection("NhapHang").document(customId.trim());
        batch.set(orderRef, order);

        // Luu Lo hang: tu dong tao soLo tang dan theo database (global).
        final ArrayList<LoHang> flatList = flattenLoHangs();
        generateSoLoAndAddToBatch(batch, customId, flatList, 0)
                .addOnSuccessListener(unused -> batch.commit()
                        .addOnSuccessListener(aVoid -> onSaveSuccess())
                        .addOnFailureListener(this::onSaveFailure))
                .addOnFailureListener(this::onSaveFailure);
    }

    private ArrayList<LoHang> flattenLoHangs() {
        ArrayList<LoHang> flat = new ArrayList<>();
        for (SelectedProduct product : selectedProducts) {
            flat.addAll(product.getLoHangs());
        }
        return flat;
    }

    /**
     * Tao soLo theo counter tren Firestore (NhapHangRepository.generateNextSoLo),
     * add vao WriteBatch, chua commit. Chay de quy tu 0..n-1.
     */
    private com.google.android.gms.tasks.Task<Void> generateSoLoAndAddToBatch(
            WriteBatch batch,
            String maNhapHang,
            ArrayList<LoHang> loHangs,
            int index
    ) {
        if (index >= loHangs.size()) {
            return com.google.android.gms.tasks.Tasks.forResult(null);
        }

        LoHang lo = loHangs.get(index);
        return repository.generateNextSoLo()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException() != null ? task.getException() : new RuntimeException("Khong tao duoc so lo");
                        return com.google.android.gms.tasks.Tasks.forException(e);
                    }
                    String soLo = task.getResult();
                    DocumentReference loRef = db.collection("LoHang").document(soLo);

                    Map<String, Object> data = lo.toFirestoreMap();
                    data.put("soLo", soLo);
                    data.put("maNhapHang", maNhapHang);
                    batch.set(loRef, data);

                    return generateSoLoAndAddToBatch(batch, maNhapHang, loHangs, index + 1);
                });
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
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
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
        spnStatus.setSelection(1);

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