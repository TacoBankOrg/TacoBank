package com.taco_bank.business_server.domain.group.repository;

import com.taco_bank.business_server.domain.group.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
}
