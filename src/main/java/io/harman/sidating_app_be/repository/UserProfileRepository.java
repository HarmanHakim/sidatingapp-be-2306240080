package io.harman.sidating_app_be.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.harman.sidating_app_be.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    List<UserProfile> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    List<UserProfile> findByDeletedAtIsNull();

    List<UserProfile> findByNameContainingIgnoreCase(String name);
}
