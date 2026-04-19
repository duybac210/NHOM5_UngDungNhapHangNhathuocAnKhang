package com.nhom5.pharma.feature.nhacungcap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class NhaCungCapRepository {
    private static NhaCungCapRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference collection;
    private boolean normalizationRunning = false;

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

    // Lọc NCC chưa xóa và sắp xếp theo Mã 1-9
    public Query getAllNhaCungCap() {
        return collection.whereEqualTo("trangThai", true)
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING);
    }

    public Query searchById(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllNhaCungCap();
        String normalized = keyword.trim().toUpperCase();
        return collection.whereEqualTo("trangThai", true)
                .orderBy(FieldPath.documentId())
                .startAt(normalized)
                .endAt(normalized + "\uf8ff");
    }

    public Task<Void> updateNhaCungCap(NhaCungCap ncc) {
        return collection.document(ncc.getId()).set(ncc);
    }

    public Task<Void> deactivateNhaCungCap(String id) {
        return collection.document(id).update("trangThai", false);
    }

    /**
     * Đồng bộ hóa schema dữ liệu cho Nhà Cung Cấp.
     * Chuyển đổi các trường cũ (ten, tenNhaCungCap) sang trường chuẩn (tenNCC).
     */
    public void ensureCanonicalSchema() {
        if (normalizationRunning) return;
        normalizationRunning = true;

        collection.get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> updates = new HashMap<>();

                // Chuẩn hóa tên NCC
                String currentName = doc.getString("tenNCC");
                if (currentName == null || currentName.trim().isEmpty()) {
                    String legacyName = doc.getString("tenNhaCungCap");
                    if (legacyName == null || legacyName.trim().isEmpty()) {
                        legacyName = doc.getString("ten");
                    }
                    if (legacyName != null && !legacyName.trim().isEmpty()) {
                        updates.put("tenNCC", legacyName.trim());
                    }
                }

                // Xóa các trường cũ
                if (doc.contains("tenNhaCungCap")) updates.put("tenNhaCungCap", FieldValue.delete());
                if (doc.contains("ten")) updates.put("ten", FieldValue.delete());

                if (!updates.isEmpty()) {
                    batch.update(doc.getReference(), updates);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                batch.commit().addOnCompleteListener(task -> normalizationRunning = false);
            } else {
                normalizationRunning = false;
            }
        }).addOnFailureListener(e -> normalizationRunning = false);
    }
}
