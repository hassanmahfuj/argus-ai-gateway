package uk.mahfuj.argus.service.dto;

import java.time.Instant;
import java.util.List;


public record DashboardResponse(
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
