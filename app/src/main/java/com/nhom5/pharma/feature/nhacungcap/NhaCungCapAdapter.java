package com.nhom5.pharma.feature.nhacungcap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.nhom5.pharma.R;

public class NhaCungCapAdapter extends FirestoreRecyclerAdapter<NhaCungCap, NhaCungCapAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public NhaCungCapAdapter(@NonNull FirestoreRecyclerOptions<NhaCungCap> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull NhaCungCap model) {
        // Kiểm tra an toàn: Nếu model hoặc ID trống
        if (model == null) return;
        
        String id = model.getId();
        holder.tvMaNCC.setText(id != null ? id : "N/A"); 
        
        // Kiểm tra an toàn: Nếu các trường dữ liệu bị null trên Firestore
        holder.tvTenNCC.setText(model.getTenNCC() != null ? model.getTenNCC() : "Chưa có tên");
        
        // Sử dụng hàm fetchSdt() an toàn đã viết trong Model
        holder.tvSDT.setText(model.fetchSdt()); 
        holder.tvEmail.setText(model.getEmail() != null ? model.getEmail() : "---");
        
        // Sử dụng hàm fetchDisplayGiaTri() chuẩn (đã đổi tên để tránh làm bẩn Firebase)
        holder.tvTongMua.setText(model.fetchDisplayGiaTri());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nha_cung_cap, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaNCC, tvTenNCC, tvSDT, tvEmail, tvTongMua;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaNCC = itemView.findViewById(R.id.tvMaNCC);
            tvTenNCC = itemView.findViewById(R.id.tvTenNCC);
            tvSDT = itemView.findViewById(R.id.tvSDT);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTongMua = itemView.findViewById(R.id.tvTongMua);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(NhaCungCap ncc);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
