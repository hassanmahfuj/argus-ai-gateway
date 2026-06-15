package uk.mahfuj.argus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


/**
 * A registered model under a {@link ProviderEntity}. The client-facing id is
 * {@code provider.name + "/" + name} (e.g. {@code zai/glm-5.1}). The
 * {@code upstreamModelName} is what Argus rewrites the request body's {@code model}
 * field to before forwarding (defaults to {@code name} when null).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "model", uniqueConstraints = {
        @UniqueConstraint(name = "uk_model_provider_name", columnNames = {"provider_id", "name"})
})
public class ModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderEntity provider;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Forwarded model name; if null the resolver uses {@link #name}. */
    @Column(name = "upstream_model_name", length = 100)
    private String upstreamModelName;

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

    /** Model name to send upstream, falling back to the catalog name. */
    public String effectiveUpstreamModel() {
        return upstreamModelName != null && !upstreamModelName.isBlank() ? upstreamModelName : name;
    }
}
