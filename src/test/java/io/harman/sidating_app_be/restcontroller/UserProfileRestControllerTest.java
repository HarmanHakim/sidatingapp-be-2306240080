package io.harman.sidating_app_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.harman.sidating_app_be.restService.UserProfileRestService;
import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileRestService userProfileRestService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UserProfileResponseDTO userProfileResponseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userProfileResponseDTO = UserProfileResponseDTO.builder()
                .id(userId)
                .name("Tester")
                .nickname("tester")
                .email("tester@example.com")
                .isActive(true)
                .build();
    }

    private AddUserProfileRequestDTO createValidAddDTO() {
        return new AddUserProfileRequestDTO(
                "testuser",                   // username
                "password123",                // password
                "User",                       // roleName
                "Tester",                     // name
                "desc",                       // nickname / bio
                LocalDate.of(2000, 1, 1),     // birthdate
                List.of("coding"),           // hobbies
                "Male",                       // gender
                "City",                       // location / city
                "Province",                   // province
                "tester@example.com",         // email
                "12345",                      // phone
                List.of("https://img"),      // interests / images
                true                          // active
        );
    }

    private UpdateUserProfileRequestDTO createValidUpdateDTO() {
        return new UpdateUserProfileRequestDTO(
                userId,
                "Tester",                     // name
                "desc",                       // nickname / bio
                LocalDate.of(2000, 1, 1),     // birthdate
                List.of("coding"),           // hobbies
                "Male",                       // gender
                "City",                       // location / city
                "Province",                   // province
                "tester@example.com",         // email
                "12345",                      // phone
                List.of("https://img"),      // interests / images
                true                          // active
        );
    }

    @Test
    void testGetAllProfiles() throws Exception {
        Mockito.when(userProfileRestService.getAllUserProfile())
                .thenReturn(List.of(userProfileResponseDTO));

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Tester"));
    }

    @Test
    void testGetUserProfile_Found() throws Exception {
        Mockito.when(userProfileRestService.getUserProfile(userId))
                .thenReturn(userProfileResponseDTO);

        mockMvc.perform(get("/api/profile/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Tester"));
    }

    @Test
    void testGetUserProfile_NotFound() throws Exception {
        Mockito.when(userProfileRestService.getUserProfile(userId))
                .thenReturn(null);

        mockMvc.perform(get("/api/profile/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User Profile Tidak Ditemukan"));
    }

    @Test
    void testCreateUserProfile_Success() throws Exception {
        AddUserProfileRequestDTO dto = createValidAddDTO();
        Mockito.when(userProfileRestService.createUserProfile(any())).thenReturn(userProfileResponseDTO);

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Tester"));
    }

    @Test
    void testCreateUserProfile_Fail() throws Exception {
        AddUserProfileRequestDTO dto = createValidAddDTO();
        Mockito.when(userProfileRestService.createUserProfile(any())).thenReturn(null);

        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("User Profile Gagal Dibuat"));
    }

    @Test
    void testUpdateUserProfile_Success() throws Exception {
        UpdateUserProfileRequestDTO dto = createValidUpdateDTO();
        Mockito.when(userProfileRestService.updateUserProfile(any())).thenReturn(userProfileResponseDTO);

        mockMvc.perform(put("/api/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Tester"));
    }

    @Test
    void testUpdateUserProfile_NotFound() throws Exception {
        UpdateUserProfileRequestDTO dto = createValidUpdateDTO();
        Mockito.when(userProfileRestService.updateUserProfile(any())).thenReturn(null);

        mockMvc.perform(put("/api/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User Profile Tidak Ditemukan"));
    }

    @Test
    void testDeleteUserProfile_Success() throws Exception {
        Mockito.when(userProfileRestService.deleteUserProfile(userId)).thenReturn(userProfileResponseDTO);

        mockMvc.perform(delete("/api/profile/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Tester"));
    }

    @Test
    void testDeleteUserProfile_NotFound() throws Exception {
        Mockito.when(userProfileRestService.deleteUserProfile(userId)).thenReturn(null);

        mockMvc.perform(delete("/api/profile/delete/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User Profile Tidak Ditemukan"));
    }
}
