package com.taco_bank.business_server.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
//@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {


    //인증없이 접근 허용할 경로
    private static final String[] PUBLIC_API_URL = {
            "/auth/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CORS 설정
//        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // CSRF 보호 비활성화
        http.csrf(AbstractHttpConfigurer::disable);
        //세션 비활성화
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //경로 관리
        http.authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
//                        .requestMatchers(PUBLIC_API_URL).permitAll()
//                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()  // OPTIONS 요청 허용
//                        .requestMatchers(ADMIN_API_URL).hasRole("ADMIN")
//                        .anyRequest().authenticated()
        );

        return http.build();
    }
}
