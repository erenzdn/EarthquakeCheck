package com.example.EarthquakeCheck.service;

public interface AdminAuthorizationService {
    void validateAdminToken(String providedToken);
}
