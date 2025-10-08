package io.harman.sidating_app_be.restdto.request.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikePostRequestDTO {
    @NotNull(message = "User Profile ID is required")
    private UUID userProfileId;

    @NotNull(message = "Post ID is required")
    private UUID id;
}
