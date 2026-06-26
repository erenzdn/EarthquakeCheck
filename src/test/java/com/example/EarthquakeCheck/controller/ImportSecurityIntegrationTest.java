package com.example.EarthquakeCheck.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.EarthquakeCheck.config.ratelimit.RateLimitConfig;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitPolicyResolver;
import com.example.EarthquakeCheck.config.security.SecurityConfig;
import com.example.EarthquakeCheck.exception.GlobalExceptionHandler;
import com.example.EarthquakeCheck.service.ImportDataService;
import com.example.EarthquakeCheck.service.impl.AdminAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ImportSecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "app.security.admin-token=test-admin-token-for-import-security-check",
        "ratelimit.enabled=true",
        "ratelimit.strict.capacity=10",
        "ratelimit.strict.refill=10",
        "ratelimit.strict.period=1m",
        "ratelimit.relaxed.capacity=100",
        "ratelimit.relaxed.refill=100",
        "ratelimit.relaxed.period=1m",
        "ratelimit.exempt-paths=/actuator/health",
        "ratelimit.relaxed-paths=/api/**",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class ImportSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportDataService importDataService;

    private MockMultipartFile sampleFile;

    @BeforeEach
    void setUp() throws Exception {
        sampleFile = new MockMultipartFile(
                "file",
                "pga-data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[] {0x50, 0x4B, 0x03, 0x04});
        doNothing().when(importDataService).importPgaData(any());
    }

    @Test
    void shouldReturn403WhenAdminTokenMissing() throws Exception {
        mockMvc.perform(multipart("/api/import/pga").file(sampleFile))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200WhenAdminTokenValid() throws Exception {
        mockMvc.perform(multipart("/api/import/pga")
                        .file(sampleFile)
                        .header("X-Admin-Token", "test-admin-token-for-import-security-check"))
                .andExpect(status().isOk());
    }

    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    @Import({
            ImportDataController.class,
            GlobalExceptionHandler.class,
            AdminAuthorizationServiceImpl.class,
            SecurityConfig.class,
            RateLimitConfig.class,
            RateLimitPolicyResolver.class
    })
    static class TestApplication {
    }
}
