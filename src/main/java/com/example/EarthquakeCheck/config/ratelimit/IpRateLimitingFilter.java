package com.example.EarthquakeCheck.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class IpRateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final RateLimitPolicyResolver policyResolver;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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

        String key = policy.name() + ":" + resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> createBucket(policy));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1L, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000d));
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
    }

    private Bucket createBucket(RateLimitPolicyResolver.RatePolicy policy) {
        RateLimitProperties.LimitPolicy selected =
                policy == RateLimitPolicyResolver.RatePolicy.STRICT ? properties.getStrict() : properties.getRelaxed();

        Bandwidth limit = Bandwidth.classic(selected.getCapacity(),
                Refill.greedy(selected.getRefill(), sanitizeDuration(selected.getPeriod())));
        return Bucket.builder().addLimit(limit).build();
    }

    private Duration sanitizeDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return Duration.ofMinutes(1);
        }
        return duration;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
