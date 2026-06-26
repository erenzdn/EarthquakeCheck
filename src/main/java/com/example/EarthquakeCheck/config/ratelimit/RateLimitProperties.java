package com.example.EarthquakeCheck.config.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    private boolean enabled = true;

    @Valid
    @NotNull
    private LimitPolicy strict = new LimitPolicy();

    @Valid
    @NotNull
    private LimitPolicy relaxed = new LimitPolicy();

    @Valid
    @NotNull
    private LimitPolicy contact = new LimitPolicy();

    @Valid
    @NotNull
    private LimitPolicy importPolicy = new LimitPolicy();

    private List<String> exemptPaths = new ArrayList<>();
    private List<String> relaxedPaths = new ArrayList<>();
    private List<String> strictPaths = new ArrayList<>();
    private List<String> contactPaths = new ArrayList<>();
    private List<String> importPaths = new ArrayList<>();

    private boolean trustForwardedFor = false;
    private String ipHeader = "X-Forwarded-For";
    private List<String> ipHeaders = new ArrayList<>(List.of("X-Forwarded-For"));
    private List<String> trustedProxyIps = new ArrayList<>();
    
    private Duration cacheExpiration = Duration.ofMinutes(15);
    private int cacheMaxSize = 100000;

    @Getter
    @Setter
    public static class LimitPolicy {
        @Min(1)
        private long capacity = 10;

        @Min(1)
        private long refill = 10;

        @NotNull
        private Duration period = Duration.ofMinutes(1);
    }
}
