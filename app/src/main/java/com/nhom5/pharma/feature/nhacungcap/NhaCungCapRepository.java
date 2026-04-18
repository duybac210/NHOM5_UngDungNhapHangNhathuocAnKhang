package com.nhom5.pharma.feature.nhacungcap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.tasks.Tasks;

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

    // Hiển thị toàn bộ để kiểm tra lỗi dữ liệu ( NCC001, 004, 005...)
    public Query getAllNhaCungCap() {
        return collection.orderBy("tenNCC", Query.Direction.ASCENDING);
    }

    public Task<Void> updateNhaCungCap(NhaCungCap ncc) {
        if (ncc.getId() == null) return Tasks.forException(new Exception("ID null"));
        return collection.document(ncc.getId()).set(ncc);
    }

    public Task<Void> deactivateNhaCungCap(String id) {
        return collection.document(id).update("trangThai", false);
    }
}
