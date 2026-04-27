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
    private EditText searchEditText;
    private boolean isSelectMode = false;
    private String supplierFilterId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_san_pham, container, false);

        if (getActivity() != null && getActivity().getIntent() != null) {
            isSelectMode = getActivity().getIntent().getBooleanExtra(com.nhom5.pharma.MainActivity.EXTRA_SELECT_MODE,
                    false);
            supplierFilterId = null;
        }

        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductAdapter(this, isSelectMode);
        rvProducts.setAdapter(adapter);

        // 1. Khởi tạo các View từ search_bar include
        View searchBarContainer = view.findViewById(R.id.search_bar);
        if (searchBarContainer != null) {
            searchEditText = searchBarContainer.findViewById(R.id.searchEditText);
            View btnAddNew = searchBarContainer.findViewById(R.id.btnAddNew);
            TextView tvTitle = view.findViewById(R.id.tvTitle);

            if (tvTitle != null) tvTitle.setText("Quản lý sản phẩm");

            if (btnAddNew != null) {
                btnAddNew.setVisibility(isSelectMode ? View.GONE : View.VISIBLE);
                btnAddNew.setOnClickListener(v -> {
                    if (isSelectMode) return;
                    com.nhom5.pharma.util.RoleHelper.checkIsManager(isManager -> {
                        if (isManager) {
                            startActivity(new Intent(requireContext(), TaoSanPhamActivity.class));
                        } else {
                            Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }

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
        }

        // 2. Khởi tạo ViewModel và Quan sát dữ liệu
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
        
        viewModel.listenToProducts(supplierFilterId);

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                fullList = products;
                String currentQuery = (searchEditText != null) ? searchEditText.getText().toString() : "";
                if (currentQuery.isEmpty()) {
                    adapter.setProductList(products);
                } else {
                    filterLocal(currentQuery);
                }
            }
        });

        return view;
    }

    private void filterLocal(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setProductList(fullList);
            return;
        }

        String q = query.toLowerCase(Locale.getDefault());
        List<Product> filtered = new ArrayList<>();
        for (Product p : fullList) {
            if ((p.getTenSP() != null && p.getTenSP().toLowerCase(Locale.getDefault()).contains(q)) ||
                    (p.getId() != null && p.getId().toLowerCase(Locale.getDefault()).contains(q))) {
                filtered.add(p);
            }
        }
        adapter.setProductList(filtered);
    }

    @Override
    public void onItemClick(Product product) {
        if (!isSelectMode || getActivity() == null)
            return;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("product_id", product.getId());
        resultIntent.putExtra("product_name", product.getTenSP());
        resultIntent.putExtra("product_price", product.getGiavon());
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    @Override
    public void onEditClick(Product product) {
        com.nhom5.pharma.util.RoleHelper.checkIsManager(isManager -> {
            if (isManager) {
                showEditDialog(product);
            } else {
                Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(Product product) {
        com.nhom5.pharma.util.RoleHelper.checkIsManager(isManager -> {
            if (isManager) {
                showDeleteConfirmDialog(product);
            } else {
                Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_delete_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogView.findViewById(R.id.iv_close_dialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.tv_skip).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm_yes).setOnClickListener(v -> {
            viewModel.deleteProduct(product.getId());
            com.nhom5.pharma.feature.history.LogRepository.getInstance().logDelete("SANPHAM", product.getId(), "Xóa sản phẩm: " + product.getTenSP());
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etMaHang = dialogView.findViewById(R.id.et_ma_hang);
        EditText etTenHang = dialogView.findViewById(R.id.et_ten_hang);
        EditText etGiaVon = dialogView.findViewById(R.id.et_gia_von);
        View ivClose = dialogView.findViewById(R.id.iv_close);

        if (product != null) {
            tvDialogTitle.setText("Sửa hàng hóa");
            etMaHang.setText(product.getId());
            etMaHang.setEnabled(false);
            etTenHang.setText(product.getTenSP());
            etGiaVon.setText(String.format(Locale.getDefault(), "%.0f", product.getGiavon()));
        }

        if (ivClose != null)
            ivClose.setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String tenHang = etTenHang.getText().toString().trim();
            if (tenHang.isEmpty()) {
                etTenHang.setError("Vui lòng nhập tên");
                return;
            }
            Product p = (product != null) ? product : new Product();
            p.setTenSP(tenHang);
            try {
                String gv = etGiaVon.getText().toString();
                if (!gv.isEmpty())
                    p.setGiavon(Double.parseDouble(gv));
            } catch (Exception ignored) {
            }
            viewModel.saveProduct(p);
            String logAction = (product != null) ? "Cập nhật" : "Tạo mới";
            com.nhom5.pharma.feature.history.LogRepository.getInstance().log(logAction.toUpperCase(), "SANPHAM", p.getId(), logAction + " sản phẩm: " + p.getTenSP());
            dialog.dismiss();
        });

        dialog.show();
    }
}
