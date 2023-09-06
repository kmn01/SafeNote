package com.trial.safenote;

import android.content.Context;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class Encryption {
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public static SecretKey generateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
//        String alias = "safenoteKeyAlias";
//        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                .setUserAuthenticationRequired(false)
//                .build();
//        keyGenerator.init(keySpec);
//        SecretKey secretKey = keyGenerator.generateKey();
//        return secretKey;
//    }
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public static void storeKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
//        String alias = "safenoteKeyAlias";
//        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//        SecretKey secretKey = generateKey();
//        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
//        keyStore.setEntry(alias, entry, null);
//    }
//    private static SecretKey accessKey(Context context) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, IOException {
//        String alias = "safenoteKeyAlias";
//        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//        KeyStore.Entry entry = keyStore.getEntry(alias, null);
//        if (entry == null || !(entry instanceof KeyStore.SecretKeyEntry)) {
//            return null;
//        }
//        SecretKey secretKey = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
//    }

    public static SecretKey generateKey(Context context, String alias) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeySpecException {
        byte[] salt = alias.getBytes(StandardCharsets.UTF_8);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        int iterations = 10000;
        int keyLength = 256;
        KeySpec keySpec = new PBEKeySpec(alias.toCharArray(), salt, iterations, keyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }
    public static String encryptText(String plaintext, Context context, String alias) throws Exception {
        SecretKey secretKey = generateKey(context, alias);
        if (secretKey == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] ivBytes = cipher.getIV(); // get the default IV
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] ivAndCipherBytes = new byte[ivBytes.length + encryptedBytes.length];
        System.arraycopy(ivBytes, 0, ivAndCipherBytes, 0, ivBytes.length);
        System.arraycopy(encryptedBytes, 0, ivAndCipherBytes, ivBytes.length, encryptedBytes.length);
        return Base64.encodeToString(ivAndCipherBytes, Base64.DEFAULT);
    }

    public static String decryptText(String encryptedtext, Context context, String alias) throws Exception {
        byte[] ivAndCipherBytes = Base64.decode(encryptedtext, Base64.DEFAULT);
        byte[] ivBytes = Arrays.copyOfRange(ivAndCipherBytes, 0, 16); // assuming the IV is 16 bytes long
        byte[] cipherBytes = Arrays.copyOfRange(ivAndCipherBytes, 16, ivAndCipherBytes.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, generateKey(context, alias), new IvParameterSpec(ivBytes));
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
