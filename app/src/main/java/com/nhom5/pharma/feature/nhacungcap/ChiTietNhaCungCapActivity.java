package com.nhom5.pharma.feature.nhacungcap;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nhom5.pharma.R;
import java.util.Date;

public class ChiTietNhaCungCapActivity extends AppCompatActivity {

    private NhaCungCap ncc;
    private NhaCungCapRepository repository;
    private String nccId;

    // View cho màn hình chi tiết
    private TextView tvTen, tvMa, tvMST, tvSDT, tvEmail, tvDiaChi, tvGiaTri, tvTongDon;
    
    // View cho màn hình chỉnh sửa
    private EditText edtSDT, edtEmail, edtDiaChi, edtMaNCC, edtMST;
    private TextView tvEditTen, tvEditTongDon, tvEditGiaTri;
    
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. PHẢI CÓ SETCONTENTVIEW NGAY TRONG ONCREATE
        setContentView(R.layout.activity_chi_tiet_nha_cung_cap);
        
        repository = NhaCungCapRepository.getInstance();
        
        // 2. NHẬN ID TỪ INTENT
        nccId = getIntent().getStringExtra("NCC_ID");
        if (nccId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID nhà cung cấp", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 3. KHỞI TẠO CÁC VIEW VÀ NÚT BẤM CỦA LAYOUT CHI TIẾT
        initDetailViews();
        setupButtons();
        
        // 4. TẢI DỮ LIỆU TỪ FIREBASE
        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        repository.getNhaCungCapById(nccId).addOnSuccessListener(documentSnapshot -> {
            // Kiểm tra an toàn: Nếu Activity đang đóng thì không làm gì
            if (isFinishing() || isDestroyed()) return;
            
            ncc = documentSnapshot.toObject(NhaCungCap.class);
            if (ncc != null) {
                ncc.setId(documentSnapshot.getId());
                if (!isEditMode) {
                    bindDataToDetail();
                } else {
                    bindDataToEdit();
                }
            } else {
                Toast.makeText(this, "Dữ liệu không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            if (isFinishing() || isDestroyed()) return;
            Toast.makeText(this, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupButtons() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        View btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) btnDelete.setOnClickListener(v -> {
            com.nhom5.pharma.util.RoleHelper.checkIsManager(isManager -> {
                if (isManager) {
                    showDeleteDialog();
                } else {
                    Toast.makeText(this, "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        View btnEdit = findViewById(R.id.btnEdit);
        if (btnEdit != null) btnEdit.setOnClickListener(v -> {
            com.nhom5.pharma.util.RoleHelper.checkIsManager(isManager -> {
                if (isManager) {
                    showEditLayout();
                } else {
                    Toast.makeText(this, "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDetailLayout() {
        isEditMode = false;
        setContentView(R.layout.activity_chi_tiet_nha_cung_cap);
        initDetailViews();
        setupButtons();
        if (ncc != null) bindDataToDetail();
    }

    private void showEditLayout() {
        isEditMode = true;
        setContentView(R.layout.activity_edit_nha_cung_cap);
        initEditViews();
        
        View btnBackEdit = findViewById(R.id.btnBackEdit);
        if (btnBackEdit != null) btnBackEdit.setOnClickListener(v -> showDetailLayout());
        
        View btnCancelEdit = findViewById(R.id.btnCancelEdit);
        if (btnCancelEdit != null) btnCancelEdit.setOnClickListener(v -> showDetailLayout());
        
        View btnSaveEdit = findViewById(R.id.btnSaveEdit);
        if (btnSaveEdit != null) btnSaveEdit.setOnClickListener(v -> saveChanges());
        
        if (ncc != null) bindDataToEdit();
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            showDetailLayout();
        } else {
            super.onBackPressed();
        }
    }

    private void initDetailViews() {
        tvTen = findViewById(R.id.tvDetailTenNCC);
        tvMa = findViewById(R.id.tvDetailMaNCC);
        tvMST = findViewById(R.id.tvDetailMST);
        tvSDT = findViewById(R.id.tvDetailSDT);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvDiaChi = findViewById(R.id.tvDetailDiaChi);
        tvGiaTri = findViewById(R.id.tvDetailGiaTri);
        tvTongDon = findViewById(R.id.tvDetailTongDon);
    }

    private void bindDataToDetail() {
        if (ncc == null) return;
        // Kiểm tra từng View trước khi setText để tránh crash
        if (tvTen != null) tvTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "---");
        if (tvMa != null) tvMa.setText("Mã NCC: " + (ncc.getId() != null ? ncc.getId() : "N/A"));
        if (tvMST != null) tvMST.setText(ncc.fetchMaSoThue());
        if (tvSDT != null) tvSDT.setText(ncc.fetchSdt());
        if (tvEmail != null) tvEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "---");
        if (tvDiaChi != null) tvDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "---");
        if (tvTongDon != null) tvTongDon.setText(ncc.fetchDisplayTongDon());
        if (tvGiaTri != null) tvGiaTri.setText(ncc.fetchDisplayGiaTri());
    }

    private void initEditViews() {
        tvEditTen = findViewById(R.id.tvEditTenNCC);
        edtMaNCC = findViewById(R.id.edtEditMaNCC);
        edtMST = findViewById(R.id.edtEditMST);
        edtSDT = findViewById(R.id.edtEditSDT);
        edtEmail = findViewById(R.id.edtEditEmail);
        edtDiaChi = findViewById(R.id.edtEditDiaChi);
        
        // Ánh xạ thêm 2 view thống kê ở màn hình sửa
        tvEditTongDon = findViewById(R.id.tvEditTongDon);
        tvEditGiaTri = findViewById(R.id.tvEditGiaTri);
    }

    private void bindDataToEdit() {
        if (ncc == null) return;
        if (tvEditTen != null) tvEditTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "");
        if (edtMaNCC != null) edtMaNCC.setText(ncc.getId() != null ? ncc.getId() : "");
        if (edtMST != null) edtMST.setText(ncc.fetchMaSoThue());
        if (edtSDT != null) edtSDT.setText(ncc.getSdt() != null ? String.valueOf(ncc.getSdt()) : "");
        if (edtEmail != null) edtEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "");
        if (edtDiaChi != null) edtDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "");
        
        // Đồng bộ dữ liệu thống kê sang màn hình sửa
        if (tvEditTongDon != null) tvEditTongDon.setText(ncc.fetchDisplayTongDon());
        if (tvEditGiaTri != null) tvEditGiaTri.setText(ncc.fetchDisplayGiaTri());
    }

    private void saveChanges() {
        if (ncc == null || edtSDT == null) return;

        ncc.setSdt(edtSDT.getText().toString().trim());
        ncc.setEmail(edtEmail.getText().toString().trim());
        ncc.setDiaChi(edtDiaChi.getText().toString().trim());
        ncc.setNgayCapNhat(new Date());

        repository.updateNhaCungCap(ncc).addOnSuccessListener(aVoid -> {
            if (isFinishing() || isDestroyed()) return;
            com.nhom5.pharma.feature.history.LogRepository.getInstance().logUpdate("NHACUNGCAP", ncc.getId(), "Cập nhật nhà cung cấp: " + ncc.getTenNCC());
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            showDetailLayout();
        }).addOnFailureListener(e -> {
            if (isFinishing() || isDestroyed()) return;
            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteDialog() {
        if (ncc == null) return;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete_ncc);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER); 
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        TextView tvMsg = dialog.findViewById(R.id.tvDeleteMessage);
        if (tvMsg != null) {
            String name = ncc.getTenNCC() != null ? ncc.getTenNCC() : "";
            tvMsg.setText("Hệ thống sẽ xóa hoàn toàn nhà cung cấp " + name + "?");
        }
        
        View btnClose = dialog.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        
        View btnSkip = dialog.findViewById(R.id.btnSkip);
        if (btnSkip != null) btnSkip.setOnClickListener(v -> dialog.dismiss());
        
        View btnConfirm = dialog.findViewById(R.id.btnConfirmDelete);
        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> {
            repository.deactivateNhaCungCap(ncc.getId()).addOnSuccessListener(aVoid -> {
                if (isFinishing() || isDestroyed()) return;
                com.nhom5.pharma.feature.history.LogRepository.getInstance().logDelete("NHACUNGCAP", ncc.getId(), "Xóa (ngừng hoạt động) nhà cung cấp: " + ncc.getTenNCC());
                Toast.makeText(this, "Đã xóa nhà cung cấp", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }).addOnFailureListener(e -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, "Lỗi khi xóa nhà cung cấp", Toast.LENGTH_SHORT).show();
            });
        });
        dialog.show();
    }
}
