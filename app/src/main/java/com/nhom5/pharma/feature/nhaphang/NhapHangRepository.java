package com.nhom5.pharma.feature.nhaphang;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.SetOptions;
import com.nhom5.pharma.feature.lohang.LoHangFilterType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

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

    // --- TRUY VẤN ĐƠN NHẬP HÀNG ---
    public Query getAllNhapHang() {
        return db.collection("NhapHang").orderBy("maID", Query.Direction.DESCENDING);
    }

    public Query searchByMaDon(String query) {
        if (query == null || query.trim().isEmpty()) return getAllNhapHang();
        String q = query.trim().toUpperCase();
        return db.collection("NhapHang")
                .whereGreaterThanOrEqualTo("maID", q)
                .whereLessThanOrEqualTo("maID", q + "\uf8ff")
                .orderBy("maID", Query.Direction.DESCENDING);
    }

    public Task<DocumentSnapshot> getNhapHangById(String orderId) {
        return db.collection("NhapHang").document(orderId).get();
    }

    // --- TRUY VẤN LÔ HÀNG ---
    public Query getAllLoHang() {
        return db.collection("LoHang").orderBy("soLo", Query.Direction.DESCENDING);
    }

    public Task<DocumentSnapshot> getLoHangById(String soLo) {
        return db.collection("LoHang").document(soLo).get();
    }

    public Task<QuerySnapshot> getLoHangByNhapHangId(String nhapHangId) {
        return db.collection("LoHang").whereEqualTo("maNhapHang", nhapHangId).get();
    }

    public Query searchLoHang(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllLoHang();
        String q = keyword.trim();
        return db.collection("LoHang")
                .whereGreaterThanOrEqualTo("soLo", q)
                .whereLessThanOrEqualTo("soLo", q + "\uf8ff")
                .orderBy("soLo", Query.Direction.DESCENDING);
    }

    public Query getLoHangByFilter(int filterType) {
        Query query = db.collection("LoHang");
        Date now = new Date();
        switch (filterType) {
            case LoHangFilterType.EXPIRING_SOON:
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 3);
                return query.whereGreaterThan("hanSuDung", new Timestamp(now))
                            .whereLessThanOrEqualTo("hanSuDung", new Timestamp(cal.getTime()))
                            .orderBy("hanSuDung", Query.Direction.ASCENDING);
            case LoHangFilterType.EXPIRED:
                return query.whereLessThan("hanSuDung", new Timestamp(now))
                            .orderBy("hanSuDung", Query.Direction.ASCENDING);
            case LoHangFilterType.LOW_STOCK:
                return query.whereLessThanOrEqualTo("soLuong", 5)
                            .orderBy("soLuong", Query.Direction.ASCENDING);
            default: return getAllLoHang();
        }
    }

    // --- TRUY VẤN KHÁC (Product, Supplier, User) ---
    public Task<DocumentSnapshot> getProductById(String productId) {
        return db.collection("SanPham").document(productId).get();
    }

    public Task<DocumentSnapshot> getSupplierById(String supplierId) {
        return db.collection("NhaCungCap").document(supplierId).get();
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection("TaiKhoan").document(userId).get();
    }

    // --- THAO TÁC DỮ LIỆU ---
    public Task<Void> deleteNhapHang(String orderId) {
        WriteBatch batch = db.batch();
        batch.delete(db.collection("NhapHang").document(orderId));
        return getLoHangByNhapHangId(orderId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
            }
            return batch.commit();
        });
    }

    public Task<Void> upsertLoHang(String soLo, LoHang lo) {
        return db.collection("LoHang").document(soLo).set(lo.toFirestoreMap(), SetOptions.merge());
    }

    public Task<Void> replaceLoHangByNhapHangId(String nhapHangId, List<LoHang> newLoHangs) {
        WriteBatch batch = db.batch();
        return getLoHangByNhapHangId(nhapHangId).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
            }
            if (newLoHangs != null) {
                for (LoHang lo : newLoHangs) {
                    lo.setMaNhapHang(nhapHangId);
                    if (lo.getSoLo() != null && !lo.getSoLo().trim().isEmpty()) {
                        batch.set(db.collection("LoHang").document(lo.getSoLo().trim()), lo.toFirestoreMap(), SetOptions.merge());
                    }
                }
            }
            return batch.commit();
        });
    }

    public Task<Void> updateLoHangNgaySanXuat(String soLo, Date date) {
        return db.collection("LoHang").document(soLo).update("ngaySanXuat", new Timestamp(date));
    }

    public Task<Void> updateLoHangHanSuDung(String soLo, Date date) {
        return db.collection("LoHang").document(soLo).update("hanSuDung", new Timestamp(date));
    }

    public Task<Void> updateLoHangDonGiaNhap(String soLo, double donGiaNhap) {
        Map<String, Object> update = new HashMap<>();
        update.put("donGiaNhap", donGiaNhap);
        return db.collection("LoHang").document(soLo).set(update, SetOptions.merge());
    }

    // --- COUNTER TỐI ƯU ---
    public Task<List<String>> generateNextIds(String counterName, String prefix, int count) {
        if (count <= 0) {
            return com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>());
        }

        DocumentReference counterRef = db.collection("counters").document(counterName);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef);
            long current = 0;
            if (snapshot.exists() && snapshot.contains("current")) {
                current = snapshot.getLong("current");
            }

            long start = current + 1;
            long end = current + count;

            transaction.update(counterRef, "current", end);

            List<String> ids = new ArrayList<>();
            for (long i = start; i <= end; i++) {
                ids.add(String.format(Locale.getDefault(), prefix + "%04d", i));
            }
            return ids;
        });
    }

    public Task<String> generateNextNhapHangId() {
        return generateNextIds("NhapHang", "NH", 1).continueWith(task -> task.getResult().get(0));
    }

    public Task<String> generateNextSoLo() {
        return generateNextIds("LoHang", "LH", 1).continueWith(task -> task.getResult().get(0));
    }
}
