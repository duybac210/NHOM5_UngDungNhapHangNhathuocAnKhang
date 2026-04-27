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
import com.nhom5.pharma.util.FirestoreValueParser;

import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NhapHangAdapter extends FirestoreRecyclerAdapter<NhapHang, NhapHangAdapter.NhapHangViewHolder> {

    private int expandedPosition = -1;
    private final NhapHangRepository repository = NhapHangRepository.getInstance();

    public NhapHangAdapter(@NonNull FirestoreRecyclerOptions<NhapHang> options) {
        super(options);
    }

    @Override
    @SuppressWarnings("RestrictedApi")
    protected void onBindViewHolder(@NonNull NhapHangViewHolder holder, int position, @NonNull NhapHang model) {
        String documentId = getSnapshots().getSnapshot(position).getId();
        String displayId = model.getDisplayId();
        if (displayId == null || displayId.trim().isEmpty()) {
            displayId = documentId;
        }
        holder.tvMaDon.setText(displayId);

        if (model.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayNhap.setText(sdf.format(model.getNgayTao()));
        }

        holder.tvTongTien.setText(String.format(Locale.getDefault(), "%,.0fđ", model.getTongTien()));

        int trangThai = model.getTrangThaiValue();
        if (trangThai == 1) {
            holder.tvTrangThai.setText(R.string.import_status_imported);
            holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTrangThai.setText(R.string.import_status_cancelled);
            holder.tvTrangThai.setTextColor(Color.parseColor("#F44336"));
        }

        // Logic Expand/Collapse
        final boolean isExpanded = (position == expandedPosition);

        int blueColor = Color.parseColor("#2196f3");
        int blackColor = Color.BLACK;
        int color = isExpanded ? blueColor : blackColor;

        holder.tvMaDon.setTextColor(color);
        holder.tvNgayNhap.setTextColor(color);
        holder.tvTongTien.setTextColor(color);

        if (holder.lineMaDon != null) {
            holder.lineMaDon.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        holder.expandableDetail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            loadDetailContent(holder, documentId, model);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION)
                return;

            int prev = expandedPosition;
            expandedPosition = isExpanded ? -1 : currentPos;

            notifyItemChanged(prev);
            notifyItemChanged(expandedPosition);
        });

        holder.btnXoaDetail.setOnClickListener(v -> showDeleteConfirmation(holder.itemView, documentId));

        holder.btnSuaDetail.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), TaoDonNhapActivity.class);
            intent.putExtra("EXTRA_EDIT_MODE", true);
            intent.putExtra("EXTRA_ORDER_ID", documentId);
            v.getContext().startActivity(intent);
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

        tvMessage.setText(String.format(view.getContext().getString(R.string.import_order_delete_message), orderId));

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

    @SuppressWarnings("RestrictedApi")
    private void loadDetailContent(NhapHangViewHolder holder, String orderId, NhapHang model) {
        if (model.getNgayTao() != null) {
            SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvNgayNhapDetail.setText(sdfFull.format(model.getNgayTao()));
        }

        if (model.getMaNCC() != null) {
            repository.getSupplierById(model.getMaNCC()).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("tenNCC");
                    if (name == null)
                        name = doc.getString("TenNCC");
                    if (name == null)
                        name = doc.getString("tenNhaCungCap");
                    if (name == null)
                        name = doc.getString("ten");
                    holder.tvNhaCungCapDetail.setText(name != null ? name : "Không xác định");
                }
            });
        }

        if (model.getMaNguoiNhap() != null) {
            repository.getUserById(model.getMaNguoiNhap()).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("tenNguoiDung");
                    if (name == null)
                        name = doc.getString("hoTen");
                    holder.tvNguoiNhapDetail.setText(name != null ? name : model.getMaNguoiNhap());
                } else {
                    holder.tvNguoiNhapDetail.setText(String.format(
                            holder.itemView.getContext().getString(R.string.user_code_format), model.getMaNguoiNhap()));
                }
            });
        }

        holder.llChiTietHang.removeAllViews();
        repository.getLoHangByNhapHangId(orderId).addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                View itemView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_chi_tiet_lo_hang_nhap_hang, holder.llChiTietHang, false);

                ((TextView) itemView.findViewById(R.id.tvSoLo)).setText(doc.getId());

                Double sl = doc.getDouble("soLuong");
                double soLuong = sl != null ? sl : 0;

                TextView tvDonGia = itemView.findViewById(R.id.tvDonGia);
                TextView tvThanhTien = itemView.findViewById(R.id.tvThanhTien);

                ((TextView) itemView.findViewById(R.id.tvSoLuong))
                        .setText(String.format(Locale.getDefault(), "%,.0f", soLuong));

                String maSP = doc.getString("maSP");
                double donGia = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "donGiaNhap"));
                if (donGia <= 0 && maSP != null) {
                    repository.getProductById(maSP).addOnSuccessListener(productDoc -> {
                        if (productDoc.exists()) {
                            double giaVon = FirestoreValueParser.safeDouble(
                                    FirestoreValueParser.safeRaw(productDoc, "giavon", "GiaVon", "giaNhap", "GiaNhap"));
                            if (giaVon > 0) {
                                tvDonGia.setText(String.format(Locale.getDefault(), "%,.0fđ", giaVon));
                                tvThanhTien.setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * giaVon));
                            }
                        }
                    });
                } else {
                    tvDonGia.setText(String.format(Locale.getDefault(), "%,.0fđ", donGia));
                    tvThanhTien.setText(String.format(Locale.getDefault(), "%,.0fđ", soLuong * donGia));
                }

                if (maSP != null) {
                    repository.getProductById(maSP).addOnSuccessListener(spDoc -> {
                        if (spDoc.exists()) {
                            String tenSP = spDoc.getString("tenSP");
                            if (tenSP == null)
                                tenSP = spDoc.getString("TenSP");
                            ((TextView) itemView.findViewById(R.id.tvTenHang)).setText(tenSP);
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
        View expandableDetail, lineMaDon;
        TextView tvNguoiNhapDetail, tvNgayNhapDetail, tvNhaCungCapDetail;
        LinearLayout llChiTietHang;
        Button btnXoaDetail, btnSuaDetail;

        public NhapHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            tvNgayNhap = itemView.findViewById(R.id.tvNgayNhap);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);

            expandableDetail = itemView.findViewById(R.id.expandableDetail);
            lineMaDon = itemView.findViewById(R.id.lineMaDon);
            tvNguoiNhapDetail = itemView.findViewById(R.id.tvNguoiNhapDetail);
            tvNgayNhapDetail = itemView.findViewById(R.id.tvNgayNhapDetail);
            tvNhaCungCapDetail = itemView.findViewById(R.id.tvNhaCungCapDetail);
            llChiTietHang = itemView.findViewById(R.id.llChiTietHang);

            btnXoaDetail = itemView.findViewById(R.id.btnXoaDetail);
            btnSuaDetail = itemView.findViewById(R.id.btnSuaDetail);
        }
    }
}
