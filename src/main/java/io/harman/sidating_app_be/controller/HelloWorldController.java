package io.harman.sidating_app_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HelloWorldController {

    @GetMapping("/")
    public String helloWorld(Model model) {
        model.addAttribute("title", "Spring Boot MVC Introduction");
        model.addAttribute("message", "Hello World from Spring Boot MVC!");
        return "hello";
    }

    @GetMapping("/{name}")
    public String helloWithPathParam(@PathVariable String name, Model model) {
        model.addAttribute("title", "Spring Boot MVC Introduction");
        model.addAttribute("message", "Hello " + name + "! Welcome to Spring Boot MVC!");
        return "hello";
    }
}
