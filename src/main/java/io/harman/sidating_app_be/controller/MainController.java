package io.harman.sidating_app_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {
    @GetMapping("/{fibonacci}")
    public String MainController(@PathVariable String fibonacci, Model model) {
        model.addAttribute("number", fibonacci);

        int n;
        try{
            n = Integer.parseInt(fibonacci);
            if(n<1){
                model.addAttribute("name", "input bukan bilangan bulat positif");
            }
            else{
                model.addAttribute("name",fibonacci(n));
            }

        }
        catch(NumberFormatException e){
            model.addAttribute("name", "input bukan bilangan bulat"); 
        }
        return "main";
    }
    private int fibonacci(int n) {
        if (n <= 2) {
            return 1;
        }
        return fibonacci(n - 2) + fibonacci(n - 1);
    }
}