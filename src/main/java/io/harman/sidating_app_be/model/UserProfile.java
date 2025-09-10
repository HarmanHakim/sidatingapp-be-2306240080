package io.harman.sidating_app_be.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private UUID id;
    private String name;
    private String nickname;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    private String hobbies;
    private String gender;
    private String location;
    private String bio;
    private String email;
    private String phoneNumber;
    private String interests;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private boolean isActive;
}