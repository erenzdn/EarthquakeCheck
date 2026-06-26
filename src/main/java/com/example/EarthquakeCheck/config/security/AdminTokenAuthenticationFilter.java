package com.example.EarthquakeCheck.config.security;

import com.example.EarthquakeCheck.service.AdminAuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class AdminTokenAuthenticationFilter extends OncePerRequestFilter {

    static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final AdminAuthorizationService adminAuthorizationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String providedToken = request.getHeader(ADMIN_TOKEN_HEADER);
        if (adminAuthorizationService.isValidAdminToken(providedToken)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "admin",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
