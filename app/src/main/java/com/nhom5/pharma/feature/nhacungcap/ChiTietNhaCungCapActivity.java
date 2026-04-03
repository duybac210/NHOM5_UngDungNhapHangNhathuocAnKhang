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

public class ChiTietNhaCungCapActivity extends AppCompatActivity {

    private NhaCungCap ncc;
    private NhaCungCapRepository repository;

    // View cho màn hình Chi tiết
    private TextView tvTen, tvMa, tvMST, tvSDT, tvEmail, tvDiaChi, tvGiaTri, tvTongDon;
    
    // View cho màn hình Chỉnh sửa
    private EditText edtSDT, edtEmail, edtDiaChi, edtMaNCC, edtMST;
    private TextView tvEditTen, tvEditTongDon, tvEditGiaTri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = NhaCungCapRepository.getInstance();
        ncc = (NhaCungCap) getIntent().getSerializableExtra("NHA_CUNG_CAP");
        showDetailLayout();
    }

    private void showDetailLayout() {
        setContentView(R.layout.activity_chi_tiet_nha_cung_cap);
        initDetailViews();
        bindDataToDetail();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteDialog());
        findViewById(R.id.btnEdit).setOnClickListener(v -> showEditLayout());
    }

    private void showEditLayout() {
        setContentView(R.layout.activity_edit_nha_cung_cap);
        initEditViews();
        bindDataToEdit();

        findViewById(R.id.btnBackEdit).setOnClickListener(v -> showDetailLayout());
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> showDetailLayout());
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveChanges());
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
        tvTen.setText(ncc.getTenNCC());
        tvMa.setText("Mã NCC: " + ncc.getId());
        tvMST.setText("Mã số thuế: " + ncc.getMaSoThue());
        tvSDT.setText(ncc.getSdt());
        tvEmail.setText(ncc.getEmail());
        tvDiaChi.setText(ncc.getDiaChi());
        
        // Lấy dữ liệu thật từ field TongDon và GiaTri trên Firestore
        tvTongDon.setText(ncc.getTongDon() != null ? ncc.getTongDon() : "0");
        tvGiaTri.setText(ncc.getGiaTri() != null ? ncc.getGiaTri() : "0");
    }

    private void initEditViews() {
        tvEditTen = findViewById(R.id.tvEditTenNCC);
        edtMaNCC = findViewById(R.id.edtEditMaNCC);
        edtMST = findViewById(R.id.edtEditMST);
        edtSDT = findViewById(R.id.edtEditSDT);
        edtEmail = findViewById(R.id.edtEditEmail);
        edtDiaChi = findViewById(R.id.edtEditDiaChi);
        tvEditTongDon = findViewById(R.id.tvEditTongDon);
        tvEditGiaTri = findViewById(R.id.tvEditGiaTri);
    }

    private void bindDataToEdit() {
        if (ncc == null) return;
        tvEditTen.setText(ncc.getTenNCC());
        edtMaNCC.setText(ncc.getId());
        edtMST.setText(ncc.getMaSoThue());
        edtSDT.setText(ncc.getSdt());
        edtEmail.setText(ncc.getEmail());
        edtDiaChi.setText(ncc.getDiaChi());
        
        // Hiển thị dữ liệu thật trên màn hình sửa
        tvEditTongDon.setText(ncc.getTongDon() != null ? ncc.getTongDon() : "0");
        tvEditGiaTri.setText(ncc.getGiaTri() != null ? ncc.getGiaTri() : "0");
    }

    private void saveChanges() {
        ncc.setSdt(edtSDT.getText().toString());
        ncc.setEmail(edtEmail.getText().toString());
        ncc.setDiaChi(edtDiaChi.getText().toString());

        repository.updateNhaCungCap(ncc).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            showDetailLayout();
        });
    }

    private void showDeleteDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete_ncc);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER); 
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvDeleteMessage);
        if (ncc != null) {
            tvMsg.setText("Hệ thống sẽ xóa hoàn toàn nhà cung cấp " + ncc.getTenNCC() + " nhưng vẫn giữ những giao dịch lịch sử nếu có. Bạn có chắc là muốn xóa?");
        }

        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnSkip).setOnClickListener(v -> dialog.dismiss());
        
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            repository.deleteNhaCungCap(ncc.getId()).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã xóa nhà cung cấp", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            });
        });

        dialog.show();
    }
}
