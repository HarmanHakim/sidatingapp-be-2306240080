package io.harman.sidating_app_be.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import io.harman.sidating_app_be.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByDeletedAtIsNull();

    List<Post> findByUserProfileIdAndDeletedAtIsNull(UUID userProfileId);
}
