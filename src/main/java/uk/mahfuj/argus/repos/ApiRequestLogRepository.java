package uk.mahfuj.argus.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.mahfuj.argus.domain.ApiRequestLog;

import java.time.Instant;
import java.util.List;


public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {

    @Query(value = """
            SELECT date_trunc(:granularity, timestamp) AS bucket,
                   COUNT(*) AS request_count,
                   COALESCE(SUM(input_tokens), 0) AS input_tokens,
                   COALESCE(SUM(output_tokens), 0) AS output_tokens
            FROM api_request_log
            WHERE timestamp >= :from AND timestamp <= :to
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> getUsageAggregation(
            @Param("granularity") String granularity,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
