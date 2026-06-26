package com.example.EarthquakeCheck.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.EarthquakeCheck.config.security.SecurityConfig;
import com.example.EarthquakeCheck.exception.GlobalExceptionHandler;
import com.example.EarthquakeCheck.service.GeoLocationService;
import com.example.EarthquakeCheck.service.impl.AdminAuthorizationServiceImpl;
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

@SpringBootTest(classes = GeoLocationControllerValidationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "app.security.admin-token=test-admin-token",
        "ratelimit.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class GeoLocationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeoLocationService geoLocationService;

    @Test
    void shouldReturn400WhenAddressTooLong() throws Exception {
        String payload = "{\"address\":\"" + "a".repeat(501) + "\"}";

        mockMvc.perform(post("/api/geolocation/coordinates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("address en fazla 500 karakter olabilir."));
    }

    @Test
    void shouldReturn200WhenAddressValid() throws Exception {
        when(geoLocationService.getCoordinatesFromAddress(anyString())).thenReturn(new double[] {41.0, 29.0});

        mockMvc.perform(post("/api/geolocation/coordinates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Istanbul\"}"))
                .andExpect(status().isOk());
    }

    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    @Import({
            GeoLocationController.class,
            GlobalExceptionHandler.class,
            AdminAuthorizationServiceImpl.class,
            SecurityConfig.class
    })
    static class TestApplication {
    }
}
