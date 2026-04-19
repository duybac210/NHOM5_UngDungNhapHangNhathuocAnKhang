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

    private TextView tvTen, tvMa, tvMST, tvSDT, tvEmail, tvDiaChi, tvGiaTri, tvTongDon;
    private EditText edtSDT, edtEmail, edtDiaChi, edtMaNCC, edtMST;
    private TextView tvEditTen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = NhaCungCapRepository.getInstance();
        
        try {
            ncc = (NhaCungCap) getIntent().getSerializableExtra("NHA_CUNG_CAP");
        } catch (Exception e) {
<<<<<<< HEAD
            Toast.makeText(this, "Lỗi truyền dữ liệu", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        
        if (ncc == null) {
            onBackPressed();
=======
            Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
            return;
        }
        
        showDetailLayout();
    }

    private void showDetailLayout() {
        setContentView(R.layout.activity_chi_tiet_nha_cung_cap);
        initDetailViews();
        bindDataToDetail();
<<<<<<< HEAD
        
        // SỬA LỖI VĂNG: Sử dụng onBackPressed() thay vì finish()
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
        
        View btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) btnDelete.setOnClickListener(v -> showDeleteDialog());
        
        View btnEdit = findViewById(R.id.btnEdit);
        if (btnEdit != null) btnEdit.setOnClickListener(v -> showEditLayout());
=======
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteDialog());
        findViewById(R.id.btnEdit).setOnClickListener(v -> showEditLayout());
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
    }

    private void showEditLayout() {
        setContentView(R.layout.activity_edit_nha_cung_cap);
        initEditViews();
        bindDataToEdit();
<<<<<<< HEAD
        
        // Quay lại màn hình Chi tiết (Không đóng Activity)
=======
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
        findViewById(R.id.btnBackEdit).setOnClickListener(v -> showDetailLayout());
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> showDetailLayout());
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveChanges());
    }

    private void initDetailViews() {
        tvTen = findViewById(R.id.tvTenHeader);
        tvMa = findViewById(R.id.tvMaHeader);
        tvMST = findViewById(R.id.tvDetailMST);
        tvSDT = findViewById(R.id.tvDetailSDT);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvDiaChi = findViewById(R.id.tvDetailDiaChi);
        tvGiaTri = findViewById(R.id.tvDetailGiaTri);
        tvTongDon = findViewById(R.id.tvDetailTongDon);
    }

    private void bindDataToDetail() {
<<<<<<< HEAD
        tvTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "---");
        tvMa.setText("Mã NCC: " + (ncc.getId() != null ? ncc.getId() : "N/A"));
        tvMST.setText(ncc.getMaSoThue() != null ? ncc.getMaSoThue().toString() : "---");
        tvSDT.setText(ncc.getSdt() != null ? ncc.getSdt() : "---");
        tvEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "---");
        tvDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "---");
        tvTongDon.setText(ncc.fetchDisplayTongDon());
        tvGiaTri.setText(ncc.fetchDisplayGiaTri());
=======
        if (ncc == null) return;
        tvTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "Chưa có tên");
        tvMa.setText("Mã NCC: " + (ncc.getId() != null ? ncc.getId() : "N/A"));
        tvMST.setText(ncc.getMaSoThue() != null ? ncc.getMaSoThue() : "---");
        tvSDT.setText(ncc.getSdt() != null ? ncc.getSdt() : "---");
        tvEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "---");
        tvDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "---");
        tvTongDon.setText(ncc.getDisplayTongDon());
        tvGiaTri.setText(ncc.getDisplayGiaTri());
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
    }

    private void initEditViews() {
        tvEditTen = findViewById(R.id.tvEditTenNCC);
        edtMaNCC = findViewById(R.id.edtEditMaNCC);
        edtMST = findViewById(R.id.edtEditMST);
        edtSDT = findViewById(R.id.edtEditSDT);
        edtEmail = findViewById(R.id.edtEditEmail);
        edtDiaChi = findViewById(R.id.edtEditDiaChi);
    }

    private void bindDataToEdit() {
<<<<<<< HEAD
        tvEditTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "");
        edtMaNCC.setText(ncc.getId() != null ? ncc.getId() : "");
        edtMST.setText(ncc.getMaSoThue() != null ? ncc.getMaSoThue().toString() : "");
        edtSDT.setText(ncc.getSdt() != null ? ncc.getSdt() : "");
        edtEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "");
        edtDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "");
    }

    private void saveChanges() {
        ncc.setSdt(edtSDT.getText().toString().trim());
        ncc.setEmail(edtEmail.getText().toString().trim());
        ncc.setDiaChi(edtDiaChi.getText().toString().trim());
        ncc.setNgayCapNhat(new Date());

=======
        if (ncc == null) return;
        tvEditTen.setText(ncc.getTenNCC() != null ? ncc.getTenNCC() : "");
        edtMaNCC.setText(ncc.getId() != null ? ncc.getId() : "");
        edtMST.setText(ncc.getMaSoThue() != null ? ncc.getMaSoThue() : "");
        edtSDT.setText(ncc.getSdt() != null ? ncc.getSdt() : "");
        edtEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "");
        edtDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "");
        tvEditTongDon.setText(ncc.getDisplayTongDon());
        tvEditGiaTri.setText(ncc.getDisplayGiaTri());
    }

    private void saveChanges() {
        if (ncc == null) return;
        ncc.setSdt(edtSDT.getText().toString().trim());
        ncc.setEmail(edtEmail.getText().toString().trim());
        ncc.setDiaChi(edtDiaChi.getText().toString().trim());
        
        // TỰ ĐỘNG CẬP NHẬT NGÀY GIỜ SỬA
        ncc.setNgayCapNhat(new Date());
        
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
        repository.updateNhaCungCap(ncc).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            showDetailLayout();
        }).addOnFailureListener(e -> {
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
<<<<<<< HEAD
        tvMsg.setText("Hệ thống sẽ xóa hoàn toàn nhà cung cấp " + (ncc.getTenNCC() != null ? ncc.getTenNCC() : "") + " nhưng vẫn giữ những giao dịch lịch sử nếu có. Bạn có chắc là muốn xóa?");
=======
        String name = ncc.getTenNCC() != null ? ncc.getTenNCC() : "nhà cung cấp này";
        tvMsg.setText("Hệ thống sẽ xóa hoàn toàn " + name + " nhưng vẫn giữ những giao dịch lịch sử nếu có. Bạn có chắc là muốn xóa?");

>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnSkip).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            repository.deactivateNhaCungCap(ncc.getId()).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã xóa nhà cung cấp", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish(); // Ở đây dùng finish() là đúng vì quay lại danh sách
            });
        });
        dialog.show();
    }
}
