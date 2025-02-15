package com.standalone.core.adapter.utils;

import android.util.Base64;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncUtil {
    private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
    public static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verify(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static String encrypt(String plainText, SecretKey secret) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();

        byte[] encryptedBytesWithIV = new byte[encryptedBytes.length + iv.length];
        System.arraycopy(iv, 0, encryptedBytesWithIV, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedBytesWithIV, iv.length, encryptedBytes.length);

        return Base64.encodeToString(encryptedBytesWithIV, Base64.DEFAULT);
    }


    public static String decrypt(String cipherText, SecretKey secret) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] encryptedBytesWithIV = Base64.decode(cipherText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = Arrays.copyOfRange(encryptedBytesWithIV, 0, cipher.getBlockSize());
        byte[] encryptedBytes = Arrays.copyOfRange(encryptedBytesWithIV, cipher.getBlockSize(), encryptedBytesWithIV.length);
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }

    public static SecretKey getSecretKey(String key) throws NoSuchAlgorithmException {
        byte[] keyBytes = createKeyBytes(key);
        return new SecretKeySpec(keyBytes, TRANSFORMATION);
    }

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(TRANSFORMATION);
        keygen.init(256);
        return keygen.generateKey();
    }

    private static byte[] createKeyBytes(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION);
        md.reset();
        return md.digest(key.getBytes(StandardCharsets.UTF_8));
    }
}
