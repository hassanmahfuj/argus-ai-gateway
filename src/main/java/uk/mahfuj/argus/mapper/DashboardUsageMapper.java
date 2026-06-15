package uk.mahfuj.argus.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.dto.DashboardUsageResponse;
import uk.mahfuj.argus.dto.DashboardUsageResponse.DataPoint;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Converts native aggregation-query rows into dashboard DTO data points,
 * keeping the row-unpacking concern out of the service layer.
 */
@Component
public class DashboardUsageMapper {

    private static final Logger log = LoggerFactory.getLogger(DashboardUsageMapper.class);

    public List<DataPoint> toDataPoints(final List<Object[]> rows) {
        final List<DataPoint> dataPoints = new ArrayList<>();
        for (final Object[] row : rows) {
            final Instant bucket = toInstant(row[0]);
            final long requestCount = ((Number) row[1]).longValue();
            final long inputTokens = ((Number) row[2]).longValue();
            final long outputTokens = ((Number) row[3]).longValue();
            log.debug("Bucket: {} -> count={}, in={}, out={}", bucket, requestCount, inputTokens, outputTokens);
            dataPoints.add(new DataPoint(bucket, requestCount, inputTokens, outputTokens));
        }
        return dataPoints;
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
