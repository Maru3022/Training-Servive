package com.example.training_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trainingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Training Service API")
                        .version("1.0.0")
                        .description("REST API for managing workout sessions, exercises, and user training statistics")
                        .contact(new Contact()
                                .name("Training Service Team")
                                .email("support@training-service.example.com")
                                .url("https://training-service.example.com")
                        )
                );
    }
}
