package io.harman.sidating_app_be.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.harman.sidating_app_be.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    List<UserProfile> findByDeletedAtIsNull();

    Optional<UserProfile> findByIdAndDeletedAtIsNull(UUID id);

    List<UserProfile> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    Optional<UserProfile> findById(UUID id);

    List <UserProfile> findByNameContainingIgnoreCase(String name);

}