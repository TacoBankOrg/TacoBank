package com.taco_bank.business_server.domain.group.repository;

import com.taco_bank.business_server.domain.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
