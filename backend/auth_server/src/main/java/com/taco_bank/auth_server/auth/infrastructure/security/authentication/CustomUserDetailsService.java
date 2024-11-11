package com.taco_bank.auth_server.auth.infrastructure.security.authentication;

import com.taco_bank.auth_server.auth.domain.model.Member;
import com.taco_bank.auth_server.auth.domain.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Username: Email
     * @param username the username identifying the user whose data is required.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));
        return new CustomUserDetails(member.getEmail(), member.getPassword(), member.getDeleted(), member.getRole().getRoleName());
    }
}
