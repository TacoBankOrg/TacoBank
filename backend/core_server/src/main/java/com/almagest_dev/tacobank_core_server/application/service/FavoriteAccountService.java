package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.account.model.FavoriteAccount;
import com.almagest_dev.tacobank_core_server.domain.account.repository.AccountRepository;
import com.almagest_dev.tacobank_core_server.domain.account.repository.FavoriteAccountRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.FavoriteAccountRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.account.FavoriteAccountResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteAccountService {

    private final FavoriteAccountRepository favoriteAccountRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    /**
     * 즐겨찾기 계좌 설정
     */
    @Transactional
    public FavoriteAccountResponseDto setAndRetrieveFavoriteAccount(FavoriteAccountRequestDto requestDto) {
        // Member 조회
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // Account 조회
        Account account = accountRepository.findByAccountNum(requestDto.getAccountNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않습니다."));

        // 중복된 즐겨찾기 계좌인지 확인
        boolean isDuplicate = favoriteAccountRepository.existsByMemberAndAccountNum(member, account.getAccountNum());
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 즐겨찾기 계좌로 등록된 계좌입니다.");
        }

        Member accountOwner = memberRepository.findById(account.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("계좌 소유자가 존재하지 않습니다."));

        // FavoriteAccount 저장
        FavoriteAccount favoriteAccount = favoriteAccountRepository.findByMemberAndAccountNum(member, requestDto.getAccountNum())
                .orElse(new FavoriteAccount());
        favoriteAccount.saveMember(member);
        favoriteAccount.saveAccountNum(account.getAccountNum());
        favoriteAccount.saveAccountHolderName(accountOwner.getName()); // 소유자의 이름을 저장
        favoriteAccount.saveBankCode(account.getBankCode());
        favoriteAccountRepository.save(favoriteAccount);

        // 응답 생성
        return new FavoriteAccountResponseDto(
                member.getId(),
                favoriteAccount.getAccountNum(),
                favoriteAccount.getAccountHolderName(),
                favoriteAccount.getBankCode()
        );

    }

    /**
     * 즐겨찾기 계좌 목록 조회
     */
    @Transactional
    public List<FavoriteAccountResponseDto> getFavoriteAccounts(Long memberId) {
        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 즐겨찾기 계좌 리스트 조회
        List<FavoriteAccount> favoriteAccounts = favoriteAccountRepository.findAllByMember(member);

        // 응답 생성
        return favoriteAccounts.stream().map(favoriteAccount -> new FavoriteAccountResponseDto(
                member.getId(),
                favoriteAccount.getAccountNum(),
                favoriteAccount.getAccountHolderName(),
                favoriteAccount.getBankCode()
        )).collect(Collectors.toList());
    }

    /**
     * 즐겨찾기 계좌 취소
     */
    @Transactional
    public void cancelFavoriteAccount(FavoriteAccountRequestDto requestDto) {
        // Member 조회
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // FavoriteAccount 조회
        FavoriteAccount favoriteAccount = favoriteAccountRepository.findByMemberAndAccountNum(member, requestDto.getAccountNum())
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기에 등록되지 않은 계좌입니다."));

        // 즐겨찾기 계좌 삭제
        favoriteAccountRepository.delete(favoriteAccount);
    }


}