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

    public interface OnProductClickListener {
        void onItemClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    private final OnProductClickListener listener;
    private final boolean selectMode;
    private final List<Product> productList = new ArrayList<>();
    private int expandedPosition = -1;

    public ProductAdapter(OnProductClickListener listener) {
        this(listener, false);
    }

    public ProductAdapter(OnProductClickListener listener, boolean selectMode) {
        this.listener = listener;
        this.selectMode = selectMode;
    }

    public void setProductList(List<Product> newList) {
        productList.clear();
        if (newList != null) {
            productList.addAll(newList);
        }
        expandedPosition = -1;
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
        boolean isExpanded = !selectMode && position == expandedPosition;
        holder.bind(product, isExpanded, selectMode);

        holder.itemView.setOnClickListener(v -> {
<<<<<<< HEAD
            if (selectMode) {
                if (listener != null) listener.onItemClick(product);
                return;
            }

            int previousExpanded = expandedPosition;
            expandedPosition = (expandedPosition == holder.getBindingAdapterPosition()) ? -1 : holder.getBindingAdapterPosition();
            if (previousExpanded >= 0) notifyItemChanged(previousExpanded);
            if (expandedPosition >= 0) notifyItemChanged(expandedPosition);
=======
            int previousExpanded = expandedPosition;
            expandedPosition = (expandedPosition == holder.getBindingAdapterPosition()) ? -1 : holder.getBindingAdapterPosition();
            notifyItemChanged(previousExpanded);
            notifyItemChanged(expandedPosition);
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
        });

        if (!selectMode) {
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(product);
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(product);
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
<<<<<<< HEAD
        private final TextView tvMaHang;
        private final TextView tvTenHang;
        private final TextView tvGiaVon;
        private final TextView tvThoiGian;
        private final TextView tvDetailTen;
        private final TextView tvDetailMaHang;
        private final TextView tvDetailMoTa;
        private final TextView tvDetailGiaVon;
        private final View layoutDetail;
        private final View btnDelete;
        private final View btnEdit;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
=======
        TextView tvMaHang, tvTenHang, tvGiaVon, tvThoiGian;
        TextView tvDetailTen, tvDetailMaHang, tvDetailMoTa, tvDetailGiaVon, tvDetailHangSX, tvDetailNuocSX;
        View layoutDetail, btnDelete, btnEdit, lineMaHang;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHang = itemView.findViewById(R.id.tv_ma_hang);
            tvTenHang = itemView.findViewById(R.id.tv_ten_hang);
            tvGiaVon = itemView.findViewById(R.id.tv_gia_von);
            tvThoiGian = itemView.findViewById(R.id.tv_thoi_gian);
<<<<<<< HEAD

            layoutDetail = itemView.findViewById(R.id.layout_detail);
            tvDetailTen = itemView.findViewById(R.id.tv_detail_ten);
            tvDetailMaHang = itemView.findViewById(R.id.tv_detail_ma_hang);
            tvDetailMoTa = itemView.findViewById(R.id.tv_detail_ma_vach);
            tvDetailGiaVon = itemView.findViewById(R.id.tv_detail_gia_von);

=======
            lineMaHang = itemView.findViewById(R.id.line_ma_hang);
            layoutDetail = itemView.findViewById(R.id.layout_detail);
            tvDetailTen = itemView.findViewById(R.id.tv_detail_ten);
            tvDetailMaHang = itemView.findViewById(R.id.tv_detail_ma_hang);
            tvDetailHangSX = itemView.findViewById(R.id.tv_detail_hang_sx);
            tvDetailNuocSX = itemView.findViewById(R.id.tv_detail_nuoc_sx);
            tvDetailMoTa = itemView.findViewById(R.id.tv_detail_mo_ta);
            tvDetailGiaVon = itemView.findViewById(R.id.tv_detail_gia_von);
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }

<<<<<<< HEAD
        void bind(Product product, boolean isExpanded, boolean selectMode) {
            tvMaHang.setText(product.getDisplayId());
            tvTenHang.setText(product.getTenSP());
            tvGiaVon.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getGiavon()));
            tvThoiGian.setText(product.getNgayTao() != null ? sdf.format(product.getNgayTao()) : "");

            if (selectMode) {
                layoutDetail.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                return;
            }

            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
=======
        void bind(Product product, boolean isExpanded) {
            tvMaHang.setText(product.getId());
            tvTenHang.setText(product.getTenSP());
            tvGiaVon.setText(String.format(Locale.getDefault(), "%,.0f", product.getGiavon()));
            
            if (product.getNgayTao() != null) {
                tvThoiGian.setText(sdf.format(product.getNgayTao()));
            } else {
                tvThoiGian.setText("---");
            }

            int color = isExpanded ? Color.parseColor("#2196f3") : Color.BLACK;
            tvMaHang.setTextColor(color);
            tvTenHang.setTextColor(color);
            tvGiaVon.setTextColor(color);
            if (lineMaHang != null) lineMaHang.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
            layoutDetail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                tvDetailTen.setText(product.getTenSP());
<<<<<<< HEAD
                tvDetailMaHang.setText(product.getDisplayId());
                tvDetailMoTa.setText(product.getMoTa() != null ? product.getMoTa() : "Chưa có");
                tvDetailGiaVon.setText(String.format(Locale.getDefault(), "%,.0fđ", product.getGiavon()));
=======
                tvDetailMaHang.setText(product.getId());
                tvDetailHangSX.setText(product.getHangSX() != null ? product.getHangSX() : "---");
                tvDetailNuocSX.setText(product.getNuocSX() != null ? product.getNuocSX() : "---");
                tvDetailMoTa.setText(product.getMoTa() != null ? product.getMoTa() : "Chưa có mô tả");
                tvDetailGiaVon.setText(String.format(Locale.getDefault(), "%,.0f", product.getGiavon()));
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
            }
        }
    }
}
