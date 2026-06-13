package uk.mahfuj.argus.service;

import org.springframework.stereotype.Service;
import uk.mahfuj.argus.domain.ApiRequestLog;
import uk.mahfuj.argus.repos.ApiRequestLogRepository;


@Service
public class TokenUsageService {

    private final ApiRequestLogRepository repository;

    public TokenUsageService(final ApiRequestLogRepository repository) {
        this.repository = repository;
    }

    public void logRequest(final ApiRequestLog logEntry) {
        repository.save(logEntry);
    }
}
