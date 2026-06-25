package com.example.gezinio.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
// 401 Unauthorized hatalarını düzgün şekilde handle edip JSON response dönmek
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring Security authentication başarısız olunca bu metod çağrılır.
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        response.setContentType("application/json;charset=UTF-8");

        // Error response body
        Map<String, Object> errorResponse = new HashMap<>();

        // Timestamp: Ne zaman oldu
        errorResponse.put("timestamp", System.currentTimeMillis());

        // Status: HTTP status code
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        // Error: Kısa açıklama
        errorResponse.put("error", "Unauthorized");

        // Message: Detaylı ama generic mesaj
        errorResponse.put("message",
                "Unauthorized access - Invalid or missing authentication token");

        // Path: Hangi endpoint
        errorResponse.put("path", request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(errorResponse);


        response.getWriter().write(jsonResponse);
    }
}