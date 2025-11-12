package io.harman.sidating_app_be.restdto.response.security;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginJwtResponseDTO {
    private String token;
    private UUID id;
    private String username;
    private String email;
    private String name;
    private String roleName;
    private String nickname;
}
