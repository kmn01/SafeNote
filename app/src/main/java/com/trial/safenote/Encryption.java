package com.trial.safenote;

import android.os.Build;
import android.util.Base64;
import java.nio.charset.StandardCharsets;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;

import java.security.KeyStore;
import java.util.Arrays;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey generateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        String alias = "safenoteKeyAlias";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build();
        keyGenerator.init(keySpec);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void storeKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
        String alias = "safenoteKeyAlias";
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey secretKey = generateKey();
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(alias, entry, null);
    }
    private static SecretKey accessKey() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, IOException {
        String alias = "safenoteKeyAlias";
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.Entry entry = keyStore.getEntry(alias, null);
        if (entry == null || !(entry instanceof KeyStore.SecretKeyEntry)) {
            return null;
        }
        SecretKey secretKey = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        return secretKey;
    }
    public static String encryptText(String plaintext) throws Exception {
        SecretKey secretKey = accessKey();
        if (secretKey == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        System.out.println("1 ==== " + cipher);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        System.out.println("3 ==== " + cipher);
        byte[] ivBytes = cipher.getIV(); // get the default IV
        System.out.println("2 ==== " + ivBytes + " " + cipher.getBlockSize());
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        System.out.println("4 ==== " + encryptedBytes + " " + ivBytes);
        // Concatenate IV and ciphertext into a single byte array
        byte[] ivAndCipherBytes = new byte[ivBytes.length + encryptedBytes.length];
        System.out.println("5 ==== " + ivAndCipherBytes);
        System.arraycopy(ivBytes, 0, ivAndCipherBytes, 0, ivBytes.length);
        System.out.println("6 ==== " + ivAndCipherBytes);
        System.arraycopy(encryptedBytes, 0, ivAndCipherBytes, ivBytes.length, encryptedBytes.length);
        // Encode the byte array as a Base64 string and return it
        System.out.println("7 ==== " + ivAndCipherBytes);
        return Base64.encodeToString(ivAndCipherBytes, Base64.DEFAULT);
    }

    public static String decryptText(String encryptedtext) throws Exception {
        byte[] ivAndCipherBytes = Base64.decode(encryptedtext, Base64.DEFAULT);
        System.out.println("1b ==== " + ivAndCipherBytes);
        byte[] ivBytes = Arrays.copyOfRange(ivAndCipherBytes, 0, 16); // assuming the IV is 16 bytes long
        System.out.println("2b ==== " + ivBytes);
        byte[] cipherBytes = Arrays.copyOfRange(ivAndCipherBytes, 16, ivAndCipherBytes.length);
        System.out.println("3b ==== " + cipherBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        System.out.println("4b ==== " + cipher);
        cipher.init(Cipher.DECRYPT_MODE, accessKey(), new IvParameterSpec(ivBytes));
        System.out.println("5b ==== " + cipher);
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        System.out.println("6b ==== " + decryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
