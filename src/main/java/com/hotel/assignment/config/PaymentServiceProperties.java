package com.hotel.assignment.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment-service")
public record PaymentServiceProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}