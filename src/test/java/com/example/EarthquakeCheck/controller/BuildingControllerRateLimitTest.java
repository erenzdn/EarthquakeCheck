package com.example.EarthquakeCheck.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitConfig;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitPolicyResolver;
import com.example.EarthquakeCheck.service.EvaluationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BuildingController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({RateLimitConfig.class, RateLimitPolicyResolver.class})
@TestPropertySource(properties = {
        "ratelimit.enabled=true",
        "ratelimit.strict.capacity=2",
        "ratelimit.strict.refill=2",
        "ratelimit.strict.period=1m",
        "ratelimit.relaxed.capacity=20",
        "ratelimit.relaxed.refill=20",
        "ratelimit.relaxed.period=1m",
        "ratelimit.exempt-paths=/actuator/health,/actuator/**,/swagger-ui/**,/v3/api-docs/**",
        "ratelimit.relaxed-paths=/api/**"
})
class BuildingControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private com.example.EarthquakeCheck.service.BuildingManagementService buildingManagementService;

    @BeforeEach
    void setUp() {
        when(evaluationService.evaluateBuilding(any()))
                .thenReturn(EvaluationResponseDTO.builder()
                        .id(1L)
                        .riskClass("LOW")
                        .message("ok")
                        .safetyGradePercentage(85)
                        .evaluatedAt(LocalDateTime.now())
                        .build());
    }

    @Test
    void shouldReturn429WhenStrictLimitExceeded() throws Exception {
        mockMvc.perform(evaluateRequest("10.0.1.1")).andExpect(status().isOk());
        mockMvc.perform(evaluateRequest("10.0.1.1")).andExpect(status().isOk());
        mockMvc.perform(evaluateRequest("10.0.1.1")).andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldTrackLimitsPerIpIndependently() throws Exception {
        mockMvc.perform(evaluateRequest("10.0.2.1")).andExpect(status().isOk());
        mockMvc.perform(evaluateRequest("10.0.2.1")).andExpect(status().isOk());
        mockMvc.perform(evaluateRequest("10.0.2.2")).andExpect(status().isOk());
        mockMvc.perform(evaluateRequest("10.0.2.1")).andExpect(status().isTooManyRequests());
        mockMvc.perform(evaluateRequest("10.0.2.2")).andExpect(status().isOk());
    }

    @Test
    void shouldBypassRateLimitOnExemptPaths() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/swagger-ui/index.html").with(request -> {
                        request.setRemoteAddr("10.0.0.1");
                        return request;
                    }))
                    .andExpect(result -> assertNotEquals(429, result.getResponse().getStatus()));
        }
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder evaluateRequest(String ip) {
        return post("/api/building/evaluate")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"yearBuilt\":2000,\"floorCount\":5,\"address\":\"Test\"}");
    }
}
