package io.harman.sidating_app_be.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.harman.sidating_app_be.model.UserProfile;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private List<UserProfile> userProfiles = new ArrayList<>();
    public UserProfileController() {
        userProfiles.add(UserProfile.builder()
            .id(UUID.randomUUID())
            .name("Muhammad Hafiz")
            .nickname("Hafiz")
            .birthdate(LocalDate.of(2004, 8, 16))
            .hobbies("Coding")
            .gender("MALE")
            .location("Jakarta")
            .bio("Information System Student.")
            .email("hafiz@example.com")
            .phoneNumber("081234567898")
            .interests("Technology, Startups, AI")
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .isActive(true)
            .build()
        );

        userProfiles.add(UserProfile.builder()
            .id(UUID.randomUUID())
            .name("Dek Depe")
            .nickname("Depe")
            .birthdate(LocalDate.of(2001, 8, 10))
            .hobbies("Traveling")
            .gender("Female")
            .location("Depok")
            .bio("Master Coder.")
            .email("depe@example.com")
            .phoneNumber("082345678901")
            .interests("Traveling, Technology")
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .isActive(true)
            .build()
        );
    }

    public List<UserProfile> getAllProfiles() {
        return userProfiles;
    }


    @GetMapping
    public String getAllProfile(Model model) {
        model.addAttribute("userProfiles", userProfiles);
        return "profile/view-all";
    }

    @GetMapping("/{id}")
    public String getProfileById(@PathVariable UUID id, Model model) {
        Optional<UserProfile> userProfileOptional = userProfiles.stream()
            .filter(user -> user.getId().equals(id))
            .findFirst();

        if (userProfileOptional.isEmpty()) {
            model.addAttribute("title", "Profile Not Found");
            model.addAttribute("message", "Profile with ID " + id + " not found.");
            return "error/404";
        }

        model.addAttribute("userProfile", userProfileOptional.get());
        return "profile/detail";
    }

    @GetMapping("/create")
    public String formProfile(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("userProfile", new UserProfile());
        return "profile/form";
    }

    @PostMapping("/create")
    public String createProfile(@ModelAttribute UserProfile userProfile, RedirectAttributes redirectAttributes) {
        userProfile.setId(UUID.randomUUID());
        userProfile.setCreatedAt(LocalDate.now());
        userProfile.setUpdatedAt(LocalDate.now());
        userProfiles.add(userProfile);

        redirectAttributes.addFlashAttribute("successMessage", "Successfully create new profile with ID = " + userProfile.getId());
        return "redirect:/profile";
    }

    @GetMapping("/update/{id}")
    public String formEditProfile(@PathVariable UUID id, Model model) {
        Optional<UserProfile> userProfileOptional = userProfiles.stream()
            .filter(user -> user.getId().equals(id))
            .findFirst();

        if (userProfileOptional.isEmpty()) {
            model.addAttribute("title", "Profile Not Found");
            model.addAttribute("message", "Profile with ID " + id + " not found.");
            return "error/404";
        }

        model.addAttribute("isEdit", true);
        model.addAttribute("userProfile", userProfileOptional.get());
        return "profile/form";
    }

    @PostMapping("/update/{id}")
    public String updateProfile(@PathVariable UUID id, @ModelAttribute UserProfile updatedProfile, RedirectAttributes redirectAttributes) {
        for (int i = 0; i < userProfiles.size(); i++) {
            if (userProfiles.get(i).getId().equals(id)) {
                updatedProfile.setId(id);
                updatedProfile.setCreatedAt(userProfiles.get(i).getCreatedAt());
                updatedProfile.setUpdatedAt(LocalDate.now());
                userProfiles.set(i, updatedProfile);

                redirectAttributes.addFlashAttribute("successMessage", "Successfully update profile with ID = " + id);
                break;
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/delete/{id}")
    public String deleteProfile(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        boolean removed = userProfiles.removeIf(user -> user.getId().equals(id));
        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", "Successfully delete profile with ID = " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Profile with ID = " + id + " not found.");
        }
        return "redirect:/profile";
    }
}
