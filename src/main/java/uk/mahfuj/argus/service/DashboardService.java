package uk.mahfuj.argus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.mahfuj.argus.repos.ApiRequestLogRepository;
import uk.mahfuj.argus.service.dto.DashboardResponse;
import uk.mahfuj.argus.service.dto.DashboardResponse.DataPoint;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final Set<String> VALID_GRANULARITIES = Set.of("hour", "day", "week", "month");

    private final ApiRequestLogRepository repository;

    public DashboardService(final ApiRequestLogRepository repository) {
        this.repository = repository;
    }

    public DashboardResponse getDashboardData(final String granularity, final Instant from, final Instant to) {
        final String normalizedGranularity = granularity.toLowerCase();
        if (!VALID_GRANULARITIES.contains(normalizedGranularity)) {
            throw new IllegalArgumentException("Invalid granularity: " + granularity
                    + ". Must be one of: hour, day, week, month");
        }

        final Instant resolvedFrom = from != null ? from : defaultFrom(normalizedGranularity);
        final Instant resolvedTo = to != null ? to : Instant.now();

        log.debug("Dashboard query: granularity={}, from={}, to={}", normalizedGranularity, resolvedFrom, resolvedTo);

        final List<Object[]> rows = repository.getUsageAggregation(normalizedGranularity, resolvedFrom, resolvedTo);

        log.debug("Dashboard query returned {} rows", rows.size());

        final List<DataPoint> dataPoints = new ArrayList<>();
        for (final Object[] row : rows) {
            final Instant bucket = toInstant(row[0]);
            final long requestCount = ((Number) row[1]).longValue();
            final long inputTokens = ((Number) row[2]).longValue();
            final long outputTokens = ((Number) row[3]).longValue();
            log.debug("Bucket: {} -> count={}, in={}, out={}", bucket, requestCount, inputTokens, outputTokens);
            dataPoints.add(new DataPoint(bucket, requestCount, inputTokens, outputTokens));
        }

        return new DashboardResponse(normalizedGranularity, resolvedFrom, resolvedTo, dataPoints);
    }

    private Instant defaultFrom(final String granularity) {
        final Instant now = Instant.now();
        return switch (granularity) {
            case "hour" -> now.minus(24, ChronoUnit.HOURS);
            case "day" -> now.minus(30, ChronoUnit.DAYS);
            case "week" -> now.minus(12 * 7, ChronoUnit.DAYS);
            case "month" -> now.minus(365, ChronoUnit.DAYS);
            default -> now.minus(30, ChronoUnit.DAYS);
        };
    }

    private static Instant toInstant(final Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        throw new IllegalStateException("Unexpected type for timestamp column: " + value.getClass());
    }
}
