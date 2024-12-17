package com.almagest_dev.tacobank_auth_server;

import com.almagest_dev.tacobank_auth_server.auth.application.service.AuthService;
import com.almagest_dev.tacobank_auth_server.auth.domain.model.Role;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.MemberRepository;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.RoleRepository;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.SignupRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService; // 테스트 대상 클래스

    @Mock
    private MemberRepository memberRepository; // Mock Repository

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Role roleUser = new Role();
        roleUser.setRoleName("ROLE_USER");

        // Mock 동작 설정 (lenient 사용)
        lenient().when(roleRepository.findByRoleName("ROLE_USER"))
                .thenReturn(Optional.of(roleUser));
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerMemberSuccess() {
        // Given: 회원가입 요청 데이터
        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "test@example.com",
                "John Doe",
                "900101",
                "securePassword1!",
                "01012345678"
        );

        // Mock 동작 설정
        when(memberRepository.existsByEmailAndDeleted(requestDTO.getEmail(), "N")).thenReturn(false);
        when(memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")).thenReturn(false);

        Role mockRole = new Role();
        mockRole.setRoleName("ROLE_USER");
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(mockRole));

        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encodedPassword");

        // When: 회원가입 메서드 실행
        authService.registerMember(requestDTO);

        // Then: 저장 동작 검증
        verify(memberRepository, times(1)).save(argThat(member ->
                member.getEmail().equals("test@example.com") &&
                        member.getTel().equals("01012345678") &&
                        member.getName().equals("John Doe") &&
                        member.getRole().getRoleName().equals("ROLE_USER")
        ));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerMemberFailDueToDuplicateEmail() {
        // Given
        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "test@example.com",
                "John Doe",
                "900101",
                "securePassword1!",
                "01012345678"
        );

        // Mock 동작 설정: 이메일 중복
        when(memberRepository.existsByEmailAndDeleted(requestDTO.getEmail(), "N")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerMember(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 이메일 입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 중복")
    void registerMemberFailDueToDuplicateTel() {
        // Given
        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "test@example.com",
                "John Doe",
                "900101",
                "securePassword1!",
                "01012345678"
        );

        // Mock 동작 설정
        when(memberRepository.existsByEmailAndDeleted(requestDTO.getEmail(), "N")).thenReturn(false);
        when(memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerMember(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 전화번호로 이미 등록된 계정이 있습니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - Role 없음")
    void registerMemberFailDueToMissingRole() {
        // Given
        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "test@example.com",
                "John Doe",
                "900101",
                "securePassword1!",
                "01012345678"
        );

        // Mock 동작 설정: Role이 조회되지 않음
        when(memberRepository.existsByEmailAndDeleted(requestDTO.getEmail(), "N")).thenReturn(false);
        when(memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")).thenReturn(false);
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.registerMember(requestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("시스템 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 유효성 검증 실패")
    void registerMemberFailDueToInvalidPassword() {
        // Given: 비밀번호가 유효하지 않은 회원가입 요청 데이터
        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "test@example.com",
                "John Doe",
                "900101",
                "short", // 비밀번호가 8자 미만
                "01012345678"
        );

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> authService.registerMember(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 최소 8자 이상이어야 합니다.");
    }
}