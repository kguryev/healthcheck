package com.ta.poc.healthcheck.configuration;

import com.ta.poc.healthcheck.indicators.PingHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore( {EndpointAutoConfiguration.class})
@AutoConfigureAfter( {HealthIndicatorAutoConfiguration.class})
public class CustomHealthIndicatorConfiguration {

    @Value("${grafana.health-check.endpoint}")
    private String gafanaHealthEndpoint;

    @Value("${elasticsearch.health-check.endpoint}")
    private String elasticSearchHealthEndpoint;

    @Value("${marqeta.health-check.endpoint}")
    private String marqetaHealthEndpoint;

    @Bean
    public HealthIndicator grafanaHealthIndicator() {
        return new PingHealthIndicator(gafanaHealthEndpoint);
    }

    @Bean
    public HealthIndicator elasticSearchHealthIndicator() {
        return new PingHealthIndicator(elasticSearchHealthEndpoint);
    }

    @Bean
    public HealthIndicator marqetaHealthIndicator() {
        return new PingHealthIndicator(marqetaHealthEndpoint);
    }
}