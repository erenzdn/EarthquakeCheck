package com.example.EarthquakeCheck.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class RateLimitPolicyResolver {

    private static final String STRICT_METHOD = "POST";
    private static final String STRICT_PATH = "/api/building/evaluate";

    private final RateLimitProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean isExempt(HttpServletRequest request) {
        return matches(request.getRequestURI(), properties.getExemptPaths());
    }

    public RatePolicy resolve(HttpServletRequest request) {
        if (isStrictEndpoint(request)) {
            return RatePolicy.STRICT;
        }

        if (matches(request.getRequestURI(), properties.getRelaxedPaths())) {
            return RatePolicy.RELAXED;
        }

        return RatePolicy.NONE;
    }

    private boolean isStrictEndpoint(HttpServletRequest request) {
        return STRICT_METHOD.equalsIgnoreCase(request.getMethod())
                && STRICT_PATH.equals(request.getRequestURI());
    }

    private boolean matches(String uri, List<String> patterns) {
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern.trim(), uri));
    }

    public enum RatePolicy {
        STRICT,
        RELAXED,
        NONE
    }
}
