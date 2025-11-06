package io.harman.sidating_app_be.restdto.request.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletePostRequestDTO {
    
    @NotNull(message = "Post ID tidak boleh kosong")
    private UUID id;
}