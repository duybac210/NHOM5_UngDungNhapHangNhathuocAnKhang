package com.nhom5.pharma.util;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class SecurityUtils {
    private static final String AES = "AES";
    // Lưu ý: Trong thực tế nên dùng Keystore để bảo mật key này. 
    // Ở đây dùng một chuỗi 16 ký tự cố định cho mục đích minh họa.
    private static final String SECRET_KEY = "AnKhangPharma@12"; 

    public static String encrypt(String password) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return password; // Trả về text gốc nếu lỗi (không khuyến khích)
        }
    }

    public static String decrypt(String encryptedPassword) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedPassword;
        }
    }
}
