package uk.mahfuj.argus.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * AES/GCM encryption of provider API keys at rest. The symmetric key is derived from
 * the {@code argus.encryption.key} property (sourced from the {@code ARGUS_ENC_KEY}
 * env var); the key itself never touches the database. Ciphertext is stored as
 * Base64 of {@code iv || ciphertext+tag}. The app fails fast at startup if no key
 * is configured — without it providers can neither be written nor read.
 */
@Service
public class CryptoService {

    private static final Logger log = LoggerFactory.getLogger(CryptoService.class);

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final String configuredKey;
    private SecretKey key;
    private SecureRandom random;

    public CryptoService(@Value("${argus.encryption.key:}") final String configuredKey) {
        this.configuredKey = configuredKey;
    }

    @PostConstruct
    void init() {
        Assert.hasText(configuredKey,
                "argus.encryption.key (env ARGUS_ENC_KEY) must be set to a non-empty secret");
        final byte[] keyBytes = new byte[16];
        final byte[] raw = configuredKey.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = raw[i % raw.length];
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
        this.random = new SecureRandom();
        log.info("CryptoService initialized (AES/GCM)");
    }

    public String encrypt(final String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            final byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            final byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            final byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to encrypt provider API key", e);
        }
    }

    public String decrypt(final String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return null;
        }
        try {
            final byte[] in = Base64.getDecoder().decode(ciphertext);
            final byte[] iv = new byte[IV_BYTES];
            final byte[] cipherText = new byte[in.length - IV_BYTES];
            System.arraycopy(in, 0, iv, 0, IV_BYTES);
            System.arraycopy(in, IV_BYTES, cipherText, 0, cipherText.length);
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to decrypt provider API key", e);
        }
    }
}
