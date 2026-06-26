package com.example.EarthquakeCheck.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.EarthquakeCheck.config.ratelimit.RateLimitConfig;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitPolicyResolver;
import com.example.EarthquakeCheck.config.security.SecurityConfig;
import com.example.EarthquakeCheck.exception.GlobalExceptionHandler;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.service.BuildingManagementService;
import com.example.EarthquakeCheck.service.EvaluationService;
import com.example.EarthquakeCheck.service.impl.AdminAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = BuildingSecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "app.security.admin-token=test-admin-token-for-building-security",
        "ratelimit.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class BuildingSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildingManagementService buildingManagementService;

    @MockitoBean
    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        when(buildingManagementService.createBuilding(any())).thenReturn(new Building());
    }

    @Test
    void shouldReturn403WhenCreatingBuildingWithoutAdminToken() throws Exception {
        mockMvc.perform(post("/api/building")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"katSayisi\":3,\"yapimYili\":2000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200WhenCreatingBuildingWithAdminToken() throws Exception {
        mockMvc.perform(post("/api/building")
                        .header("X-Admin-Token", "test-admin-token-for-building-security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"katSayisi\":3,\"yapimYili\":2000}"))
                .andExpect(status().isOk());
    }

    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    @Import({
            BuildingController.class,
            GlobalExceptionHandler.class,
            AdminAuthorizationServiceImpl.class,
            SecurityConfig.class,
            RateLimitConfig.class,
            RateLimitPolicyResolver.class
    })
    static class TestApplication {
    }
}
