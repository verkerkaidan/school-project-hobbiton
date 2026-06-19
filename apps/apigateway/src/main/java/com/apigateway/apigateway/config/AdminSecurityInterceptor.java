package com.apigateway.apigateway.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminSecurityInterceptor.class);

    private final JwtUtil jwtUtil;

    public AdminSecurityInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("[AdminSecurityInterceptor] Missing or malformed Authorization header");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access requires a valid Bearer token");
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isAdmin(token)) {
            logger.warn("[AdminSecurityInterceptor] Token does not have ROLE_ADMIN");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access requires ROLE_ADMIN");
            return false;
        }

        return true;
    }
}
