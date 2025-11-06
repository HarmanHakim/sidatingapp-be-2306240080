package io.harman.sidating_app_be.restdto.response.userProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserProfileResponseDTO {
    private UUID id;
    private String name;
    private String nickname;
    private LocalDate birthdate;
    private Integer age; // hitung dari birthdate
    private String ageGroup; // kategori umur (18-25, 26-35, 36-45, 45+)
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
