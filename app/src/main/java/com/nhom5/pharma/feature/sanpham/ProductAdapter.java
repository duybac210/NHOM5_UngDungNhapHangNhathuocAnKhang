package com.nhom5.pharma.feature.sanpham;

import android.graphics.Color;
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
            expandedPosition = (expandedPosition == holder.getBindingAdapterPosition()) ? -1 : holder.getBindingAdapterPosition();
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
        TextView tvDetailTen, tvDetailMaHang, tvDetailMoTa;
        View layoutDetail, btnDelete, btnEdit, lineMaHang;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHang = itemView.findViewById(R.id.tv_ma_hang);
            tvTenHang = itemView.findViewById(R.id.tv_ten_hang);
            tvGiaVon = itemView.findViewById(R.id.tv_gia_von);
            tvThoiGian = itemView.findViewById(R.id.tv_thoi_gian);
            lineMaHang = itemView.findViewById(R.id.line_ma_hang);
            layoutDetail = itemView.findViewById(R.id.layout_detail);
            tvDetailTen = itemView.findViewById(R.id.tv_detail_ten);
            tvDetailMaHang = itemView.findViewById(R.id.tv_detail_ma_hang);
            tvDetailMoTa = itemView.findViewById(R.id.tv_detail_mo_ta);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }

        void bind(Product product, boolean isExpanded) {
            tvMaHang.setText(product.getId());
            tvTenHang.setText(product.getTenSP());
            tvGiaVon.setText(String.format(Locale.getDefault(), "%,.0f", product.getGiavon()));
            
            if (product.getNgayTao() != null) {
                tvThoiGian.setText(sdf.format(product.getNgayTao()));
            } else {
                tvThoiGian.setText("---");
            }

            int blueColor = Color.parseColor("#2196f3");
            int blackColor = Color.BLACK;

            // Đổi màu dựa trên việc có đang xem chi tiết (isExpanded) hay không
            int color = isExpanded ? blueColor : blackColor;

            tvMaHang.setTextColor(color);
            tvTenHang.setTextColor(color);
            tvGiaVon.setTextColor(color);
            tvThoiGian.setTextColor(color);

            // Gạch chân màu xanh dưới mã hàng khi xem chi tiết
            if (lineMaHang != null) {
                lineMaHang.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            }

            layoutDetail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                tvDetailTen.setText(product.getTenSP());
                tvDetailMaHang.setText(product.getId());
                tvDetailMoTa.setText(product.getMoTa() != null ? product.getMoTa() : "Chưa có mô tả");
            }
        }
    }
}
