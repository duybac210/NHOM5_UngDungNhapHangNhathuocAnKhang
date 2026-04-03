package com.nhom5.pharma.feature.sanpham;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class SanPhamFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private List<Product> fullList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_san_pham, container, false);
        
        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ProductAdapter(this);
        rvProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            fullList = products;
            adapter.setProductList(products);
        });

        viewModel.listenToProducts();

        // Nút thêm sản phẩm
        View btnAdd = view.findViewById(R.id.iv_add);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showEditDialog(null));
        }

        EditText etSearch = view.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            adapter.setProductList(fullList);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : fullList) {
                if ((p.getTenSP() != null && p.getTenSP().toLowerCase().contains(query.toLowerCase())) || 
                    (p.getId() != null && p.getId().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(p);
                }
            }
            adapter.setProductList(filtered);
        }
    }

    @Override
    public void onEditClick(Product product) {
        showEditDialog(product);
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    viewModel.deleteProduct(product.getId());
                    Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etMaHang = dialogView.findViewById(R.id.et_ma_hang);
        EditText etMaVach = dialogView.findViewById(R.id.et_ma_vach);
        EditText etTenHang = dialogView.findViewById(R.id.et_ten_hang);
        EditText etGiaVon = dialogView.findViewById(R.id.et_gia_von);
        EditText etHangSX = dialogView.findViewById(R.id.et_hang_sx);
        EditText etNuocSX = dialogView.findViewById(R.id.et_nuoc_sx);

        if (product != null) {
            etMaHang.setText(product.getId());
            etMaVach.setText(product.getMaVach());
            etTenHang.setText(product.getTenSP());
            etGiaVon.setText(String.format("%.0f", product.getGiavon()));
            etHangSX.setText(product.getHangSX());
            etNuocSX.setText(product.getNuocSX());
        } else {
            etMaHang.setHint("Tự động");
        }

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String tenHang = etTenHang.getText().toString();
            if (tenHang.isEmpty()) {
                etTenHang.setError("Vui lòng nhập tên");
                return;
            }

            double giaVon = 0;
            try {
                giaVon = Double.parseDouble(etGiaVon.getText().toString());
            } catch (Exception ignored) {}

            Product p = product != null ? product : new Product();
            p.setTenSP(tenHang);
            p.setGiavon(giaVon);
            p.setMaVach(etMaVach.getText().toString());
            p.setHangSX(etHangSX.getText().toString());
            p.setNuocSX(etNuocSX.getText().toString());
            p.setTrangThai(true);

            viewModel.saveProduct(p);
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã lưu vào Firestore", Toast.LENGTH_SHORT).show();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.iv_close).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
