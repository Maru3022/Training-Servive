package com.example.training_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trainingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Training Service API")
                        .description("Polished REST API for managing training sessions, set updates, and bulk-loading flows.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Training Service Team")
                                .email("team@training-service.local"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://example.com/internal-license")))
                .addServersItem(new Server()
                        .url("http://localhost:8085")
                        .description("Local development server"));
    }
}
