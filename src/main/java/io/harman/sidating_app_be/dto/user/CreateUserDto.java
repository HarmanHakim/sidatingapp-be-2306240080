package io.harman.sidating_app_be.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Nickname is required")
    private String nickname;

    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    private LocalDate birthdate;

    @NotBlank(message = "Hobbies are required")
    private String hobbies;
    
    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Location is required")
    private String location;

    @Size(max = 255, message = "Bio must be less than 255 characters")
    private String bio;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Interests are required")
    private String interests;
}