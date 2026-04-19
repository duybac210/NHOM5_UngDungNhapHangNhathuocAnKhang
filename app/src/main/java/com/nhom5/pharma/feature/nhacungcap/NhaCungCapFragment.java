package com.nhom5.pharma.feature.nhacungcap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
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
    private ImageButton btnAddNCC;

    public NhaCungCapFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nha_cung_cap, container, false);
        
        rvNhaCungCap = view.findViewById(R.id.rvNhaCungCap);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnAddNCC = view.findViewById(R.id.btnAddNCC);
        
        repository = NhaCungCapRepository.getInstance();

        setupRecyclerView();
        setupSearch();
        setupAddButton();

        return view;
    }

    private void setupRecyclerView() {
        Query query = repository.getAllNhaCungCap();
        FirestoreRecyclerOptions<NhaCungCap> options = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                .setQuery(query, NhaCungCap.class)
                .setLifecycleOwner(getViewLifecycleOwner())
                .build();

        adapter = new NhaCungCapAdapter(options);
        
        // FIX LỖI CRASH: Bọc LayoutManager để bắt lỗi IndexOutOfBoundsException
        rvNhaCungCap.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("NhaCungCapFragment", "RecyclerView Inconsistency detected - Bắt lỗi thành công");
                }
            }
        });
        
        rvNhaCungCap.setAdapter(adapter);

        adapter.setOnItemClickListener(ncc -> {
            if (ncc != null && ncc.getId() != null && getContext() != null) {
                Intent intent = new Intent(getContext(), ChiTietNhaCungCapActivity.class);
                intent.putExtra("NCC_ID", ncc.getId()); 
                startActivity(intent);
            }
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
                Query newQuery = repository.searchById(searchText);
                FirestoreRecyclerOptions<NhaCungCap> newOptions = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                        .setQuery(newQuery, NhaCungCap.class)
                        .setLifecycleOwner(getViewLifecycleOwner())
                        .build();
                adapter.updateOptions(newOptions);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddButton() {
        if (btnAddNCC != null) {
            btnAddNCC.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), CreateSupplierActivity.class);
                startActivity(intent);
            });
        }
    }
}
