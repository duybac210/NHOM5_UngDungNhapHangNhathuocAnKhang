package com.nhom5.pharma.feature.sanpham;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.nhom5.pharma.R;
import java.text.DecimalFormat;

public class SanPhamAdapter extends FirestoreRecyclerAdapter<SanPham, SanPhamAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SanPham sanPham);
    }

    public SanPhamAdapter(@NonNull FirestoreRecyclerOptions<SanPham> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull SanPham model) {
        holder.tvName.setText(model.getTenSanPham());
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvPrice.setText(formatter.format(model.getGiaBan()) + " đ");
        holder.tvManufacturer.setText(model.getHangSanXuat());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_selected_product làm layout mẫu hoặc tạo cái mới đơn giản hơn
        // Ở đây tôi giả định bạn muốn một layout đơn giản cho danh sách chọn
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvManufacturer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
            tvPrice = itemView.findViewById(android.R.id.text2);
            tvManufacturer = new TextView(itemView.getContext()); // Placeholder
        }
    }
}
