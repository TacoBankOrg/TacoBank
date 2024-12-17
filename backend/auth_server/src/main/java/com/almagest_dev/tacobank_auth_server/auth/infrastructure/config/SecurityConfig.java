package com.almagest_dev.tacobank_auth_server.auth.infrastructure.config;

import com.almagest_dev.tacobank_auth_server.auth.infrastructure.persistence.TokenBlackList;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication.CustomAuthenticationFilter;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication.JwtAuthenticationFilter;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication.JwtProvider;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler.CustomAccessDeniedHandler;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler.CustomAuthenticationEntryPoint;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler.CustomLogoutSuccessHandler;
import com.almagest_dev.tacobank_auth_server.common.util.RedisSessionUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
//@EnableWebSecurity(debug = true)
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final RedisSessionUtil redisSessionUtil;
    private TokenBlackList tokenBlackList;

    private static final String[] PUBLIC_API_URL = { "/taco/auth/login", "/taco/auth/members", "/taco/auth/email" }; // 인증 없이도 접근 가능한 경로
    private static final String ADMIN_API_URL = "/taco/admin/**"; // 관리자만 접근 가능한 경로

    public SecurityConfig(JwtProvider jwtProvider, RedisSessionUtil redisSessionUtil, TokenBlackList tokenBlackList) {
        this.jwtProvider = jwtProvider;
        this.redisSessionUtil = redisSessionUtil;
        this.tokenBlackList = tokenBlackList;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, tokenBlackList);
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter("/taco/auth/login", authenticationManager, jwtProvider, redisSessionUtil);

        http
                .csrf((csrf) -> csrf.disable()) // CSRF 보호 비활성화
                .cors((cors) -> cors.configurationSource(CorsConfig.corsConfigurationSource())) // CORS 설정
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PUBLIC_API_URL).permitAll() // 인증 없이 접근 가능한 경로
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS 요청 허용
                        .requestMatchers(ADMIN_API_URL).hasRole("ADMIN") // Admin 페이지 권한 제한
                        .anyRequest().authenticated()) // 이외 요청은 모두 인증 확인
                .exceptionHandling((e) -> e
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증되지 않은 사용자 접근 혹은 유효한 인증정보 부족한 경우(401 Unauthorized)
                        .accessDeniedHandler(new CustomAccessDeniedHandler()) // 403 Forbidden
                )
                .anonymous((anonymous) -> anonymous.disable()) // 익명 인증 비활성화
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, LogoutFilter.class) // JwtAuthenticationFilter가 먼저 실행
                .addFilterAfter(customAuthenticationFilter, JwtAuthenticationFilter.class)
                .logout((logout) -> logout
                        .logoutUrl("/taco/auth/logout") // 로그아웃 요청 URL
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(jwtProvider, tokenBlackList)) // 로그아웃 성공 핸들러
                        .deleteCookies("Authorization") // Authorization 쿠키 삭제
                        // .invalidateHttpSession(true) // 세션 무효화 (STATELESS 설정이므로 거의 의미 없음)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
