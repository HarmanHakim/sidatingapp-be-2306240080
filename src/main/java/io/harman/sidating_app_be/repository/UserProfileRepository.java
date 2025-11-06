package io.harman.sidating_app_be.repository;

import io.harman.sidating_app_be.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    List<UserProfile> findByNameContainingIgnoreCase(String name);
    UserProfile findByUsername(String username);
}
