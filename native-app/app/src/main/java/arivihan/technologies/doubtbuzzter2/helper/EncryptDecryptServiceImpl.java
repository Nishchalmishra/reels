package arivihan.technologies.doubtbuzzter2.helper;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import arivihan.technologies.doubtbuzzter2.BuildConfig;

public class EncryptDecryptServiceImpl {

    private static final String TAG = "EncryptDecrypt";
    private static final String ALGORITHM = "AES";

    private static String mySecretKey = BuildConfig.Secreat_Key;

    // Cached key — rebuilt only when mySecretKey changes
    private static SecretKeySpec cachedSecretKey = null;
    private static String cachedKeySource = null;

    public static synchronized void setMySecretKey(String newKey) {
        if (newKey != null && !newKey.equals(mySecretKey)) {
            mySecretKey = newKey;
            cachedSecretKey = null;
            cachedKeySource = null;
        }
    }

    /**
     * Returns a cached SecretKeySpec, rebuilding it only when mySecretKey changes.
     * Synchronized to be safe on Volley's thread pool.
     */
    private static synchronized SecretKeySpec getSecretKey() {
        if (cachedSecretKey != null && mySecretKey.equals(cachedKeySource)) {
            return cachedSecretKey;
        }
        try {
            byte[] key = mySecretKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            cachedSecretKey = new SecretKeySpec(key, ALGORITHM);
            cachedKeySource = mySecretKey;
            return cachedSecretKey;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-1 not available", e);
            return null;
        }
    }

    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null) return null;
        try {
            SecretKeySpec keySpec = getSecretKey();
            if (keySpec == null) return null;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error", e);
            return null;
        }
    }

    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null) return null;
        try {
            SecretKeySpec keySpec = getSecretKey();
            if (keySpec == null) return null;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = android.util.Base64.decode(strToDecrypt, android.util.Base64.DEFAULT);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Return null — returning the encrypted string as-is would give callers garbage data
            Log.e(TAG, "Decryption error", e);
            return null;
        }
    }
}
