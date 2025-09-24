package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserProfileService userProfileService;


    @GetMapping
    public String viewAllPosts(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "desc") String order, 
            Model model) {

        UUID userUuid = null;
        if (userId != null && !"all".equals(userId)) {
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", "Invalid user filter, showing all posts.");
            }
        }

        String sortOrder = "desc"; 
        if ("oldest".equals(order)) {
            sortOrder = "asc";
        } else if ("newest".equals(order)) {
            sortOrder = "desc";
        }

        List<ReadPostDto> posts = postService.getAllPostsDto(userUuid, sortOrder);

        if (posts == null) posts = new ArrayList<>();

        model.addAttribute("posts", posts);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());

        model.addAttribute("selectedUserId", userId != null ? userId : "all");
        model.addAttribute("selectedOrder", order != null ? order : "newest"); 

        return "posts/view-all";
    }
    
    @GetMapping("/{id}")
    public String viewPost(@PathVariable UUID id, Model model) {
        Post post = postService.getPost(id);
        if (post == null) {
            model.addAttribute("title", "Post Not Found");
            model.addAttribute("message", "Post with ID " + id + " not found.");
            return "error/404";
        }

        model.addAttribute("post", post);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        return "posts/detail";
    }

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new CreatePostDto());
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("isEdit", false);
        return "posts/form";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute @Valid CreatePostDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/posts/create";
        }

        Post newPost = postService.createPost(dto);
        if (newPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create new post.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Successfully created post.");
        }
        return "redirect:/posts";
    }

    @GetMapping("/update/{id}")
    public String updatePostForm(@PathVariable UUID id, Model model) {
        Post post = postService.getPost(id);
        if (post == null) {
            model.addAttribute("title", "Post Not Found");
            model.addAttribute("message", "Post with ID " + id + " not found.");
            return "error/404";
        }

        UpdatePostDto dto = UpdatePostDto.builder()
                .id(post.getId())
                .userProfileId(post.getUserProfileId())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .isActive(post.isActive())
                .build();

        model.addAttribute("post", dto);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("postId", id);
        model.addAttribute("isEdit", true);

        return "posts/form";
    }

    @PutMapping("/update/{id}")
    public String updatePost(@PathVariable UUID id,
                             @ModelAttribute @Valid UpdatePostDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/posts/update/" + id;
        }

        dto.setId(id);
        Post updatedPost = postService.updatePost(dto);
        if (updatedPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Post with ID " + id + " not found or deleted.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully updated post with ID " + id);
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Successfully update post with ID " + id);
        return "redirect:/posts";
    }

    @DeleteMapping("/delete/{id}")
    public String deletePost(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Post removedPost = postService.deletePost(id);
        if (removedPost != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully delete post with ID " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Post with ID " + id + " not found or already deleted.");
        }
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable UUID id,
                           @RequestParam UUID userId,
                           RedirectAttributes redirectAttributes) {
        Post likedPost = postService.likePost(id, userId);
        if (likedPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to like/unlike post.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Post liked/unliked successfully.");
        }
        return "redirect:/posts/" + id;
    }

}
