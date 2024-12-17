package com.almagest_dev.tacobank_core_server.domain.account.repository;

import com.almagest_dev.tacobank_core_server.domain.account.model.Account;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdAndVerified(Long id, String verified);
    long countByMember(Member member);
    Optional<Account> findByIdAndMember(Long id, Member member);
    Optional<Account> findByAccountNum(String accountNum);
    List<Account> findByMember_Id(Long memberId);

    List<Account> findByMember_IdAndVerified(Long memberId, String verified); // 특정 멤버의 출금 가능한 계좌 조회



}
