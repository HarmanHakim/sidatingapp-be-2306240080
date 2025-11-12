package io.harman.sidating_app_be.restcontroller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.harman.sidating_app_be.restService.UserProfileRestService;
import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;

@ExtendWith(MockitoExtension.class)
class UserProfileRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserProfileRestService userProfileRestService;

    @InjectMocks
    private UserProfileRestController userProfileRestController;

    private ObjectMapper objectMapper;
    private UserProfileResponseDTO sampleUserProfileResponse;
    private AddUserProfileRequestDTO addUserProfileRequest;
    private UpdateUserProfileRequestDTO updateUserProfileRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.randomUUID();

        sampleUserProfileResponse = UserProfileResponseDTO.builder()
                .id(userId)
                .name("John Doe")
                .nickname("Johnny")
                .email("john@example.com")
                .phoneNumber("081234567890")
                .location("Jakarta")
                .bio("Test bio")

                .interests("Reading, Gaming")
                .hobbies("Coding, Music")
                .gender("MALE")
                .birthdate(LocalDate.of(1995, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        addUserProfileRequest = new AddUserProfileRequestDTO();
        addUserProfileRequest.setUsername("johndoe");
        addUserProfileRequest.setPassword("password123");
        addUserProfileRequest.setName("John Doe");
        addUserProfileRequest.setNickname("Johnny");
        addUserProfileRequest.setEmail("john@example.com");
        addUserProfileRequest.setPhoneNumber("081234567890");
        addUserProfileRequest.setLocation("Jakarta");
        addUserProfileRequest.setBio("Test bio");
        addUserProfileRequest.setInterests(Arrays.asList("Reading", "Gaming"));
        addUserProfileRequest.setHobbies(Arrays.asList("Coding", "Music"));
        addUserProfileRequest.setGender("MALE");
        addUserProfileRequest.setBirthdate(LocalDate.of(1995, 1, 1));
        addUserProfileRequest.setRoleName("User");

        updateUserProfileRequest = new UpdateUserProfileRequestDTO();
        updateUserProfileRequest.setId(userId);
        updateUserProfileRequest.setName("John Doe Updated");
        updateUserProfileRequest.setNickname("Johnny Updated");
        updateUserProfileRequest.setEmail("john.updated@example.com");
        updateUserProfileRequest.setPhoneNumber("081234567891");
        updateUserProfileRequest.setLocation("Bandung");
        updateUserProfileRequest.setBio("Updated bio");
        updateUserProfileRequest.setInterests(Arrays.asList("Reading", "Gaming", "Coding"));
        updateUserProfileRequest.setHobbies(Arrays.asList("Music"));
        updateUserProfileRequest.setGender("MALE");
        updateUserProfileRequest.setBirthdate(LocalDate.of(1995, 1, 1));
    }

    @Test
    void getAllUserProfile_ShouldReturnAllProfiles() throws Exception {
        List<UserProfileResponseDTO> profiles = Arrays.asList(sampleUserProfileResponse);
        when(userProfileRestService.getAllUserProfile()).thenReturn(profiles);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Ditemukan"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(userId.toString()));

        verify(userProfileRestService).getAllUserProfile();
    }

    @Test
    void getAllUserProfile_ShouldReturnForbidden_WhenNotAuthorized() throws Exception {
        List<UserProfileResponseDTO> profiles = Arrays.asList(sampleUserProfileResponse);
        when(userProfileRestService.getAllUserProfile()).thenReturn(profiles);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Ditemukan"));

        verify(userProfileRestService).getAllUserProfile();
    }

    @Test
    void getUserProfileById_ShouldReturnProfile_WhenProfileExists() throws Exception {
        when(userProfileRestService.getUserProfile(userId)).thenReturn(sampleUserProfileResponse);

        mockMvc.perform(get("/api/profile/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Ditemukan"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));

        verify(userProfileRestService).getUserProfile(userId);
    }

    @Test
    void getUserProfileById_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(userProfileRestService.getUserProfile(userId)).thenReturn(null);

        mockMvc.perform(get("/api/profile/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User Profile Tidak Ditemukan"));

        verify(userProfileRestService).getUserProfile(userId);
    }

    @Test
    void searchUserProfiles_ShouldReturnMatchingProfiles() throws Exception {
        List<UserProfileResponseDTO> profiles = Arrays.asList(sampleUserProfileResponse);
        when(userProfileRestService.searchUserProfilesByName("John")).thenReturn(profiles);

        mockMvc.perform(get("/api/profile")
                        .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Ditemukan"))
                .andExpect(jsonPath("$.data").isArray());

        verify(userProfileRestService).searchUserProfilesByName("John");
    }

    @Test
    void searchUserProfiles_ShouldReturnAllProfiles_WhenNameIsEmpty() throws Exception {
        List<UserProfileResponseDTO> profiles = Arrays.asList(sampleUserProfileResponse);
        when(userProfileRestService.getAllUserProfile()).thenReturn(profiles);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(userProfileRestService).getAllUserProfile();
    }

    @Test
    void createUserProfile_ShouldReturnCreatedProfile_WhenValidRequest() throws Exception {
        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(sampleUserProfileResponse);

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addUserProfileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Dibuat"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }

    @Test
    void createUserProfile_ShouldReturnBadRequest_WhenUsernameAlreadyExists() throws Exception {
        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addUserProfileRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("username sudah digunakan")));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }

    @Test
    void createUserProfile_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        AddUserProfileRequestDTO invalidRequest = new AddUserProfileRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userProfileRestService, never()).createUserProfile(any());
    }

    @Test
    void updateUserProfile_ShouldReturnUpdatedProfile_WhenValidRequest() throws Exception {
        when(userProfileRestService.updateUserProfile(any(UpdateUserProfileRequestDTO.class)))
                .thenReturn(sampleUserProfileResponse);

        mockMvc.perform(put("/api/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Diupdate"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));

        verify(userProfileRestService).updateUserProfile(any(UpdateUserProfileRequestDTO.class));
    }

    @Test
    void updateUserProfile_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(userProfileRestService.updateUserProfile(any(UpdateUserProfileRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserProfileRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User Profile Tidak Ditemukan"));

        verify(userProfileRestService).updateUserProfile(any(UpdateUserProfileRequestDTO.class));
    }

    @Test
    void updateUserProfile_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        UpdateUserProfileRequestDTO invalidRequest = new UpdateUserProfileRequestDTO();
        invalidRequest.setId(null); // Missing required ID

        mockMvc.perform(put("/api/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userProfileRestService, never()).updateUserProfile(any());
    }

    @Test
    void deleteUserProfile_ShouldReturnSuccess_WhenProfileDeleted() throws Exception {
        when(userProfileRestService.deleteUserProfile(userId)).thenReturn(sampleUserProfileResponse);

        mockMvc.perform(delete("/api/profile/delete/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data User Profile Berhasil Dihapus"));

        verify(userProfileRestService).deleteUserProfile(userId);
    }

    @Test
    void deleteUserProfile_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(userProfileRestService.deleteUserProfile(userId)).thenReturn(null);

        mockMvc.perform(delete("/api/profile/delete/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Tidak Ditemukan")));

        verify(userProfileRestService).deleteUserProfile(userId);
    }

    @Test
    void deleteUserProfile_ShouldReturnNotFound_WhenInvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(userProfileRestService.deleteUserProfile(invalidId)).thenReturn(null);

        mockMvc.perform(delete("/api/profile/delete/" + invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(userProfileRestService).deleteUserProfile(invalidId);
    }

    @Test
    void getAllUserProfile_ShouldReturnEmptyList_WhenNoProfiles() throws Exception {
        List<UserProfileResponseDTO> emptyList = Arrays.asList();
        when(userProfileRestService.getAllUserProfile()).thenReturn(emptyList);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userProfileRestService).getAllUserProfile();
    }

    @Test
    void createUserProfile_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addUserProfileRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }
}
