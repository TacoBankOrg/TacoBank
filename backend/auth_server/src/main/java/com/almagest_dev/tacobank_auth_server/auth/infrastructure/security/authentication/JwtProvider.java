package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 토큰 생성
     */
    public String createToken(Authentication authentication, Long memberId) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 권한을 클레임에 추가
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("ROLES", roles)
                .claim("memberId", memberId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check Member ID
            Long memberId = claims.get("memberId", Long.class);
            if (memberId == null) {
                log.warn("JwtProvider::validateToken member ID is null");
                return false;
            }

            return true;
        } catch (JwtException | IllegalStateException exception) {
            log.warn("JwtProvider::validateToken 유효하지 않은 토큰: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, getAuthoritiesFromToken(token));
    }

    /**
     * 토큰에서 사용자 이름(username = email) 추출
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰에서 클레임 추출
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 권한 정보 추출
     */
    public Collection<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        List<String> role = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("ROLES", List.class);

        return role.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 남은 만료시간 조회
     */
    public long getRemainingExpiration(String token) {
        try {
            Claims claims = getClaimsFromToken(token); // 토큰에서 클레임 추출
            long expirationTime = claims.getExpiration().getTime(); // 만료 시간 (밀리초)
            return expirationTime - System.currentTimeMillis(); // 남은 시간 계산
        } catch (Exception e) {
            log.error("JwtProvider - 만료 시간 계산 실패: {}", e.getMessage());
            return 0; // 실패 시 0 반환
        }
    }
}
