package com.example.EarthquakeCheck.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminAuthorizationServiceImplTest {

    private AdminAuthorizationServiceImpl adminAuthorizationService;

    @BeforeEach
    void setUp() {
        adminAuthorizationService = new AdminAuthorizationServiceImpl();
        ReflectionTestUtils.setField(adminAuthorizationService, "adminToken", "known-admin-token-value-32chars!!");
    }

    @Test
    void shouldReturnTrueForValidToken() {
        assertTrue(adminAuthorizationService.isValidAdminToken("known-admin-token-value-32chars!!"));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(adminAuthorizationService.isValidAdminToken("wrong-admin-token-value-32chars!"));
    }

    @Test
    void shouldReturnFalseForBlankToken() {
        assertFalse(adminAuthorizationService.isValidAdminToken("   "));
    }
}
