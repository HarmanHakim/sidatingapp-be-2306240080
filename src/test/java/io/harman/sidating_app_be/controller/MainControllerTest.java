package io.harman.sidating_app_be.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {

    private MainController controller;
    private Model model;

    @BeforeEach
    void setUp() {
        controller = new MainController();
        model = new ExtendedModelMap();
    }

    @Test
    void testMainPageWithDefaultName() {
        String result = controller.mainPage("SiDating User", model);
        
        assertEquals("main", result);
        assertEquals("SiDating User", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithCustomName() {
        String result = controller.mainPage("John Doe", model);
        
        assertEquals("main", result);
        assertEquals("John Doe", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithEmptyName() {
        String result = controller.mainPage("", model);
        
        assertEquals("main", result);
        assertEquals("SiDating User", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithSpecialCharactersInName() {
        String result = controller.mainPage("José María", model);
        
        assertEquals("main", result);
        assertEquals("José María", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithNullName() {
        String result = controller.mainPage(null, model);
        
        assertEquals("main", result);
        assertEquals("SiDating User", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithWhitespaceOnlyName() {
        String result = controller.mainPage("   ", model);
        
        assertEquals("main", result);
        assertEquals("SiDating User", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithTabsAndSpacesName() {
        String result = controller.mainPage("\t  \n  ", model);
        
        assertEquals("main", result);
        assertEquals("SiDating User", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithNameHavingLeadingAndTrailingSpaces() {
        String result = controller.mainPage("  John  ", model);
        
        assertEquals("main", result);
        assertEquals("  John  ", model.getAttribute("name"));
    }

    @Test
    void testMainPageWithVeryLongName() {
        String longName = "A".repeat(1000);
        String result = controller.mainPage(longName, model);
        
        assertEquals("main", result);
        assertEquals(longName, model.getAttribute("name"));
    }

    @Test
    void testMainPageWithNumericName() {
        String result = controller.mainPage("12345", model);
        
        assertEquals("main", result);
        assertEquals("12345", model.getAttribute("name"));
    }
}