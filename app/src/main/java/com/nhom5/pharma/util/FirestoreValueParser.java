package com.nhom5.pharma.util;

import com.google.firebase.firestore.DocumentSnapshot;

public final class FirestoreValueParser {
    private FirestoreValueParser() {
    }

    public static String safeString(DocumentSnapshot doc, String field) {
        if (doc == null || field == null) {
            return null;
        }
        try {
            return doc.getString(field);
        } catch (RuntimeException ignored) {
            Object raw = safeRaw(doc, field);
            return raw == null ? null : String.valueOf(raw);
        }
    }

    public static Double safeDouble(DocumentSnapshot doc, String field) {
        if (doc == null || field == null) {
            return null;
        }
        return toDouble(safeRaw(doc, field));
    }

    public static Double safeDouble(Object raw) {
        return toDouble(raw);
    }

    public static Integer safeInt(DocumentSnapshot doc, String field) {
        Double value = safeDouble(doc, field);
        return value == null ? null : value.intValue();
    }

    public static Object safeRaw(DocumentSnapshot doc, String... fields) {
        if (doc == null || fields == null) {
            return null;
        }
        for (String field : fields) {
            try {
                Object value = doc.get(field);
                if (value != null) {
                    return value;
                }
            } catch (RuntimeException ignored) {
            }
        }
        return null;
    }

    private static Double toDouble(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }
        if (raw instanceof Boolean) {
            return (Boolean) raw ? 1d : 0d;
        }
        if (raw instanceof String) {
            String value = ((String) raw).trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}

