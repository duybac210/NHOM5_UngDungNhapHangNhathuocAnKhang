package com.nhom5.pharma.feature.history;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityLog {
    private String userEmail;
    private String action; // CREATE, UPDATE, DELETE
    private String targetType; // NHACUNGCAP, SANPHAM, NHAPHANG
    private String targetId;
    private String details;
    
    @ServerTimestamp
    private Date timestamp;

    public ActivityLog() {}

    public ActivityLog(String userEmail, String action, String targetType, String targetId, String details) {
        this.userEmail = userEmail;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userEmail", userEmail);
        map.put("action", action);
        map.put("targetType", targetType);
        map.put("targetId", targetId);
        map.put("details", details);
        map.put("timestamp", FieldValue.serverTimestamp());
        return map;
    }

    // Getters and Setters
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
