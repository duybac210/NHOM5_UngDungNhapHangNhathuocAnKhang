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

public class ProductViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("SanPham");
    
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private ListenerRegistration listener;

    public ProductViewModel() {
        startListening();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    private void startListening() {
        if (listener != null) return;
        
        listener = productsRef.orderBy("ngayTao", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ProductVM", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Product p = doc.toObject(Product.class);
                            if (p != null) {
                                p.setId(doc.getId());
                                list.add(p);
                            }
                        }
                        products.setValue(list);
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
        // Gọi hàm layTrangThaiBoolean() thay vì isTrangThai() để tránh xung đột
        data.put("trangThai", product.layTrangThaiBoolean());
        data.put("ngayCapNhat", FieldValue.serverTimestamp());
        
        if (product.getId() != null && !product.getId().isEmpty() && !product.getId().equals("Tự động tạo")) {
            productsRef.document(product.getId()).set(data, SetOptions.merge());
        } else {
            data.put("ngayTao", FieldValue.serverTimestamp());
            productsRef.add(data);
        }
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
