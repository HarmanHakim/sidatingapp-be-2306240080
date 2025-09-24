package io.harman.sidating_app_be.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;

@WebMvcTest(UserProfileController.class)
@ContextConfiguration(classes = {UserProfileController.class})
@Import(UserProfileService.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    private UUID userId;
    private UserProfile userProfile;
    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;
    private ReadUserProfileDto readUserProfileDto;

    @BeforeEach
        void setUp() {
        userId = UUID.randomUUID();

        userProfile = UserProfile.builder()
                .id(userId)
                .name("John Doe")
                .nickname("Johnny")
                .birthdate(LocalDate.of(1995, 5, 20))
                .hobbies("Reading")
                .gender("MALE")
                .location("Jakarta")
                .bio("I love adventure!")
                .email("john@apap.id")
                .phoneNumber("081234567890")
                .interests("Technology")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) 
                .build();

        createUserDto = CreateUserDto.builder()
                .name("John Doe")
                .nickname("Johnny")
                .birthdate(LocalDate.of(1995, 5, 20))
                .hobbies("Reading")
                .gender("MALE")
                .location("Jakarta")
                .bio("I love adventure!")
                .email("john@apap.id")
                .phoneNumber("081234567890")
                .interests("Technology")
                .isActive(true)
                .build();

        updateUserDto = UpdateUserDto.builder()
                .id(userId)
                .name("John Updated")
                .nickname("Johnny2")
                .birthdate(LocalDate.of(1995, 5, 20))
                .hobbies("Gaming")
                .gender("MALE")
                .location("Bandung")
                .bio("Updated bio!")
                .email("john.updated@apap.id")
                .phoneNumber("081234567891")
                .interests("Gaming")
                .isActive(true)
                .build();

        readUserProfileDto = ReadUserProfileDto.builder()
                .id(userId)
                .name("John Doe")
                .nickname("Johnny")
                .birthdate(LocalDate.of(1995, 5, 20))
                .age(30)
                .ageGroup("26-35")
                .hobbies("Reading")
                .gender("MALE")
                .location("Jakarta")
                .bio("I love adventure!")
                .email("john@apap.id")
                .phoneNumber("081234567890")
                .interests("Technology")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) 
                .build();

        }

        @Test
        void testViewAllProfiles() throws Exception {
                List<UserProfile> userProfiles = Arrays.asList(userProfile);

                when(userProfileService.searchProfilesByName(null)).thenReturn(userProfiles);
                when(userProfileService.toReadUserProfileDto(any(UserProfile.class))).thenReturn(readUserProfileDto);

                mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("profile/view-all"))
                        .andExpect(model().attribute("userProfiles", hasSize(1)))
                        .andExpect(model().attribute("userProfiles",
                                contains(hasProperty("id", equalTo(userId)))))
                        .andExpect(model().attribute("search", nullValue()));

                verify(userProfileService, times(1)).searchProfilesByName(null);
                verify(userProfileService, times(1)).toReadUserProfileDto(any(UserProfile.class));
        }


    @Test
    void testGetProfileById_Found() throws Exception {
        when(userProfileService.getUserProfile(userId)).thenReturn(userProfile);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/detail"))
                .andExpect(model().attribute("userProfile", userProfile));
    }

    @Test
    void testGetProfileById_NotFound() throws Exception {
        when(userProfileService.getUserProfile(userId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Profile Not Found"))
                .andExpect(model().attribute("message", "Profile with ID " + userId + " not found."));
    }

    @Test
    void testFormProfile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profile/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("userProfile"));
    }

    @Test
    void testCreateProfile_Success() throws Exception {
        when(userProfileService.createUserProfile(any(CreateUserDto.class))).thenReturn(userProfile);

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/create")
                        .param("name", createUserDto.getName())
                        .param("nickname", createUserDto.getNickname())
                        .param("birthdate", createUserDto.getBirthdate().toString())
                        .param("hobbies", createUserDto.getHobbies())
                        .param("gender", createUserDto.getGender())
                        .param("location", createUserDto.getLocation())
                        .param("bio", createUserDto.getBio())
                        .param("email", createUserDto.getEmail())
                        .param("phoneNumber", createUserDto.getPhoneNumber())
                        .param("interests", createUserDto.getInterests())
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Successfully created profile with ID " + userProfile.getId()));
    }

    @Test
    void testCreateProfile_ValidationError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/profile/create")
                        .param("name", "") // Empty name triggers validation error
                        .param("nickname", createUserDto.getNickname())
                        .param("birthdate", createUserDto.getBirthdate().toString())
                        .param("hobbies", createUserDto.getHobbies())
                        .param("gender", createUserDto.getGender())
                        .param("location", createUserDto.getLocation())
                        .param("bio", createUserDto.getBio())
                        .param("email", createUserDto.getEmail())
                        .param("phoneNumber", createUserDto.getPhoneNumber())
                        .param("interests", createUserDto.getInterests())
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/create"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testFormEditProfile_Found() throws Exception {
        when(userProfileService.getUserProfile(userId)).thenReturn(userProfile);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/update/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("profileId", userId))
                .andExpect(model().attributeExists("userProfile"));
    }

    @Test
    void testFormEditProfile_NotFound() throws Exception {
        when(userProfileService.getUserProfile(userId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/update/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Profile Not Found"))
                .andExpect(model().attribute("message", "Profile with ID " + userId + " not found."));
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        when(userProfileService.updateUserProfile(any(UpdateUserDto.class))).thenReturn(userProfile);

        mockMvc.perform(MockMvcRequestBuilders.put("/profile/update/{id}", userId)
                        .param("id", userId.toString())
                        .param("name", updateUserDto.getName())
                        .param("nickname", updateUserDto.getNickname())
                        .param("birthdate", updateUserDto.getBirthdate().toString())
                        .param("hobbies", updateUserDto.getHobbies())
                        .param("gender", updateUserDto.getGender())
                        .param("location", updateUserDto.getLocation())
                        .param("bio", updateUserDto.getBio())
                        .param("email", updateUserDto.getEmail())
                        .param("phoneNumber", updateUserDto.getPhoneNumber())
                        .param("interests", updateUserDto.getInterests())
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Successfully updated profile with ID " + userId));
    }

    @Test
    void testUpdateProfile_NotFound() throws Exception {
        when(userProfileService.updateUserProfile(any(UpdateUserDto.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.put("/profile/update/{id}", userId)
                        .param("id", userId.toString())
                        .param("name", updateUserDto.getName())
                        .param("nickname", updateUserDto.getNickname())
                        .param("birthdate", updateUserDto.getBirthdate().toString())
                        .param("hobbies", updateUserDto.getHobbies())
                        .param("gender", updateUserDto.getGender())
                        .param("location", updateUserDto.getLocation())
                        .param("bio", updateUserDto.getBio())
                        .param("email", updateUserDto.getEmail())
                        .param("phoneNumber", updateUserDto.getPhoneNumber())
                        .param("interests", updateUserDto.getInterests())
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessage", "Profile with ID " + userId + " not found or deleted."));
    }

    @Test
    void testUpdateProfile_ValidationError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/profile/update/{id}", userId)
                        .param("id", userId.toString())
                        .param("name", "") // Empty name triggers validation error
                        .param("nickname", updateUserDto.getNickname())
                        .param("birthdate", updateUserDto.getBirthdate().toString())
                        .param("hobbies", updateUserDto.getHobbies())
                        .param("gender", updateUserDto.getGender())
                        .param("location", updateUserDto.getLocation())
                        .param("bio", updateUserDto.getBio())
                        .param("email", updateUserDto.getEmail())
                        .param("phoneNumber", updateUserDto.getPhoneNumber())
                        .param("interests", updateUserDto.getInterests())
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/update/" + userId))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testDeleteProfile_Success() throws Exception {
        when(userProfileService.deleteProfile(userId)).thenReturn(userProfile);

        mockMvc.perform(MockMvcRequestBuilders.delete("/profile/delete/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage", "Successfully deleted profile with ID " + userId));
    }

    @Test
    void testDeleteProfile_NotFound() throws Exception {
        when(userProfileService.deleteProfile(userId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.delete("/profile/delete/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessage", "Profile with ID " + userId + " not found or already deleted."));
    }

    @Test
    void testShowMatchForm() throws Exception {
        List<ReadUserProfileDto> dtos = Arrays.asList(readUserProfileDto);
        when(userProfileService.getAllUserProfilesDto()).thenReturn(dtos);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/match"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attribute("userProfiles", dtos));
    }

    @Test
    void testMatchProfilesPost_Success() throws Exception {
        UUID userId2 = UUID.randomUUID();
        when(userProfileService.getUserProfile(userId)).thenReturn(userProfile);
        when(userProfileService.getUserProfile(userId2)).thenReturn(userProfile);
        when(userProfileService.toReadUserProfileDto(any(UserProfile.class))).thenReturn(readUserProfileDto);
        when(userProfileService.getMatchScore(userId, userId2)).thenReturn(80);
        when(userProfileService.getMatchMessage(80)).thenReturn("Cocok");
        when(userProfileService.getMatchImage(80)).thenReturn("https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg");
        when(userProfileService.getAllUserProfilesDto()).thenReturn(Arrays.asList(readUserProfileDto));

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/match")
                        .param("user1Id", userId.toString())
                        .param("user2Id", userId2.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attribute("user1", readUserProfileDto))
                .andExpect(model().attribute("user2", readUserProfileDto))
                .andExpect(model().attribute("matchScore", 80))
                .andExpect(model().attribute("message", "Cocok"))
                .andExpect(model().attribute("imageUrl", "https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg"))
                .andExpect(model().attribute("userProfiles", Arrays.asList(readUserProfileDto)));
    }

    @Test
    void testMatchProfilesPost_NotFound() throws Exception {
        UUID userId2 = UUID.randomUUID();
        when(userProfileService.getUserProfile(userId)).thenReturn(null);
        when(userProfileService.getUserProfile(userId2)).thenReturn(null);
        when(userProfileService.getAllUserProfilesDto()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/match")
                        .param("user1Id", userId.toString())
                        .param("user2Id", userId2.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/match"))
                .andExpect(model().attribute("error", "One or both profiles not found."))
                .andExpect(model().attribute("userProfiles", Collections.emptyList()));
    }
}
