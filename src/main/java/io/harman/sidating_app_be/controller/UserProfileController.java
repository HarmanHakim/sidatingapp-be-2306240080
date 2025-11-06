package io.harman.sidating_app_be.controller;

import java.util.List;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

// import org.h2.engine.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;
import jakarta.validation.Valid;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;

import java.time.LocalDate;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;


    @GetMapping
    // public String getAllProfile(Model model) {
    //     List<UserProfile> userProfilesList = userProfileService.getAllUserProfile();
    //     // Lakukan mapping dari List<UserProfile> ke List<ReadUserProfileDto>
    //     List<ReadUserProfileDto> dtoList = userProfilesList.stream()
    //             .map(userProfileService::mapToReadUserProfileDto)
    //             .toList();
    //     model.addAttribute("userProfiles", dtoList);
    //     return "profile/view-all";
    // }
    public String getAllProfile(@RequestParam(required = false) String search, Model model) {
        List<UserProfile> userProfiles;
        if (search != null && !search.trim().isEmpty()) {
            userProfiles = userProfileService.searchUserProfilesByName(search);
        } else {
            userProfiles = userProfileService.getAllUserProfile();
        }

        List<ReadUserProfileDto> dtoList = userProfiles.stream()
                .map(userProfileService::mapToReadUserProfileDto)
                .collect(Collectors.toList());

        model.addAttribute("userProfiles", dtoList);
        model.addAttribute("search", search); 
        
        return "profile/view-all";
    }

    @GetMapping("/{id}")
    public String getProfileById(@PathVariable UUID id, Model model) {
        UserProfile userProfile = userProfileService.getUserProfile(id);
        if (userProfile == null) {
            model.addAttribute("title", "Profile Not Found");
            model.addAttribute("message", "Profile with ID " + id + " not found.");
                return "error/404";
            }
    
            model.addAttribute("userProfile", userProfile);
            return "profile/detail";
    }

    @GetMapping("/create")
    public String formProfile(Model model) {
        model.addAttribute("isEdit", false);
        // Add the list of all user profiles to the model
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("userDto", new CreateUserDto());
        return "profile/form";
    }

    @PostMapping("/create")
    public String createProfile(@Valid @ModelAttribute CreateUserDto createUserDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // If there are validation errors, return to the form page with the errors
            model.addAttribute("isEdit", false);
            return "profile/form";
        }

        UserProfile createdProfile = userProfileService.createUserProfile(createUserDto);
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

        // Create and populate the DTO from the entity
        UpdateUserDto updateUserDto = UpdateUserDto.builder()
            .id(userProfile.getId())
            .name(userProfile.getName())
            .nickname(userProfile.getNickname())
            .birthdate(userProfile.getBirthdate())
            .hobbies(userProfile.getHobbies())
            .gender(userProfile.getGender())
            .location(userProfile.getLocation())
            .bio(userProfile.getBio())
            .email(userProfile.getEmail())
            .phoneNumber(userProfile.getPhoneNumber())
            .interests(userProfile.getInterests())
            .isActive(userProfile.isActive())
            .build();

        model.addAttribute("isEdit", true);
        model.addAttribute("userDto", updateUserDto); // Changed attribute name
        // Add the list of all user profiles to the model
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("isEdit", true);
        model.addAttribute("userDto", updateUserDto);
    
        return "profile/form";
    }

    @PutMapping("/update/{id}")
    public String updateProfile(@PathVariable UUID id, @Valid @ModelAttribute UpdateUserDto updateUserDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("userDto", updateUserDto); // Ensure the DTO is re-added to the model
            return "profile/form";
        }

        // You should ensure the ID from the path variable is set in the DTO
        updateUserDto.setId(id);
        UserProfile userProfile = userProfileService.updateUserProfile(updateUserDto);
        
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Profile with ID " + id + " not found.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Successfully update profile with ID " + id);
        }
        return "redirect:/profile";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProfile(@PathVariable UUID id,
                                RedirectAttributes redirectAttributes) {
        UserProfile removed = userProfileService.deleteProfile(id);

        if (removed != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully deleted profile with ID " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Profile with ID " + id + " not found.");
        }
        return "redirect:/profile";
    }

    @GetMapping("/match")
    public String match(@RequestParam(required = false) String userId1,
                       @RequestParam(required = false) String userId2,
                       Model model) {
        List<UserProfile> userProfilesList = userProfileService.getAllUserProfile();
        model.addAttribute("userProfiles", userProfilesList);

        if (userId1 != null && userId2 != null) {
            UUID id1 = UUID.fromString(userId1);
            UUID id2 = UUID.fromString(userId2);

            UserProfile user1 = userProfileService.getUserProfile(id1);
            UserProfile user2 = userProfileService.getUserProfile(id2);

            if (user1 == null || user2 == null) {
                model.addAttribute("title", "Match Error");
                model.addAttribute("message", "One or both user profiles not found.");
                return "error/404";
            }
            
            // Call the correct service methods
            int score = userProfileService.getRandomMatchScore(id1, id2);
            String message = userProfileService.getMatchMessage(score);
            String imageLink = userProfileService.getMatchImage(score);

            model.addAttribute("user1", user1);
            model.addAttribute("user2", user2);
            model.addAttribute("score", score);
            model.addAttribute("message", message);
            model.addAttribute("imageLink", imageLink);
        }
        return "profile/match";
    }

}


