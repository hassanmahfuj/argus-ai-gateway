package uk.mahfuj.argus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


/**
 * An upstream AI provider (host + credentials), e.g. {@code zai}, {@code xai},
 * {@code google}, {@code deepseek}. Each provider declares which endpoint shape(s)
 * it supports by populating the corresponding base URL column. The {@code apiKey}
 * is stored AES-encrypted (see {@code CryptoService}); Argus decrypts at resolve
 * time and injects it as a Bearer token.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "provider", uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_name", columnNames = "name")
})
public class ProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** AES ciphertext — never plaintext. */
    @Column(name = "api_key", nullable = false, columnDefinition = "text")
    private String apiKey;

    /** Non-null + enabled ⇒ supports the OpenAI endpoint shape. */
    @Column(name = "openai_base_url", length = 500)
    private String openaiBaseUrl;

    /** Non-null + enabled ⇒ supports the Anthropic endpoint shape. */
    @Column(name = "anthropic_base_url", length = 500)
    private String anthropicBaseUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        final Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
