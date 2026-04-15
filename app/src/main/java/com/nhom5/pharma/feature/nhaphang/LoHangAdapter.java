package com.nhom5.pharma.feature.nhaphang;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoHangAdapter extends FirestoreRecyclerAdapter<LoHang, LoHangAdapter.LoHangViewHolder> {

    public LoHangAdapter(@NonNull FirestoreRecyclerOptions<LoHang> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull LoHangViewHolder holder, int position, @NonNull LoHang model) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);

        String soLo = snapshot.getId();
        Date hanSuDung = firstDate(snapshot, "hanSuDung", "HanSuDung", "hansudung");
        String maHang = firstNonEmpty(snapshot, "maHang", "MaHang", "maSP", "MaSP", "maNhapHang", "MaNhapHang");
        Date ngayNhap = firstDate(snapshot, "ngayNhap", "NgayNhap", "ngayTao", "createdAt");
        double soLuong = firstNumber(snapshot, model.getSoLuong(), "soLuong", "SoLuong");
        long soNgayConLai = calcRemainingDays(ngayNhap, hanSuDung);

        holder.tvSoLo.setText(defaultText(soLo));
        holder.tvHanSuDung.setText(formatDate(hanSuDung));
        holder.tvMaHang.setText(defaultText(maHang));
        holder.tvNgayNhap.setText(formatDate(ngayNhap));
        holder.tvSoLuong.setText(String.format(Locale.getDefault(), "%,.0f", soLuong));
        holder.tvSoNgayConLai.setText(soNgayConLai == Long.MIN_VALUE ? "-" : String.valueOf(soNgayConLai));
    }

    @NonNull
    @Override
    public LoHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lo_hang, parent, false);
        return new LoHangViewHolder(view);
    }

    private static String firstNonEmpty(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            String value = snapshot.getString(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private static double firstNumber(DocumentSnapshot snapshot, double fallback, String... keys) {
        for (String key : keys) {
            Number value = snapshot.getDouble(key);
            if (value != null) {
                return value.doubleValue();
            }
            Object raw = snapshot.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
        }
        return fallback;
    }

    private static Date firstDate(DocumentSnapshot snapshot, String... keys) {
        for (String key : keys) {
            Date value = toDate(snapshot.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
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
            String[] patterns = new String[]{"dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"};
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
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private static long calcRemainingDays(Date ngayNhap, Date hanSuDung) {
        if (ngayNhap == null || hanSuDung == null) {
            return Long.MIN_VALUE;
        }
        // So ngay con lai theo du lieu Firebase: han su dung - ngay nhap.
        long diffMillis = stripTime(hanSuDung).getTime() - stripTime(ngayNhap).getTime();
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

    public static class LoHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvSoLo;
        TextView tvHanSuDung;
        TextView tvMaHang;
        TextView tvNgayNhap;
        TextView tvSoLuong;
        TextView tvSoNgayConLai;

        public LoHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSoLo = itemView.findViewById(R.id.tvSoLo);
            tvHanSuDung = itemView.findViewById(R.id.tvHanSuDung);
            tvMaHang = itemView.findViewById(R.id.tvMaHang);
            tvNgayNhap = itemView.findViewById(R.id.tvNgayNhap);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvSoNgayConLai = itemView.findViewById(R.id.tvSoNgayConLai);
        }
    }
}
