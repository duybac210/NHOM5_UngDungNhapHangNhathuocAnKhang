package com.nhom5.pharma.feature.nhaphang;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietNhapHangActivity extends AppCompatActivity {

    private TextView tvOrderCodeTitle, tvTrangThaiHeader, tvNguoiNhap, tvNgayNhapMeta, tvTenNhaCungCap;
    private LinearLayout llChiTiet;
    private String nhapHangId;
    private NhapHangRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nhap_hang);

        repository = NhapHangRepository.getInstance();
        nhapHangId = getIntent().getStringExtra("NHAP_HANG_ID");

        initViews();
        loadData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn nhập hàng");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());

        tvOrderCodeTitle = findViewById(R.id.tvOrderCodeTitle);
        tvTrangThaiHeader = findViewById(R.id.tvTrangThaiHeader);
        tvNguoiNhap = findViewById(R.id.tvNguoiNhap);
        tvNgayNhapMeta = findViewById(R.id.tvNgayNhapMeta);
        tvTenNhaCungCap = findViewById(R.id.tvTenNhaCungCap);
        llChiTiet = findViewById(R.id.llChiTiet);

        findViewById(R.id.btnXoa).setOnClickListener(v -> showDeleteConfirmationDialog());
        findViewById(R.id.btnLuu).setOnClickListener(v -> finish());
    }

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        Button btnBoQua = dialogView.findViewById(R.id.btnBoQua);
        Button btnXoa = dialogView.findViewById(R.id.btnXoaConfirm);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);

        tvMessage.setText("Xóa phiếu nhập hàng " + (nhapHangId != null ? nhapHangId : "") + "?");

        btnBoQua.setOnClickListener(v -> dialog.dismiss());
        btnXoa.setOnClickListener(v -> {
            dialog.dismiss();
            deleteOrder();
        });

        dialog.show();
    }

    private void deleteOrder() {
        if (nhapHangId == null) return;
        repository.deleteNhapHang(nhapHangId).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
            finish(); 
        });
    }

    private void loadData() {
        if (nhapHangId == null) return;
        repository.getNhapHangById(nhapHangId).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                NhapHang nhapHang = NhapHang.fromDocument(doc);
                displayNhapHangInfo(nhapHang);

                if (nhapHang.getMaNCC() != null) {
                    fetchSupplierName(nhapHang.getMaNCC());
                }

                // Truy vấn tên người dùng thay vì để cứng Admin
                if (nhapHang.getMaNguoiNhap() != null) {
                    fetchUserName(nhapHang.getMaNguoiNhap());
                } else {
                    tvNguoiNhap.setText("Không xác định");
                }

                fetchLoHangList(doc.getId());
            }
        });
    }

    private void displayNhapHangInfo(NhapHang nhapHang) {
        tvOrderCodeTitle.setText(nhapHang.getDisplayId());
        int trangThai = nhapHang.getTrangThaiValue();
        if (trangThai == 1) {
            tvTrangThaiHeader.setText("Đã nhập kho");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvTrangThaiHeader.setText("Đã hủy");
            tvTrangThaiHeader.setTextColor(Color.parseColor("#F44336"));
        }
        
        if (nhapHang.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvNgayNhapMeta.setText(sdf.format(nhapHang.getNgayTao()));
        }
    }

    private void fetchSupplierName(String maNCC) {
        repository.getSupplierById(maNCC).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("tenNCC");
                if (name == null) name = doc.getString("TenNCC");
                if (name == null) name = doc.getString("tenNhaCungCap");
                if (name == null) name = doc.getString("ten");
                tvTenNhaCungCap.setText(name != null ? name : "Không xác định");
            }
        });
    }

    private void fetchUserName(String maNguoiNhap) {
        repository.getUserById(maNguoiNhap).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Thử cả 2 trường hợp tên trường (tenNguoiDung hoặc hoTen)
                String name = doc.getString("tenNguoiDung");
                if (name == null) name = doc.getString("hoTen");
                if (name == null) name = "Người dùng (" + maNguoiNhap + ")";
                tvNguoiNhap.setText(name);
            } else {
                tvNguoiNhap.setText("Mã: " + maNguoiNhap);
            }
        });
    }

    private void fetchLoHangList(String id) {
        llChiTiet.removeAllViews();
        repository.getLoHangByNhapHangId(id).addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_chi_tiet_lo_hang, llChiTiet, false);
                ((TextView)itemView.findViewById(R.id.tvSoLo)).setText(doc.getId());
                
                Double sl = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "soLuong"));
                Double dg = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "donGiaNhap"));
                
                double soLuong = sl != null ? sl : 0;
                double donGia = dg != null ? dg : 0;
                
                ((TextView)itemView.findViewById(R.id.tvSoLuong)).setText(String.format(Locale.getDefault(), "%,.0f", soLuong));
                ((TextView)itemView.findViewById(R.id.tvDonGia)).setText(String.format(Locale.getDefault(), "%,.0fđ", donGia));
                ((TextView)itemView.findViewById(R.id.tvThanhTien)).setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * donGia));
                
                String maSP = doc.getString("maSP");
                if (maSP != null) {
                    repository.getProductById(maSP).addOnSuccessListener(spDoc -> {
                        if (spDoc.exists()) {
                            String tenSP = FirestoreValueParser.safeString(spDoc, "tenSP");
                            if (tenSP == null) tenSP = FirestoreValueParser.safeString(spDoc, "TenSP");
                            ((TextView)itemView.findViewById(R.id.tvTenHang)).setText(tenSP);
                        }
                    });
                }
                llChiTiet.addView(itemView);
            }
        });
    }

    public static class NhapHangFragment extends Fragment {

        private RecyclerView recyclerViewNhapHang;
        private EditText searchEditText;
        private NhapHangAdapter adapter;
        private final NhapHangRepository repository = NhapHangRepository.getInstance();

        public NhapHangFragment() {}

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_nhap_hang, container, false);

            recyclerViewNhapHang = view.findViewById(R.id.recyclerViewNhapHang);

            View searchBarContainer = view.findViewById(R.id.search_bar);
            searchEditText = view.findViewById(R.id.searchEditText);
            if (searchEditText == null && searchBarContainer != null) {
                searchEditText = searchBarContainer.findViewById(R.id.searchEditText);
            }

            ImageButton btnAddNew = view.findViewById(R.id.btnAddNew);
            if (btnAddNew == null && searchBarContainer != null) {
                btnAddNew = searchBarContainer.findViewById(R.id.btnAddNew);
            }

            setupRecyclerView();
            setupSearchFunctionality();

            if (btnAddNew != null) {
                btnAddNew.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(requireActivity(), TaoDonNhapActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Không mở được màn tạo đơn nhập", Toast.LENGTH_SHORT).show();
                        Log.e("NhapHangFragment", "Open TaoDonNhapActivity failed", e);
                    }
                });
            } else {
                Log.w("NhapHangFragment", "Không tìm thấy nút thêm mới");
            }

            return view;
        }

        private void setupRecyclerView() {
            Query query = repository.getAllNhapHang();
            FirestoreRecyclerOptions<NhapHang> options = new FirestoreRecyclerOptions.Builder<NhapHang>()
                    .setQuery(query, NhapHang.class)
                    .build();

            adapter = new NhapHangAdapter(options);

            recyclerViewNhapHang.setLayoutManager(new LinearLayoutManager(getContext()) {
                @Override
                public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                    try {
                        super.onLayoutChildren(recycler, state);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e("RecyclerView", "Chặn lỗi văng app");
                    }
                }
            });

            recyclerViewNhapHang.setAdapter(adapter);
        }

        private void setupSearchFunctionality() {
            if (searchEditText != null) {
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Query query = repository.searchByMaDon(s.toString().toUpperCase());

                        FirestoreRecyclerOptions<NhapHang> options = new FirestoreRecyclerOptions.Builder<NhapHang>()
                                .setQuery(query, NhapHang.class)
                                .build();
                        adapter.updateOptions(options);
                    }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void afterTextChanged(Editable s) {}
                });
            }
        }

        @Override public void onStart() { super.onStart(); if (adapter != null) adapter.startListening(); }
        @Override public void onStop() { super.onStop(); if (adapter != null) adapter.stopListening(); }
    }
}
