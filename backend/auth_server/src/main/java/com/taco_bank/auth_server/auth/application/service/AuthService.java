package com.taco_bank.auth_server.auth.application.service;

import com.taco_bank.auth_server.auth.application.dto.SignupRequestDTO;
import com.taco_bank.auth_server.auth.domain.model.Member;
import com.taco_bank.auth_server.auth.domain.model.Role;
import com.taco_bank.auth_server.auth.domain.repository.MemberRepository;
import com.taco_bank.auth_server.auth.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerMember(SignupRequestDTO requestDTO) {
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        if (memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")) { // 삭제된 계정이 아닌데 동일 전화번호가 있는 경우
            throw new IllegalArgumentException("해당 전화번호로 이미 등록된 계정이 있습니다. 로그인 해주세요.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        // Role 세팅
        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.findById(1L)
                        .orElseThrow(()-> new IllegalStateException("시스템 오류가 발생했습니다. 관리자에게 문의하세요.")))
                ;
        // 멤버 저장
        Member member = Member.createMember(requestDTO.getEmail(), encodedPassword, requestDTO.getName(), requestDTO.getTel(), role);
        memberRepository.save(member);
    }
}
