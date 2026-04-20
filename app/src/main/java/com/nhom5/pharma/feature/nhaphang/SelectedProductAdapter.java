package com.nhom5.pharma.feature.nhaphang;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        if (holder.quantityWatcher != null) {
            holder.tvQuantity.removeTextChangedListener(holder.quantityWatcher);
        }
        
        holder.tvProductName.setText(product.getTenSanPham());
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvProductPrice.setText(formatter.format(product.getDonGia()) + " đồng/hộp");
        holder.tvQuantity.setText(String.valueOf(product.getSoLuong()));
        holder.tvQuantity.setSelection(holder.tvQuantity.getText().length());
        holder.qtySelector.setBackgroundColor(Color.parseColor("#F0F0F0"));
        holder.btnMinus.setText("-");
        holder.btnPlus.setText("+");

        holder.quantityWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    int qty = Integer.parseInt(raw);
                    if (qty < 1) qty = 1;
                    if (qty != product.getSoLuong()) {
                        product.setSoLuong(qty);
                        if (listener != null) listener.onQuantityChanged();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        };
        holder.tvQuantity.addTextChangedListener(holder.quantityWatcher);
        holder.tvQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String raw = holder.tvQuantity.getText() != null ? holder.tvQuantity.getText().toString().trim() : "";
                if (raw.isEmpty()) {
                    product.setSoLuong(1);
                    holder.tvQuantity.setText("1");
                    if (listener != null) listener.onQuantityChanged();
                }
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            product.setSoLuong(product.getSoLuong() + 1);
            holder.tvQuantity.setText(String.valueOf(product.getSoLuong()));
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (product.getSoLuong() > 1) {
                product.setSoLuong(product.getSoLuong() - 1);
                holder.tvQuantity.setText(String.valueOf(product.getSoLuong()));
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

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder.quantityWatcher != null) {
            holder.tvQuantity.removeTextChangedListener(holder.quantityWatcher);
            holder.quantityWatcher = null;
        }
        holder.tvQuantity.setOnFocusChangeListener(null);
        super.onViewRecycled(holder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, btnMinus, btnPlus;
        EditText tvQuantity;
        LinearLayout qtySelector;
        TextWatcher quantityWatcher;

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
