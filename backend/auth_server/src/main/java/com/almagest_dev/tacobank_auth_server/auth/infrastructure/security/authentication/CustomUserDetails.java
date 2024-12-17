package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;


public class CustomUserDetails implements UserDetails {
    private Long memberId; // 멤버 ID
    private String email;
    private String password;
    private String role; // 멤버 권한
    private String deleted; // 탈퇴 여부
    private String mydataLinked; // 최초 계좌 연동 여부
    private Collection<GrantedAuthority> authorities; //권한 목록

    public CustomUserDetails(Long memberId, String email, String password, String deleted, String mydataLinked, String role) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.deleted = deleted;
        this.mydataLinked = mydataLinked;
        this.role = role;
        this.authorities = createAuthority(role);
    }

    private Collection<GrantedAuthority> createAuthority(String role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role)); // Role을 Authority로 설정
        return authorities;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMydataLinked() {
        return mydataLinked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 계정 잠김 여부
     * true : 잠기지 않음
     * false : 잠김
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return deleted != null && !deleted.equals('Y');

    }
}
