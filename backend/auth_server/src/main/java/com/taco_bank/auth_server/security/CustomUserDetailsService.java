package com.taco_bank.auth_server.security;

import com.taco_bank.auth_server.domain.Member;
import com.taco_bank.auth_server.repository.MemberRepository;
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
                .orElseThrow(() -> new UsernameNotFoundException(""));
        return new CustomUserDetails(member.getEmail(), member.getPassword(), member.getDeleted(), member.getRole().getRoleName());
    }
}
