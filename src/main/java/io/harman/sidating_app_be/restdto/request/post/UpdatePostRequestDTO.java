package io.harman.sidating_app_be.restdto.request.post;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdatePostRequestDTO {

    @NotBlank(message = "Caption is required")
    private String caption;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Post ID tidak boleh kosong")
    private UUID id;
}
