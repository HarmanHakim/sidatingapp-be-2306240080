package io.harman.sidating_app_be.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import io.harman.sidating_app_be.model.UserProfile;
import java.util.List;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    List<UserProfile> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    List<UserProfile> findByDeletedAtIsNull();
}
