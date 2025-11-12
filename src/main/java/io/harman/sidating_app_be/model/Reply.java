package io.harman.sidating_app_be.model;
 
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "replies")
@SQLDelete(sql = "UPDATE replies SET deleted_at = NOW() WHERE id=?")
@Where(clause = "deleted_at IS NULL")
public class Reply {
    @Id
    @GeneratedValue(generator = "system-uuid")
    private UUID id;
 
    @Column(name = "post_id", nullable = false)
    private UUID postId;
 
    @Column(name = "user_profile_id", nullable = false)
    private UUID userProfileId;
 
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
 
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
 
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
 
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
 