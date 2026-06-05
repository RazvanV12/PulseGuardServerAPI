package com.personal.pulseguardserverapi.config;

import org.springframework.context.annotation.Configuration;

/**
 * Jackson 3.x (Spring Boot 4.x) changed the namespace from com.fasterxml.jackson
 * to tools.jackson and removed SerializationFeature.WRITE_DATES_AS_TIMESTAMPS.
 *
 * In Jackson 3.x, JavaTimeModule is auto-registered by Spring Boot and serializes
 * LocalDateTime as ISO-8601 strings ("2026-06-05T12:00:00") by default — no extra
 * configuration is needed here.
 *
 * If you ever need to customize Jackson, inject Jackson2ObjectMapperBuilderCustomizer
 * and use the tools.jackson.* API directly (not com.fasterxml.jackson.*).
 */
@Configuration
public class JacksonConfig {
    // Spring Boot 4 auto-configuration covers everything we need.
}
