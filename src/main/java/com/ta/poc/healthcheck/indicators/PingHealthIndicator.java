package com.ta.poc.healthcheck.indicators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PingHealthIndicator implements HealthIndicator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String url;
    private final TypeReference<HashMap<String, String>> mapTypeReference = new TypeReference<HashMap<String, String>>() {};

    public PingHealthIndicator(String url) {
        this.url = url;
    }

    @Override
    public Health health() {
        StopWatch watch = new StopWatch();

        ResponseEntity<String> responseEntity;
        try {
            watch.start();
            responseEntity = new RestTemplate().getForEntity(url, String.class);
            watch.stop();
        } catch (Exception e) {
            return Health.down()
                         .withDetail("latency", watch.getTotalTimeMillis())
                         .withDetail("error", e.getMessage())
                         .build();
        }

        return getHealth(responseEntity, watch.getTotalTimeMillis());
    }

    private Health getHealth(ResponseEntity<String> responseEntity, long latency) {
        Health.Builder builder = (responseEntity.getStatusCode().equals(HttpStatus.OK) ?
                                  Health.up() :
                                  Health.down())
            .withDetail("latency", latency);

        try {
            Map<String, String> map = OBJECT_MAPPER.readValue(responseEntity.getBody(), mapTypeReference);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.withDetail(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            return builder.down().withDetail("latency", latency).withDetail("error", e.getMessage()).build();
        }

        return builder.build();
    }
}
