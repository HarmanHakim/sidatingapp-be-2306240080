package io.harman.sidating_app_be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.harman.sidating_app_be.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
