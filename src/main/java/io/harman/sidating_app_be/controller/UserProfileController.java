
package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public String viewAllProfiles(@RequestParam(name = "search", required = false) String search,
                                Model model) {
        List<ReadUserProfileDto> dtos = userProfileService.searchProfilesByName(search)
                .stream()
                .map(userProfileService::toReadUserProfileDto)
                .toList();

        model.addAttribute("userProfiles", dtos);
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
        model.addAttribute("userProfile", new CreateUserDto());
        return "profile/form";
    }

    @PostMapping("/create")
    public String createProfile(@ModelAttribute @Valid CreateUserDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile/create";
        }

        UserProfile created = userProfileService.createUserProfile(dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Successfully created profile with ID " + created.getId());
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

        UpdateUserDto dto = UpdateUserDto.builder()
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

        model.addAttribute("userProfile", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("profileId", id);

        return "profile/form";
    }

    @PutMapping("/update/{id}")
    public String updateProfile(@PathVariable UUID id,
                                @ModelAttribute @Valid UpdateUserDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile/update/" + id;
        }

        dto.setId(id);
        UserProfile updated = userProfileService.updateUserProfile(dto);
        if (updated == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Profile with ID " + id + " not found or deleted.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully updated profile with ID " + id);
        }
        return "redirect:/profile";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProfile(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        UserProfile removed = userProfileService.deleteProfile(id);
        if (removed != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully deleted profile with ID " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Profile with ID " + id + " not found or already deleted.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/match")
public String matchProfilesPost(@RequestParam("user1Id") UUID userId1,
                                @RequestParam("user2Id") UUID userId2,
                                Model model) {
    ReadUserProfileDto user1 = userProfileService.toReadUserProfileDto(
            userProfileService.getUserProfile(userId1)
    );
    ReadUserProfileDto user2 = userProfileService.toReadUserProfileDto(
            userProfileService.getUserProfile(userId2)
    );

    if (user1 == null || user2 == null) {
        model.addAttribute("error", "One or both profiles not found.");
        model.addAttribute("userProfiles", userProfileService.getAllUserProfilesDto()); 
        return "profile/match";
    }

    int score = userProfileService.getMatchScore(userId1, userId2);
    String message = userProfileService.getMatchMessage(score);
    String imageUrl = userProfileService.getMatchImage(score);

    model.addAttribute("user1", user1);
    model.addAttribute("user2", user2);
    model.addAttribute("matchScore", score);
    model.addAttribute("message", message);
    model.addAttribute("imageUrl", imageUrl);
    model.addAttribute("userProfiles", userProfileService.getAllUserProfilesDto());

    return "profile/match";
}


    @GetMapping("/match")
    public String showMatchForm(Model model) {
        model.addAttribute("userProfiles", userProfileService.getAllUserProfilesDto());
        return "profile/match";
    }



}
