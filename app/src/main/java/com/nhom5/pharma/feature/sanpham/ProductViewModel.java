package com.nhom5.pharma.feature.sanpham;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

public class ProductViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("SanPham");
    
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
<<<<<<< HEAD
    private boolean legacySeedCleanupAttempted = false;
=======
    private ListenerRegistration listener;

    public ProductViewModel() {
        startListening();
    }
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)

    public LiveData<List<Product>> getProducts() {
        return products;
    }

<<<<<<< HEAD
    public void listenToProducts() {
        cleanupLegacySeedOnce();
        ProductSchemaSync.syncOnce(db);

        // Sắp xếp theo ID (Mã hàng) tăng dần
        productsRef.orderBy(com.google.firebase.firestore.FieldPath.documentId(), Query.Direction.ASCENDING)
=======
    private void startListening() {
        if (listener != null) return;
        
        listener = productsRef.orderBy("ngayTao", Query.Direction.DESCENDING)
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ProductVM", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            if (Product.isLegacyBadProductId(doc.getId())) {
                                continue;
                            }
                            Product p = doc.toObject(Product.class);
                            if (p != null) {
                                p.setId(doc.getId());
                                list.add(p);
                            }
                        }
<<<<<<< HEAD
                        Collections.sort(productList, new Comparator<Product>() {
                            @Override
                            public int compare(Product o1, Product o2) {
                                long n1 = Product.extractProductNumber(o1.getId());
                                long n2 = Product.extractProductNumber(o2.getId());
                                if (n1 != n2) {
                                    if (n1 < 0) return 1;
                                    if (n2 < 0) return -1;
                                    return Long.compare(n1, n2);
                                }
                                String id1 = o1.getId() == null ? "" : o1.getId();
                                String id2 = o2.getId() == null ? "" : o2.getId();
                                return id1.compareTo(id2);
                            }
                        });
                        products.setValue(productList);
=======
                        products.setValue(list);
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
                    }
                });
    }

    private void cleanupLegacySeedOnce() {
        if (legacySeedCleanupAttempted) {
            return;
        }
        legacySeedCleanupAttempted = true;

        productsRef.document("SP00001")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        productsRef.document("SP00001").delete();
                    }
                });
    }


    public void saveProduct(Product product) {
        if (product == null) {
            return;
        }

        Map<String, Object> data = buildProductData(product, false);

        if (product.getId() != null && !product.getId().trim().isEmpty()) {
            data.put("maID", product.getId().trim());
            productsRef.document(product.getId().trim()).update(data);
            return;
        }

        db.collection("SanPham")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String nextId = Product.buildNextProductId(snapshot.getDocuments());
                    product.setId(nextId);

                    Map<String, Object> createData = buildProductData(product, true);
                    createData.put("maID", nextId);

                    productsRef.document(nextId).set(createData);
                })
                .addOnFailureListener(e -> Log.e("ProductVM", "Create product failed: " + e.getMessage(), e));
    }

    private Map<String, Object> buildProductData(Product product, boolean isCreate) {
        Map<String, Object> data = new HashMap<>();
        data.put("maID", product.getId());
        data.put("tenSP", product.getTenSP());
        data.put("giavon", product.getGiavon());
        data.put("maVach", product.getMaVach() != null ? product.getMaVach() : "");
        data.put("moTa", product.getMoTa() != null ? product.getMoTa() : "");
        data.put("hangSX", product.getHangSX());
        data.put("nuocSX", product.getNuocSX());
        // Gọi hàm layTrangThaiBoolean() thay vì isTrangThai() để tránh xung đột
        data.put("trangThai", product.layTrangThaiBoolean());
        data.put("ngayCapNhat", FieldValue.serverTimestamp());
<<<<<<< HEAD

        if (isCreate) {
=======
        
        if (product.getId() != null && !product.getId().isEmpty() && !product.getId().equals("Tự động tạo")) {
            productsRef.document(product.getId()).set(data, SetOptions.merge());
        } else {
>>>>>>> c80b2bd (Lưu code giao diện san pham mượt và fix crash)
            data.put("ngayTao", FieldValue.serverTimestamp());
            data.put("createdAt", FieldValue.serverTimestamp());
        } else {
            // Xoa cac key alias cu de schema trong Firestore dong nhat.
            data.put("tenSanPham", FieldValue.delete());
            data.put("giaVon", FieldValue.delete());
            data.put("hangSanXuat", FieldValue.delete());
            data.put("nuocSanXuat", FieldValue.delete());
        }

        return data;
    }

    public void deleteProduct(String id) {
        if (id != null) productsRef.document(id).delete();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listener != null) listener.remove();
    }
}
