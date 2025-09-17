package io.harman.sidating_app_be.controller;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public String getAllProfile(Model model) {
        List<UserProfile> userProfilesList = userProfileService.getAllUserProfile();
        model.addAttribute("userProfiles", userProfilesList);
        return "profile/view-all";
    }

    @GetMapping("/{id}")
    public String getProfileById(@PathVariable UUID id, Model model) {
        UserProfile userProfile = userProfileService.getUserProfile(id);
        
        if (userProfile == null) {
            model.addAttribute("title","Profile Not Found");
            model.addAttribute("message", "Profile with ID " + id + " not found.");
            return "error/404";
        }
        
        model.addAttribute("userProfile", userProfile);
        return "profile/detail";
    }

    @GetMapping("/create")
    public String formProfile(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("userProfile", new UserProfile());
        return "profile/form";
    }

    @PostMapping("/create")
    public String createProfile(@ModelAttribute UserProfile userProfile,
                              RedirectAttributes redirectAttributes) {
        UserProfile createdProfile = userProfileService.createUserProfile(userProfile);
        redirectAttributes.addFlashAttribute("successMessage",
            "Successfully create new profile with ID " + createdProfile.getId());
        return "redirect:/profile";
    }

    @GetMapping("/update/{id}")
    public String formEditProfile(@PathVariable UUID id, Model model) {
        UserProfile userProfile = userProfileService.getUserProfile(id);
        
        if (userProfile == null) {
            model.addAttribute("title", "Profile Not Found");
            model.addAttribute("message", "Profile with ID " + id + " not found.");
            return "error/404";
        }
        
        model.addAttribute("isEdit", true);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("profileId", id);
        
        return "profile/form";
    }

    @PutMapping("/update/{id}")
    public String updateProfile(@PathVariable UUID id,
                              @ModelAttribute UserProfile updatedProfile,
                              RedirectAttributes redirectAttributes) {
        UserProfile userProfile = userProfileService.updateUserProfile(updatedProfile);
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Profile with ID " + id + " not found.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                "Successfully update profile with ID " + id);
        }
        return "redirect:/profile";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProfile(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        UserProfile removed = userProfileService.deleteProfile(id);
        
        if (removed != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                "Successfully delete profile with ID " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Profile with ID " + id + " not found.");
        }
        
        return "redirect:/profile";
    }
    @GetMapping("/match")
    public String matchPage(@RequestParam(value = "userId1", required = false) UUID userId1,
                            @RequestParam(value = "userId2", required = false) UUID userId2,
                            Model model) {
        
        model.addAttribute("allUsers", userProfileService.getAllUserProfile());

        if (userId1 != null && userId2 != null) {
            Map<String, Object> result = userProfileService.calculateMatchScore(userId1, userId2);
            model.addAttribute("matchResult", result); // 'result' adalah Map
            model.addAttribute("selectedUser1", userId1);
            model.addAttribute("selectedUser2", userId2);
        }

        return "profile/match";
    }
}
