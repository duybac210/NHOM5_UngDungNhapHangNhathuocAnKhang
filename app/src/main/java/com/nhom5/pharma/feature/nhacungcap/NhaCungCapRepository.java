package com.nhom5.pharma.feature.nhacungcap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class NhaCungCapRepository {
    private static NhaCungCapRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference collection;
    private boolean schemaNormalized;
    private boolean schemaNormalizing;

    private NhaCungCapRepository() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection("NhaCungCap");
    }

    public static synchronized NhaCungCapRepository getInstance() {
        if (instance == null) {
            instance = new NhaCungCapRepository();
        }
        return instance;
    }

    public Query getAllNhaCungCap() {
        normalizeSupplierSchemaOnce();
        return collection.orderBy(FieldPath.documentId(), Query.Direction.ASCENDING);
    }

    public void ensureCanonicalSchema() {
        normalizeSupplierSchemaOnce();
    }

    private void normalizeSupplierSchemaOnce() {
        if (schemaNormalized || schemaNormalizing) {
            return;
        }
        schemaNormalizing = true;

        collection.get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    boolean hasChanges = false;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Map<String, Object> updates = new HashMap<>();

                        String tenNCC = safeString(doc, "tenNCC");
                        String tenNhaCungCap = safeString(doc, "tenNhaCungCap");
                        if (isBlank(tenNCC) && !isBlank(tenNhaCungCap)) {
                            updates.put("tenNCC", tenNhaCungCap);
                        }
                        if (!isBlank(tenNhaCungCap)) {
                            updates.put("tenNhaCungCap", FieldValue.delete());
                        }

                        String sdt = safeString(doc, "sdt");
                        String phone = safeString(doc, "phone");
                        if (isBlank(sdt) && !isBlank(phone)) {
                            updates.put("sdt", phone);
                        }
                        if (!isBlank(phone)) {
                            updates.put("phone", FieldValue.delete());
                        }

                        String diaChi = safeString(doc, "diaChi");
                        String address = safeString(doc, "address");
                        if (isBlank(diaChi) && !isBlank(address)) {
                            updates.put("diaChi", address);
                        }
                        if (!isBlank(address)) {
                            updates.put("address", FieldValue.delete());
                        }

                        if (!doc.contains("soLuong")) {
                            Object tongDon = doc.get("TongDon");
                            if (tongDon == null) tongDon = doc.get("tongDon");
                            if (tongDon != null) {
                                updates.put("soLuong", toLongValue(tongDon));
                            }
                        }
                        if (!doc.contains("tongMua")) {
                            Object giaTri = doc.get("GiaTri");
                            if (giaTri == null) giaTri = doc.get("giaTri");
                            if (giaTri != null) {
                                updates.put("tongMua", toLongValue(giaTri));
                            }
                        }

                        if (doc.contains("TongDon")) updates.put("TongDon", FieldValue.delete());
                        if (doc.contains("tongDon")) updates.put("tongDon", FieldValue.delete());
                        if (doc.contains("GiaTri")) updates.put("GiaTri", FieldValue.delete());
                        if (doc.contains("giaTri")) updates.put("giaTri", FieldValue.delete());
                        if (doc.contains("maID")) updates.put("maID", FieldValue.delete());

                        Object createdAt = doc.get("createdAt");
                        if (!doc.contains("ngayTao") && createdAt != null) {
                            updates.put("ngayTao", createdAt);
                        }
                        if (doc.contains("createdAt")) updates.put("createdAt", FieldValue.delete());

                        Object trangThaiRaw = doc.get("trangThai");
                        if (trangThaiRaw instanceof Number) {
                            updates.put("trangThai", ((Number) trangThaiRaw).intValue() != 0);
                        }

                        if (!doc.contains("ngayCapNhat")) {
                            updates.put("ngayCapNhat", FieldValue.serverTimestamp());
                        }

                        if (!updates.isEmpty()) {
                            batch.update(doc.getReference(), updates);
                            hasChanges = true;
                        }
                    }

                    if (hasChanges) {
                        batch.commit()
                                .addOnSuccessListener(unused -> {
                                    schemaNormalized = true;
                                    schemaNormalizing = false;
                                })
                                .addOnFailureListener(e -> schemaNormalizing = false);
                    } else {
                        schemaNormalized = true;
                        schemaNormalizing = false;
                    }
                })
                .addOnFailureListener(e -> schemaNormalizing = false);
    }

    private static String safeString(DocumentSnapshot doc, String key) {
        try {
            return doc.getString(key);
        } catch (RuntimeException ex) {
            Object raw = doc.get(key);
            return raw == null ? null : String.valueOf(raw);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static long toLongValue(Object raw) {
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(raw).replaceAll("[^0-9-]", ""));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public Query searchById(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllNhaCungCap();
        }

        String normalized = keyword.trim().toUpperCase();
        return collection
                .orderBy(FieldPath.documentId())
                .startAt(normalized)
                .endAt(normalized + "\uf8ff");
    }

    public Task<Void> updateNhaCungCap(NhaCungCap ncc) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("tenNCC", ncc.getTenNCC());
        updates.put("maSoThue", ncc.getMaSoThue() == null ? "" : ncc.getMaSoThue());
        updates.put("sdt", ncc.getSdt());
        updates.put("email", ncc.getEmail());
        updates.put("diaChi", ncc.getDiaChi());
        updates.put("trangThai", ncc.isTrangThai());
        updates.put("ngayCapNhat", FieldValue.serverTimestamp());

        // Xoa key cu de dung schema Firestore moi.
        updates.put("tenNhaCungCap", FieldValue.delete());
        updates.put("phone", FieldValue.delete());
        updates.put("address", FieldValue.delete());
        updates.put("maID", FieldValue.delete());
        updates.put("TongDon", FieldValue.delete());
        updates.put("tongDon", FieldValue.delete());
        updates.put("GiaTri", FieldValue.delete());
        updates.put("giaTri", FieldValue.delete());
        updates.put("createdAt", FieldValue.delete());

        return collection.document(ncc.getId()).update(updates);
    }

    public Task<Void> deleteNhaCungCap(String id) {
        return collection.document(id).delete();
    }

    // Lấy tổng số đơn nhập hàng của nhà cung cấp này
    public Task<QuerySnapshot> getTotalOrders(String nccId) {
        return db.collection("NhapHang")
                .whereEqualTo("maNCC", nccId)
                .get();
    }
}
