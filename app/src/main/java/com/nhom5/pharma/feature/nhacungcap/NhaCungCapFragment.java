package com.nhom5.pharma.feature.nhacungcap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;

public class NhaCungCapFragment extends Fragment {

    private RecyclerView rvNhaCungCap;
    private NhaCungCapAdapter adapter;
    private NhaCungCapRepository repository;
    private EditText edtSearch;

    public NhaCungCapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nha_cung_cap, container, false);
        
        rvNhaCungCap = view.findViewById(R.id.rvNhaCungCap);
        // ID đúng trong layout_common_search_bar là searchEditText
        edtSearch = view.findViewById(R.id.searchEditText);
        repository = NhaCungCapRepository.getInstance();

        setupRecyclerView();
        setupSearch();

        view.findViewById(R.id.btnAddNCC).setOnClickListener(v -> {
            // Logic thêm NCC mới nếu cần
        });

        return view;
    }

    private void setupRecyclerView() {
        Query query = repository.getAllNhaCungCap();
        FirestoreRecyclerOptions<NhaCungCap> options = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                .setQuery(query, NhaCungCap.class)
                .build();

        adapter = new NhaCungCapAdapter(options);
        rvNhaCungCap.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNhaCungCap.setAdapter(adapter);

        adapter.setOnItemClickListener(ncc -> {
            // Chuyển sang màn hình Chi tiết (Màn hình 2)
            Intent intent = new Intent(getContext(), ChiTietNhaCungCapActivity.class);
            intent.putExtra("NHA_CUNG_CAP", ncc);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        if (edtSearch == null) return;
        
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                Query newQuery;
                if (searchText.isEmpty()) {
                    newQuery = repository.getAllNhaCungCap();
                } else {
                    // Tìm kiếm theo Document ID (Mã NCC)
                    newQuery = repository.getAllNhaCungCap()
                            .orderBy("__name__") // Tìm theo Document ID
                            .startAt(searchText)
                            .endAt(searchText + "\uf8ff");
                }
                
                FirestoreRecyclerOptions<NhaCungCap> newOptions = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                        .setQuery(newQuery, NhaCungCap.class)
                        .build();
                adapter.updateOptions(newOptions);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

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
}
