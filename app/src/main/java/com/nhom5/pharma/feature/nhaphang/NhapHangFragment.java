package com.nhom5.pharma.feature.nhaphang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;

public class NhapHangFragment extends Fragment {

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
