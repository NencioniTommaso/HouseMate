package com.housemate.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LoggingContextFilter extends OncePerRequestFilter {

    public static final String MDC_KEY_REQUEST_ID = "requestId";
    public static final String MDC_KEY_USER_ID = "userId";
    public static final String MDC_KEY_CLIENT_IP = "clientIp";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String DEFAULT_USER_ID = "anonymous";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        String clientIp = resolveClientIp(request);

        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_KEY_USER_ID, DEFAULT_USER_ID);
        MDC.put(MDC_KEY_CLIENT_IP, clientIp);

        response.setHeader(HEADER_REQUEST_ID, requestId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP {} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
