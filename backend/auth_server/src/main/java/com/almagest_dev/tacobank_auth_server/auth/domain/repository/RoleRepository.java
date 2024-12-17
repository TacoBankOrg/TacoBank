package com.almagest_dev.tacobank_auth_server.auth.domain.repository;

import com.almagest_dev.tacobank_auth_server.auth.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
