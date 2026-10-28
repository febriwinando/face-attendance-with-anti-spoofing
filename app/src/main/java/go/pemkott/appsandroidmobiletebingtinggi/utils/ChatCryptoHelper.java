package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class ChatCryptoHelper {

    private static final String TAG = "ChatCryptoHelper";
    private static final String SECRET_PASS = "AbsensiPemkoTebingTinggi2026SecKey!";
    private static final String PREFIX = "ENC::";

    private static SecretKeySpec secretKeySpec;

    private static synchronized SecretKeySpec getSecretKey() {
        if (secretKeySpec == null) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] key = sha.digest(SECRET_PASS.getBytes(StandardCharsets.UTF_8));
                secretKeySpec = new SecretKeySpec(key, "AES");
            } catch (Exception e) {
                Log.e(TAG, "Error generating secret key", e);
            }
        }
        return secretKeySpec;
    }

    /**
     * Enkripsi Pesan Teks menggunakan AES-256
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return "";
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Encrypt error: " + e.getMessage());
            return plainText;
        }
    }

    /**
     * Dekripsi Pesan Teks Terenkripsi
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return "";
        if (!encryptedText.startsWith(PREFIX)) {
            return encryptedText; // Unencrypted plain text message
        }

        try {
            String cleanBase64 = encryptedText.substring(PREFIX.length());
            byte[] combined = Base64.decode(cleanBase64, Base64.NO_WRAP);

            if (combined.length < 17) return encryptedText;

            byte[] iv = new byte[16];
            byte[] cipherText = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, cipherText, 0, cipherText.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivSpec);

            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            Log.e(TAG, "Decrypt error: " + e.getMessage());
            return encryptedText;
        }
    }
}
