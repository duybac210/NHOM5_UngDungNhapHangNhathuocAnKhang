package com.nhom5.pharma.feature.nhaphang;

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
        // 1. Hiển thị Mã đơn (Lấy ID của Document trong Firestore)
        String id = getSnapshots().getSnapshot(position).getId();
        holder.tvMaDon.setText(id.substring(0, Math.min(id.length(), 6))); // Lấy 6 ký tự đầu cho gọn

        // 2. Định dạng ngày tháng
        if (model.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayNhap.setText(sdf.format(model.getCreatedAt()));
        }

        // 3. Định dạng tiền tệ
        holder.tvTongTien.setText(String.format("%,.0f đ", model.getTongTien()));

        // 4. Hiển thị trạng thái màu sắc cho "xịn"
        if (model.getTrangThai() == 1) {
            holder.tvTrangThai.setText("Đã nhập hàng");
            holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
        } else {
            holder.tvTrangThai.setText("Chờ xử lý");
            holder.tvTrangThai.setTextColor(Color.parseColor("#F44336")); // Đỏ
        }
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