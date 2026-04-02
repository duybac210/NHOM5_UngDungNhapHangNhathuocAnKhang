package com.nhom5.pharma.feature.nhaphang;

import com.google.android.gms.tasks.Task;
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
                .orderBy("ngayTao", Query.Direction.DESCENDING);
    }

    // Thêm lại phương thức tìm kiếm để tránh lỗi biên dịch
    public Query searchByTenNhaCungCap(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllNhapHang();
        }
        
        // Lưu ý: Vì trong NhapHang không có field tenNhaCungCap, 
        // tạm thời tìm theo maNCC để code không bị lỗi.
        return db.collection("NhapHang")
                .orderBy("maNCC")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    public Task<DocumentSnapshot> getNhapHangById(String id) {
        return db.collection("NhapHang").document(id).get();
    }

    public Task<DocumentSnapshot> getSupplierById(String maNCC) {
        return db.collection("NhaCungCap").document(maNCC).get();
    }

    public Task<QuerySnapshot> getLoHangByNhapHangId(String nhapHangId) {
        return db.collection("LoHang")
                .whereEqualTo("maNhapHang", nhapHangId)
                .get();
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
}
