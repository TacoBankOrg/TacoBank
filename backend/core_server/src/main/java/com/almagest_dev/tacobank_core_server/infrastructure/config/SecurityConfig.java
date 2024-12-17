package com.almagest_dev.tacobank_core_server.infrastructure.config;

import com.almagest_dev.tacobank_core_server.infrastructure.security.authentication.MemberLockFilter;
import com.almagest_dev.tacobank_core_server.infrastructure.security.authentication.JwtAuthenticationFilter;
import com.almagest_dev.tacobank_core_server.infrastructure.security.authentication.JwtProvider;
import com.almagest_dev.tacobank_core_server.infrastructure.security.handler.CustomAccessDeniedHandler;
import com.almagest_dev.tacobank_core_server.infrastructure.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@EnableWebSecurity(debug = true)
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MemberLockFilter memberLockFilter;

    private static final String[] PUBLIC_API_URL = { // 인증 없이도 접근 가능한 경로
            "/taco/core/auth/**"
            , "/taco/core/members/email-recovery"
            , "/taco/core/members/password-recovery/**"
    };
    private static final String ADMIN_API_URL = "/taco/core/admin/**"; // 관리자만 접근 가능한 경로
    private static final String MANAGE_API_URL = "/taco/core/manage/**"; // 관리자 + 매니저만 접근 가능한 경로

    public SecurityConfig(JwtProvider jwtProvider, JwtAuthenticationFilter jwtAuthenticationFilter, MemberLockFilter memberLockFilter) {
        this.jwtProvider = jwtProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.memberLockFilter = memberLockFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf((csrf) -> csrf.disable()) // CSRF 보호 비활성화
                .cors((cors) -> cors.configurationSource(CorsConfig.corsConfigurationSource())) // CORS 설정
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PUBLIC_API_URL).permitAll() // 인증 없이 접근 가능한 경로
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS 요청 허용
                        .requestMatchers(ADMIN_API_URL).hasRole("ADMIN") // Admin 페이지 권한 제한
                        .requestMatchers(MANAGE_API_URL).hasAnyRole("ADMIN", "MANAGER") // Admin, Manage 페이지 권한 제한
                        .anyRequest().authenticated()) // 이외 요청은 모두 인증 확인
                .exceptionHandling((e) -> e
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증되지 않은 사용자 접근 혹은 유효한 인증정보 부족한 경우(401 Unauthorized)
                        .accessDeniedHandler(new CustomAccessDeniedHandler()) // 403 Forbidden
                )
                .anonymous((anonymous) -> anonymous.disable()) // 익명 인증 비활성화
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(memberLockFilter, JwtAuthenticationFilter.class) // 계정 잠금 필터
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
