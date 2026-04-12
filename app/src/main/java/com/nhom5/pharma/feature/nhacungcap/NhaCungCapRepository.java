package com.nhom5.pharma.feature.nhacungcap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class NhaCungCapRepository {
    private static NhaCungCapRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference collection;

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
        return collection.orderBy(FieldPath.documentId(), Query.Direction.ASCENDING);
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
        return collection.document(ncc.getId()).set(ncc);
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
