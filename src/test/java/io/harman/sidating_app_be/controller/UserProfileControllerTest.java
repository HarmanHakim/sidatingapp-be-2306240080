package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    private MockMvc mockMvc;
    private UserProfile sampleUserProfile;
    private UserProfile sampleUserProfile2;
    private ReadUserProfileDto sampleReadUserProfileDto;
    private List<UserProfile> sampleUserProfiles;
    private UUID userId;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");
        
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
                .setViewResolvers(viewResolver)
                .build();

        // Setup test data
        userId = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        
        sampleUserProfile = new UserProfile();
        sampleUserProfile.setId(userId);
        sampleUserProfile.setName("John Doe");
        sampleUserProfile.setNickname("Johnny");
        sampleUserProfile.setBirthdate(LocalDate.of(1990, 1, 1));
        sampleUserProfile.setEmail("john@example.com");
        sampleUserProfile.setGender("Male");
        sampleUserProfile.setLocation("Jakarta");
        sampleUserProfile.setBio("Test bio");
        sampleUserProfile.setActive(true);
        
        sampleUserProfile2 = new UserProfile();
        sampleUserProfile2.setId(userId2);
        sampleUserProfile2.setName("Jane Doe");
        sampleUserProfile2.setNickname("Janie");
        sampleUserProfile2.setBirthdate(LocalDate.of(1992, 5, 15));
        sampleUserProfile2.setEmail("jane@example.com");
        sampleUserProfile2.setGender("Female");
        sampleUserProfile2.setLocation("Bandung");
        sampleUserProfile2.setBio("Another test bio");
        sampleUserProfile2.setActive(true);
        
        sampleUserProfiles = Arrays.asList(sampleUserProfile, sampleUserProfile2);
        
        sampleReadUserProfileDto = new ReadUserProfileDto();
        sampleReadUserProfileDto.setId(userId);
        sampleReadUserProfileDto.setName("John Doe");
        sampleReadUserProfileDto.setNickname("Johnny");
        sampleReadUserProfileDto.setBirthdate(LocalDate.of(1990, 1, 1));
        sampleReadUserProfileDto.setEmail("john@example.com");
    }

    @Test
    void getAllProfile_WithoutSearch_ShouldReturnAllProfiles() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);
        when(userProfileService.mapToReadUserProfileDto(sampleUserProfile)).thenReturn(sampleReadUserProfileDto);
        when(userProfileService.mapToReadUserProfileDto(sampleUserProfile2)).thenReturn(new ReadUserProfileDto());

        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view-all"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("search", (String) null));
        
        verify(userProfileService).getAllUserProfile();
        verify(userProfileService, never()).searchUserProfilesByName(any());
    }

    @Test
    void getAllProfile_WithSearch_ShouldReturnSearchResults() throws Exception {
        // Given
        String searchQuery = "John";
        List<UserProfile> searchResults = Arrays.asList(sampleUserProfile);
        when(userProfileService.searchUserProfilesByName(searchQuery)).thenReturn(searchResults);
        when(userProfileService.mapToReadUserProfileDto(sampleUserProfile)).thenReturn(sampleReadUserProfileDto);

        // When & Then
        mockMvc.perform(get("/profile")
                .param("search", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view-all"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("search", searchQuery));
        
        verify(userProfileService).searchUserProfilesByName(searchQuery);
        verify(userProfileService, never()).getAllUserProfile();
    }

    @Test
    void getAllProfile_WithEmptySearch_ShouldReturnAllProfiles() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);
        when(userProfileService.mapToReadUserProfileDto(any())).thenReturn(sampleReadUserProfileDto);

        // When & Then
        mockMvc.perform(get("/profile")
                .param("search", "   ")) // Empty/whitespace search
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view-all"));
        
        verify(userProfileService).getAllUserProfile();
        verify(userProfileService, never()).searchUserProfilesByName(any());
    }

    @Test
    void getProfileById_ExistingProfile_ShouldReturnDetailView() throws Exception {
        // Given
        when(userProfileService.getUserProfile(userId)).thenReturn(sampleUserProfile);

        // When & Then
        mockMvc.perform(get("/profile/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/detail"))
                .andExpect(model().attributeExists("userProfile"))
                .andExpect(model().attribute("userProfile", sampleUserProfile));
        
        verify(userProfileService).getUserProfile(userId);
    }

    @Test
    void getProfileById_NonExistingProfile_ShouldReturn404View() throws Exception {
        // Given
        when(userProfileService.getUserProfile(userId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/profile/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Profile Not Found"))
                .andExpect(model().attributeExists("message"));
        
        verify(userProfileService).getUserProfile(userId);
    }

    @Test
    void formProfile_ShouldReturnCreateForm() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/profile/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attributeExists("userDto"));
        
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void createProfile_ValidationErrors_ShouldReturnFormWithErrors() throws Exception {
        // When & Then - Testing with invalid email format
        mockMvc.perform(post("/profile/create")
                .param("name", "") // Empty name should cause validation error
                .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", false));
        
        verify(userProfileService, never()).createUserProfile(any());
    }

    @Test
    void formEditProfile_ExistingProfile_ShouldReturnEditForm() throws Exception {
        // Given
        when(userProfileService.getUserProfile(userId)).thenReturn(sampleUserProfile);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/profile/update/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("userDto"))
                .andExpect(model().attributeExists("userProfiles"));
        
        verify(userProfileService).getUserProfile(userId);
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void formEditProfile_NonExistingProfile_ShouldReturn404View() throws Exception {
        // Given
        when(userProfileService.getUserProfile(userId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/profile/update/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Profile Not Found"));
        
        verify(userProfileService).getUserProfile(userId);
    }

    @Test
    void updateProfile_ValidationErrors_ShouldReturnFormWithErrors() throws Exception {
        // When & Then - Testing with invalid data
        mockMvc.perform(put("/profile/update/{id}", userId)
                .param("name", "") // Empty name should cause validation error
                .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", true));
        
        verify(userProfileService, never()).updateUserProfile(any());
    }

    @Test
    void deleteProfile_ExistingProfile_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(userProfileService.deleteProfile(userId)).thenReturn(sampleUserProfile);

        // When & Then
        mockMvc.perform(delete("/profile/delete/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(userProfileService).deleteProfile(userId);
    }

    @Test
    void deleteProfile_NonExistingProfile_ShouldRedirectWithErrorMessage() throws Exception {
        // Given
        when(userProfileService.deleteProfile(userId)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/profile/delete/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("errorMessage"));
        
        verify(userProfileService).deleteProfile(userId);
    }

    @Test
    void match_WithoutUserIds_ShouldReturnMatchFormOnly() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/profile/match"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attributeDoesNotExist("user1"))
                .andExpect(model().attributeDoesNotExist("user2"))
                .andExpect(model().attributeDoesNotExist("score"));
        
        verify(userProfileService).getAllUserProfile();
        verify(userProfileService, never()).getRandomMatchScore(any(), any());
    }

    @Test
    void match_WithValidUserIds_ShouldReturnMatchResult() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);
        when(userProfileService.getUserProfile(userId)).thenReturn(sampleUserProfile);
        when(userProfileService.getUserProfile(userId2)).thenReturn(sampleUserProfile2);
        when(userProfileService.getRandomMatchScore(userId, userId2)).thenReturn(85);
        when(userProfileService.getMatchMessage(85)).thenReturn("Great match!");
        when(userProfileService.getMatchImage(85)).thenReturn("match-high.png");

        // When & Then
        mockMvc.perform(get("/profile/match")
                .param("userId1", userId.toString())
                .param("userId2", userId2.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("user1", sampleUserProfile))
                .andExpect(model().attribute("user2", sampleUserProfile2))
                .andExpect(model().attribute("score", 85))
                .andExpect(model().attribute("message", "Great match!"))
                .andExpect(model().attribute("imageLink", "match-high.png"));
        
        verify(userProfileService).getAllUserProfile();
        verify(userProfileService).getUserProfile(userId);
        verify(userProfileService).getUserProfile(userId2);
        verify(userProfileService).getRandomMatchScore(userId, userId2);
        verify(userProfileService).getMatchMessage(85);
        verify(userProfileService).getMatchImage(85);
    }

    @Test
    void match_WithInvalidUserId1_ShouldReturn404() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);
        when(userProfileService.getUserProfile(userId)).thenReturn(null); // User1 not found
        when(userProfileService.getUserProfile(userId2)).thenReturn(sampleUserProfile2);

        // When & Then
        mockMvc.perform(get("/profile/match")
                .param("userId1", userId.toString())
                .param("userId2", userId2.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Match Error"))
                .andExpect(model().attributeExists("message"));
        
        verify(userProfileService, never()).getRandomMatchScore(any(), any());
    }

    @Test
    void match_WithInvalidUserId2_ShouldReturn404() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);
        when(userProfileService.getUserProfile(userId)).thenReturn(sampleUserProfile);
        when(userProfileService.getUserProfile(userId2)).thenReturn(null); // User2 not found

        // When & Then
        mockMvc.perform(get("/profile/match")
                .param("userId1", userId.toString())
                .param("userId2", userId2.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Match Error"));
        
        verify(userProfileService, never()).getRandomMatchScore(any(), any());
    }

    @Test
    void match_WithPartialParameters_ShouldReturnFormOnly() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then - Only userId1 provided
        mockMvc.perform(get("/profile/match")
                .param("userId1", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attributeDoesNotExist("score"));
        
        verify(userProfileService, never()).getRandomMatchScore(any(), any());
    }
}