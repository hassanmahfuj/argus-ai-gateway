package uk.mahfuj.aigateway.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("uk.mahfuj.aigateway.domain")
@EnableJpaRepositories("uk.mahfuj.aigateway.repos")
@EnableTransactionManagement
public class DomainConfig {
}
