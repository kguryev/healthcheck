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

import java.util.HashMap;

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

    @Value("${split.io.health-check.endpoint}")
    private String splitIoHealthEndpoint;

    @Value("${cloudflare.health-check.endpoint}")
    private String cloudflareHealthEndpoint;

    @Value("${fullstory.health-check.endpoint}")
    private String fullstoryHealthEndpoint;

    @Bean
    public HealthIndicator grafanaHealthIndicator() {
        return new PingHealthIndicator(gafanaHealthEndpoint);
    }

    @Bean
    public HealthIndicator elasticSearchHealthIndicator() {
        return new PingHealthIndicator(elasticSearchHealthEndpoint, new HashMap<String, String>() {{
            put("green", "UP");
            put("yellow", "UP");
            put("red", "DOWN");
        }});
    }

    @Bean
    public HealthIndicator marqetaHealthIndicator() {
        return new PingHealthIndicator(marqetaHealthEndpoint);
    }

    @Bean
    public HealthIndicator splitIoHealthIndicator() {
        return new PingHealthIndicator(splitIoHealthEndpoint);
    }

    @Bean
    public HealthIndicator cloudflareHealthIndicator() {
        return new PingHealthIndicator(cloudflareHealthEndpoint);
    }

    @Bean
    public HealthIndicator fullstoryHealthIndicator() {
        return new PingHealthIndicator(fullstoryHealthEndpoint);
    }
}