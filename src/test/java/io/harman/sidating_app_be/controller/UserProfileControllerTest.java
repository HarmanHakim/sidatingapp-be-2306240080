package io.harman.sidating_app_be.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    private UUID existingId;
    private UUID fakeId = UUID.randomUUID();
    private UserProfile dummyProfile;

    @BeforeEach
    void setUp() {
        existingId = UUID.randomUUID();
        dummyProfile = UserProfile.builder()
                .id(existingId)
                .name("Test User")
                .nickname("Tester")
                .email("test@example.com")
                .phoneNumber("0811223344")
                .birthdate(LocalDate.of(2000, 1, 1))
                .build();

        // Mock service methods instead of calling them directly
        List<UserProfile> profiles = List.of(dummyProfile);
        
        when(userProfileService.createUserProfile(any(UserProfile.class))).thenReturn(dummyProfile);
        when(userProfileService.getAllUserProfile()).thenReturn(profiles);
        when(userProfileService.getUserProfile(existingId)).thenReturn(dummyProfile);
        when(userProfileService.getUserProfile(fakeId)).thenReturn(null);
        when(userProfileService.updateUserProfile(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile arg = invocation.getArgument(0);
            if (arg.getId() != null && arg.getId().equals(existingId)) return dummyProfile;
            return null;
        });
        when(userProfileService.deleteProfile(existingId)).thenReturn(dummyProfile);
        when(userProfileService.deleteProfile(fakeId)).thenReturn(null);
    }

    @Test
    void testGetAllProfiles() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view-all"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testGetProfileByIdFound() throws Exception {
        mockMvc.perform(get("/profile/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/detail"))
                .andExpect(model().attributeExists("userProfile"));
    }

    @Test
    void testGetProfileByIdNotFound() throws Exception {
        mockMvc.perform(get("/profile/" + fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testFormCreateProfile() throws Exception {
        mockMvc.perform(get("/profile/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("userProfile"));
    }

    @Test
    void testCreateProfile() throws Exception {
        mockMvc.perform(post("/profile/create")
                .param("name", "Test User")
                .param("nickname", "Tester")
                .param("email", "test@example.com")
                .param("phoneNumber", "0811223344")
                .param("birthdate", "2000-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testFormEditProfileFound() throws Exception {
        mockMvc.perform(get("/profile/update/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("userProfile"))
                .andExpect(model().attribute("profileId", existingId));
    }



    @Test
    void testFormEditProfileNotFound() throws Exception {
        mockMvc.perform(get("/profile/update/" + fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testUpdateProfileFound() throws Exception {
        mockMvc.perform(put("/profile/update/" + existingId)
                .param("name", "Updated User")
                .param("nickname", "UpdatedNick")
                .param("email", "updated@example.com")
                .param("phoneNumber", "0811223344")
                .param("birthdate", "1999-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testUpdateProfileNotFound() throws Exception {
        mockMvc.perform(put("/profile/update/" + fakeId)
                .param("name", "Not Exist")
                .param("nickname", "None")
                .param("email", "none@example.com")
                .param("phoneNumber", "0800000")
                .param("birthdate", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testDeleteProfileFound() throws Exception {
        mockMvc.perform(delete("/profile/delete/" + existingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testDeleteProfileNotFound() throws Exception {
        mockMvc.perform(delete("/profile/delete/" + fakeId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }
}