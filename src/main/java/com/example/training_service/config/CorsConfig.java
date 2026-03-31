package com.example.training_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        
        registry.addMapping("/trainings/**")
                .allowedOriginPatterns(
                        "http://localhost:3000",      // React dev server
                        "http://localhost:5173",      // Vite dev server
                        "http://localhost:8085",      // Backend itself
                        allowedOrigins != null ? allowedOrigins : "*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
