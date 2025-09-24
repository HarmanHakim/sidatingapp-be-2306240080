package io.harman.sidating_app_be.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.UUID;

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


