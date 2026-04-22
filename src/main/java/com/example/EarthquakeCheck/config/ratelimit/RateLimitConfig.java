package com.example.EarthquakeCheck.config.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public IpRateLimitingFilter ipRateLimitingFilter(
            RateLimitProperties properties, RateLimitPolicyResolver policyResolver) {
        return new IpRateLimitingFilter(properties, policyResolver);
    }

    @Bean
    public FilterRegistrationBean<IpRateLimitingFilter> rateLimitFilterRegistration(IpRateLimitingFilter filter) {
        FilterRegistrationBean<IpRateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
