package com.scholarsmanuscript.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaEncryptor {

    @Value("${encryption.private-key:}")
    private String privateKeyBase64;

    @Value("${encryption.public-key:}")
    private String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            if (privateKeyBase64 != null && !privateKeyBase64.isEmpty()) {
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(keySpec);
            }

            if (publicKeyBase64 != null && !publicKeyBase64.isEmpty()) {
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = keyFactory.generatePublic(keySpec);
            }

            // Generate new key pair if not configured
            if (this.privateKey == null || this.publicKey == null) {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair keyPair = keyGen.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
                System.out.println("=== RSA Key Pair Generated (configure in production) ===");
                System.out.println("Public Key (Base64): " + Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
                System.out.println("Private Key (Base64): " + Base64.getEncoder().encodeToString(this.privateKey.getEncoded()));
                System.out.println("===========================================================");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA keys", e);
        }
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
