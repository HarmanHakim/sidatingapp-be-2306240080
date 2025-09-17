package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.service.PostService;
import io.harman.sidating_app_be.service.UserProfileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "desc") String sort,
            Model model) {

        List<Post> filteredPosts = postService.getAllPost(userId, sort);

        model.addAttribute("posts", filteredPosts);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("selectedUser", userId);
        model.addAttribute("selectedSort", sort);

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
        model.addAttribute("post", new Post());
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("isEdit", false);
        return "posts/form";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        Post newPost = postService.createPost(post);
        if (newPost == null) {
            redirectAttributes.addFlashAttribute("message", "Failed create new post");
            return "redirect:/posts";
        }
        redirectAttributes.addFlashAttribute("message", "Successfully create new post");
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

        model.addAttribute("post", post);
        model.addAttribute("userProfiles", userProfileService.getAllUserProfile());
        model.addAttribute("postId", id);
        model.addAttribute("isEdit", true);

        return "posts/form";
    }

    @PutMapping("/update/{id}")
    public String updatePost(@PathVariable UUID id,
                           @ModelAttribute Post post,
                           RedirectAttributes redirectAttributes) {
        Post updatedPost = postService.updatePost(post);
        if (updatedPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Post with ID " + id + " not found.");
            return "redirect:/posts";
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
                "Post with ID " + id + " not found.");
        }

        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable UUID id,
                           @RequestParam UUID userId,
                           RedirectAttributes redirectAttributes) {
        Post likedPost = postService.likePost(id, userId);
        if (likedPost == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to like post.");
        } else {        
            redirectAttributes.addFlashAttribute("successMessage",
            "Post liked!");
        }
        return "redirect:/posts/" + id;
    }
}