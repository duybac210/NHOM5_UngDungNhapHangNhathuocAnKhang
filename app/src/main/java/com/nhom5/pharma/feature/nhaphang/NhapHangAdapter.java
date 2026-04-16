package com.nhom5.pharma.feature.nhaphang;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.nhom5.pharma.R;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class NhapHangAdapter extends FirestoreRecyclerAdapter<NhapHang, NhapHangAdapter.NhapHangViewHolder> {

    public NhapHangAdapter(@NonNull FirestoreRecyclerOptions<NhapHang> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NhapHangViewHolder holder, int position, @NonNull NhapHang model) {
        String documentId = getSnapshots().getSnapshot(position).getId();
        String displayId = model.getDisplayId();
        if (displayId == null || displayId.trim().isEmpty()) {
            displayId = documentId;
        }
        holder.tvMaDon.setText(displayId);

        if (model.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayNhap.setText(sdf.format(model.getNgayTao()));
        }

        // Bỏ khoảng trống: "%,.0f đ" -> "%,.0fđ"
        holder.tvTongTien.setText(String.format(Locale.getDefault(), "%,.0fđ", model.getTongTien()));

        int trangThai = model.getTrangThaiValue();
        if (trangThai == 1) {
            holder.tvTrangThai.setText("Đã nhập kho");
            holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTrangThai.setText("Đã hủy");
            holder.tvTrangThai.setTextColor(Color.parseColor("#F44336"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChiTietNhapHangActivity.class);
            intent.putExtra("NHAP_HANG_ID", documentId);
            v.getContext().startActivity(intent);
        });
    }

    @NonNull
    @Override
    public NhapHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nhap_hang, parent, false);
        return new NhapHangViewHolder(view);
    }

    class NhapHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDon, tvNgayNhap, tvTongTien, tvTrangThai;

        public NhapHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            tvNgayNhap = itemView.findViewById(R.id.tvNgayNhap);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
