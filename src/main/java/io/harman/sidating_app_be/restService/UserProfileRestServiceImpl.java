package io.harman.sidating_app_be.restservice;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.RoleRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restdto.request.userprofile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userprofile.UserProfileResponseDTO;
import io.harman.sidating_app_be.security.jwt.JwtUtils;
import jakarta.annotation.PostConstruct;

@Service
public class UserProfileRestServiceImpl implements UserProfileRestService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private RoleRepository roleRepository;

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
                .password(hashPassword(dto.getPassword()))
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
                .isActive(dto.isActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return convertToUserProfileResponseDTO(userProfileRepository.save(userProfile));
    }
    @Override
    public List<UserProfileResponseDTO> getAllUserProfile() {
        UserProfile authUser = getAuthenticatedUser();
        if (!isAdmin(authUser)) {
            throw new SecurityException("You are not authorized to view all user profiles.");
        }
        return userProfileRepository.findAll()
                .stream()
                .map(this::convertToUserProfileResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponseDTO> searchUserProfilesByName(String name) {
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
        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(id);
        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("You are not authorized to view this specific profile.");
        }
        return convertToUserProfileResponseDTO(userProfile);
    }

@Override
public UserProfileResponseDTO updateUserProfile(UpdateUserProfileRequestDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(dto.getId()).orElse(null);
        if (userProfile == null) {
            return null;
        }
        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(userProfile.getId());
        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("You are not authorized to view this specific profile.");
        }
        Role role = roleRepository.findByRoleName(dto.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + dto.getRoleName()));
        userProfile = UserProfile.builder()
                .username(dto.getEmail())
                .password(hashPassword(dto.getPassword()))
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
                .isActive(dto.isActive())
                .updatedAt(LocalDateTime.now())
                .build();
        return convertToUserProfileResponseDTO(userProfileRepository.save(userProfile));
    }

    @Override
    public UserProfileResponseDTO deleteUserProfile(UUID id) {
            UserProfile userProfile = userProfileRepository.findById(id).orElse(null);
            if (userProfile == null) {
                return null;
            }
            UserProfile authUser = getAuthenticatedUser();
            if (!isAdmin(authUser)) {
                throw new SecurityException("You are not authorized to delete profiles.");
            }
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

    @PostConstruct
    public void initializeDefaultUsers() {
        // Initialize Admin role
        if (roleRepository.findByRoleName("Admin").orElse(null) == null) {
            Role role = new Role();
            role.setRoleName("Admin");
            roleRepository.save(role);
        }

        // Initialize User role
        if (roleRepository.findByRoleName("User").isEmpty()) {
            Role role = new Role();
            role.setRoleName("User");
            roleRepository.save(role);
        }

        // Initialize admin user
        if (userProfileRepository.findByUsername("admin") == null) {
            UserProfile user = new UserProfile();
            user.setName("Admin");
            user.setUsername("admin");
            user.setNickname("Admin");
            user.setEmail("admin@sidating-app");
            user.setPhoneNumber("0000000000");
            user.setPassword(hashPassword("admin123"));
            user.setRole(roleRepository.findByRoleName("Admin").orElse(null));
            userProfileRepository.save(user);
        }

        // Initialize default user
        if (userProfileRepository.findByUsername("user") == null) {
            UserProfile user = new UserProfile();
            user.setName("User");
            user.setUsername("user");
            user.setNickname("User");
            user.setEmail("user@sidating-app");
            user.setPhoneNumber("0000000001");
            user.setPassword(hashPassword("user123"));
            user.setRole(roleRepository.findByRoleName("User").orElse(null));
            userProfileRepository.save(user);
        }
    }

    public String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Autowired
    private JwtUtils jwtUtils;

    private UserProfile getAuthenticatedUser() {
        String currentUsername = jwtUtils.getCurrentUsername();
        UserProfile authUser = userProfileRepository.findByUsername(currentUsername);
        if (authUser == null) {
            throw new UsernameNotFoundException("Authenticated user not found.");
        }
        return authUser;
    }

    private boolean isAdmin(UserProfile user) {
        return user.getRole() != null && "Admin".equalsIgnoreCase(user.getRole().getRoleName());
    }
}