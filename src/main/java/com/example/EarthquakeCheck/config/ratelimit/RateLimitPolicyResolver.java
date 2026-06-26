package com.example.EarthquakeCheck.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;

@Component
@RequiredArgsConstructor
public class RateLimitPolicyResolver {

    private static final String STRICT_METHOD = "POST";
    private static final String STRICT_PATH = "/api/building/evaluate";
    private static final String CONTACT_METHOD = "POST";
    private static final String CONTACT_PATH = "/api/contact/messages";

    private final RateLimitProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public boolean isExempt(HttpServletRequest request) {
        return matches(getNormalizedPath(request), properties.getExemptPaths());
    }

    public RatePolicy resolve(HttpServletRequest request) {
        if (isStrictEndpoint(request)) {
            return RatePolicy.STRICT;
        }

        if (isContactEndpoint(request)) {
            return RatePolicy.CONTACT;
        }

        if (matches(getNormalizedPath(request), properties.getRelaxedPaths())) {
            return RatePolicy.RELAXED;
        }

        return RatePolicy.NONE;
    }

    private boolean isStrictEndpoint(HttpServletRequest request) {
        return matchesPolicyPaths(request, properties.getStrictPaths(), STRICT_METHOD, STRICT_PATH);
    }

    private boolean isContactEndpoint(HttpServletRequest request) {
        return matchesPolicyPaths(request, properties.getContactPaths(), CONTACT_METHOD, CONTACT_PATH);
    }

    private String getNormalizedPath(HttpServletRequest request) {
        String path = urlPathHelper.getLookupPathForRequest(request);
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private boolean matchesPolicyPaths(HttpServletRequest request, List<String> configuredPaths, String defaultMethod, String defaultPath) {
        String method = request.getMethod();
        String path = getNormalizedPath(request);

        if (configuredPaths == null || configuredPaths.isEmpty()) {
            return defaultMethod.equalsIgnoreCase(method) && defaultPath.equals(path);
        }

        return configuredPaths.stream().anyMatch(configured -> {
            String pattern = configured.trim();
            String expectedMethod = null;
            if (pattern.contains(":")) {
                int index = pattern.indexOf(":");
                expectedMethod = pattern.substring(0, index).trim();
                pattern = pattern.substring(index + 1).trim();
            }

            if (pattern.length() > 1 && pattern.endsWith("/")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            boolean methodMatches = (expectedMethod == null || expectedMethod.equalsIgnoreCase(method));
            boolean pathMatches = pathMatcher.match(pattern, path);
            return methodMatches && pathMatches;
        });
    }

    private boolean matches(String path, List<String> patterns) {
        if (patterns == null) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> {
            String trimmed = pattern.trim();
            if (trimmed.length() > 1 && trimmed.endsWith("/")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            return pathMatcher.match(trimmed, path);
        });
    }

    public enum RatePolicy {
        STRICT,
        CONTACT,
        RELAXED,
        NONE
    }
}
