package com.trial.safenote;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import androidx.annotation.RequiresApi;

public class KeyManagement {

    static String alias = "SafeNoteAuthKeysAlias";

    public static SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Key size, you can adjust this as needed
        return keyGenerator.generateKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void generateAndStoreAsymmetricKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();
        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
    }

    public static void encryptAndStoreSymmetricKey(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        SecretKey symmetricKey = generateSymmetricKey();

        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/"
                + KeyProperties.BLOCK_MODE_ECB + "/"
                + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedSymmetricKey = cipher.doFinal(symmetricKey.getEncoded());

        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("encrypted_symmetric_key", Base64.encodeToString(encryptedSymmetricKey, Base64.DEFAULT));
        editor.apply();
    }

    public static SecretKey decryptSymmetricKey(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/"
                + KeyProperties.BLOCK_MODE_ECB + "/"
                + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String encryptedSymmetricKeyString = sharedPreferences.getString("encrypted_symmetric_key", "");
        byte[] encryptedSymmetricKey = Base64.decode(encryptedSymmetricKeyString, Base64.DEFAULT);
        byte[] decryptedSymmetricKey = cipher.doFinal(encryptedSymmetricKey);
        return new SecretKeySpec(decryptedSymmetricKey, "AES");
    }

    public static String encryptPassword(String password, Context context) throws Exception {
        SecretKey symmetricKey = decryptSymmetricKey(context);
        byte[] ivBytes = new byte[16]; // AES block size is 16 bytes
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);

        byte[] encryptedPasswordBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        byte[] combinedBytes = new byte[ivBytes.length + encryptedPasswordBytes.length];
        System.arraycopy(ivBytes, 0, combinedBytes, 0, ivBytes.length);
        System.arraycopy(encryptedPasswordBytes, 0, combinedBytes, ivBytes.length, encryptedPasswordBytes.length);

        return Base64.encodeToString(combinedBytes, Base64.DEFAULT);
    }

    public static String decryptPassword(String encryptedPassword, Context context) throws Exception {
        SecretKey symmetricKey = decryptSymmetricKey(context);
        byte[] combinedBytes = Base64.decode(encryptedPassword, android.util.Base64.DEFAULT);
        byte[] ivBytes = new byte[16]; // AES block size is 16 bytes

        System.arraycopy(combinedBytes, 0, ivBytes, 0, ivBytes.length);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
        byte[] decryptedPasswordBytes = cipher.doFinal(combinedBytes, ivBytes.length, combinedBytes.length - ivBytes.length);

        return new String(decryptedPasswordBytes, StandardCharsets.UTF_8);
    }

}
