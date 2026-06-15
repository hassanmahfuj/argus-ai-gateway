package uk.mahfuj.argus.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.mahfuj.argus.dto.DashboardUsageResponse;
import uk.mahfuj.argus.exception.BadRequestException;
import uk.mahfuj.argus.mapper.DashboardUsageMapper;
import uk.mahfuj.argus.repository.ApiRequestLogRepository;


@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final Set<String> VALID_GRANULARITIES = Set.of("hour", "day", "week", "month");

    private final ApiRequestLogRepository repository;
    private final DashboardUsageMapper mapper;

    public DashboardService(final ApiRequestLogRepository repository, final DashboardUsageMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public DashboardUsageResponse getDashboardData(final String granularity, final Instant from, final Instant to) {
        final String normalizedGranularity = granularity.toLowerCase();
        if (!VALID_GRANULARITIES.contains(normalizedGranularity)) {
            throw new BadRequestException("Invalid granularity: " + granularity
                    + ". Must be one of: hour, day, week, month");
        }

        final Instant resolvedFrom = from != null ? from : defaultFrom(normalizedGranularity);
        final Instant resolvedTo = to != null ? to : Instant.now();

        log.debug("Dashboard query: granularity={}, from={}, to={}", normalizedGranularity, resolvedFrom, resolvedTo);

        final List<Object[]> rows = repository.getUsageAggregation(normalizedGranularity, resolvedFrom, resolvedTo);
        log.debug("Dashboard query returned {} rows", rows.size());

        return new DashboardUsageResponse(normalizedGranularity, resolvedFrom, resolvedTo, mapper.toDataPoints(rows));
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
}
