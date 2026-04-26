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
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.nhom5.pharma.util.FirestoreValueParser;
import java.util.Date;
import com.google.firebase.Timestamp;
import java.util.Collections;

public class ProductViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("SanPham");
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private ListenerRegistration listener;

    public ProductViewModel() {
        startListening();
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void listenToProducts(String supplierId) {
        if (listener != null) {
            listener.remove();
            listener = null;
        }

        // Bỏ lệnh orderBy() để tránh lỗi FAILED_PRECONDITION (Requires an index)
        Query query = productsRef;
        final String normalizedSupplierId = supplierId == null ? null : supplierId.trim();

        listener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                errorMessage.setValue("Lỗi Firebase: " + error.getMessage());
                Log.e("ProductVM", "Firestore Listen failed: " + error.getMessage());
                return;
            }
            if (value != null) {
                List<Product> list = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    try {
                        String productSupplierId = firstNonEmpty(doc, "maNCC", "maNhaCungCap", "supplierId");
                        if (normalizedSupplierId != null && !normalizedSupplierId.isEmpty()) {
                            if (productSupplierId == null || !normalizedSupplierId.equals(productSupplierId.trim())) {
                                continue;
                            }
                        }

                        Product p = new Product();
                        p.setId(doc.getId());
                        p.setMaID(FirestoreValueParser.safeString(doc, "maID"));
                        Object tenSP = FirestoreValueParser.safeRaw(doc, "tenSP", "tenSanPham");
                        p.setTenSP(tenSP != null ? String.valueOf(tenSP) : "");
                        
                        Double giaVon = FirestoreValueParser.safeDouble(doc, "giavon");
                        if (giaVon == null) giaVon = FirestoreValueParser.safeDouble(doc, "giaVon");
                        p.setGiavon(giaVon != null ? giaVon : 0.0);
                        
                        Double giaBan = FirestoreValueParser.safeDouble(doc, "giaBan");
                        p.setGiaBan(giaBan != null ? giaBan : 0.0);
                        
                        p.setMaVach(FirestoreValueParser.safeString(doc, "maVach"));
                        p.setMoTa(FirestoreValueParser.safeString(doc, "moTa"));
                        
                        Object hangSX = FirestoreValueParser.safeRaw(doc, "hangSX", "hangSanXuat");
                        p.setHangSX(hangSX != null ? String.valueOf(hangSX) : "");
                        
                        Object nuocSX = FirestoreValueParser.safeRaw(doc, "nuocSX", "nuocSanXuat");
                        p.setNuocSX(nuocSX != null ? String.valueOf(nuocSX) : "");
                        p.setTrangThai(FirestoreValueParser.safeRaw(doc, "trangThai"));
                        
                        Object ngayTaoRaw = doc.get("ngayTao");
                        if (ngayTaoRaw instanceof Timestamp) {
                            p.setNgayTao(((Timestamp) ngayTaoRaw).toDate());
                        }

                        if (productSupplierId != null && !productSupplierId.trim().isEmpty()) {
                            p.setMaNCC(productSupplierId.trim());
                        }
                        list.add(p);
                    } catch (Exception e) {
                        Log.e("ProductVM", "Lỗi parse document: " + doc.getId() + " - " + e.getMessage());
                    }
                }
                
                // Sắp xếp danh sách cục bộ (Local Sorting) thay vì bắt Firebase sắp xếp
                Collections.sort(list, (p1, p2) -> {
                    if (p1.getId() == null) return 1;
                    if (p2.getId() == null) return -1;
                    return p2.getId().compareTo(p1.getId()); // Mới nhất lên đầu
                });
                
                products.setValue(list);
            }
        });
    }

    private void startListening() {
        listenToProducts(null);
    }

    public void saveProduct(Product product) {
        if (product == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("tenSP", product.getTenSP());
        data.put("giavon", product.getGiavon());
        data.put("giaBan", product.getGiaBan());
        data.put("maID", product.getMaID());
        data.put("maNCC", product.getMaNCC());
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

    private String firstNonEmpty(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String value = doc.getString(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listener != null) listener.remove();
    }
}
