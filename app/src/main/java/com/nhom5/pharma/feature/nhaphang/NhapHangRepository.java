package com.nhom5.pharma.feature.nhaphang;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class NhapHangRepository {
    private static NhapHangRepository instance;
    private final FirebaseFirestore db;

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
        return db.collection("NhapHang")
                .orderBy("NgayTao", Query.Direction.DESCENDING);
    }

    public Query searchByTenNhaCungCap(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllNhapHang();
        }
        
        return db.collection("NhapHang")
                .orderBy("tenNhaCungCap")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    public Task<DocumentSnapshot> getNhapHangById(String id) {
        return db.collection("NhapHang").document(id).get();
    }

    // Lấy thông tin nhà cung cấp từ Reference
    public Task<DocumentSnapshot> getSupplierByRef(DocumentReference ref) {
        return ref.get();
    }

    // Lấy danh sách lô hàng theo ID đơn nhập hàng
    public Task<QuerySnapshot> getLoHangByNhapHangId(String nhapHangId) {
        DocumentReference orderRef = db.collection("NhapHang").document(nhapHangId);
        return db.collection("LoHang")
                .whereEqualTo("NhapHangID", orderRef)
                .get();
    }

    // Lấy thông tin sản phẩm từ Reference (để lấy tên sản phẩm)
    public Task<DocumentSnapshot> getProductByRef(DocumentReference ref) {
        return ref.get();
    }

    // Xóa đơn nhập hàng và các lô hàng liên quan dùng Batch để đảm bảo tính toàn vẹn
    public Task<Void> deleteNhapHang(String nhapHangId) {
        WriteBatch batch = db.batch();
        DocumentReference nhapHangRef = db.collection("NhapHang").document(nhapHangId);
        
        // 1. Xóa đơn nhập hàng
        batch.delete(nhapHangRef);

        // 2. Phải tìm và xóa các lô hàng liên quan (Thực hiện query trước rồi xóa trong batch)
        return getLoHangByNhapHangId(nhapHangId).continueWithTask(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
            }
            return batch.commit();
        });
    }
}
