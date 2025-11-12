package io.harman.sidating_app_be.restservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.RoleRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restService.UserProfileRestServiceImpl;
import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;
import io.harman.sidating_app_be.security.jwt.JwtUtils;

@ExtendWith(MockitoExtension.class)
class UserProfileRestServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserProfileRestServiceImpl userProfileRestService;

    private UserProfile sampleUserProfile;
    private Role adminRole;
    private Role userRole;
    private AddUserProfileRequestDTO addUserProfileRequest;
    private UpdateUserProfileRequestDTO updateUserProfileRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Create a Role for the user
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("Admin");

        userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleName("User");

        sampleUserProfile = new UserProfile();
        sampleUserProfile.setId(userId);
        sampleUserProfile.setUsername("testadmin");
        sampleUserProfile.setRole(adminRole);
        sampleUserProfile.setName("John Doe");
        sampleUserProfile.setNickname("Johnny");
        sampleUserProfile.setEmail("john@example.com");
        sampleUserProfile.setPhoneNumber("081234567890");
        sampleUserProfile.setLocation("Jakarta");
        sampleUserProfile.setBio("Test bio");
        sampleUserProfile.setInterests("Reading, Gaming");
        sampleUserProfile.setGender("MALE");
        sampleUserProfile.setBirthdate(LocalDate.of(1995, 1, 1));
        sampleUserProfile.setCreatedAt(LocalDateTime.now());
        sampleUserProfile.setUpdatedAt(LocalDateTime.now());

        addUserProfileRequest = new AddUserProfileRequestDTO();
        addUserProfileRequest.setUsername("johndoe");
        addUserProfileRequest.setPassword("password123");
        addUserProfileRequest.setRoleName("User");
        addUserProfileRequest.setName("John Doe");
        addUserProfileRequest.setNickname("Johnny");
        addUserProfileRequest.setEmail("john@example.com");
        addUserProfileRequest.setPhoneNumber("081234567890");
        addUserProfileRequest.setLocation("Jakarta");
        addUserProfileRequest.setBio("Test bio");
        addUserProfileRequest.setInterests(Arrays.asList("Reading", "Gaming"));
        addUserProfileRequest.setGender("MALE");
        addUserProfileRequest.setBirthdate(LocalDate.of(1995, 1, 1));

        updateUserProfileRequest = new UpdateUserProfileRequestDTO();
        updateUserProfileRequest.setId(userId);
        updateUserProfileRequest.setPassword("password123");
        updateUserProfileRequest.setRoleName("User");
        updateUserProfileRequest.setName("John Doe Updated");
        updateUserProfileRequest.setNickname("Johnny Updated");
        updateUserProfileRequest.setEmail("john.updated@example.com");
        updateUserProfileRequest.setPhoneNumber("081234567891");
        updateUserProfileRequest.setLocation("Bandung");
        updateUserProfileRequest.setBio("Updated bio");
        updateUserProfileRequest.setInterests(Arrays.asList("Reading", "Gaming", "Coding"));
        updateUserProfileRequest.setGender("MALE");
        updateUserProfileRequest.setBirthdate(LocalDate.of(1995, 1, 1));
    }

    @Test
    void getAllUserProfile_ShouldReturnAllActiveProfiles() {
        // Mock authenticated user (admin)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        
        List<UserProfile> userProfiles = Arrays.asList(sampleUserProfile);
        when(userProfileRepository.findAll()).thenReturn(userProfiles);

        List<UserProfileResponseDTO> result = userProfileRestService.getAllUserProfile();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getId());
        assertEquals("John Doe", result.get(0).getName());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findAll();
    }

    @Test
    void getAllUserProfile_ShouldReturnEmptyList_WhenNoActiveProfiles() {
        // Mock authenticated user (admin)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        when(userProfileRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserProfileResponseDTO> result = userProfileRestService.getAllUserProfile();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findAll();
    }

    @Test
    void getUserProfile_ShouldReturnProfile_WhenProfileExists() {
        // Mock authenticated user (admin or owner)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));

        UserProfileResponseDTO result = userProfileRestService.getUserProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
    }

    @Test
    void getUserProfile_ShouldReturnNull_WhenProfileNotFound() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        UserProfileResponseDTO result = userProfileRestService.getUserProfile(userId);

        assertNull(result);
        verify(userProfileRepository).findById(userId);
    }

    @Test
    void getUserProfile_ShouldReturnProfile_WhenProfileIsDeleted() {
        // Mock authenticated user (admin or owner)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        sampleUserProfile.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));

        UserProfileResponseDTO result = userProfileRestService.getUserProfile(userId);

        // The service doesn't filter deleted profiles in getUserProfile
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
    }

    @Test
    void createUserProfile_ShouldCreateProfile_WhenValidRequest() {
        when(userProfileRepository.findByUsername("johndoe")).thenReturn(null);
        when(roleRepository.findByRoleName("User")).thenReturn(Optional.of(userRole));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleUserProfile);

        UserProfileResponseDTO result = userProfileRestService.createUserProfile(addUserProfileRequest);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userProfileRepository).findByUsername("johndoe");
        verify(roleRepository).findByRoleName("User");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createUserProfile_ShouldHandleException_WhenSaveFails() {
        when(userProfileRepository.findByUsername("johndoe")).thenReturn(null);
        when(roleRepository.findByRoleName("User")).thenReturn(Optional.of(userRole));
        when(userProfileRepository.save(any(UserProfile.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            userProfileRestService.createUserProfile(addUserProfileRequest);
        });

        verify(userProfileRepository).findByUsername("johndoe");
        verify(roleRepository).findByRoleName("User");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateUserProfile_ShouldUpdateProfile_WhenValidRequest() {
        // Mock authenticated user (admin or owner)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));
        when(roleRepository.findByRoleName("User")).thenReturn(Optional.of(userRole));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleUserProfile);

        UserProfileResponseDTO result = userProfileRestService.updateUserProfile(updateUserProfileRequest);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
        verify(roleRepository).findByRoleName("User");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateUserProfile_ShouldReturnNull_WhenProfileNotFound() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        UserProfileResponseDTO result = userProfileRestService.updateUserProfile(updateUserProfileRequest);

        assertNull(result);
        verify(userProfileRepository).findById(userId);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_ShouldUpdateProfile_WhenProfileIsDeleted() {
        // Mock authenticated user (admin or owner)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        sampleUserProfile.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));
        when(roleRepository.findByRoleName("User")).thenReturn(Optional.of(userRole));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleUserProfile);

        UserProfileResponseDTO result = userProfileRestService.updateUserProfile(updateUserProfileRequest);

        // The service doesn't check for deleted status in update
        assertNotNull(result);
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
        verify(roleRepository).findByRoleName("User");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void deleteUserProfile_ShouldMarkAsDeleted_WhenProfileExists() {
        // Mock authenticated user (admin)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleUserProfile);

        UserProfileResponseDTO result = userProfileRestService.deleteUserProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertNotNull(sampleUserProfile.getDeletedAt());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
        verify(userProfileRepository).save(sampleUserProfile);
    }

    @Test
    void deleteUserProfile_ShouldReturnNull_WhenProfileNotFound() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        UserProfileResponseDTO result = userProfileRestService.deleteUserProfile(userId);

        assertNull(result);
        verify(userProfileRepository).findById(userId);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void deleteUserProfile_ShouldMarkAsDeleted_WhenProfileAlreadyDeleted() {
        // Mock authenticated user (admin)
        when(jwtUtils.getCurrentUsername()).thenReturn("testadmin");
        when(userProfileRepository.findByUsername("testadmin")).thenReturn(sampleUserProfile);
        sampleUserProfile.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(sampleUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(sampleUserProfile);

        UserProfileResponseDTO result = userProfileRestService.deleteUserProfile(userId);

        // The service doesn't check if already deleted
        assertNotNull(result);
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testadmin");
        verify(userProfileRepository).findById(userId);
        verify(userProfileRepository).save(sampleUserProfile);
    }

    @Test
    void searchUserProfilesByName_ShouldReturnMatchingProfiles() {
        String searchTerm = "John";
        List<UserProfile> userProfiles = Arrays.asList(sampleUserProfile);
        when(userProfileRepository.findByNameContainingIgnoreCase(searchTerm))
                .thenReturn(userProfiles);

        List<UserProfileResponseDTO> result = userProfileRestService.searchUserProfilesByName(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(userProfileRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void searchUserProfilesByName_ShouldReturnEmptyList_WhenNoMatches() {
        String searchTerm = "NonExistent";
        when(userProfileRepository.findByNameContainingIgnoreCase(searchTerm))
                .thenReturn(new ArrayList<>());

        List<UserProfileResponseDTO> result = userProfileRestService.searchUserProfilesByName(searchTerm);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userProfileRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    // Remove the mapping method tests since they're not public methods
}