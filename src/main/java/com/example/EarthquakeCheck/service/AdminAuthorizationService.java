package com.example.EarthquakeCheck.service;

public interface AdminAuthorizationService {

    boolean isValidAdminToken(String providedToken);

    void validateAdminToken(String providedToken);
}
