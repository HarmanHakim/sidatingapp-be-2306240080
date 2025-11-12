package io.harman.sidating_app_be.restdto.external;
 
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private UUID id;
    private UUID userProfileId;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}