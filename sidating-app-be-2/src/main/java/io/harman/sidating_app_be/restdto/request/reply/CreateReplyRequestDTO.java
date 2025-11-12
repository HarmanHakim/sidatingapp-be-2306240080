package io.harman.sidating_app_be.restdto.request.reply;
 
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequestDTO {
    @NotNull(message = "Post ID is required")
    private UUID postId;
 
    @NotNull(message = "User Profile ID is required")
    private UUID userProfileId;
 
    @NotBlank(message = "Content is required")
    private String content;
}