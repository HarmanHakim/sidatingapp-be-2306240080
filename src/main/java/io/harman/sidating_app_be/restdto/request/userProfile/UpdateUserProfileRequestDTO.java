package io.harman.sidating_app_be.restdto.request.userProfile;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateUserProfileRequestDTO {
    
    @NotNull(message = "ID is required")
    private UUID id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Nickname is required")
    private String nickname;
    
    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    
    @Size(max = 500, message = "Hobbies must not exceed 500 characters")
    private List<String> hobbies;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @Size(max = 10, message = "Maximum 10 interests allowed")
    private List<String> interests;
    
    private boolean isActive;
}
