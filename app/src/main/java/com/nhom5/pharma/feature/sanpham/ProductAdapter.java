package com.nhom5.pharma.feature.sanpham;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.pharma.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private int expandedPosition = -1;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProductList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, position == expandedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousExpanded = expandedPosition;
            if (expandedPosition == position) {
                expandedPosition = -1;
            } else {
                expandedPosition = position;
            }
            notifyItemChanged(previousExpanded);
            notifyItemChanged(expandedPosition);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaHang, tvTenHang, tvGiaVon, tvThoiGian;
        TextView tvDetailTen, tvDetailMaHang, tvDetailMoTa, tvDetailGiaVon;
        View layoutDetail, btnDelete, btnEdit;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHang = itemView.findViewById(R.id.tv_ma_hang);
            tvTenHang = itemView.findViewById(R.id.tv_ten_hang);
            tvGiaVon = itemView.findViewById(R.id.tv_gia_von);
            tvThoiGian = itemView.findViewById(R.id.tv_thoi_gian);
            
            layoutDetail = itemView.findViewById(R.id.layout_detail);
            tvDetailTen = itemView.findViewById(R.id.tv_detail_ten);
            tvDetailMaHang = itemView.findViewById(R.id.tv_detail_ma_hang);
            tvDetailMoTa = itemView.findViewById(R.id.tv_detail_ma_vach); // Reuse ID for Mo Ta
            tvDetailGiaVon = itemView.findViewById(R.id.tv_detail_gia_von);
            
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }

        void bind(Product product, boolean isExpanded) {
            tvMaHang.setText(product.getId()); // Document ID is Ma Hang
            tvTenHang.setText(product.getTenSP());
            tvGiaVon.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getGiavon()));
            if (product.getNgayTao() != null) {
                tvThoiGian.setText(sdf.format(product.getNgayTao()));
            } else {
                tvThoiGian.setText("");
            }

            layoutDetail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if (isExpanded) {
                tvDetailTen.setText(product.getTenSP());
                tvDetailMaHang.setText(product.getId());
                tvDetailMoTa.setText(product.getMoTa() != null ? product.getMoTa() : "Chưa có");
                tvDetailGiaVon.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getGiavon()));
            }
        }
    }
}
