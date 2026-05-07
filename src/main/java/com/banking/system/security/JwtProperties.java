package com.banking.system.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "banking.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes
) {
}
