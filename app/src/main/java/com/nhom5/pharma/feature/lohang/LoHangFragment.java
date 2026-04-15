package com.nhom5.pharma.feature.lohang;

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
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.nhaphang.LoHang;
import com.nhom5.pharma.feature.nhaphang.LoHangAdapter;
import com.nhom5.pharma.feature.nhaphang.NhapHangRepository;

public class LoHangFragment extends Fragment {

    private RecyclerView recyclerViewLoHang;
    private EditText searchEditText;
    private LoHangAdapter adapter;
    private final NhapHangRepository repository = NhapHangRepository.getInstance();

    public LoHangFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lo_hang, container, false);
        recyclerViewLoHang = view.findViewById(R.id.recyclerViewLoHang);
        searchEditText = view.findViewById(R.id.searchEditText);

        setupRecyclerView();
        setupSearchFunctionality();
        return view;
    }

    private void setupRecyclerView() {
        Query query = repository.getAllLoHang();
        FirestoreRecyclerOptions<LoHang> options = new FirestoreRecyclerOptions.Builder<LoHang>()
                .setQuery(query, LoHang.class)
                .build();

        adapter = new LoHangAdapter(options);

        recyclerViewLoHang.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("RecyclerView", "Chan loi vang app");
                }
            }
        });

        recyclerViewLoHang.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        if (searchEditText == null) {
            return;
        }
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Query query = repository.searchLoHang(s.toString());
                FirestoreRecyclerOptions<LoHang> options = new FirestoreRecyclerOptions.Builder<LoHang>()
                        .setQuery(query, LoHang.class)
                        .build();
                adapter.updateOptions(options);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}