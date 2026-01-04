package com.smartcrops.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilterConfig implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // ✅ VERY IMPORTANT: echo origin (not wildcard)
        String origin = req.getHeader("Origin");
        if (origin != null) {
            res.setHeader("Access-Control-Allow-Origin", origin);
        }

        res.setHeader(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
        );

        res.setHeader(
            "Access-Control-Allow-Headers",
            "Authorization, Content-Type, Accept"
        );

        res.setHeader("Access-Control-Allow-Credentials", "true");

        // ✅ IMPORTANT: cache preflight result
        res.setHeader("Access-Control-Max-Age", "3600");

        // ✅ Handle OPTIONS preflight request
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}
