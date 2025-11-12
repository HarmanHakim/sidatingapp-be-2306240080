package io.harman.sidating_app_be.restdto.response.reply;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.UUID;

import io.harman.sidating_app_be.restdto.external.PostDTO;
import io.harman.sidating_app_be.restdto.external.UserProfileDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponseDTO {
    private UUID id;
    private UUID postId;
    private UUID userProfileId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // Data from BE1
    private UserProfileDTO userProfile;
    private PostDTO post;
}