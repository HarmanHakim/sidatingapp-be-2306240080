package io.harman.sidating_app_be.restdto.request.post;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequestDTO {

    @NotNull(message = "Id is required")
    private UUID id;
    
    private String imageUrl;
    
    private String caption;
}
