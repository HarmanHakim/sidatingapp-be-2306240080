package io.harman.sidating_app_be.dto.post;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {

    @NotNull(message = "UserProfileId is required")
    private UUID userProfileId;

    @NotEmpty(message = "ImageUrl is required")
    private String imageUrl;

    @NotEmpty(message = "Caption is required")
    private String caption;


}


