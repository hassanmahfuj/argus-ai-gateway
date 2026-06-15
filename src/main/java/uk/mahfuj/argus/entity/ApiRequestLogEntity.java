package uk.mahfuj.argus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "api_request_log", indexes = {
        @Index(name = "idx_apirequestlog_timestamp", columnList = "timestamp"),
        @Index(name = "idx_apirequestlog_provider_timestamp", columnList = "provider, timestamp")
})
public class ApiRequestLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "model", length = 100)
    private String model;

    /** The model/alias string exactly as the client sent it (before resolution). */
    @Column(name = "requested_model", length = 200)
    private String requestedModel;

    @Column(name = "input_tokens")
    private Integer inputTokens = 0;

    @Column(name = "output_tokens")
    private Integer outputTokens = 0;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "is_streaming")
    private Boolean isStreaming = false;
}
