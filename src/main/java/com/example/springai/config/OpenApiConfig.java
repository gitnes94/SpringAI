package com.example.springai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurerar Swagger UI / OpenAPI-dokumentationen.
 * Åtkomlig på: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Middleware Service API")
                        .version("1.0.0")
                        .description("""
                                Middleware-tjänst som fungerar som en bro mellan slutanvändare och LLM-modeller.
                                Stöder flera personligheter, konversationsminne per session och robust felhantering.
                                """)
                        .contact(new Contact()
                                .name("Laboration 1 – AI-Integrerad Spring Boot Service")));
    }
}