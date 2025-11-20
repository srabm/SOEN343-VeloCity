package com.concordia.velocity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Web configuration for CORS (Cross-Origin Resource Sharing)
 * This allows your Vue.js frontend to communicate with the Spring Boot backend
 */
@Configuration
public class WebConfig {

    /**
     * Configure CORS to allow requests from frontend
     * Uses allowedOriginPatterns for compatibility with allowCredentials
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Use allowedOriginPatterns instead of allowedOrigins when credentials are enabled
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",      // Any port on localhost
                "http://127.0.0.1:*"       // Any port on 127.0.0.1
        ));

        // Or specify exact ports for better security:
        // config.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:5173",
        //     "http://localhost:3000",
        //     "http://127.0.0.1:5173",
        //     "http://127.0.0.1:3000"
        // ));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Allow all standard HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Cache preflight requests for 1 hour
        config.setMaxAge(3600L);

        // Apply CORS configuration to all API endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}