package io.harman.sidating_app_be.restdto.request.post;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequestDTO {

    @NotNull(message = "User Profile ID is required")
    private UUID userProfileId;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    @NotBlank(message = "Caption is required")
    private String caption;
    
    
}