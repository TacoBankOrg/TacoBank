package com.taco_bank.business_server.domain.member.repository;

import com.taco_bank.business_server.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
