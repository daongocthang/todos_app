package com.standalone.core.ext;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    public static class EncryptionError extends RuntimeException {
        public EncryptionError(Throwable e) {
            super(e);
        }
    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "SHA-256";
    private final Cipher reader;
    private final Cipher writer;

    public Encryption(String key) {
        try {
            this.reader = Cipher.getInstance(TRANSFORMATION);
            this.writer = Cipher.getInstance(TRANSFORMATION);

            initCiphers(key);

        } catch (GeneralSecurityException e) {
            throw new EncryptionError(e);
        }
    }

    private void initCiphers(String key) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        IvParameterSpec ivSpec = getIv();
        SecretKey secretKey = getSecretKey(key);

        writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
    }

    protected IvParameterSpec getIv() {
        byte[] iv = new byte[writer.getBlockSize()];
        System.arraycopy("d79bf29765cf8bc9998f7873f7d7bcc4".getBytes(), 0, iv, 0, writer.getBlockSize());
        return new IvParameterSpec(iv);
    }

    protected SecretKey getSecretKey(String key) throws NoSuchAlgorithmException {
        byte[] keyBytes = createSecretKey(key);
        return new SecretKeySpec(keyBytes, TRANSFORMATION);
    }

    private byte[] createSecretKey(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.reset();
        return md.digest(key.getBytes(StandardCharsets.UTF_8));
    }

    public String encrypt(String value) {
        byte[] securedValue;
        securedValue = convert(writer, value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(securedValue, Base64.NO_WRAP);
    }

    public String decrypt(String securedEncodeValue) {
        byte[] securedValue = Base64.decode(securedEncodeValue, Base64.NO_WRAP);
        byte[] value = convert(reader, securedValue);
        return new String(value, StandardCharsets.UTF_8);
    }

    private static byte[] convert(Cipher cipher, byte[] bs) throws EncryptionError {
        try {
            return cipher.doFinal(bs);
        } catch (Exception e) {
            throw new EncryptionError(e);
        }
    }

}
