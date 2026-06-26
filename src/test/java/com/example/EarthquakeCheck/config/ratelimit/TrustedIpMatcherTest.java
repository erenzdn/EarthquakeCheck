package com.example.EarthquakeCheck.config.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrustedIpMatcherTest {

    @Test
    void shouldMatchExactTrustedIp() {
        assertTrue(TrustedIpMatcher.isTrusted("127.0.0.1", List.of("127.0.0.1")));
    }

    @Test
    void shouldMatchCidrRange() {
        assertTrue(TrustedIpMatcher.isTrusted("172.18.0.5", List.of("172.16.0.0/12")));
        assertFalse(TrustedIpMatcher.isTrusted("203.0.113.10", List.of("172.16.0.0/12")));
    }

    @Test
    void shouldReturnFalseWhenTrustedListEmpty() {
        assertFalse(TrustedIpMatcher.isTrusted("127.0.0.1", List.of()));
    }
}
