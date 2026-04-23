package com.arcabank.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // Start time of the request
        long startTime = System.currentTimeMillis();

        // Pass the request further down the chain (to Security and the Controller)
        filterChain.doFilter(request, response);

        // Calculate how long the processing took
        long duration = System.currentTimeMillis() - startTime;

        // Log the result in the following format: Method, Path, Status Code
        log.info("HTTP Request: [{}] {} - Status: {} ({}ms)",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            duration);
    }
}
