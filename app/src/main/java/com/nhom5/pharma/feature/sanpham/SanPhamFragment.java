package com.nhom5.pharma.feature.sanpham;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.pharma.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SanPhamFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private List<Product> fullList = new ArrayList<>();
    private boolean isSelectMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_san_pham, container, false);

        if (getActivity() != null && getActivity().getIntent() != null) {
            isSelectMode = getActivity().getIntent().getBooleanExtra("SELECT_MODE", false);
        }
        
        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        
<<<<<<< HEAD
<<<<<<< HEAD
        adapter = new ProductAdapter(this, isSelectMode);
=======
        // Sử dụng Adapter chuẩn (nhẹ hơn FirestoreRecyclerAdapter)
=======
>>>>>>> d1df932 (Hoàn thiện giao diện sản phẩm: đồng bộ header, màu sắc xanh khi mở rộng, fix crash)
        adapter = new ProductAdapter(this);
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
        rvProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                fullList = products;
                adapter.setProductList(products);
            }
        });

<<<<<<< HEAD
        // Nút thêm sản phẩm (+) ở góc trên bên phải
        View btnAdd = view.findViewById(R.id.iv_add);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), TaoSanPhamActivity.class);
                startActivity(intent);
            });
=======
        // Cập nhật tiêu đề từ layout chung
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Quản lý sản phẩm");
        }

        // Nút thêm sản phẩm (+)
        View btnAddNew = view.findViewById(R.id.btnAddNew);
        if (btnAddNew != null) {
            btnAddNew.setOnClickListener(v -> showEditDialog(null));
>>>>>>> d1df932 (Hoàn thiện giao diện sản phẩm: đồng bộ header, màu sắc xanh khi mở rộng, fix crash)
        }

        // Thanh tìm kiếm
        EditText searchEditText = view.findViewById(R.id.searchEditText);
        if (searchEditText != null) {
            searchEditText.setHint("Tìm kiếm sản phẩm...");
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLocal(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

<<<<<<< HEAD
    @Override
    public void onItemClick(Product product) {
        if (!isSelectMode || getActivity() == null) {
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("product_id", product.getId());
        resultIntent.putExtra("product_name", product.getTenSP());
        resultIntent.putExtra("product_price", product.getGiavon());
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private void filterProducts(String query) {
=======
    private void filterLocal(String query) {
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
        if (query.isEmpty()) {
            adapter.setProductList(fullList);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : fullList) {
<<<<<<< HEAD
                String normalizedId = p.getDisplayId();
                if ((p.getTenSP() != null && p.getTenSP().toLowerCase().contains(query.toLowerCase())) ||
                    (p.getId() != null && p.getId().toLowerCase().contains(query.toLowerCase())) ||
                    (normalizedId != null && normalizedId.toLowerCase().contains(query.toLowerCase()))) {
=======
                String q = query.toLowerCase();
                if ((p.getTenSP() != null && p.getTenSP().toLowerCase().contains(q)) || 
                    (p.getId() != null && p.getId().toLowerCase().contains(q))) {
>>>>>>> d1df932 (Hoàn thiện giao diện sản phẩm: đồng bộ header, màu sắc xanh khi mở rộng, fix crash)
                    filtered.add(p);
                }
            }
            adapter.setProductList(filtered);
        }
    }

    @Override public void onEditClick(Product product) { showEditDialog(product); }
    @Override public void onDeleteClick(Product product) { showDeleteConfirmDialog(product); }

    private void showDeleteConfirmDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_delete_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogView.findViewById(R.id.iv_close_dialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.tv_skip).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm_yes).setOnClickListener(v -> {
            viewModel.deleteProduct(product.getId());
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etMaHang = dialogView.findViewById(R.id.et_ma_hang);
        EditText etMaVach = dialogView.findViewById(R.id.et_ma_vach);
        EditText etTenHang = dialogView.findViewById(R.id.et_ten_hang);
        EditText etGiaVon = dialogView.findViewById(R.id.et_gia_von);
        EditText etHangSX = dialogView.findViewById(R.id.et_hang_sx);
        EditText etNuocSX = dialogView.findViewById(R.id.et_nuoc_sx);
        View ivClose = dialogView.findViewById(R.id.iv_close);

        if (product != null) {
<<<<<<< HEAD
            etMaHang.setText(product.getDisplayId());
=======
            tvDialogTitle.setText("Sửa hàng hóa");
            etMaHang.setText(product.getId());
>>>>>>> d1df932 (Hoàn thiện giao diện sản phẩm: đồng bộ header, màu sắc xanh khi mở rộng, fix crash)
            etMaVach.setText(product.getMaVach());
            etTenHang.setText(product.getTenSP());
            etGiaVon.setText(String.format(Locale.getDefault(), "%.0f", product.getGiavon()));
            etHangSX.setText(product.getHangSX());
            etNuocSX.setText(product.getNuocSX());
        } else {
            tvDialogTitle.setText("Thêm hàng hóa");
            etMaHang.setHint("Tự động tạo");
        }

        // Nút X và nút Bỏ qua để thoát dialog
        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String tenHang = etTenHang.getText().toString().trim();
            if (tenHang.isEmpty()) {
                etTenHang.setError("Vui lòng nhập tên");
                return;
            }

            Product p = product != null ? product : new Product();
            p.setTenSP(tenHang);
            p.setMaVach(etMaVach.getText().toString());
            p.setHangSX(etHangSX.getText().toString());
            p.setNuocSX(etNuocSX.getText().toString());
            p.setTrangThai(true);
            try {
                String gv = etGiaVon.getText().toString();
                if (!gv.isEmpty()) p.setGiavon(Double.parseDouble(gv));
            } catch (Exception ignored) {}

            viewModel.saveProduct(p);
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã lưu thành công", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
