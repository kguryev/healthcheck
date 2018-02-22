package com.ta.poc.healthcheck.indicators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

public class PingHealthIndicator implements HealthIndicator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<MediaType> JSON_TYPE = of(MediaType.APPLICATION_JSON,
                                                       MediaType.APPLICATION_JSON_UTF8,
                                                       MediaType.valueOf("application/json;charset=utf-8")).collect(toSet());

    private final String url;

    private final Map<String, String> customStatusMapping;

    public PingHealthIndicator(String url) {
        this(url, new HashMap<>());
    }

    public PingHealthIndicator(String url, Map<String, String> customStatusMapping) {
        this.url = url;
        this.customStatusMapping = customStatusMapping;
    }

    @Override
    public Health health() {
        Health.Builder builder;
        ResponseEntity<String> responseEntity;
        try {
            StopWatch watch = new StopWatch();
            watch.start();
            responseEntity = new RestTemplate().getForEntity(url, String.class);
            watch.stop();

            builder = responseEntity.getStatusCode().equals(HttpStatus.OK) ? Health.up() : Health.down();
            builder.withDetail("latency", watch.getTotalTimeMillis()).build();

            if (JSON_TYPE.contains(responseEntity.getHeaders().getContentType())) {
                JsonNode jsonNode = OBJECT_MAPPER.readTree(responseEntity.getBody());
                addDetailsFromJsonResponse(builder, jsonNode);
            }
        } catch (Exception e) {
            builder = Health.down().withDetail("error", e.getMessage());
        }
        return builder.build();
    }

    private void addDetailsFromJsonResponse(Health.Builder builder, JsonNode jsonNode) {
        if (customStatusMapping != null && !customStatusMapping.isEmpty()) {
            JsonNode statusNode = jsonNode.get("status");
            if (statusNode != null && statusNode.getNodeType().equals(JsonNodeType.STRING)) {
                ((ObjectNode)jsonNode).put("status", mapCustomStatus((TextNode)statusNode));
            }
        }

        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            builder.withDetail(entry.getKey(), entry.getValue());
        }
    }

    private String mapCustomStatus(TextNode statusNode) {
        return customStatusMapping.getOrDefault(statusNode.asText(), statusNode.asText());
    }
}
