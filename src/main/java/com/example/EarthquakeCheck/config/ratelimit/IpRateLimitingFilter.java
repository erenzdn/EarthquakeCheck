package com.example.EarthquakeCheck.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class IpRateLimitingFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RateLimitProperties properties;
    private final RateLimitPolicyResolver policyResolver;
    private final Cache<String, Bucket> buckets;

    public IpRateLimitingFilter(RateLimitProperties properties, RateLimitPolicyResolver policyResolver) {
        this.properties = properties;
        this.policyResolver = policyResolver;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(properties.getCacheExpiration())
                .maximumSize(properties.getCacheMaxSize())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled() || policyResolver.isExempt(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitPolicyResolver.RatePolicy policy = policyResolver.resolve(request);
        if (policy == RateLimitPolicyResolver.RatePolicy.NONE) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = policy.name() + ":" + clientIp;
        Bucket bucket = buckets.get(key, ignored -> createBucket(policy));
        if (bucket == null) {
            bucket = createBucket(policy);
        }

        long limitCapacity = getLimitCapacity(policy);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(limitCapacity));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.setHeader("X-Rate-Limit-Reset", "0");
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1L, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000d));
        
        log.warn("Rate limit exceeded for IP: {} on URI: {} [Policy: {}] - Throttled for {} seconds", 
                clientIp, request.getRequestURI(), policy, retryAfterSeconds);

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(limitCapacity));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", 429);
        payload.put("error", "Rate limit exceeded");
        payload.put("details", "Cok fazla istek gonderdiniz. Lutfen daha sonra tekrar deneyiniz.");
        payload.put("path", request.getRequestURI());
        payload.put("retryAfterSeconds", retryAfterSeconds);

        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(payload));
    }

    private Bucket createBucket(RateLimitPolicyResolver.RatePolicy policy) {
        RateLimitProperties.LimitPolicy selected = switch (policy) {
            case STRICT -> properties.getStrict();
            case IMPORT -> properties.getImportPolicy();
            case CONTACT -> properties.getContact();
            case RELAXED -> properties.getRelaxed();
            case NONE -> properties.getRelaxed();
        };

        Bandwidth limit = Bandwidth.classic(selected.getCapacity(),
                Refill.greedy(selected.getRefill(), sanitizeDuration(selected.getPeriod())));
        return Bucket.builder().addLimit(limit).build();
    }

    private long getLimitCapacity(RateLimitPolicyResolver.RatePolicy policy) {
        return switch (policy) {
            case STRICT -> properties.getStrict().getCapacity();
            case IMPORT -> properties.getImportPolicy().getCapacity();
            case CONTACT -> properties.getContact().getCapacity();
            case RELAXED -> properties.getRelaxed().getCapacity();
            case NONE -> properties.getRelaxed().getCapacity();
        };
    }

    private Duration sanitizeDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return Duration.ofMinutes(1);
        }
        return duration;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (!properties.isTrustForwardedFor()) {
            return remoteAddr;
        }

        if (!TrustedIpMatcher.isTrusted(remoteAddr, properties.getTrustedProxyIps())) {
            return remoteAddr;
        }

        for (String headerName : resolveIpHeaders()) {
            String headerValue = request.getHeader(headerName);
            if (headerValue == null || headerValue.isBlank()) {
                continue;
            }

            String clientIp = headerValue.split(",")[0].trim();
            if (!clientIp.isBlank()) {
                return clientIp;
            }
        }

        return remoteAddr;
    }

    private java.util.List<String> resolveIpHeaders() {
        if (properties.getIpHeaders() != null && !properties.getIpHeaders().isEmpty()) {
            return properties.getIpHeaders();
        }

        String legacyHeader = properties.getIpHeader();
        if (legacyHeader == null || legacyHeader.isBlank()) {
            return java.util.List.of("X-Forwarded-For");
        }

        return java.util.List.of(legacyHeader);
    }
}
