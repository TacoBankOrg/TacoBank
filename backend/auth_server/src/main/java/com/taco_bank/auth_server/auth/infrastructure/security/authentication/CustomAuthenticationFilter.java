package com.taco_bank.auth_server.auth.infrastructure.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco_bank.auth_server.auth.application.dto.LoginRequestDTO;
import com.taco_bank.auth_server.common.exception.ExceptionResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final JwtProvider jwtProvider;

    public CustomAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(defaultFilterProcessesUrl, authenticationManager);
        this.jwtProvider = jwtProvider;
        log.info("CustomAuthenticationFilter START");
    }

    /**
     *
     * @param request from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     * redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        log.info("CustomAuthenticationFilter::attemptAuthentication - username: " + username + " | password : " + password);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password); // 인증되지 않은 상태
        log.info("CustomAuthenticationFilter::attemptAuthentication - authRequest");
        return getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 인증 성공시
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // SecurityContextHolder에 Authenticaton 객체 자동 세팅
        // 로그인 성공시 JWT 토큰 생성
        String token = jwtProvider.createToken(authResult);
        log.info("CustomAuthenticationFilter::successfulAuthentication - token: " + token);

        response.addHeader("Authorization", "Bearer " + token);
    }

    /**
     * 인증 실패시
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("CustomAuthenticationFilter::unsuccessfulAuthentication - Authentication failed: " + failed.getMessage());

        // 실패 메시지 반환
        String errorMessage = "아이디 또는 비밀번호가 잘못되었습니다. 다시 시도해주세요.";
        ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", errorMessage);
    }
}
