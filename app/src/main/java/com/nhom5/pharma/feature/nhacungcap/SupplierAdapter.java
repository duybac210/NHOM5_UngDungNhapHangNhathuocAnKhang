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

public class SupplierAdapter extends FirestoreRecyclerAdapter<Supplier, SupplierAdapter.ViewHolder> {

    public SupplierAdapter(@NonNull FirestoreRecyclerOptions<Supplier> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Supplier model) {
        holder.tvMa.setText(model.getMaID());
        holder.tvTen.setText(model.getTenNhaCungCap());
        holder.tvPhone.setText(model.getPhone());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMa, tvTen, tvPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMa = itemView.findViewById(R.id.tvMaID);
            tvTen = itemView.findViewById(R.id.tvTen);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }
}
