package io.harman.sidating_app_be.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor 
@AllArgsConstructor 
@Entity
@Table(name = "posts")
public class Post {
    @Id
    private UUID id;

    @Column (name = "userProfileId", nullable = false)
    private UUID userProfileId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "id_user", referencedColumnName = "id")
    private UserProfile userProfile;

    @Column (name = "imageUrl", nullable = false)
    private String imageUrl;

    @Column (name = "caption")
    private String caption;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; 

    @Column (name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany (fetch = FetchType.LAZY)

    @JoinTable(
        name = "likes",
        joinColumns = @JoinColumn (name = "post_id"),
        inverseJoinColumns = @JoinColumn (name = "user_profile_id")
    )
    private List<UserProfile> likes;

    @Column (name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column (name = "is_active")
    private boolean isActive;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
    }
}
