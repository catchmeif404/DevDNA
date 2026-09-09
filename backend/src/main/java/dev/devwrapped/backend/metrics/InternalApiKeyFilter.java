package dev.devwrapped.backend.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.admin.metrics-api-key:}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/internal/")) {
            String provided = request.getHeader("X-Admin-Client-Key");
            if (expectedKey.isBlank() || !expectedKey.equals(provided)) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"forbidden\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
