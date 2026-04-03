package com.nhom5.pharma.feature.sanpham;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("SanPham");
    
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void listenToProducts() {
        // Sắp xếp theo ID (Mã hàng) tăng dần
        productsRef.orderBy(com.google.firebase.firestore.FieldPath.documentId(), Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ProductVM", "Listen failed: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Product> productList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Product p = doc.toObject(Product.class);
                            if (p != null) {
                                p.setId(doc.getId());
                                productList.add(p);
                            }
                        }
                        products.setValue(productList);
                    }
                });
    }

    public void saveProduct(Product product) {
        Map<String, Object> data = new HashMap<>();
        data.put("tenSP", product.getTenSP());
        data.put("giavon", product.getGiavon());
        data.put("maVach", product.getMaVach());
        data.put("moTa", product.getMoTa());
        data.put("hangSX", product.getHangSX());
        data.put("nuocSX", product.getNuocSX());
        data.put("trangThai", product.isTrangThai());
        data.put("ngayCapNhat", FieldValue.serverTimestamp());
        
        if (product.getId() != null && !product.getId().isEmpty()) {
            productsRef.document(product.getId()).update(data);
        } else {
            data.put("ngayTao", FieldValue.serverTimestamp());
            productsRef.add(data);
        }
    }

    public void deleteProduct(String id) {
        productsRef.document(id).delete();
    }
}
