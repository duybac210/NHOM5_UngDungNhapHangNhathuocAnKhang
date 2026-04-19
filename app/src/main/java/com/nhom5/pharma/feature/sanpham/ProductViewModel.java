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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProductViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("SanPham");
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private ListenerRegistration listener;

    public ProductViewModel() {
        startListening();
    }

    public LiveData<List<Product>> getProducts() { return products; }

    private void startListening() {
        if (listener != null) return;
        
        // LUÔN LẮNG NGHE REAL-TIME: Firebase thay đổi -> App nhảy dữ liệu ngay
        listener = productsRef.orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ProductVM", "Firestore Listen failed: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Product p = doc.toObject(Product.class);
                                if (p == null) p = new Product();
                                p.setId(doc.getId()); // Đồng bộ ID từ Firebase
                                list.add(p);
                            } catch (Exception e) {
                                Log.e("ProductVM", "Lỗi parse document: " + doc.getId());
                            }
                        }
                        products.setValue(list);
                    }
                });
    }

    public void saveProduct(Product product) {
        if (product == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("tenSP", product.getTenSP());
        data.put("giavon", product.getGiavon());
        data.put("giaBan", product.getGiaBan());
        data.put("maID", product.getMaID());
        data.put("maVach", product.getMaVach());
        data.put("moTa", product.getMoTa());
        data.put("hangSX", product.getHangSX());
        data.put("nuocSX", product.getNuocSX());
        data.put("trangThai", product.layTrangThaiBoolean());
        data.put("ngayCapNhat", FieldValue.serverTimestamp());
        
        if (product.getId() != null && !product.getId().isEmpty()) {
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
