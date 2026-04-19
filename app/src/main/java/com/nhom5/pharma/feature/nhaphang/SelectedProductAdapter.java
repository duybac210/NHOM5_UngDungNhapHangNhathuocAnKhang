package com.nhom5.pharma.feature.nhaphang;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom5.pharma.R;
import java.text.DecimalFormat;
import java.util.List;

public class SelectedProductAdapter extends RecyclerView.Adapter<SelectedProductAdapter.ViewHolder> {

    private List<SelectedProduct> productList;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public SelectedProductAdapter(List<SelectedProduct> productList, OnQuantityChangeListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectedProduct product = productList.get(position);
        
        holder.tvProductName.setText(product.getTenSanPham());
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvProductPrice.setText(formatter.format(product.getDonGia()) + " đồng/hộp");
        holder.tvQuantity.setText(String.valueOf(product.getSoLuong()));
        holder.qtySelector.setBackgroundColor(Color.parseColor("#F0F0F0"));
        holder.btnMinus.setText("-");
        holder.btnPlus.setText("+");

        holder.btnPlus.setOnClickListener(v -> {
            product.setSoLuong(product.getSoLuong() + 1);
            notifyItemChanged(position);
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (product.getSoLuong() > 1) {
                product.setSoLuong(product.getSoLuong() - 1);
                notifyItemChanged(position);
            } else {
                // Nếu giảm về 0 thì xóa khỏi danh sách
                productList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, productList.size());
            }
            if (listener != null) listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvQuantity, btnMinus, btnPlus;
        LinearLayout qtySelector;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            qtySelector = itemView.findViewById(R.id.qtySelector);
        }
    }
}
