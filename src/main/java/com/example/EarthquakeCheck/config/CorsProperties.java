package com.example.EarthquakeCheck.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>(List.of("Content-Type", "X-Admin-Token", "Accept"));
    private List<String> exposedHeaders =
            new ArrayList<>(List.of("X-Rate-Limit-Limit", "X-Rate-Limit-Remaining", "X-Rate-Limit-Reset"));
    private boolean allowCredentials = true;
}
