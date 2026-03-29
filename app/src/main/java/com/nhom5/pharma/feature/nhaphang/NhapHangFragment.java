package com.nhom5.pharma.feature.nhaphang;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.util.Log;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhom5.pharma.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NhapHangFragment extends Fragment {

    private RecyclerView recyclerViewNhapHang;
    private EditText searchEditText;
    private NhapHangAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public NhapHangFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nhap_hang, container, false);

        recyclerViewNhapHang = view.findViewById(R.id.recyclerViewNhapHang);
        searchEditText = view.findViewById(R.id.searchEditText);

        setupRecyclerView();
        setupSearchFunctionality();

        // Nút thêm dữ liệu mẫu (để bạn test nhanh)
        view.findViewById(R.id.btn_add_sample).setOnClickListener(v -> createSampleData());

        return view;
    }

    private void setupRecyclerView() {
        // Query mặc định: Lấy toàn bộ đơn hàng mới nhất lên đầu
        Query query = db.collection("import_orders")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<NhapHang> options = new FirestoreRecyclerOptions.Builder<NhapHang>()
                .setQuery(query, NhapHang.class)
                .build();

        adapter = new NhapHangAdapter(options);
        recyclerViewNhapHang.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNhapHang.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateQuery(s.toString());
                }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updateQuery(String searchText) {
        Query query = db.collection("import_orders").orderBy("tenNhaCungCap");

        if (!searchText.isEmpty()) {
            query = query.startAt(searchText).endAt(searchText + "\uf8ff");
        } else {
            // Nếu ô tìm kiếm trống, quay lại sắp xếp theo ngày
            query = db.collection("import_orders").orderBy("createdAt", Query.Direction.DESCENDING);
        }

        FirestoreRecyclerOptions<NhapHang> options = new FirestoreRecyclerOptions.Builder<NhapHang>()
                .setQuery(query, NhapHang.class)
                .build();

        adapter.updateOptions(options); // FirebaseUI sẽ tự động cập nhật lại danh sách
    }

    // Các hàm onStart/onStop bắt buộc để FirebaseUI hoạt động
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    // Hàm tạo dữ liệu mẫu
    private void createSampleData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> order = new HashMap<>();
        order.put("tenNhaCungCap", "Dược phẩm Trung Ương 1");
        order.put("tongTien", 1500000.0);
        order.put("trangThai", 1);
        order.put("createdAt", FieldValue.serverTimestamp());

        // Giả lập danh sách chi tiết (NhapChiTiet)
        List<Map<String, Object>> listChiTiet = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("tenSanPham", "Paracetamol 500mg");
        item1.put("soLuong", 100);
        item1.put("donGia", 5000.0);
        listChiTiet.add(item1);

        order.put("chiTiet", listChiTiet);

        db.collection("import_orders")
                .add(order)
                .addOnSuccessListener(documentReference -> Log.d("DB", "Đã tạo dữ liệu mẫu!"))
                .addOnFailureListener(e -> Log.e("DB", "Lỗi: " + e.getMessage()));
    }
}