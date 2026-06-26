package com.example.EarthquakeCheck.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductionSecurityValidatorTest {

    @Test
    void shouldRejectMissingAdminToken() {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(validator, "adminToken", "");
        ReflectionTestUtils.setField(validator, "dbPassword", "secure-db-password");

        assertThrows(IllegalStateException.class, validator::validateProductionSecrets);
    }

    @Test
    void shouldRejectDefaultAdminToken() {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(validator, "adminToken", "change-this-admin-token");
        ReflectionTestUtils.setField(validator, "dbPassword", "secure-db-password");

        assertThrows(IllegalStateException.class, validator::validateProductionSecrets);
    }

    @Test
    void shouldRejectShortAdminToken() {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(validator, "adminToken", "too-short-token");
        ReflectionTestUtils.setField(validator, "dbPassword", "secure-db-password");

        assertThrows(IllegalStateException.class, validator::validateProductionSecrets);
    }

    @Test
    void shouldRejectMissingDbPassword() {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(
                validator,
                "adminToken",
                "this-is-a-valid-production-admin-token-value");
        ReflectionTestUtils.setField(validator, "dbPassword", "");

        assertThrows(IllegalStateException.class, validator::validateProductionSecrets);
    }

    @Test
    void shouldAcceptValidProductionSecrets() {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(
                validator,
                "adminToken",
                "this-is-a-valid-production-admin-token-value");
        ReflectionTestUtils.setField(validator, "dbPassword", "secure-db-password");

        assertDoesNotThrow(validator::validateProductionSecrets);
    }
}
