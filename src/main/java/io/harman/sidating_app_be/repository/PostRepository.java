package io.harman.sidating_app_be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.harman.sidating_app_be.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByUserProfileIdAndDeletedAtIsNull(UUID userId);

    List<Post> findByDeletedAtIsNull();

    List<Post> findAllByDeletedAtIsNull();

    List<Post> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Post> findByUserProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(UUID userId, LocalDateTime startOfDay,
            LocalDateTime endOfDay);

    Optional<Post> findByIdAndDeletedAtIsNull(UUID id);

    List<Post> findByUserProfileId(UUID userId);

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findAllByOrderByCreatedAtAsc();

    long countByDeletedAtIsNull();

    long countByUserProfileIdAndDeletedAtIsNull(UUID userProfileId);

    List<Post> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Post> findByUserProfileIdAndCreatedAtBetween(UUID userProfileId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}