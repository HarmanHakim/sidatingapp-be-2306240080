package io.harman.sidating_app_be.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileController controller;

    private UUID existingId;
    private UUID fakeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        existingId = controller.getAllProfiles().get(0).getId();
    }

    @Test
    void testGetAllProfiles() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view-all"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testGetProfileById() throws Exception {
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
                .andExpect(model().attribute("title", "Profile Not Found"))
                .andExpect(model().attribute("message", "Profile with ID " + fakeId + " not found."));
    }

    @Test
    void testGetCreateForm() throws Exception {
        mockMvc.perform(get("/profile/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attributeExists("userProfile"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    void testCreateProfile() throws Exception {
        mockMvc.perform(post("/profile/create")
                .param("name", "Test User")
                .param("nickname", "Test")
                .param("email", "test@example.com")
                .param("phoneNumber", "081234567890")
                .param("location", "Test City")
                .param("gender", "MALE")
                .param("birthdate", "1990-01-01")
                .param("hobbies", "Testing")
                .param("interests", "Quality Assurance")
                .param("bio", "Test user bio")
                .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testGetEditForm() throws Exception {
        mockMvc.perform(get("/profile/update/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/form"))
                .andExpect(model().attributeExists("userProfile"))
                .andExpect(model().attribute("isEdit", true));
    }

    @Test
    void testGetEditFormNotFound() throws Exception {
        mockMvc.perform(get("/profile/update/" + fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Profile Not Found"))
                .andExpect(model().attribute("message", "Profile with ID " + fakeId + " not found."));
    }

    @Test
    void testUpdateProfile() throws Exception {
        mockMvc.perform(post("/profile/update/" + existingId)
                .param("name", "Updated User")
                .param("nickname", "Updated")
                .param("email", "updated@example.com")
                .param("phoneNumber", "081234567890")
                .param("location", "Updated City")
                .param("gender", "FEMALE")
                .param("birthdate", "1990-01-01")
                .param("hobbies", "Updated Hobbies")
                .param("interests", "Updated Interests")
                .param("bio", "Updated bio")
                .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testDeleteProfile() throws Exception {
        mockMvc.perform(get("/profile/delete/" + existingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testDeleteProfileNotFound() throws Exception {
        mockMvc.perform(get("/profile/delete/" + fakeId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }
}
