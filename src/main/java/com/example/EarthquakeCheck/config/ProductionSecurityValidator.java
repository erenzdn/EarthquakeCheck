package com.example.EarthquakeCheck.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSecurityValidator {

    private static final String DEFAULT_ADMIN_TOKEN = "change-this-admin-token";
    private static final int MIN_ADMIN_TOKEN_LENGTH = 32;

    @Value("${app.security.admin-token:}")
    private String adminToken;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @PostConstruct
    void validateProductionSecrets() {
        if (adminToken == null || adminToken.isBlank()) {
            throw new IllegalStateException(
                    "Prod ortaminda APP_ADMIN_TOKEN ortam degiskeni zorunludur.");
        }
        if (DEFAULT_ADMIN_TOKEN.equals(adminToken)) {
            throw new IllegalStateException(
                    "Prod ortaminda varsayilan admin token kullanilamaz. APP_ADMIN_TOKEN degerini degistirin.");
        }
        if (adminToken.length() < MIN_ADMIN_TOKEN_LENGTH) {
            throw new IllegalStateException(
                    "Prod ortaminda APP_ADMIN_TOKEN en az " + MIN_ADMIN_TOKEN_LENGTH + " karakter olmalidir.");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException(
                    "Prod ortaminda DB_PASSWORD ortam degiskeni zorunludur.");
        }
    }
}
