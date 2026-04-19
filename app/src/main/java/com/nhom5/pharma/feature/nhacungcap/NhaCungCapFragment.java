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
import android.widget.Toast;
import androidx.annotation.NonNull;
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
<<<<<<< HEAD
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
=======
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
        View view = inflater.inflate(R.layout.fragment_nha_cung_cap, container, false);
        
        rvNhaCungCap = view.findViewById(R.id.rvNhaCungCap);
        edtSearch = view.findViewById(R.id.searchEditText);
        repository = NhaCungCapRepository.getInstance();

        setupRecyclerView();
        setupSearch();

<<<<<<< HEAD
=======
        View btnAddNcc = view.findViewById(R.id.btnAddNCC);
        if (btnAddNcc != null) {
            Log.d("NhaCungCapFragment", "Button found: btnAddNCC");
            btnAddNcc.setClickable(true);
            btnAddNcc.setEnabled(true);
            btnAddNcc.setOnClickListener(v -> {
                try {
                    Log.d("NhaCungCapFragment", "Button clicked!");
                    startActivity(new Intent(requireActivity(), CreateSupplierActivity.class));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Không mở được màn tạo nhà cung cấp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("NhaCungCapFragment", "Error opening CreateSupplierActivity", e);
                }
            });
        } else {
            Log.w("NhaCungCapFragment", "Button NOT found: btnAddNCC");
        }

>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
        return view;
    }

    private void setupRecyclerView() {
        Query query = repository.getAllNhaCungCap();
        FirestoreRecyclerOptions<NhaCungCap> options = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                .setQuery(query, NhaCungCap.class)
                .setLifecycleOwner(getViewLifecycleOwner()) // THÊM DÒNG NÀY ĐỂ CHỐNG VĂNG APP
                .build();

        adapter = new NhaCungCapAdapter(options);
        rvNhaCungCap.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNhaCungCap.setHasFixedSize(true);
        rvNhaCungCap.setAdapter(adapter);

        adapter.setOnItemClickListener(ncc -> {
            if (ncc != null) {
                Intent intent = new Intent(getContext(), ChiTietNhaCungCapActivity.class);
                intent.putExtra("NHA_CUNG_CAP", ncc);
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
<<<<<<< HEAD
                Query newQuery;
                if (searchText.isEmpty()) {
                    newQuery = repository.getAllNhaCungCap();
                } else {
                    newQuery = repository.getAllNhaCungCap()
                            .orderBy("tenNCC")
                            .startAt(searchText)
                            .endAt(searchText + "\uf8ff");
                }
=======
                Query newQuery = repository.searchById(searchText);
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
                
                FirestoreRecyclerOptions<NhaCungCap> newOptions = new FirestoreRecyclerOptions.Builder<NhaCungCap>()
                        .setQuery(newQuery, NhaCungCap.class)
                        .setLifecycleOwner(getViewLifecycleOwner())
                        .build();
                adapter.updateOptions(newOptions);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Xóa startListening và stopListening vì đã có LifecycleOwner tự quản lý
}
