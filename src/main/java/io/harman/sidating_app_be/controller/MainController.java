package io.harman.sidating_app_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String mainPage(@RequestParam(value = "name", defaultValue = "SiDating User") String name, Model model) {
        // If name is empty, use default value
        if (name == null || name.trim().isEmpty()) {
            name = "SiDating User";
        }
        model.addAttribute("name", name);
        return "main";
    }
}
