package io.harman.sidating_app_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {
    @GetMapping("/{name}")
    public String MainController(@PathVariable String name, Model model) {
        model.addAttribute("name", name);
        return "main";
    }
}
