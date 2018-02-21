package com.ta.poc.healthcheck.indicators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Iterator;
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

        ResponseEntity<JsonNode> responseEntity;
        try {
            watch.start();
            responseEntity = new RestTemplate().getForEntity(url, JsonNode.class);
            watch.stop();
        } catch (Exception e) {
            return Health.down()
                         .withDetail("latency", watch.getTotalTimeMillis())
                         .withDetail("error", e.getMessage())
                         .build();
        }

        return getHealth(responseEntity, watch.getTotalTimeMillis());
    }

    private Health getHealth(ResponseEntity<JsonNode> responseEntity, long latency) {
        Health.Builder builder = (responseEntity.getStatusCode().equals(HttpStatus.OK) ?
                                  Health.up() :
                                  Health.down())
            .withDetail("latency", latency);

        JsonNode jsonNode = responseEntity.getBody();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            builder.withDetail(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }
}
