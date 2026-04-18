package com.nhom5.pharma.feature.nhaphang;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom5.pharma.feature.lohang.LoHangFilterType;
import com.nhom5.pharma.util.FirestoreValueParser;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NhapHangRepository {
    private static NhapHangRepository instance;
    private final FirebaseFirestore db;
    private boolean statusNormalizationDone;
    private boolean statusNormalizationRunning;
    private boolean fieldNormalizationDone;
    private boolean fieldNormalizationRunning;
    private static final String DEFAULT_MA_NGUOI_NHAP = "USER003";

    private NhapHangRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized NhapHangRepository getInstance() {
        if (instance == null) {
            instance = new NhapHangRepository();
        }
        return instance;
    }

    public Query getAllNhapHang() {
        normalizeStatusSchemaOnce();
        normalizeFieldSchemaOnce();
        forceNormalizeKnownPendingDocs();
        return db.collection("NhapHang")
                .orderBy("ngayTao", Query.Direction.DESCENDING);
    }

    private void normalizeStatusSchemaOnce() {
        if (statusNormalizationDone || statusNormalizationRunning) {
            return;
        }
        statusNormalizationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Object statusRaw = safeGetField(doc, "trangThai");
                String currentText = safeGetString(doc, "trangThaiText");

                int normalizedStatus = normalizeTrangThaiValue(statusRaw, currentText);
                String normalizedText = normalizedStatus == 1 ? "Đã nhập kho" : "Đã hủy";

                Integer currentStatus = FirestoreValueParser.safeInt(doc, "trangThai");

                boolean statusNeedsUpdate = currentStatus == null || currentStatus != normalizedStatus;
                boolean textNeedsUpdate = currentText == null || !normalizedText.equals(currentText);

                if (statusNeedsUpdate || textNeedsUpdate) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("trangThai", normalizedStatus);
                    updates.put("trangThaiText", normalizedText);
                    batch.update(doc.getReference(), updates);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                batch.commit()
                        .addOnSuccessListener(unused -> {
                            statusNormalizationDone = true;
                            statusNormalizationRunning = false;
                        })
                        .addOnFailureListener(e -> statusNormalizationRunning = false);
            } else {
                statusNormalizationDone = true;
                statusNormalizationRunning = false;
            }
        }).addOnFailureListener(e -> statusNormalizationRunning = false);
    }

    private void normalizeFieldSchemaOnce() {
        if (fieldNormalizationDone || fieldNormalizationRunning) {
            return;
        }
        fieldNormalizationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> updates = buildLegacyFieldUpdates(doc);
                if (!updates.isEmpty()) {
                    batch.update(doc.getReference(), updates);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                batch.commit()
                        .addOnSuccessListener(unused -> {
                            fieldNormalizationDone = true;
                            fieldNormalizationRunning = false;
                        })
                        .addOnFailureListener(e -> fieldNormalizationRunning = false);
            } else {
                fieldNormalizationDone = true;
                fieldNormalizationRunning = false;
            }
        }).addOnFailureListener(e -> fieldNormalizationRunning = false);
    }

    private Map<String, Object> buildLegacyFieldUpdates(DocumentSnapshot doc) {
        Map<String, Object> updates = new HashMap<>();

        String maNCC = safeGetString(doc, "maNCC");
        String maNhaCungCap = safeGetString(doc, "maNhaCungCap");
        if ((maNCC == null || maNCC.trim().isEmpty()) && maNhaCungCap != null && !maNhaCungCap.trim().isEmpty()) {
            updates.put("maNCC", maNhaCungCap.trim());
        }

        String maNguoiNhap = safeGetString(doc, "maNguoiNhap");
        if (maNguoiNhap == null || maNguoiNhap.trim().isEmpty()) {
            updates.put("maNguoiNhap", DEFAULT_MA_NGUOI_NHAP);
        }

        if (!doc.contains("ghiChu")) {
            updates.put("ghiChu", "");
        }

        if (!doc.contains("ngayTao")) {
            Object ngayNhapRaw = safeGetField(doc, "ngayNhap");
            Object createdAtRaw = safeGetField(doc, "createdAt");
            if (ngayNhapRaw != null) {
                updates.put("ngayTao", ngayNhapRaw);
            } else if (createdAtRaw != null) {
                updates.put("ngayTao", createdAtRaw);
            } else {
                updates.put("ngayTao", Timestamp.now());
            }
        }

        updates.put("ngayCapNhat", FieldValue.serverTimestamp());

        // Xoa cac key lech schema cu.
        if (doc.contains("maNhaCungCap")) updates.put("maNhaCungCap", FieldValue.delete());
        if (doc.contains("tenNhaCungCap")) updates.put("tenNhaCungCap", FieldValue.delete());
        if (doc.contains("hinhThucThanhToan")) updates.put("hinhThucThanhToan", FieldValue.delete());
        if (doc.contains("createdAt")) updates.put("createdAt", FieldValue.delete());
        if (doc.contains("maID")) updates.put("maID", FieldValue.delete());

        return updates;
    }

    private void forceNormalizeKnownPendingDocs() {
        forceNormalizeDoc("NH0021");
        forceNormalizeDoc("NH0022");
        forceNormalizeDoc("NH0023");
        forceNormalizeDoc("NH004");
        forceNormalizeDoc("NH0004");
    }

    private void forceNormalizeDoc(String id) {
        db.collection("NhapHang")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        return;
                    }

                    Object statusRaw = safeGetField(doc, "trangThai");
                    String currentText = safeGetString(doc, "trangThaiText");

                    int normalizedStatus = normalizeTrangThaiValue(statusRaw, currentText);
                    String normalizedText = normalizedStatus == 1 ? "Đã nhập kho" : "Đã hủy";

                    Map<String, Object> updates = buildLegacyFieldUpdates(doc);

                    Integer rawStatus = FirestoreValueParser.safeInt(doc, "trangThai");
                    if (rawStatus == null || rawStatus != normalizedStatus) {
                        updates.put("trangThai", normalizedStatus);
                    }
                    if (currentText == null || !normalizedText.equals(currentText)) {
                        updates.put("trangThaiText", normalizedText);
                    }

                    if (!updates.isEmpty()) {
                        doc.getReference().update(updates);
                    }
                });
    }

    private Object safeGetField(DocumentSnapshot doc, String field) {
        try {
            return doc.get(field);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String safeGetString(DocumentSnapshot doc, String field) {
        try {
            return doc.getString(field);
        } catch (RuntimeException ignored) {
            Object raw = safeGetField(doc, field);
            return raw == null ? null : String.valueOf(raw);
        }
    }

    private int normalizeTrangThaiValue(Object trangThaiRaw, String trangThaiText) {
        if (trangThaiRaw instanceof Number) {
            int value = ((Number) trangThaiRaw).intValue();
            return value == 1 ? 1 : 0;
        }
        if (trangThaiRaw instanceof Boolean) {
            return (Boolean) trangThaiRaw ? 1 : 0;
        }
        if (trangThaiRaw instanceof String) {
            String value = ((String) trangThaiRaw).trim().toLowerCase(Locale.ROOT);
            if ("1".equals(value) || "true".equals(value) || "da nhap kho".equals(value) || "đã nhập kho".equals(value)) {
                return 1;
            }
            return 0;
        }

        if (trangThaiText != null) {
            String text = trangThaiText.trim().toLowerCase(Locale.ROOT);
            if ("da nhap kho".equals(text) || "đã nhập kho".equals(text) || "đã nhập hàng".equals(text)) {
                return 1;
            }
        }
        return 0;
    }

    public Query getAllLoHang() {
        return db.collection("LoHang")
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING);
    }

    // Tìm kiếm theo Mã đơn (Document ID) thời gian thực
    public Query searchByMaDon(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllNhapHang();
        }

        return db.collection("NhapHang")
                .orderBy(FieldPath.documentId())
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    public Query searchLoHang(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllLoHang();
        }

        String keyword = searchText.trim();
        return db.collection("LoHang")
                .orderBy(FieldPath.documentId())
                .startAt(keyword)
                .endAt(keyword + "\uf8ff");
    }

    public Query getLoHangByFilter(int filterType) {
        switch (filterType) {
            case LoHangFilterType.EXPIRING_SOON:
            case LoHangFilterType.EXPIRED:
                // 2 filter nay can tinh (hanSuDung - ngayNhap), xu ly o Adapter.
                return getAllLoHang();
            case LoHangFilterType.LOW_STOCK:
                return getAllLoHang();
            case LoHangFilterType.ALL:
            default:
                return getAllLoHang();
        }
    }

    public Task<DocumentSnapshot> getNhapHangById(String id) {
        return db.collection("NhapHang").document(id).get();
    }

    public Task<DocumentSnapshot> getSupplierById(String maNCC) {
        return db.collection("NhaCungCap").document(maNCC).get();
    }

    // Lấy thông tin tài khoản người nhập từ mã người nhập (String ID)
    public Task<DocumentSnapshot> getUserById(String maNguoiNhap) {
        return db.collection("TaiKhoan").document(maNguoiNhap).get();
    }

    public Task<QuerySnapshot> getLoHangByNhapHangId(String nhapHangId) {
        return db.collection("LoHang")
                .whereEqualTo("maNhapHang", nhapHangId)
                .get();
    }

    public Task<DocumentSnapshot> getLoHangById(String soLo) {
        return db.collection("LoHang").document(soLo).get();
    }

    public Task<DocumentSnapshot> getProductById(String maSP) {
        return db.collection("SanPham").document(maSP).get();
    }

    public Task<Void> deleteNhapHang(String nhapHangId) {
        WriteBatch batch = db.batch();
        batch.delete(db.collection("NhapHang").document(nhapHangId));

        return getLoHangByNhapHangId(nhapHangId).continueWithTask(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
            }
            return batch.commit();
        });
    }

    public void ensureLegacyFieldSchema() {
        normalizeFieldSchemaOnce();
    }
}
