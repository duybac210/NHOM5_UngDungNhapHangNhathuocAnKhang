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
        // KIỂM TRA AN TOÀN DATA
        if (model == null) return;
        
        // Gán ID từ snapshot để đảm bảo chính xác
        String id = getSnapshots().getSnapshot(position).getId();
        model.setId(id);

        holder.tvMaNCC.setText(id); 
        holder.tvTenNCC.setText(model.getTenNCC() != null ? model.getTenNCC() : "Chưa có tên");
        holder.tvSDT.setText(model.fetchSdt()); 
        holder.tvEmail.setText(model.getEmail() != null ? model.getEmail() : "---");
        holder.tvTongMua.setText(model.fetchDisplayGiaTri());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Kiểm tra lại position trước khi gọi listener
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(currentPos));
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nha_cung_cap, parent, false);
        return new ViewHolder(view);
    }

    // Ghi đè để tránh lỗi IndexOutOfBounds khi data thay đổi nhanh
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyDataSetChanged();
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
