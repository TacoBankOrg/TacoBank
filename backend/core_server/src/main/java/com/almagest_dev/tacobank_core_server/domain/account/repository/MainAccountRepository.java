package com.almagest_dev.tacobank_core_server.domain.account.repository;

import com.almagest_dev.tacobank_core_server.domain.account.model.MainAccount;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface MainAccountRepository extends JpaRepository<MainAccount, Long> {
    Optional<MainAccount> findByMember(Member member);
    Optional<MainAccount> findByMemberId(Long memberId);

}
