package com.nhom5.pharma.feature.nhaphang;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.lohang.ChiTietLoHangActivity;
import com.nhom5.pharma.feature.lohang.LoHangFilterType;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoHangAdapter extends FirestoreRecyclerAdapter<LoHang, LoHangAdapter.LoHangViewHolder> {

    public interface OnFilteredCountChangedListener {
        void onChanged(int filteredCount);
    }

    private int activeFilterType = LoHangFilterType.ALL;
    private final OnFilteredCountChangedListener countChangedListener;

    public LoHangAdapter(@NonNull FirestoreRecyclerOptions<LoHang> options) {
        this(options, null);
    }

    public LoHangAdapter(@NonNull FirestoreRecyclerOptions<LoHang> options,
                         OnFilteredCountChangedListener countChangedListener) {
        super(options);
        this.countChangedListener = countChangedListener;
    }

    public void setActiveFilterType(int filterType) {
        this.activeFilterType = filterType;
        notifyDataSetChanged();
        notifyFilteredCount();
    }

    @Override
    protected void onBindViewHolder(@NonNull LoHangViewHolder holder, int position, @NonNull LoHang model) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String docId = snapshot.getId();
        String soLo = firstNonEmpty(snapshot, docId, "soLo", "SoLo", "maLo", "MaLo");
        Date hanSuDung = firstDate(snapshot, model.getHanSuDung(), "hanSuDung", "HanSuDung", "hansudung");
        String maHang = firstNonEmpty(snapshot, model.getMaSP(), "maHang", "MaHang", "maSP", "MaSP", "maNhapHang", "MaNhapHang");
        Date ngayNhap = firstDate(snapshot, model.getNgayNhap(), "ngayNhap", "NgayNhap", "ngayTao", "createdAt");
        double soLuong = firstNumber(snapshot, model.getSoLuong(), "soLuong", "SoLuong");
        long soNgayConLai = calcDaysUntilExpiry(hanSuDung);
        boolean shouldDisplay = matchesCurrentFilter(soNgayConLai, soLuong);

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            );
        }

        if (!shouldDisplay) {
            holder.itemView.setVisibility(View.GONE);
            layoutParams.height = 0;
            holder.itemView.setLayoutParams(layoutParams);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);
        layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        holder.itemView.setLayoutParams(layoutParams);

        holder.tvSoLo.setText(defaultText(soLo));
        holder.tvHanSuDung.setText(formatDate(hanSuDung));
        holder.tvMaHang.setText(defaultText(maHang));
        holder.tvNgayNhap.setText(formatDate(ngayNhap));
        holder.tvSoLuong.setText(String.format(Locale.getDefault(), "%,.0f", soLuong));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChiTietLoHangActivity.class);
            intent.putExtra(ChiTietLoHangActivity.EXTRA_SO_LO, docId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyFilteredCount();
    }

    @NonNull
    @Override
    public LoHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lo_hang, parent, false);
        return new LoHangViewHolder(view);
    }

    private static String firstNonEmpty(DocumentSnapshot snapshot, String fallback, String... keys) {
        for (String key : keys) {
            String value = snapshot.getString(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return TextUtils.isEmpty(fallback) ? null : fallback;
    }

    private static double firstNumber(DocumentSnapshot snapshot, double fallback, String... keys) {
        for (String key : keys) {
            Double value = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(snapshot, key));
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    private static Date firstDate(DocumentSnapshot snapshot, Date fallback, String... keys) {
        for (String key : keys) {
            Date value = toDate(snapshot.get(key));
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    private static Date toDate(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Timestamp) {
            return ((Timestamp) raw).toDate();
        }
        if (raw instanceof Date) {
            return (Date) raw;
        }
        if (raw instanceof Number) {
            long epochMillis = ((Number) raw).longValue();
            return new Date(epochMillis);
        }
        if (raw instanceof String) {
            String value = ((String) raw).trim();
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            if (TextUtils.isDigitsOnly(value)) {
                try {
                    return new Date(Long.parseLong(value));
                } catch (NumberFormatException ignored) {
                }
            }

            String[] patterns = new String[]{
                    "dd/MM/yyyy",
                    "dd/MM/yy",
                    "yyyy-MM-dd",
                    "dd-MM-yyyy",
                    "yyyy/MM/dd",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            };
            for (String pattern : patterns) {
                try {
                    return new SimpleDateFormat(pattern, Locale.getDefault()).parse(value);
                } catch (ParseException ignored) {
                }
            }
        }
        return null;
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date);
    }

    private static long calcDaysUntilExpiry(Date hanSuDung) {
        if (hanSuDung == null) {
            return Long.MIN_VALUE;
        }
        long diffMillis = stripTime(hanSuDung).getTime() - stripTime(new Date()).getTime();
        return diffMillis / (24L * 60L * 60L * 1000L);
    }

    private static Date stripTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static String defaultText(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }

    private boolean matchesCurrentFilter(long soNgayConLai, double soLuong) {
        switch (activeFilterType) {
            case LoHangFilterType.EXPIRING_SOON:
                // Quy uoc: date = hanSuDung - ngayNhap, sap het han khi 1..15 ngay.
                return soNgayConLai > 0 && soNgayConLai <= 15;
            case LoHangFilterType.EXPIRED:
                // Theo yeu cau: date = 0 la het han.
                return soNgayConLai <= 0;
            case LoHangFilterType.LOW_STOCK:
                return soLuong <= 10;
            case LoHangFilterType.ALL:
            default:
                return true;
        }
    }

    private int countMatchingItems() {
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            DocumentSnapshot snapshot = getSnapshots().getSnapshot(i);
            LoHang model = getItem(i);
            Date hanSuDung = firstDate(snapshot, model.getHanSuDung(), "hanSuDung", "HanSuDung", "hansudung");
            Date ngayNhap = firstDate(snapshot, model.getNgayNhap(), "ngayNhap", "NgayNhap", "ngayTao", "createdAt");
            double soLuong = firstNumber(snapshot, model.getSoLuong(), "soLuong", "SoLuong");
            long soNgayConLai = calcDaysUntilExpiry(hanSuDung);
            if (matchesCurrentFilter(soNgayConLai, soLuong)) {
                count++;
            }
        }
        return count;
    }

    private void notifyFilteredCount() {
        if (countChangedListener == null) {
            return;
        }
        countChangedListener.onChanged(countMatchingItems());
    }

    public static class LoHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvSoLo;
        TextView tvHanSuDung;
        TextView tvMaHang;
        TextView tvNgayNhap;
        TextView tvSoLuong;

        public LoHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSoLo = itemView.findViewById(R.id.tvSoLo);
            tvHanSuDung = itemView.findViewById(R.id.tvHanSuDung);
            tvMaHang = itemView.findViewById(R.id.tvMaSP);
            tvNgayNhap = itemView.findViewById(R.id.tvNgayNhap);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
