package io.harman.sidating_app_be.dto.user;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;

import jakarta.validation.constraints.Past;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Nickname is required")
    private String nickname;

    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    @NotEmpty(message = "Hobbies is required")
    private String hobbies;

    @NotEmpty(message = "Gender is required")
    private String gender;

    @NotEmpty(message = "Location is required")
    private String location;

    @NotEmpty(message = "Bio is required")
    private String bio;

    @NotEmpty(message = "Email is required")
    private String email;

    @NotEmpty(message = "Phone number is required")
    private String phoneNumber;

    @NotEmpty(message = "Interests is required")
    private String interests;

    private Boolean isActive = true;

}
