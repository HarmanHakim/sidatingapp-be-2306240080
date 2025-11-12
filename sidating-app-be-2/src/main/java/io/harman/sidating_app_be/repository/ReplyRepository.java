package io.harman.sidating_app_be.repository;
 
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.harman.sidating_app_be.model.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, UUID> {
    List<Reply> findByPostId(UUID postId);
    List<Reply> findByUserProfileId(UUID userProfileId);
    List<Reply> findByPostIdOrderByCreatedAtDesc(UUID postId);
}