package com.example.EarthquakeCheck.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsProperties.getAllowedOrigins().toArray(String[]::new);
        String[] allowedHeaders = corsProperties.getAllowedHeaders().toArray(String[]::new);
        String[] exposedHeaders = corsProperties.getExposedHeaders().toArray(String[]::new);
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders(allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .allowCredentials(corsProperties.isAllowCredentials());
    }
}
