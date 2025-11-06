package io.harman.sidating_app_be.restService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.RoleRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;

@Service
public class UserProfileRestServiceImpl implements UserProfileRestService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponseDTO createUserProfile(AddUserProfileRequestDTO dto) {
        if (userProfileRepository.findByUsername(dto.getUsername()) != null) {
            return null;
        }
        
        // Determine role: if roleName is not provided or empty, default to "User"
        String roleName = (dto.getRoleName() == null || dto.getRoleName().trim().isEmpty()) ? "User" : dto.getRoleName();
        
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
        
        UserProfile userProfile = UserProfile.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .name(dto.getName())
                .nickname(dto.getNickname())
                .birthdate(dto.getBirthdate())
                .hobbies(convertListToString(dto.getHobbies()))
                .gender(dto.getGender())
                .location(dto.getLocation())
                .bio(dto.getBio())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .interests(convertListToString(dto.getInterests()))
                .isActive(true)
                .build();

        return convertToUserProfileResponseDTO(userProfileRepository.save(userProfile));
    }

    @Override
    public List<UserProfileResponseDTO> getAllUserProfile() {
        List<UserProfile> allUserProfiles = userProfileRepository.findAll();

        return allUserProfiles.stream()
                .map(userProfile -> convertToUserProfileResponseDTO(userProfile))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponseDTO> searchUserProfileByName(String name) {
        List<UserProfile> userProfiles;

        // If search term is empty or null, return all profiles
        if (name == null || name.trim().isEmpty()) {
            userProfiles = userProfileRepository.findAll();
        } else {
            // Search by name containing (case-insensitive)
            userProfiles = userProfileRepository.findByNameContainingIgnoreCase(name.trim());
        }

        // Convert to DTOs
        return userProfiles.stream()
                .map(userProfile -> convertToUserProfileResponseDTO(userProfile))
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileResponseDTO getUserProfile(UUID id) {
        UserProfile userProfile = userProfileRepository.findById(id).orElse(null);
        if (userProfile == null) {
            return null;
        }
        return convertToUserProfileResponseDTO(userProfile);
    }

    @Override
    public UserProfileResponseDTO updateUserProfile(UpdateUserProfileRequestDTO updateUserDto) {
        UserProfile userProfile = userProfileRepository.findById(updateUserDto.getId()).orElse(null);

        if (userProfile == null) return null;

        userProfile = userProfile.toBuilder()
                .id(updateUserDto.getId())
                .name(updateUserDto.getName())
                .nickname(updateUserDto.getNickname())
                .birthdate(updateUserDto.getBirthdate())
                .hobbies(convertListToString(updateUserDto.getHobbies()))
                .gender(updateUserDto.getGender())
                .location(updateUserDto.getLocation())
                .bio(updateUserDto.getBio())
                .email(updateUserDto.getEmail())
                .phoneNumber(updateUserDto.getPhoneNumber())
                .interests(convertListToString(updateUserDto.getInterests()))
                .isActive(updateUserDto.isActive())
                .build();

        return convertToUserProfileResponseDTO(userProfileRepository.save(userProfile));
    }

    @Override
    public UserProfileResponseDTO deleteUserProfile(UUID id) {
        UserProfile userProfile = userProfileRepository.findById(id).orElse(null);
        if (userProfile == null) {
            return null;
        }

        // Soft delete
        userProfile.setDeletedAt(LocalDateTime.now());
        return convertToUserProfileResponseDTO(userProfileRepository.save(userProfile));
    }

    private String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(", ", list);
    }

    private UserProfileResponseDTO convertToUserProfileResponseDTO(UserProfile userProfile) {
        Integer age = null;
        String ageGroup = null;
        if (userProfile.getBirthdate() != null) {
            age = Period.between(userProfile.getBirthdate(), LocalDate.now()).getYears();

            if (age >= 18 && age <= 25) {
                ageGroup = "18-25";
            } else if (age >= 26 && age <= 35) {
                ageGroup = "26-35";
            } else if (age >= 36 && age <= 45) {
                ageGroup = "36-45";
            } else if (age > 45) {
                ageGroup = "45+";
            } else {
                ageGroup = "Under 18";
            }
        }

        return UserProfileResponseDTO.builder()
                .id(userProfile.getId())
                .name(userProfile.getName())
                .nickname(userProfile.getNickname())
                .birthdate(userProfile.getBirthdate())
                .age(age)
                .ageGroup(ageGroup)
                .hobbies(userProfile.getHobbies())
                .gender(userProfile.getGender())
                .location(userProfile.getLocation())
                .bio(userProfile.getBio())
                .email(userProfile.getEmail())
                .phoneNumber(userProfile.getPhoneNumber())
                .interests(userProfile.getInterests())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .isActive(userProfile.isActive())
                .build();
    }
}

