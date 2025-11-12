package io.harman.sidating_app_be.restdto.request.post;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreatePostRequestDTO {

    @NotBlank(message = "Caption is required")
    private String caption;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private UUID userProfileId;
}