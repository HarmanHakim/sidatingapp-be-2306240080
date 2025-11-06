package io.harman.sidating_app_be.restdto.request.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginJwtRequestDTO {
    private String username;
    private String password;
}
