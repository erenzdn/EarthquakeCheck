package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.service.AdminAuthorizationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

    @Value("${app.security.admin-token}")
    private String adminToken;

    @Override
    public boolean isValidAdminToken(String providedToken) {
        if (providedToken == null || providedToken.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                adminToken.getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void validateAdminToken(String providedToken) {
        if (!isValidAdminToken(providedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin yetkisi gerekli.");
        }
    }
}
