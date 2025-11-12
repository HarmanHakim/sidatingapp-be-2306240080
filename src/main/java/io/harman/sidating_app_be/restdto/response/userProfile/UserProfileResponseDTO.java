package io.harman.sidating_app_be.restdto.response.userProfile;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
	private UUID id;
	private String name;
	private String nickname;
	private String role;
	private LocalDate birthdate;
	private Integer age; // Dihitung dari birthdate
	private String ageGroup; // Kategoni umur (18-25, 26-35, 36-45, 45+)
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