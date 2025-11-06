package io.harman.sidating_app_be.restservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.RoleRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restService.UserProfileRestServiceImpl;
import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;

public class UserProfileRestServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileRestServiceImpl userProfileRestService;

    private UUID userId;
    private UserProfile user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = UserProfile.builder()
                .id(userId)
                .name("Jane Doe")
                .nickname("jane")
                .birthdate(LocalDate.of(2000, 1, 1))
                .email("jane@example.com")
                .phoneNumber("123")
                .isActive(true)
                .build();
    }

    @Test
    void testCreateUserProfile() {
        AddUserProfileRequestDTO dto = new AddUserProfileRequestDTO(
                "janeuser", "password123", "User",
                "Jane Doe", "jane", LocalDate.of(2000, 1, 1),
                List.of("reading"), "Female", "City", "Bio",
                "jane@example.com", "12345", List.of("music"), true);

        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userProfileRepository.findByUsername(anyString())).thenReturn(null);
        when(roleRepository.findByRoleName(anyString())).thenReturn(java.util.Optional.of(new io.harman.sidating_app_be.model.Role()));

        UserProfileResponseDTO result = userProfileRestService.createUserProfile(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        verify(userProfileRepository).save(any());
    }

    @Test
    void testGetAllUserProfiles() {
        when(userProfileRepository.findAll()).thenReturn(List.of(user));

        List<UserProfileResponseDTO> result = userProfileRestService.getAllUserProfile();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane Doe");
    }

    @Test
    void testSearchUserProfileByName_Found() {
        when(userProfileRepository.findByNameContainingIgnoreCase("Jane")).thenReturn(List.of(user));

        List<UserProfileResponseDTO> result = userProfileRestService.searchUserProfileByName("Jane");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("jane");
    }

    @Test
    void testGetUserProfileById_Found() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponseDTO result = userProfileRestService.getUserProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
    }

    @Test
    void testUpdateUserProfile_Success() {
        UpdateUserProfileRequestDTO dto = new UpdateUserProfileRequestDTO(
                userId, "Jane Updated", "jane", LocalDate.of(2000, 1, 1),
                List.of("reading"), "Female", "City", "Bio",
                "jane@example.com", "12345", List.of("music"), true);

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserProfileResponseDTO result = userProfileRestService.updateUserProfile(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Updated");
    }

    @Test
    void testDeleteUserProfile_SoftDelete() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserProfileResponseDTO result = userProfileRestService.deleteUserProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }
}
