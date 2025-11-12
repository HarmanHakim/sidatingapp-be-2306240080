package io.harman.sidating_app_be.restdto.external;
 
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private UUID id;
    private String username;
    private String name;
    private String nickname;
    private LocalDate birthdate;
    private String hobbies;
    private String gender;
    private String role;
    private String location;
    private String bio;
    private String email;
    private String phoneNumber;
    private String interests;
    private boolean isActive;
}