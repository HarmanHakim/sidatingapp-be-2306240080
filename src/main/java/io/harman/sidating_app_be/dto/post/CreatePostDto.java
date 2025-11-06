package io.harman.sidating_app_be.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {
    @NotNull(message = "User profile ID is required")
    private UUID userProfileId;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Size(max = 500, message = "Caption must be less than 500 characters")
    private String caption;
}