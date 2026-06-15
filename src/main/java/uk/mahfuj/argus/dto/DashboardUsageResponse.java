package uk.mahfuj.argus.dto;

import java.time.Instant;
import java.util.List;


public record DashboardUsageResponse(
        String granularity,
        Instant from,
        Instant to,
        List<DataPoint> dataPoints
) {

    public record DataPoint(
            Instant bucket,
            long requestCount,
            long inputTokens,
            long outputTokens
    ) {}
}
