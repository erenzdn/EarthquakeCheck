package com.example.EarthquakeCheck.config.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RateLimitPolicyResolverTest {

    private RateLimitProperties properties;
    private RateLimitPolicyResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        resolver = new RateLimitPolicyResolver(properties);
    }

    @Test
    void shouldResolveImportPolicyForPgaImportEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/import/pga");

        assertEquals(RateLimitPolicyResolver.RatePolicy.IMPORT, resolver.resolve(request));
    }

    @Test
    void shouldPreferImportPolicyOverRelaxedForImportPath() {
        properties.getRelaxedPaths().add("/api/**");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/import/pga");

        assertEquals(RateLimitPolicyResolver.RatePolicy.IMPORT, resolver.resolve(request));
    }
}
