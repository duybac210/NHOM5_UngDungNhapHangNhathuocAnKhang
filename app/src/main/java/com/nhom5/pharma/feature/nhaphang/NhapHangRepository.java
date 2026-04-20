package com.nhom5.pharma.feature.nhaphang;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom5.pharma.feature.lohang.LoHangFilterType;
import com.nhom5.pharma.util.FirestoreValueParser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.regex.Pattern;

public class NhapHangRepository {
    private static NhapHangRepository instance;
    private final FirebaseFirestore db;
    private boolean statusNormalizationDone;
    private boolean statusNormalizationRunning;
    private boolean fieldNormalizationDone;
    private boolean fieldNormalizationRunning;
    private boolean maIdNormalizationDone;
    private boolean maIdNormalizationRunning;
    private boolean docIdMigrationDone;
    private boolean docIdMigrationRunning;
    private boolean importIdResequenceDone;
    private boolean importIdResequenceRunning;
    private static final Pattern IMPORT_CODE_PATTERN = Pattern.compile("^NH(\\d+)$");
    private static final String DEFAULT_MA_NGUOI_NHAP = "USER003";

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
        normalizeStatusSchemaOnce();
        normalizeFieldSchemaOnce();
        normalizeMaIdSchemaOnce();
        forceNormalizeKnownPendingDocs();
        return db.collection("NhapHang")
                .orderBy("maID", Query.Direction.DESCENDING);
    }

    private void normalizeMaIdSchemaOnce() {
        if (maIdNormalizationDone || maIdNormalizationRunning) {
            return;
        }
        maIdNormalizationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            long maxNumber = 0;
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String code = safeGetString(doc, "maID");
                if (code == null || code.trim().isEmpty()) {
                    code = doc.getId();
                }
                long number = extractNhapHangNumber(code);
                if (number > maxNumber) {
                    maxNumber = number;
                }
            }

            List<DocumentSnapshot> invalidDocs = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String maID = safeGetString(doc, "maID");
                if (!isValidNhapHangCode(maID)) {
                    invalidDocs.add(doc);
                }
            }

            if (invalidDocs.isEmpty()) {
                maIdNormalizationDone = true;
                maIdNormalizationRunning = false;
                migrateLegacyDocumentIdsOnce();
                return;
            }

            WriteBatch batch = db.batch();
            long next = maxNumber + 1;
            for (DocumentSnapshot doc : invalidDocs) {
                batch.update(doc.getReference(), "maID", String.format(Locale.getDefault(), "NH%04d", next++));
                batch.update(doc.getReference(), "ngayCapNhat", FieldValue.serverTimestamp());
            }

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        maIdNormalizationDone = true;
                        maIdNormalizationRunning = false;
                        migrateLegacyDocumentIdsOnce();
                    })
                    .addOnFailureListener(e -> maIdNormalizationRunning = false);
        }).addOnFailureListener(e -> maIdNormalizationRunning = false);
    }

    private void resequenceImportIdsFromOneOnce() {
        if (importIdResequenceDone || importIdResequenceRunning) {
            return;
        }
        importIdResequenceRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
            if (docs.isEmpty()) {
                importIdResequenceDone = true;
                importIdResequenceRunning = false;
                return;
            }

            Collections.sort(docs, Comparator.comparingLong(doc -> {
                String code = safeGetString(doc, "maID");
                if (code == null || code.trim().isEmpty()) {
                    code = doc.getId();
                }
                long n = extractNhapHangNumber(code);
                return n < 0 ? Long.MAX_VALUE : n;
            }));

            LinkedList<MigrationItem> tempMigrations = new LinkedList<>();
            LinkedList<MigrationItem> finalMigrations = new LinkedList<>();
            long next = 1;

            for (DocumentSnapshot doc : docs) {
                String targetId = String.format(Locale.getDefault(), "NH%04d", next++);
                String oldId = doc.getId();
                String currentMaId = safeGetString(doc, "maID");

                boolean needsMove = !oldId.equals(targetId);
                boolean needsMaIdFix = currentMaId == null || !targetId.equals(currentMaId.trim().toUpperCase(Locale.ROOT));
                if (!needsMove && !needsMaIdFix) {
                    continue;
                }

                Map<String, Object> data = doc.getData();
                if (data == null) {
                    data = new HashMap<>();
                } else {
                    data = new HashMap<>(data);
                }
                data.put("maID", targetId);
                data.put("ngayCapNhat", FieldValue.serverTimestamp());

                if (needsMove) {
                    String tempId = "TMP_" + UUID.randomUUID().toString().replace("-", "");
                    tempMigrations.add(new MigrationItem(oldId, tempId, data));
                    finalMigrations.add(new MigrationItem(tempId, targetId, data));
                } else {
                    doc.getReference().update(data);
                }
            }

            if (tempMigrations.isEmpty() && finalMigrations.isEmpty()) {
                importIdResequenceDone = true;
                importIdResequenceRunning = false;
                return;
            }

            processResequenceQueue(tempMigrations, () ->
                    processResequenceQueue(finalMigrations, () -> {
                        importIdResequenceDone = true;
                        importIdResequenceRunning = false;
                    })
            );
        }).addOnFailureListener(e -> importIdResequenceRunning = false);
    }

    private void processResequenceQueue(LinkedList<MigrationItem> migrations, Runnable onDone) {
        if (migrations.isEmpty()) {
            onDone.run();
            return;
        }

        MigrationItem item = migrations.removeFirst();
        DocumentReference oldRef = db.collection("NhapHang").document(item.oldId);
        DocumentReference newRef = db.collection("NhapHang").document(item.targetId);

        db.collection("LoHang")
                .whereEqualTo("maNhapHang", item.oldId)
                .get()
                .addOnSuccessListener(loSnapshot -> {
                    WriteBatch batch = db.batch();
                    batch.set(newRef, item.data, SetOptions.merge());

                    for (DocumentSnapshot loDoc : loSnapshot.getDocuments()) {
                        batch.update(loDoc.getReference(), "maNhapHang", item.targetId);
                        batch.update(loDoc.getReference(), "ngayCapNhat", FieldValue.serverTimestamp());
                    }

                    batch.delete(oldRef);
                    batch.commit()
                            .addOnSuccessListener(unused -> processResequenceQueue(migrations, onDone))
                            .addOnFailureListener(e -> importIdResequenceRunning = false);
                })
                .addOnFailureListener(e -> importIdResequenceRunning = false);
    }

    private void migrateLegacyDocumentIdsOnce() {
        if (docIdMigrationDone || docIdMigrationRunning) {
            return;
        }
        docIdMigrationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            LinkedList<MigrationItem> migrations = new LinkedList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String oldId = doc.getId();
                String maID = safeGetString(doc, "maID");
                if (!isValidNhapHangCode(maID)) {
                    continue;
                }
                String targetId = maID.trim().toUpperCase(Locale.ROOT);
                if (oldId.equals(targetId)) {
                    continue;
                }

                Map<String, Object> data = doc.getData();
                if (data == null) {
                    data = new HashMap<>();
                } else {
                    data = new HashMap<>(data);
                }
                data.put("maID", targetId);
                data.put("ngayCapNhat", FieldValue.serverTimestamp());
                migrations.add(new MigrationItem(oldId, targetId, data));
            }

            if (migrations.isEmpty()) {
                docIdMigrationDone = true;
                docIdMigrationRunning = false;
                resequenceImportIdsFromOneOnce();
                return;
            }

            processNextMigration(migrations);
        }).addOnFailureListener(e -> docIdMigrationRunning = false);
    }

    private void processNextMigration(LinkedList<MigrationItem> migrations) {
        if (migrations.isEmpty()) {
            docIdMigrationDone = true;
            docIdMigrationRunning = false;
            resequenceImportIdsFromOneOnce();
            return;
        }

        MigrationItem item = migrations.removeFirst();
        DocumentReference oldRef = db.collection("NhapHang").document(item.oldId);
        DocumentReference newRef = db.collection("NhapHang").document(item.targetId);

        newRef.get().addOnSuccessListener(newDoc -> {
            db.collection("LoHang")
                    .whereEqualTo("maNhapHang", item.oldId)
                    .get()
                    .addOnSuccessListener(loSnapshot -> {
                        WriteBatch batch = db.batch();
                        if (!newDoc.exists()) {
                            batch.set(newRef, item.data, SetOptions.merge());
                        }

                        for (DocumentSnapshot loDoc : loSnapshot.getDocuments()) {
                            batch.update(loDoc.getReference(), "maNhapHang", item.targetId);
                            batch.update(loDoc.getReference(), "ngayCapNhat", FieldValue.serverTimestamp());
                        }

                        batch.delete(oldRef);
                        batch.commit()
                                .addOnSuccessListener(unused -> processNextMigration(migrations))
                                .addOnFailureListener(e -> docIdMigrationRunning = false);
                    })
                    .addOnFailureListener(e -> docIdMigrationRunning = false);
        }).addOnFailureListener(e -> docIdMigrationRunning = false);
    }

    private static final class MigrationItem {
        final String oldId;
        final String targetId;
        final Map<String, Object> data;

        MigrationItem(String oldId, String targetId, Map<String, Object> data) {
            this.oldId = oldId;
            this.targetId = targetId;
            this.data = data;
        }
    }

    private boolean isValidNhapHangCode(String code) {
        return code != null && IMPORT_CODE_PATTERN.matcher(code.trim().toUpperCase(Locale.ROOT)).matches();
    }

    private long extractNhapHangNumber(String rawId) {
        if (rawId == null) {
            return -1;
        }
        String normalized = rawId.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("NH") || normalized.length() <= 2) {
            return -1;
        }
        try {
            return Long.parseLong(normalized.substring(2));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void normalizeStatusSchemaOnce() {
        if (statusNormalizationDone || statusNormalizationRunning) {
            return;
        }
        statusNormalizationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Object statusRaw = safeGetField(doc, "trangThai");
                String currentText = safeGetString(doc, "trangThaiText");

                int normalizedStatus = normalizeTrangThaiValue(statusRaw, currentText);
                String normalizedText = normalizedStatus == 1 ? "Đã nhập kho" : "Đã hủy";

                Integer currentStatus = FirestoreValueParser.safeInt(doc, "trangThai");

                boolean statusNeedsUpdate = currentStatus == null || currentStatus != normalizedStatus;
                boolean textNeedsUpdate = currentText == null || !normalizedText.equals(currentText);

                if (statusNeedsUpdate || textNeedsUpdate) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("trangThai", normalizedStatus);
                    updates.put("trangThaiText", normalizedText);
                    batch.update(doc.getReference(), updates);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                batch.commit()
                        .addOnSuccessListener(unused -> {
                            statusNormalizationDone = true;
                            statusNormalizationRunning = false;
                        })
                        .addOnFailureListener(e -> statusNormalizationRunning = false);
            } else {
                statusNormalizationDone = true;
                statusNormalizationRunning = false;
            }
        }).addOnFailureListener(e -> statusNormalizationRunning = false);
    }

    private void normalizeFieldSchemaOnce() {
        if (fieldNormalizationDone || fieldNormalizationRunning) {
            return;
        }
        fieldNormalizationRunning = true;

        db.collection("NhapHang").get().addOnSuccessListener(snapshot -> {
            WriteBatch batch = db.batch();
            boolean hasChanges = false;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> updates = buildLegacyFieldUpdates(doc);
                if (!updates.isEmpty()) {
                    batch.update(doc.getReference(), updates);
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                batch.commit()
                        .addOnSuccessListener(unused -> {
                            fieldNormalizationDone = true;
                            fieldNormalizationRunning = false;
                        })
                        .addOnFailureListener(e -> fieldNormalizationRunning = false);
            } else {
                fieldNormalizationDone = true;
                fieldNormalizationRunning = false;
            }
        }).addOnFailureListener(e -> fieldNormalizationRunning = false);
    }

    private Map<String, Object> buildLegacyFieldUpdates(DocumentSnapshot doc) {
        Map<String, Object> updates = new HashMap<>();

        String maNCC = safeGetString(doc, "maNCC");
        String maNhaCungCap = safeGetString(doc, "maNhaCungCap");
        if ((maNCC == null || maNCC.trim().isEmpty()) && maNhaCungCap != null && !maNhaCungCap.trim().isEmpty()) {
            updates.put("maNCC", maNhaCungCap.trim());
        }

        String maNguoiNhap = safeGetString(doc, "maNguoiNhap");
        if (maNguoiNhap == null || maNguoiNhap.trim().isEmpty()) {
            updates.put("maNguoiNhap", DEFAULT_MA_NGUOI_NHAP);
        }

        if (!doc.contains("ghiChu")) {
            updates.put("ghiChu", "");
        }

        if (!doc.contains("ngayTao")) {
            Object ngayNhapRaw = safeGetField(doc, "ngayNhap");
            Object createdAtRaw = safeGetField(doc, "createdAt");
            if (ngayNhapRaw != null) {
                updates.put("ngayTao", ngayNhapRaw);
            } else if (createdAtRaw != null) {
                updates.put("ngayTao", createdAtRaw);
            } else {
                updates.put("ngayTao", Timestamp.now());
            }
        }

        updates.put("ngayCapNhat", FieldValue.serverTimestamp());

        // Xoa cac key lech schema cu.
        if (doc.contains("maNhaCungCap")) updates.put("maNhaCungCap", FieldValue.delete());
        if (doc.contains("tenNhaCungCap")) updates.put("tenNhaCungCap", FieldValue.delete());
        if (doc.contains("hinhThucThanhToan")) updates.put("hinhThucThanhToan", FieldValue.delete());
        if (doc.contains("createdAt")) updates.put("createdAt", FieldValue.delete());

        return updates;
    }

    private void forceNormalizeKnownPendingDocs() {
        forceNormalizeDoc("NH0021");
        forceNormalizeDoc("NH0022");
        forceNormalizeDoc("NH0023");
        forceNormalizeDoc("NH004");
        forceNormalizeDoc("NH0004");
    }

    private void forceNormalizeDoc(String id) {
        db.collection("NhapHang")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        return;
                    }

                    Object statusRaw = safeGetField(doc, "trangThai");
                    String currentText = safeGetString(doc, "trangThaiText");

                    int normalizedStatus = normalizeTrangThaiValue(statusRaw, currentText);
                    String normalizedText = normalizedStatus == 1 ? "Đã nhập kho" : "Đã hủy";

                    Map<String, Object> updates = buildLegacyFieldUpdates(doc);

                    Integer rawStatus = FirestoreValueParser.safeInt(doc, "trangThai");
                    if (rawStatus == null || rawStatus != normalizedStatus) {
                        updates.put("trangThai", normalizedStatus);
                    }
                    if (currentText == null || !normalizedText.equals(currentText)) {
                        updates.put("trangThaiText", normalizedText);
                    }

                    if (!updates.isEmpty()) {
                        doc.getReference().update(updates);
                    }
                });
    }

    private Object safeGetField(DocumentSnapshot doc, String field) {
        try {
            return doc.get(field);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String safeGetString(DocumentSnapshot doc, String field) {
        try {
            return doc.getString(field);
        } catch (RuntimeException ignored) {
            Object raw = safeGetField(doc, field);
            return raw == null ? null : String.valueOf(raw);
        }
    }

    private int normalizeTrangThaiValue(Object trangThaiRaw, String trangThaiText) {
        if (trangThaiRaw instanceof Number) {
            int value = ((Number) trangThaiRaw).intValue();
            return value == 1 ? 1 : 0;
        }
        if (trangThaiRaw instanceof Boolean) {
            return (Boolean) trangThaiRaw ? 1 : 0;
        }
        if (trangThaiRaw instanceof String) {
            String value = ((String) trangThaiRaw).trim().toLowerCase(Locale.ROOT);
            if ("1".equals(value) || "true".equals(value) || "da nhap kho".equals(value) || "đã nhập kho".equals(value)) {
                return 1;
            }
            return 0;
        }

        if (trangThaiText != null) {
            String text = trangThaiText.trim().toLowerCase(Locale.ROOT);
            if ("da nhap kho".equals(text) || "đã nhập kho".equals(text) || "đã nhập hàng".equals(text)) {
                return 1;
            }
        }
        return 0;
    }

    public Query getAllLoHang() {
        return db.collection("LoHang")
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING);
    }

    // Tìm kiếm theo mã đơn chuẩn NHxxxx
    public Query searchByMaDon(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllNhapHang();
        }

        String keyword = searchText.trim().toUpperCase(Locale.ROOT);

        return db.collection("NhapHang")
                .orderBy("maID")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff");
    }

    public Query searchLoHang(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllLoHang();
        }

        String keyword = searchText.trim();
        return db.collection("LoHang")
                .orderBy(FieldPath.documentId())
                .startAt(keyword)
                .endAt(keyword + "\uf8ff");
    }

    public Query getLoHangByFilter(int filterType) {
        switch (filterType) {
            case LoHangFilterType.EXPIRING_SOON:
            case LoHangFilterType.EXPIRED:
                // 2 filter nay can tinh (hanSuDung - ngayNhap), xu ly o Adapter.
                return getAllLoHang();
            case LoHangFilterType.LOW_STOCK:
                return getAllLoHang();
            case LoHangFilterType.EXPIRY_ASC:
                return db.collection("LoHang")
                        .orderBy("hanSuDung", Query.Direction.ASCENDING);
            case LoHangFilterType.EXPIRY_DESC:
                return db.collection("LoHang")
                        .orderBy("hanSuDung", Query.Direction.DESCENDING);
            case LoHangFilterType.ALL:
            default:
                return getAllLoHang();
        }
    }

    public Task<DocumentSnapshot> getNhapHangById(String id) {
        return db.collection("NhapHang").document(id).get();
    }

    public Task<DocumentSnapshot> getSupplierById(String maNCC) {
        return db.collection("NhaCungCap").document(maNCC).get();
    }

    // Lấy thông tin tài khoản người nhập từ mã người nhập (String ID)
    public Task<DocumentSnapshot> getUserById(String maNguoiNhap) {
        return db.collection("TaiKhoan").document(maNguoiNhap).get();
    }

    public Task<QuerySnapshot> getLoHangByNhapHangId(String nhapHangId) {
        return db.collection("LoHang")
                .whereEqualTo("maNhapHang", nhapHangId)
                .get();
    }

    public Task<DocumentSnapshot> getLoHangById(String soLo) {
        return db.collection("LoHang").document(soLo).get();
    }

    public Task<DocumentSnapshot> getProductById(String maSP) {
        return db.collection("SanPham").document(maSP).get();
    }

    public Task<Void> upsertLoHang(String soLo, LoHang loHang) {
        if (soLo == null || soLo.trim().isEmpty()) {
            throw new IllegalArgumentException("soLo khong duoc rong");
        }
        if (loHang == null) {
            throw new IllegalArgumentException("loHang khong duoc null");
        }

        loHang.setSoLo(soLo.trim());
        return db.collection("LoHang")
                .document(soLo.trim())
                .set(loHang.toFirestoreMap(), SetOptions.merge());
    }

    public Task<Void> updateLoHangNgaySanXuat(String soLo, Date ngaySanXuat) {
        if (soLo == null || soLo.trim().isEmpty()) {
            throw new IllegalArgumentException("soLo khong duoc rong");
        }
        if (ngaySanXuat == null) {
            throw new IllegalArgumentException("ngaySanXuat khong duoc null");
        }

        Map<String, Object> update = new HashMap<>();
        update.put("ngaySanXuat", ngaySanXuat);
        return db.collection("LoHang")
                .document(soLo.trim())
                .set(update, SetOptions.merge());
    }

    public Task<Void> updateLoHangDonGiaNhap(String soLo, double donGiaNhap) {
        if (soLo == null || soLo.trim().isEmpty()) {
            throw new IllegalArgumentException("soLo khong duoc rong");
        }

        Map<String, Object> update = new HashMap<>();
        update.put("donGiaNhap", donGiaNhap);
        return db.collection("LoHang")
                .document(soLo.trim())
                .set(update, SetOptions.merge());
    }


    public Task<Void> replaceLoHangByNhapHangId(String nhapHangId, List<LoHang> loHangs) {
        if (nhapHangId == null || nhapHangId.trim().isEmpty()) {
            throw new IllegalArgumentException("nhapHangId khong duoc rong");
        }

        WriteBatch batch = db.batch();
        return getLoHangByNhapHangId(nhapHangId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException() != null
                        ? task.getException()
                        : new IllegalStateException("Khong the tai danh sach lo hang hien tai");
            }

            for (DocumentSnapshot doc : task.getResult()) {
                batch.delete(doc.getReference());
            }

            if (loHangs != null) {
                for (LoHang loHang : loHangs) {
                    if (loHang == null) {
                        continue;
                    }
                    loHang.setMaNhapHang(nhapHangId);
                    String soLo = loHang.getSoLo();
                    if (soLo == null || soLo.trim().isEmpty()) {
                        continue;
                    }
                    batch.set(db.collection("LoHang").document(soLo.trim()), loHang.toFirestoreMap(), SetOptions.merge());
                }
            }

            return batch.commit();
        });
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

    public void ensureLegacyFieldSchema() {
        normalizeFieldSchemaOnce();
    }

    public void ensureCanonicalImportIdSchema() {
        normalizeMaIdSchemaOnce();
    }
}
