package io.harman.sidating_app_be.controler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import io.harman.sidating_app_be.controller.UserProfileController;

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

    private UUID existingId; // 4 usages
    private UUID fakeId = UUID.randomUUID(); // 1 usage

    @BeforeEach
    void setUp() {
        existingId = controller.getAllProfiles().get(0).getId();
    }

    @Test
    void testGetAllProfiles() throws Exception {
        mockMvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(view().name("profiles/list"))
                .andExpect(model().attributeExists("profiles"));
    }

    @Test
    void testGetProfileById() throws Exception {
        mockMvc.perform(get("/profiles/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/detail"))
                .andExpect(model().attributeExists("userProfile"));
    }

    @Test
    void testGetProfileByIdNotFound() throws Exception {
        mockMvc.perform(get("/profiles/" + fakeId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("title", ""))
                .andExpect(model().attribute("message", ""));
    }


}
