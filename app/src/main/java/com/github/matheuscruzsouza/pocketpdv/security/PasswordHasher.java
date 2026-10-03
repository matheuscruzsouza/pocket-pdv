package com.github.matheuscruzsouza.pocketpdv.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitário de hashing e verificação de senhas baseado no padrão Android/JCA.
 * Utiliza PBKDF2WithHmacSHA256 com salt criptográfico aleatório e comparação em tempo constante.
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2";
    private static final int DEFAULT_ITERATIONS = 10000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("A senha não pode ser nula");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, HASH_BITS);

        return PREFIX + "$" + DEFAULT_ITERATIONS + "$" + bytesToHex(salt) + "$" + bytesToHex(hash);
    }

    public static boolean checkPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }

        storedHash = storedHash.trim();

        // Se estiver no formato PBKDF2
        if (storedHash.startsWith(PREFIX + "$")) {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = hexToBytes(parts[2]);
                byte[] expectedHash = hexToBytes(parts[3]);

                byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);
                return MessageDigest.isEqual(expectedHash, actualHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Suporte a migração: comparação em tempo constante com texto puro legado
        byte[] expectedBytes = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = password.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    public static boolean needsRehash(String storedHash) {
        if (storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }
        storedHash = storedHash.trim();
        if (!storedHash.startsWith(PREFIX + "$")) {
            return true; // Formato legado de texto puro
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return true;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            return iterations < DEFAULT_ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Falha ao calcular PBKDF2: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
