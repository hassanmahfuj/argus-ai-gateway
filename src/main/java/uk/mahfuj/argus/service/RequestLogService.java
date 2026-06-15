package uk.mahfuj.argus.service;

import org.springframework.stereotype.Service;

import uk.mahfuj.argus.entity.ApiRequestLogEntity;
import uk.mahfuj.argus.repository.ApiRequestLogRepository;


/**
 * Persists proxied-request logs. Renamed from {@code TokenUsageService} since it
 * stores the full request log, not just token usage.
 */
@Service
public class RequestLogService {

    private final ApiRequestLogRepository repository;

    public RequestLogService(final ApiRequestLogRepository repository) {
        this.repository = repository;
    }

    public void save(final ApiRequestLogEntity logEntry) {
        repository.save(logEntry);
    }
}
