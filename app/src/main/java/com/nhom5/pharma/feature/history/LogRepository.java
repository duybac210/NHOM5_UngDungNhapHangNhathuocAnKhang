package com.nhom5.pharma.feature.history;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogRepository {
    private static LogRepository instance;
    private final FirebaseFirestore db;

    private LogRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized LogRepository getInstance() {
        if (instance == null) {
            instance = new LogRepository();
        }
        return instance;
    }

    public void log(String action, String targetType, String targetId, String details) {
        String userEmail = "Anonymous";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        ActivityLog activityLog = new ActivityLog(userEmail, action, targetType, targetId, details);
        db.collection("LichSuHoatDong").add(activityLog.toMap());
    }

    // Helper methods for specific actions
    public void logCreate(String targetType, String targetId, String details) {
        log("CREATE", targetType, targetId, details);
    }

    public void logUpdate(String targetType, String targetId, String details) {
        log("UPDATE", targetType, targetId, details);
    }

    public void logDelete(String targetType, String targetId, String details) {
        log("DELETE", targetType, targetId, details);
    }
}
