package io.harman.sidating_app_be;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.harman.sidating_app_be.controller.MainController;

@WebMvcTest(MainController.class)
public class MainTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMainControllerEndpoint() throws Exception {
        String name = "/harman";
        mockMvc.perform(MockMvcRequestBuilders.get(name))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.view().name("main"));
    }
}
