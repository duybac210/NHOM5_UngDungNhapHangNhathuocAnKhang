package com.nhom5.pharma.feature.sanpham;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public final class ProductSchemaSync {
    private static boolean attempted;

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
                batch.commit();
            }
        });
    }

    private static Map<String, Object> buildUpdates(DocumentSnapshot doc) {
        Map<String, Object> updates = new HashMap<>();

        String tenSP = doc.getString("tenSP");
        String tenSanPham = doc.getString("tenSanPham");
        if ((tenSP == null || tenSP.trim().isEmpty()) && tenSanPham != null && !tenSanPham.trim().isEmpty()) {
            updates.put("tenSP", tenSanPham.trim());
        }

        Number giavon = doc.getDouble("giavon");
        Number giaVon = doc.getDouble("giaVon");
        if (giavon == null && giaVon != null) {
            updates.put("giavon", giaVon.doubleValue());
        }

        String hangSX = doc.getString("hangSX");
        String hangSanXuat = doc.getString("hangSanXuat");
        if ((hangSX == null || hangSX.trim().isEmpty()) && hangSanXuat != null && !hangSanXuat.trim().isEmpty()) {
            updates.put("hangSX", hangSanXuat.trim());
        }

        String nuocSX = doc.getString("nuocSX");
        String nuocSanXuat = doc.getString("nuocSanXuat");
        if ((nuocSX == null || nuocSX.trim().isEmpty()) && nuocSanXuat != null && !nuocSanXuat.trim().isEmpty()) {
            updates.put("nuocSX", nuocSanXuat.trim());
        }

        String maID = doc.getString("maID");
        if (maID == null || maID.trim().isEmpty()) {
            updates.put("maID", doc.getId());
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

        return updates;
    }
}

