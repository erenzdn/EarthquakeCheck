package com.example.EarthquakeCheck.config.security;

import com.example.EarthquakeCheck.service.AdminAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AdminTokenAuthenticationFilter adminTokenAuthenticationFilter(
            AdminAuthorizationService adminAuthorizationService) {
        return new AdminTokenAuthenticationFilter(adminAuthorizationService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, AdminTokenAuthenticationFilter adminTokenAuthenticationFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/building/evaluate")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/geolocation/coordinates")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact/messages")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pga/value")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pga/value")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/import/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/building")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/building/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/contact/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(adminTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
