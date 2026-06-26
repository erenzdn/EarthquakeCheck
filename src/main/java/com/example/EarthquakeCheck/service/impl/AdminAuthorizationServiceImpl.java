package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.service.AdminAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

    @Value("${app.security.admin-token}")
    private String adminToken;

    @Override
    public void validateAdminToken(String providedToken) {
        if (providedToken == null || providedToken.isBlank() || !adminToken.equals(providedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin yetkisi gerekli.");
        }
    }
}
