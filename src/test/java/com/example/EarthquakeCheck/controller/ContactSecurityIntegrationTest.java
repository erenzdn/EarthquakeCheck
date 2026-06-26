package com.example.EarthquakeCheck.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.EarthquakeCheck.DTO.ContactResponse;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitConfig;
import com.example.EarthquakeCheck.config.ratelimit.RateLimitPolicyResolver;
import com.example.EarthquakeCheck.exception.GlobalExceptionHandler;
import com.example.EarthquakeCheck.model.ContactMessageStatus;
import com.example.EarthquakeCheck.service.ContactMessageService;
import com.example.EarthquakeCheck.service.impl.AdminAuthorizationServiceImpl;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ContactSecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "app.security.admin-token=test-admin-token",
        "ratelimit.enabled=true",
        "ratelimit.strict.capacity=10",
        "ratelimit.strict.refill=10",
        "ratelimit.strict.period=1m",
        "ratelimit.relaxed.capacity=100",
        "ratelimit.relaxed.refill=100",
        "ratelimit.relaxed.period=1m",
        "ratelimit.contact.capacity=5",
        "ratelimit.contact.refill=5",
        "ratelimit.contact.period=1h",
        "ratelimit.exempt-paths=/swagger-ui/**,/v3/api-docs/**",
        "ratelimit.relaxed-paths=/api/**"
})
class ContactSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactMessageService contactMessageService;

    @BeforeEach
    void setUp() {
        ContactResponse response = new ContactResponse(
                UUID.randomUUID(),
                "Jane Doe",
                "jane@example.com",
                "Bilgi",
                "Merhaba, destek rica ediyorum.",
                ContactMessageStatus.UNREAD,
                LocalDateTime.now());

        when(contactMessageService.createMessage(any())).thenReturn(response);
        when(contactMessageService.markAsRead(any())).thenReturn(response);
        when(contactMessageService.getAllMessages(any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(response)));
    }

    @Test
    void shouldReturn403WhenAdminTokenMissing() throws Exception {
        mockMvc.perform(get("/api/contact/admin/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200WhenAdminTokenValid() throws Exception {
        mockMvc.perform(get("/api/contact/admin/messages")
                        .header("X-Admin-Token", "test-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldReturn429WhenContactRateLimitExceeded() throws Exception {
        String payload = """
                {
                  "fullName": "Rate Limit Test",
                  "email": "ratelimit@example.com",
                  "subject": "Limit",
                  "message": "Bu mesaj rate limit testinin bir parcasidir."
                }
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/contact/messages")
                            .with(request -> {
                                request.setRemoteAddr("10.10.10.10");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/contact/messages")
                        .with(request -> {
                            request.setRemoteAddr("10.10.10.10");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"))
                .andExpect(jsonPath("$.path").value("/api/contact/messages"))
                .andExpect(jsonPath("$.retryAfterSeconds").isNumber());
    }

    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    @Import({
            ContactController.class,
            GlobalExceptionHandler.class,
            AdminAuthorizationServiceImpl.class,
            RateLimitConfig.class,
            RateLimitPolicyResolver.class
    })
    static class TestApplication {
    }
}
