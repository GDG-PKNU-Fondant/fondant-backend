package com.fondant.infra.portone.config;

import io.portone.sdk.server.PortOneClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

    @Value("${portone.api-secret}") private String apiSecret;
    @Value("${portone.api-base}") private String apiBase;

    @Bean
    public PortOneClient portOneClient() {
        return new PortOneClient(apiSecret, apiBase, null);
    }
}