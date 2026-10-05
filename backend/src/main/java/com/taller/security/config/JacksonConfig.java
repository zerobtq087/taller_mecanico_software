package com.taller.security.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    private static final int MAX_JSON_STRING_LENGTH = 30_000_000;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonReadConstraints() {
        return builder -> builder.postConfigurer(objectMapper -> objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder()
                        .maxStringLength(MAX_JSON_STRING_LENGTH)
                        .build()));
    }
}
