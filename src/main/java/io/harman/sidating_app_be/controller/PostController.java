package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.service.PostService;
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
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public String getAllPosts(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "desc") String sort,
            Model model) {

        List<Post> posts = postService.getAllPost(userId, sort);

        // Mapping ke DTO
        List<ReadPostDto> dtoList = posts.stream()
                .map(postService::mapToReadPostDto)
                .toList();

        model.addAttribute("posts", dtoList);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());

        return "post/view-all";
    }

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new CreatePostDto());
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("isEdit", false);
        return "post/form";
    }

    @PostMapping("/create")
    public String createPost(
            @Valid @ModelAttribute("post") CreatePostDto createPostDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
            return "post/form";
        }

        Post created = postService.createPost(createPostDto);
        if (created == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User profile not found or an error occurred.");
            return "redirect:/post/create";
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "Post created successfully with ID " + created.getId());
        return "redirect:/post";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable UUID id, Model model) {
        Post post = postService.getPost(id);
        if (post == null) {
            model.addAttribute("title", "Post Not Found");
            model.addAttribute("message", "Post with ID " + id + " not found.");
            return "error/404";
        }
        
        // Tambahkan objek 'post' ke model
        model.addAttribute("post", post);
        
        // Tambahkan list 'userProfiles' ke model agar bisa digunakan di Thymeleaf
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        
        return "post/detail";
    }

    @GetMapping("/update/{id}")
    public String updatePostForm(@PathVariable UUID id, Model model) {
        Post postEntity = postService.getPost(id);
        if (postEntity == null) {
            return "redirect:/posts";
        }

        UpdatePostDto updateDto = new UpdatePostDto();
        updateDto.setId(postEntity.getId());
        updateDto.setCaption(postEntity.getCaption());
        updateDto.setImageUrl(postEntity.getImageUrl());
        updateDto.setUserProfileId(postEntity.getUserProfileId());

        model.addAttribute("post", updateDto);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("isEdit", true);

        return "post/form";
    }

    @PutMapping("/update/{id}")
    public String updatePost(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdatePostDto updatePostDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
            return "post/form";
        }

        updatePostDto.setId(id);
        Post updated = postService.updatePost(updatePostDto);

        if (updated == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post with ID " + updatePostDto.getId() + " not found.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Post updated successfully with ID " + updated.getId());
        }

        return "redirect:/post";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Post deleted = postService.deletePost(id);
        if (deleted == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post with ID " + id + " not found.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Post deleted successfully.");
        }
        return "redirect:/post";
    }

    @PostMapping("/like/{postId}")
    public String likePost(
        @PathVariable UUID postId,
        @RequestParam UUID userProfileId,
        RedirectAttributes redirectAttributes) {

        Post post = postService.likePost(postId, userProfileId);
        if (post == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post or User not found.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Post liked/unliked successfully.");
        }
        return "redirect:/post/" + postId;
    }

    @GetMapping("/post/{id}")
    public String getPostDetail(@PathVariable UUID id, Model model) {
        Post post = postService.getPost(id);

        if (post == null) {
            return "redirect:/post"; // kalau ga ketemu
        }

        // kirim post
        model.addAttribute("post", post);

        // kirim userProfiles biar dropdown keisi
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());

        return "post/detail";
    }

}
