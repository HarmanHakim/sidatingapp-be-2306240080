package io.harman.sidating_app_be.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadPostDto {
    private UUID id;
    private UUID userProfileId;
    private String userProfileName;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private List<String> likes;
    private Integer likeCount;
    private String timeAgo;

    private Boolean isLikedByCurrentUser; 

}