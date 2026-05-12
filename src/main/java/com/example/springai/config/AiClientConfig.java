package com.example.springai.config;

import com.example.springai.exception.AiRateLimitException;
import com.example.springai.exception.AiServiceException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    @Bean
    public RestClient aiRestClient(AiProperties properties) {
        System.out.println(">>> AI BASE URL: " + properties.baseUrl());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    int status = res.getStatusCode().value();
                    if (status == 429 || status == 503) {
                        throw new AiRateLimitException("Retrybar fel: " + status);
                    }
                    throw new AiServiceException("Ej retrybar fel: " + status);
                })
                .build();
    }
}