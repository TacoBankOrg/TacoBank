package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.LoginRequestDTO;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.LoginResponseDto;
import com.almagest_dev.tacobank_auth_server.common.constants.RedisKeyConstants;
import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import com.almagest_dev.tacobank_auth_server.common.exception.RedisSessionException;
import com.almagest_dev.tacobank_auth_server.common.util.RedisSessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final JwtProvider jwtProvider;
    private final RedisSessionUtil redisSessionUtil;

    public CustomAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, JwtProvider jwtProvider, RedisSessionUtil redisSessionUtil) {
        super(defaultFilterProcessesUrl, authenticationManager);
        this.jwtProvider = jwtProvider;
        this.redisSessionUtil = redisSessionUtil;
        log.info("CustomAuthenticationFilter START");
    }

    /**
     * @param request  from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     *                 redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

        // 이메일과 비밀번호를 Request Attribute에 저장
        request.setAttribute("email", loginRequest.getEmail());
        request.setAttribute("password", loginRequest.getPassword());

        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {
            // Redis에서 계정 잠금 상태 확인
            if (redisSessionUtil.isLocked(username)) {
                ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new AuthResponseDto<>("UNAUTHORIZED", "계정이 잠겨 있습니다. 10분 후 다시 시도하거나 고객센터에 문의해주세요."));
                return null;
            }
        } catch (RedisSessionException ex) {
            log.warn("RedisSessionException - " + ex.getMessage());
            int httpStatus = ex.getHttpStatus().value();
            String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

            ResponseWriter.writeExceptionResponse(response, httpStatus, new AuthResponseDto<>("FAILURE", message));
            return null;
        }

        log.info("CustomAuthenticationFilter::attemptAuthentication - username: " + username);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password); // 인증되지 않은 상태
        return getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 인증 성공시
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();

        try {
            // Redis에서 실패 횟수 초기화
            redisSessionUtil.cleanupRedisKeys("CustomAuthenticationFilter", username, RedisKeyConstants.FAILURE_PREFIX);
        }  catch (RedisSessionException ex) {
            log.warn("RedisSessionException - " + ex.getMessage());
            int httpStatus = ex.getHttpStatus().value();
            String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

            ResponseWriter.writeExceptionResponse(response, httpStatus, new AuthResponseDto<>("FAILURE", message));
            return;
        }

        // SecurityContextHolder에 Authenticaton 객체 자동 세팅
        // CustomUserDetails 에서 멤버 ID 추출
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        Long memberId = userDetails.getMemberId();

        // 로그인 성공시 JWT 토큰 생성 & 쿠키 세팅
        String token = jwtProvider.createToken(authResult, memberId);
        log.info("CustomAuthenticationFilter::successfulAuthentication - token: " + token);

        Cookie authorizationCookie = new Cookie("Authorization", token);
        authorizationCookie.setHttpOnly(true);
        authorizationCookie.setMaxAge(60 * 10); // 10분
        authorizationCookie.setPath("/"); // 모든 경로에서 쿠키가 유효하도록 설정

        response.addCookie(authorizationCookie);
        response.addHeader("Authorization", "Bearer " + token);

        // 로그인 성공 응답 반환
        LoginResponseDto responseDto = new LoginResponseDto(memberId, userDetails.getMydataLinked());
        log.info("memberId: {}, getMydataLinked: {}" , memberId, userDetails.getMydataLinked());

        ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, new AuthResponseDto<>("SUCCESS", "로그인에 성공했습니다!", responseDto));
    }

    /**
     * 인증 실패시
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("CustomAuthenticationFilter::unsuccessfulAuthentication - Authentication failed: " + failed.getMessage());

        // 비밀번호 실패
        if (failed instanceof BadCredentialsException) {
            // Request Attribute에서 email 가져옴
            String username = (String) request.getAttribute("email");
            String redisKey = RedisKeyConstants.FAILURE_PREFIX + username; // 실패 횟수 키

            try {
                // 실패 횟수 증가
                Long failureCount = redisSessionUtil.incrementIfExists(redisKey, 1L, 10, TimeUnit.MINUTES); // TTL 10분 설정
                log.info("CustomAuthenticationFilter::unsuccessfulAuthentication - Failure count for {}: {}", username, failureCount);

                // 실패 횟수가 5회 이상일 경우 계정 잠금 처리
                if (failureCount >= 5) {
                    redisSessionUtil.lockAccess(username, 10, TimeUnit.MINUTES); // 계정 잠금 상태 저장

                    log.warn("CustomAuthenticationFilter::unsuccessfulAuthentication - Account locked for {}", username);
                    ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_FORBIDDEN, new AuthResponseDto<>("FAILURE", "비밀번호 입력이 5회 이상 실패하여 계정이 10분간 잠겼습니다. 10분 후 다시 시도하거나 고객센터에 문의해주세요."));
                    return;
                }

            } catch (RedisSessionException ex) {
                log.warn("RedisSessionException - " + ex.getMessage());
                int httpStatus = ex.getHttpStatus().value();
                String message = (ex.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) ? "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요." : ex.getMessage();

                ResponseWriter.writeExceptionResponse(response, httpStatus, new AuthResponseDto<>("FAILURE", message));
                return;
            }
        }

        // 실패 메시지 반환
        ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new AuthResponseDto<>("UNAUTHORIZED", "아이디 또는 비밀번호가 잘못되었습니다. 다시 시도해주세요."));
    }
}
