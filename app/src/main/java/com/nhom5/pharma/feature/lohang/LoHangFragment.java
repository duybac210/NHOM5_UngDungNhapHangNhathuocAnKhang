package com.nhom5.pharma.feature.lohang;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.nhaphang.LoHang;
import com.nhom5.pharma.feature.nhaphang.LoHangAdapter;
import com.nhom5.pharma.feature.nhaphang.NhapHangRepository;

import java.util.Date;

public class LoHangFragment extends Fragment {

    private RecyclerView recyclerViewLoHang;
    private EditText searchEditText;
    private ImageView ivFilter;
    private TextView tvEmptyState;
    private LoHangAdapter adapter;
    private PopupWindow filterPopupWindow;
    private int selectedFilter = LoHangFilterType.ALL;
    private String currentSearchKeyword = "";
    private final NhapHangRepository repository = NhapHangRepository.getInstance();

    public LoHangFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lo_hang, container, false);
        recyclerViewLoHang = view.findViewById(R.id.recyclerViewLoHang);
        searchEditText = view.findViewById(R.id.searchEditText);
        ivFilter = view.findViewById(R.id.ivFilter);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        setupSearchFunctionality();
        setupFilterDropdown();
        updateFilterIconState();
        return view;
    }

    private void setupRecyclerView() {
        // Tu dong dong bo counter ma lo theo du lieu hien co tren Firebase.
        repository.ensureLoHangCounterNormalized();
        Query query = repository.getAllLoHang();
        FirestoreRecyclerOptions<LoHang> options = new FirestoreRecyclerOptions.Builder<LoHang>()
                .setQuery(query, snapshot -> parseLoHang(snapshot))
                .build();

        adapter = new LoHangAdapter(options, this::updateEmptyState);
        adapter.setActiveFilterType(selectedFilter);

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
                currentSearchKeyword = s == null ? "" : s.toString();
                applyFilterAndSearch();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterDropdown() {
        if (ivFilter == null) {
            return;
        }
        ivFilter.setOnClickListener(v -> showFilterDropdown());
    }

    private void showFilterDropdown() {
        if (getContext() == null || ivFilter == null) {
            return;
        }

        dismissFilterDropdown();

        View popupView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_lo_hang_filter_dropdown, recyclerViewLoHang, false);

        RadioGroup rgFilter = popupView.findViewById(R.id.rgFilter);

        rgFilter.check(getRadioIdForFilter(selectedFilter));

        rgFilter.setOnCheckedChangeListener((group, checkedId) -> {
            selectedFilter = getFilterForRadioId(checkedId);
            applyFilterAndSearch();
            dismissFilterDropdown();
        });

        int popupWidth = resolveFilterPopupWidth();
        int xOffset = ivFilter.getWidth() - popupWidth - dpToPx(4);

        filterPopupWindow = new PopupWindow(
                popupView,
                popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        filterPopupWindow.setOutsideTouchable(true);
        filterPopupWindow.setElevation(12f);
        filterPopupWindow.showAsDropDown(ivFilter, xOffset, 6);
    }

    private int resolveFilterPopupWidth() {
        // Dropdown gon, can phai theo icon filter.
        return dpToPx(190);
    }

    private int dpToPx(int dp) {
        if (getContext() == null) {
            return dp;
        }
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private void dismissFilterDropdown() {
        if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        }
    }

    private void applyFilterAndSearch() {
        if (adapter == null) {
            return;
        }

        Query query;
        if (selectedFilter == LoHangFilterType.ALL) {
            query = repository.searchLoHang(currentSearchKeyword);
        } else {
            query = repository.getLoHangByFilter(selectedFilter);
        }

        FirestoreRecyclerOptions<LoHang> options = new FirestoreRecyclerOptions.Builder<LoHang>()
                .setQuery(query, snapshot -> parseLoHang(snapshot))
                .build();
        adapter.setActiveFilterType(selectedFilter);
        adapter.updateOptions(options);
        updateFilterIconState();
    }

    private LoHang parseLoHang(com.google.firebase.firestore.DocumentSnapshot snapshot) {
        LoHang item = new LoHang();
        String soLo = firstNonEmpty(snapshot, "soLo", "SoLo", "maLo", "MaLo");
        item.setSoLo(soLo != null ? soLo : snapshot.getId());
        item.setMaNhapHang(firstNonEmpty(snapshot, "maNhapHang", "MaNhapHang"));
        item.setMaSP(firstNonEmpty(snapshot, "maSP", "MaSP", "maHang", "MaHang"));
        item.setSoLuong(firstNumber(snapshot, "soLuong", "SoLuong"));
        item.setDonGiaNhap(firstNumber(snapshot, "donGiaNhap", "DonGiaNhap", "giaNhap", "GiaNhap"));
        item.setNgayNhap(firstDate(snapshot, "ngayNhap", "NgayNhap", "ngayTao", "createdAt"));
        item.setHanSuDung(firstDate(snapshot, "hanSuDung", "HanSuDung", "hansudung"));
        item.setNgayTao(firstDate(snapshot, "ngayTao", "createdAt", "NgayTao"));
        return item;
    }

    private static String firstNonEmpty(com.google.firebase.firestore.DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            String value = snapshot.getString(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static double firstNumber(com.google.firebase.firestore.DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Object raw = snapshot.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
            if (raw instanceof String) {
                try {
                    return Double.parseDouble(((String) raw).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0d;
    }

    private static Date firstDate(com.google.firebase.firestore.DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Object raw = snapshot.get(key);
            if (raw instanceof Date) {
                return (Date) raw;
            }
            if (raw instanceof Timestamp) {
                return ((Timestamp) raw).toDate();
            }
            if (raw instanceof Number) {
                return new Date(((Number) raw).longValue());
            }
        }
        return null;
    }

    private int getRadioIdForFilter(int filterType) {
        switch (filterType) {
            case LoHangFilterType.EXPIRING_SOON:
                return R.id.rbExpiringSoon;
            case LoHangFilterType.EXPIRED:
                return R.id.rbExpired;
            case LoHangFilterType.LOW_STOCK:
                return R.id.rbLowStock;
            case LoHangFilterType.EXPIRY_ASC:
                return R.id.rbExpiryAsc;
            case LoHangFilterType.EXPIRY_DESC:
                return R.id.rbExpiryDesc;
            case LoHangFilterType.ALL:
            default:
                return R.id.rbAll;
        }
    }

    private int getFilterForRadioId(int radioId) {
        if (radioId == R.id.rbExpiringSoon) {
            return LoHangFilterType.EXPIRING_SOON;
        }
        if (radioId == R.id.rbExpired) {
            return LoHangFilterType.EXPIRED;
        }
        if (radioId == R.id.rbLowStock) {
            return LoHangFilterType.LOW_STOCK;
        }
        if (radioId == R.id.rbExpiryAsc) {
            return LoHangFilterType.EXPIRY_ASC;
        }
        if (radioId == R.id.rbExpiryDesc) {
            return LoHangFilterType.EXPIRY_DESC;
        }
        return LoHangFilterType.ALL;
    }

    private void updateFilterIconState() {
        if (ivFilter == null || getContext() == null) {
            return;
        }
        int tintColor = selectedFilter == LoHangFilterType.ALL
                ? R.color.text_gray
                : R.color.login_primary;
        ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), tintColor));
    }

    private void updateEmptyState(int filteredCount) {
        if (tvEmptyState == null || recyclerViewLoHang == null) {
            return;
        }
        boolean isEmpty = filteredCount == 0;
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewLoHang.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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

    @Override
    public void onDestroyView() {
        dismissFilterDropdown();
        super.onDestroyView();
    }
}