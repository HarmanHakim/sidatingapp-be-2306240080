package io.harman.sidating_app_be.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMainPageWithDefaultName() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "SiDating User"));
    }

    @Test
    void testMainPageWithCustomName() throws Exception {
        mockMvc.perform(get("/").param("name", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "John Doe"));
    }

    @Test
    void testMainPageWithEmptyName() throws Exception {
        mockMvc.perform(get("/").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "SiDating User"));
    }

    @Test
    void testMainPageWithSpecialCharactersInName() throws Exception {
        mockMvc.perform(get("/").param("name", "José María"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "José María"));
    }

    @Test
    void testMainPageContentType() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
