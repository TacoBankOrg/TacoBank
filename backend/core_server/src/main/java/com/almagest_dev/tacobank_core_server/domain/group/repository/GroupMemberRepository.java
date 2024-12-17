package com.almagest_dev.tacobank_core_server.domain.group.repository;


import com.almagest_dev.tacobank_core_server.domain.group.model.Group;
import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {



    Optional<GroupMember> findByPayGroupIdAndMemberId(Long groupId, Long memberId);
    List<GroupMember> findByMemberIdAndStatus(Long memberId, String status);
    List<GroupMember> findByPayGroupAndStatus(Group payGroup, String status);
    List<GroupMember> findByPayGroupId(Long groupId);
    List<GroupMember> findByPayGroup(Group group);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.payGroup = :payGroup AND gm.member.id = :memberId")
    Optional<GroupMember> findByPayGroupAndMemberId(@Param("payGroup") Group payGroup, @Param("memberId") Long memberId);

}
