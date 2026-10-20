package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the rate-limiting key based on Client IP Address or API Key header.
     * Guarantees isolated token buckets per client.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just(apiKey);
            }
            return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress());
        };
    }
}
