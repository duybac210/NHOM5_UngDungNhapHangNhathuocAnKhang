package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public final class ProductSchemaSync {
    private static boolean attempted;
    private static boolean supplierBackfillAttempted;

    private ProductSchemaSync() {
    }

    public static synchronized void syncOnce(FirebaseFirestore db) {
        if (attempted || db == null) {
            return;
        }
        attempted = true;

        db.collection("SanPham").get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> updates = buildUpdates(doc);
                if (!updates.isEmpty()) {
                    hasChanges = true;
                    batch.update(doc.getReference(), updates);
                }
            }

            if (hasChanges) {
                batch.commit().addOnSuccessListener(unused -> syncSupplierMappingOnce(db));
            } else {
                syncSupplierMappingOnce(db);
            }
        });
    }

    private static synchronized void syncSupplierMappingOnce(FirebaseFirestore db) {
        if (supplierBackfillAttempted || db == null) {
            return;
        }
        supplierBackfillAttempted = true;

        db.collection("LoHang").get().addOnSuccessListener(loHangSnapshot -> {
            Map<String, String> productToImport = new HashMap<>();
            Set<String> importIds = new HashSet<>();

            for (DocumentSnapshot loDoc : loHangSnapshot.getDocuments()) {
                String maSP = FirestoreValueParser.safeString(loDoc, "maSP");
                String maNhapHang = FirestoreValueParser.safeString(loDoc, "maNhapHang");
                if (maSP == null || maSP.trim().isEmpty() || maNhapHang == null || maNhapHang.trim().isEmpty()) {
                    continue;
                }
                productToImport.put(maSP.trim(), maNhapHang.trim());
                importIds.add(maNhapHang.trim());
            }

            if (productToImport.isEmpty() || importIds.isEmpty()) {
                return;
            }

            db.collection("NhapHang").get().addOnSuccessListener(nhapSnapshot -> {
                Map<String, String> importToSupplier = new HashMap<>();
                for (DocumentSnapshot nhDoc : nhapSnapshot.getDocuments()) {
                    if (!importIds.contains(nhDoc.getId())) {
                        continue;
                    }
                    String maNCC = FirestoreValueParser.safeString(nhDoc, "maNCC");
                    if (maNCC == null || maNCC.trim().isEmpty()) {
                        maNCC = FirestoreValueParser.safeString(nhDoc, "maNhaCungCap");
                    }
                    if (maNCC != null && !maNCC.trim().isEmpty()) {
                        importToSupplier.put(nhDoc.getId(), maNCC.trim());
                    }
                }

                if (importToSupplier.isEmpty()) {
                    return;
                }

                db.collection("SanPham").get().addOnSuccessListener(spSnapshot -> {
                    WriteBatch batch = db.batch();
                    boolean hasChanges = false;

                    for (DocumentSnapshot spDoc : spSnapshot.getDocuments()) {
                        String currentMaNCC = FirestoreValueParser.safeString(spDoc, "maNCC");
                        if (currentMaNCC != null && !currentMaNCC.trim().isEmpty()) {
                            continue;
                        }

                        String maSP = FirestoreValueParser.safeString(spDoc, "maID");
                        if (maSP == null || maSP.trim().isEmpty()) {
                            maSP = spDoc.getId();
                        }

                        String maNhapHang = productToImport.get(maSP);
                        if (maNhapHang == null) {
                            continue;
                        }
                        String maNCC = importToSupplier.get(maNhapHang);
                        if (maNCC == null || maNCC.trim().isEmpty()) {
                            continue;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("maNCC", maNCC);
                        updates.put("ngayCapNhat", FieldValue.serverTimestamp());
                        batch.update(spDoc.getReference(), updates);
                        hasChanges = true;
                    }

                    if (hasChanges) {
                        batch.commit();
                    }
                });
            });
        });
    }

    private static Map<String, Object> buildUpdates(DocumentSnapshot doc) {
        Map<String, Object> updates = new HashMap<>();

        String tenSP = FirestoreValueParser.safeString(doc, "tenSP");
        String tenSanPham = FirestoreValueParser.safeString(doc, "tenSanPham");
        if ((tenSP == null || tenSP.trim().isEmpty()) && tenSanPham != null && !tenSanPham.trim().isEmpty()) {
            updates.put("tenSP", tenSanPham.trim());
        }

        Double giavon = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "giavon"));
        Double giaVon = FirestoreValueParser.safeDouble(FirestoreValueParser.safeRaw(doc, "giaVon"));
        if (giavon == null && giaVon != null) {
            updates.put("giavon", giaVon);
        }

        String hangSX = FirestoreValueParser.safeString(doc, "hangSX");
        String hangSanXuat = FirestoreValueParser.safeString(doc, "hangSanXuat");
        if ((hangSX == null || hangSX.trim().isEmpty()) && hangSanXuat != null && !hangSanXuat.trim().isEmpty()) {
            updates.put("hangSX", hangSanXuat.trim());
        }

        String nuocSX = FirestoreValueParser.safeString(doc, "nuocSX");
        String nuocSanXuat = FirestoreValueParser.safeString(doc, "nuocSanXuat");
        if ((nuocSX == null || nuocSX.trim().isEmpty()) && nuocSanXuat != null && !nuocSanXuat.trim().isEmpty()) {
            updates.put("nuocSX", nuocSanXuat.trim());
        }

        String maID = FirestoreValueParser.safeString(doc, "maID");
        if (maID == null || maID.trim().isEmpty()) {
            updates.put("maID", doc.getId());
        }

        String maNCC = FirestoreValueParser.safeString(doc, "maNCC");
        String maNhaCungCap = FirestoreValueParser.safeString(doc, "maNhaCungCap");
        if ((maNCC == null || maNCC.trim().isEmpty()) && maNhaCungCap != null && !maNhaCungCap.trim().isEmpty()) {
            updates.put("maNCC", maNhaCungCap.trim());
        }

        if (doc.contains("tenSanPham")) {
            updates.put("tenSanPham", FieldValue.delete());
        }
        if (doc.contains("giaVon")) {
            updates.put("giaVon", FieldValue.delete());
        }
        if (doc.contains("hangSanXuat")) {
            updates.put("hangSanXuat", FieldValue.delete());
        }
        if (doc.contains("nuocSanXuat")) {
            updates.put("nuocSanXuat", FieldValue.delete());
        }
        if (doc.contains("maNhaCungCap")) {
            updates.put("maNhaCungCap", FieldValue.delete());
        }

        return updates;
    }
}

