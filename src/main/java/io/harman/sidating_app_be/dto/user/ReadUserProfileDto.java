package io.harman.sidating_app_be.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadUserProfileDto {
    private UUID id;
    private String name;
    private String nickname;
    private String role;
    private LocalDate birthdate;
    private Integer age;
    private String ageGroup;
    private String hobbies;
    private String gender;
    private String location;
    private String bio;
    private String email;
    private String phoneNumber;
    private String interests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}