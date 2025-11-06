package io.harman.sidating_app_be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.RoleRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.security.jwt.JwtUtils;
import jakarta.annotation.PostConstruct;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserProfile createUserProfile(CreateUserDto dto) {
        // Get or create default User role
        Role userRole = roleRepository.findByRoleName("User")
                .orElseThrow(() -> new RuntimeException("Default User role not found. Please initialize roles first."));

        // Generate default username and password based on email
        String username = dto.getEmail().split("@")[0];
        String password = passwordEncoder.encode("password123"); // Default password

        UserProfile userProfile = UserProfile.builder()
                // DO NOT set ID manually - let Hibernate generate it
                .username(username)
                .password(password)
                .role(userRole)
                .name(dto.getName())
                .nickname(dto.getNickname())
                .birthdate(dto.getBirthdate())
                .hobbies(dto.getHobbies())
                .gender(dto.getGender())
                .location(dto.getLocation())
                .bio(dto.getBio())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .interests(dto.getInterests())
                .isActive(true)
                .build();
        return userProfileRepository.save(userProfile);
    }

    @Override
    public List<UserProfile> getAllUserProfile() {
        UserProfile authUser = getAuthenticatedUser();
        if (!isAdmin(authUser)) {
            throw new SecurityException("Access denied. Only admins can access all user profiles.");
        }


        return userProfileRepository.findByDeletedAtIsNull();
    }

    @Override
    public UserProfile getUserProfile(UUID id) {
        UserProfile user = userProfileRepository.findById(id).orElse(null);
        if (user == null || user.getDeletedAt() != null)
            return null;

        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(user.getId());

        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("Access denied. You can only access your own profile.");
        }

        return user;
    }

    @Override
    public UserProfile updateUserProfile(UpdateUserDto dto) {
        UserProfile existing = userProfileRepository.findById(dto.getId()).orElse(null);
        if (existing == null || existing.getDeletedAt() != null)
            return null;

        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(existing.getId());

        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("Access denied. You can only update your own profile.");
        }

        existing.setName(dto.getName());
        existing.setNickname(dto.getNickname());
        existing.setBirthdate(dto.getBirthdate());
        existing.setHobbies(dto.getHobbies());
        existing.setGender(dto.getGender());
        existing.setLocation(dto.getLocation());
        existing.setBio(dto.getBio());
        existing.setEmail(dto.getEmail());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setInterests(dto.getInterests());
        existing.setActive(dto.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(existing);
    }

    @Override
    public UserProfile deleteProfile(UUID id) {
        UserProfile user = userProfileRepository.findById(id).orElse(null);
        if (user == null || user.getDeletedAt() != null)
            return null;

        UserProfile authUser = getAuthenticatedUser();
        if (!isAdmin(authUser)) {
            throw new SecurityException("Access denied. You can only delete your own profile.");
        }

        user.setDeletedAt(LocalDateTime.now());
        return userProfileRepository.save(user);
    }

    @Override
    public int getRandomMatchScore(UUID userId1, UUID userId2) {
        return new Random().nextInt(101);
    }

    @Override
    public int getMatchScore(UUID userId1, UUID userId2) {
        return getRandomMatchScore(userId1, userId2);
    }

    @Override
    public String getMatchMessage(int score) {
        if (score <= 50)
            return "Sebatas Teman";
        else if (score <= 70)
            return "Teman Sejati";
        else if (score <= 90)
            return "Cocok";
        else
            return "Cinta Abadi";
    }

    @Override
    public String getMatchImage(int score) {
        if (score <= 50)
            return "https://image.idntimes.com/posts/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg";
        else if (score <= 70)
            return "https://pbs.twimg.com/media/FddMGmGVIAA1TZK.jpg";
        else if (score <= 90)
            return "https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg";
        else
            return "https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg";
    }

    public ReadUserProfileDto toReadUserProfileDto(UserProfile user) {
        Integer age = null;
        String ageGroup = null;

        if (user.getBirthdate() != null) {
            age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();

            if (age >= 18 && age <= 25)
                ageGroup = "18-25";
            else if (age <= 35)
                ageGroup = "26-35";
            else if (age <= 45)
                ageGroup = "36-45";
            else
                ageGroup = "45+";
        }

        return ReadUserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthdate(user.getBirthdate())
                .age(age)
                .ageGroup(ageGroup)
                .hobbies(user.getHobbies())
                .gender(user.getGender())
                .location(user.getLocation())
                .bio(user.getBio())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .interests(user.getInterests())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.isActive())
                .build();
    }

    @Override
    public List<ReadUserProfileDto> getAllUserProfilesDto() {
        return getAllUserProfile().stream()
                .map(this::toReadUserProfileDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfile> searchProfilesByName(String name) {
        if (name == null || name.isBlank()) {
            return userProfileRepository.findByDeletedAtIsNull();
        }
        return userProfileRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(name);
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

    public UserProfile getAuthenticatedUser() {
        String currentUsername = jwtUtils.getCurrentUsername();
        UserProfile authUser = userProfileRepository.findByUsername(currentUsername);
        if (authUser == null) {
            throw new UsernameNotFoundException("Authenticated user not found.");
        }
        return authUser;
    }

    public boolean isAdmin(UserProfile user) {
        return user.getRole() != null && "Admin".equalsIgnoreCase(user.getRole().getRoleName());
    }
}
