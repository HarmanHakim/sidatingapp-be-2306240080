package io.harman.sidating_app_be.repository;

import io.harman.sidating_app_be.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUserProfileIdOrderByCreatedAtDesc(UUID userProfileId);
    List<Post> findByUserProfileIdOrderByCreatedAtAsc(UUID userProfileId);
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findAllByOrderByCreatedAtAsc();
    List<Post> findByUserProfileId(UUID userProfileId);
    List<Post> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Post> findByUserProfileIdAndCreatedAtBetween(UUID userProfileId, LocalDateTime start, LocalDateTime end);
}
