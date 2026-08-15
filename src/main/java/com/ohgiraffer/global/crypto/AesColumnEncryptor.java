package com.ohgiraffer.global.crypto;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesColumnEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static final String VERSION_PREFIX = "v1:";
    private final SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesColumnEncryptor(@Value("${encryption.aes.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16) {
            throw new IllegalStateException("AES-128 키는 정확히 16바이트여야 합니다. 현재: " + keyBytes.length + "바이트");
        }
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return VERSION_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ENCRYPTION_FAILED, e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        if (!encryptedText.startsWith(VERSION_PREFIX)) {
            throw new BusinessException(ErrorCode.DECRYPTION_FAILED);
        }

        try {
            String payload = encryptedText.substring(VERSION_PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(payload);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            byte[] cipherBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DECRYPTION_FAILED, e);
        }
    }
}