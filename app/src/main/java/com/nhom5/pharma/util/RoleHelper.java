package com.nhom5.pharma.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoleHelper {

    public interface RoleCheckCallback {
        void onRoleChecked(boolean isManager);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải là quản lý hay không.
     * @param callback Kết quả trả về: true nếu là quản lý, false nếu là nhân viên hoặc lỗi.
     */
    public static void checkIsManager(RoleCheckCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onRoleChecked(false);
            return;
        }

        FirebaseFirestore.getInstance().collection("TaiKhoan")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String role = doc.getString("chucVu");
                        if (role == null) role = doc.getString("role");

                        boolean isManager = "manager".equalsIgnoreCase(role) || "quản lý".equalsIgnoreCase(role);
                        callback.onRoleChecked(isManager);
                    } else {
                        callback.onRoleChecked(false);
                    }
                })
                .addOnFailureListener(e -> callback.onRoleChecked(false));
    }
}
