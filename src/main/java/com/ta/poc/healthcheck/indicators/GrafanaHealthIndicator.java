package com.ta.poc.healthcheck.indicators;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Component
public class GrafanaHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrafanaHealthIndicator.class);

    @Value("${management.health.grafana.host}")
    private String host;

    @Value("${management.health.grafana.port}")
    private int port;

    @Value("${management.health.grafana.timeout:5000}")
    private int timeout;

    @Override
    public Health health() {
        StopWatch watch = new StopWatch();
        Socket socket = null;
        try {
            socket = new Socket();
            watch.start();
            socket.connect(new InetSocketAddress(host, port), timeout);
            watch.stop();
            return Health.up().withDetail("latency", watch.getTime()).build();
        } catch (final Exception e) {
            return Health.down().withDetail("latency", watch.getTime()).build();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (final IOException e) {
                    LOGGER.debug("Unable to close socket", e);
                }
            }
        }
    }
}
