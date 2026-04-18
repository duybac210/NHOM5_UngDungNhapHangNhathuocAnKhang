package com.nhom5.pharma.feature.nhaphang;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom5.pharma.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NhapHangAdapter extends FirestoreRecyclerAdapter<NhapHang, NhapHangAdapter.NhapHangViewHolder> {

    private int expandedPosition = -1;
    private final NhapHangRepository repository = NhapHangRepository.getInstance();
    private static final int ACTIVE_BLUE = Color.parseColor("#2196f3");
    private static final int DEFAULT_TEXT = Color.BLACK;

    public NhapHangAdapter(@NonNull FirestoreRecyclerOptions<NhapHang> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NhapHangViewHolder holder, int position, @NonNull NhapHang model) {
        String id = getSnapshots().getSnapshot(position).getId();
        holder.tvMaDon.setText(id);

        if (model.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayNhap.setText(sdf.format(model.getNgayTao()));
        }

        holder.tvTongTien.setText(String.format(Locale.getDefault(), "%,.0fđ", model.getTongTien()));

        if (model.isTrangThai()) {
            holder.tvTrangThai.setText("Đã nhập hàng");
            holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTrangThai.setText("Đã hủy");
            holder.tvTrangThai.setTextColor(Color.parseColor("#F44336"));
        }

        // Logic Expand/Collapse
        final boolean isExpanded = (position == expandedPosition);
        holder.expandableDetail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        int color = isExpanded ? ACTIVE_BLUE : DEFAULT_TEXT;
        holder.tvMaDon.setTextColor(color);
        holder.tvNgayNhap.setTextColor(color);
        holder.tvTongTien.setTextColor(color);
        holder.tvTrangThai.setTextColor(isExpanded ? ACTIVE_BLUE : (model.isTrangThai() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336")));
        holder.lineMaDon.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        if (isExpanded) {
            loadDetailContent(holder, id, model);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int prev = expandedPosition;
            expandedPosition = (expandedPosition == currentPos) ? -1 : currentPos;

            if (prev != -1) {
                notifyItemChanged(prev);
            }
            if (expandedPosition != -1) {
                notifyItemChanged(expandedPosition);
            }
        });

        holder.btnXoaDetail.setOnClickListener(v -> showDeleteConfirmation(holder.itemView, id));
        
        holder.btnSuaDetail.setOnClickListener(v -> {
            // Logic cho nút Sửa (có thể mở màn hình chỉnh sửa hoặc Dialog)
            Toast.makeText(v.getContext(), "Tính năng sửa đang được phát triển cho mã " + id, Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmation(View view, String orderId) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_confirm_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(view.getContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        Button btnBoQua = dialogView.findViewById(R.id.btnBoQua);
        Button btnXoa = dialogView.findViewById(R.id.btnXoaConfirm);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);

        tvMessage.setText("Xóa phiếu nhập hàng " + orderId + "?");

        btnBoQua.setOnClickListener(v -> dialog.dismiss());
        btnXoa.setOnClickListener(v -> {
            dialog.dismiss();
            repository.deleteNhapHang(orderId).addOnSuccessListener(aVoid -> {
                Toast.makeText(view.getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                expandedPosition = -1;
            });
        });

        dialog.show();
    }

    private void loadDetailContent(NhapHangViewHolder holder, String orderId, NhapHang model) {
        if (model.getNgayTao() != null) {
            SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvNgayNhapDetail.setText(sdfFull.format(model.getNgayTao()));
        }

        if (model.getMaNCC() != null) {
            repository.getSupplierById(model.getMaNCC()).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("tenNCC");
                    if (name == null) name = doc.getString("TenNCC");
                    holder.tvNhaCungCapDetail.setText(name);
                }
            });
        }

        if (model.getMaNguoiNhap() != null) {
            repository.getUserById(model.getMaNguoiNhap()).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("tenNguoiDung");
                    if (name == null) name = doc.getString("hoTen");
                    holder.tvNguoiNhapDetail.setText(name != null ? name : model.getMaNguoiNhap());
                } else {
                    holder.tvNguoiNhapDetail.setText("Mã: " + model.getMaNguoiNhap());
                }
            });
        }

        holder.llChiTietHang.removeAllViews();
        repository.getLoHangByNhapHangId(orderId).addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                View itemView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_chi_tiet_lo_hang, holder.llChiTietHang, false);
                
                ((TextView)itemView.findViewById(R.id.tvSoLo)).setText(doc.getId());
                
                Double sl = doc.getDouble("soLuong");
                Double dg = doc.getDouble("donGiaNhap");
                double soLuong = sl != null ? sl : 0;
                double donGia = dg != null ? dg : 0;
                
                ((TextView)itemView.findViewById(R.id.tvSoLuong)).setText(String.format(Locale.getDefault(), "%,.0f", soLuong));
                ((TextView)itemView.findViewById(R.id.tvDonGia)).setText(String.format(Locale.getDefault(), "%,.0fđ", donGia));
                ((TextView)itemView.findViewById(R.id.tvThanhTien)).setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * donGia));
                
                String maSP = doc.getString("maSP");
                if (maSP != null) {
                    repository.getProductById(maSP).addOnSuccessListener(spDoc -> {
                        if (spDoc.exists()) {
                            String tenSP = spDoc.getString("tenSP");
                            if (tenSP == null) tenSP = spDoc.getString("TenSP");
                            ((TextView)itemView.findViewById(R.id.tvTenHang)).setText(tenSP);
                        }
                    });
                }
                holder.llChiTietHang.addView(itemView);
            }
        });
    }

    @NonNull
    @Override
    public NhapHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nhap_hang, parent, false);
        return new NhapHangViewHolder(view);
    }

    public static class NhapHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDon, tvNgayNhap, tvTongTien, tvTrangThai;
        View lineMaDon;
        View expandableDetail;
        TextView tvNguoiNhapDetail, tvNgayNhapDetail, tvNhaCungCapDetail;
        LinearLayout llChiTietHang;
        Button btnXoaDetail, btnSuaDetail;

        public NhapHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            lineMaDon = itemView.findViewById(R.id.line_ma_don);
            tvNgayNhap = itemView.findViewById(R.id.tvNgayNhap);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            
            expandableDetail = itemView.findViewById(R.id.expandableDetail);
            tvNguoiNhapDetail = itemView.findViewById(R.id.tvNguoiNhapDetail);
            tvNgayNhapDetail = itemView.findViewById(R.id.tvNgayNhapDetail);
            tvNhaCungCapDetail = itemView.findViewById(R.id.tvNhaCungCapDetail);
            llChiTietHang = itemView.findViewById(R.id.llChiTietHang);
            
            btnXoaDetail = itemView.findViewById(R.id.btnXoaDetail);
            btnSuaDetail = itemView.findViewById(R.id.btnSuaDetail);
        }
    }
}
