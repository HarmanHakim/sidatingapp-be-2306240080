package io.harman.sidating_app_be.restdto.request.reply;
 
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReplyRequestDTO {
    @NotNull(message = "User profile ID is required")
    private UUID userProfileId;
 
    @NotBlank(message = "Content is required")
    private String content;
}
