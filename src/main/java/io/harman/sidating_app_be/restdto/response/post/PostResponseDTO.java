package io.harman.sidating_app_be.restdto.response.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private UUID id;
    private UUID userProfileId;
    private String userProfileName; // Nama user yang membuat post
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private List<String> likes;
    private Integer likeCount; // Jumlah like
    private String timeAgo; // (hours ago, days ago, weeks ago, years ago)
}
