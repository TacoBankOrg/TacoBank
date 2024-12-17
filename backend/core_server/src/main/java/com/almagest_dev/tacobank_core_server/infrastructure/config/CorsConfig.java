package com.almagest_dev.tacobank_core_server.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

public class CorsConfig {
    @Bean
    public static CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true); // 쿠키 허용
        config.addAllowedHeader("*"); // 모든 Header 허용

        // 리소스 허용할 URL
        List<String> allowedOriginPatterns = new ArrayList<>();
        allowedOriginPatterns.add("https://bill-us.com");
        config.setAllowedOrigins(allowedOriginPatterns);

        // 허용 Http Method 지정
        List<String> allowedMethods = new ArrayList<>();
        allowedMethods.add("GET");
        allowedMethods.add("POST");
        allowedMethods.add("PUT");
        allowedMethods.add("PATCH");
        allowedMethods.add("DELETE");
        allowedMethods.add("OPTIONS");
        config.setAllowedMethods(allowedMethods);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 위 규칙을 적용
        return source;
    }
}
