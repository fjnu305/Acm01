package org.fjnu305.acm01.module.sync.vault;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.sync.config.SyncProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** AES-GCM vault for OJ cookies and tokens.
 */
@Slf4j
@Component
public class CredentialVault {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CredentialVault(SyncProperties syncProperties) {
        byte[] keyBytes = Base64.getDecoder().decode(syncProperties.getCredentialKeyBase64());
        if (keyBytes.length != 32) {
            throw new IllegalStateException("sync.credential-key-base64 must decode to 32 bytes (AES-256)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public EncryptedCredential encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return new EncryptedCredential(null, null);
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return new EncryptedCredential(
                    Base64.getEncoder().encodeToString(encrypted),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt credential", e);
        }
    }

    public String decrypt(String encryptedBase64, String ivBase64) {
        if (encryptedBase64 == null || ivBase64 == null) {
            return null;
        }
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to decrypt credential", e);
            return null;
        }
    }

    public record EncryptedCredential(String encrypted, String iv) {}
}
