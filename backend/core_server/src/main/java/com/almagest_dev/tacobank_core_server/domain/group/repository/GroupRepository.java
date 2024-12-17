package com.almagest_dev.tacobank_core_server.domain.group.repository;


import com.almagest_dev.tacobank_core_server.domain.group.model.Group;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByLeaderIdAndActivated(Long leaderId, String activated);
    Optional<Group> findByNameAndActivated(String name, String activated);
    Optional<Group> findByIdAndLeaderId(Long groupId, Long leaderId);

    @Query("SELECT DISTINCT g FROM Group g " +
            "JOIN g.payGroups pg " +
            "WHERE pg.member.id = :memberId AND pg.status = 'ACCEPTED'")
    List<Group> findGroupsByAcceptedMember(@Param("memberId") Long memberId);

}
