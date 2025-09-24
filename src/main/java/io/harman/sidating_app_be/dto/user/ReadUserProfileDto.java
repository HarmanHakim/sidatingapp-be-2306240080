package io.harman.sidating_app_be.dto.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadUserProfileDto {
    private UUID id;
    private String name;
    private String nickname;
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
