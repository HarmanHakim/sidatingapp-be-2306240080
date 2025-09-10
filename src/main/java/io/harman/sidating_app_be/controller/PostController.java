package io.harman.sidating_app_be.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;

@Controller
@RequestMapping("/posts")
public class PostController {
    
    @Autowired
    private UserProfileController userProfileController;
    
    private List<Post> posts = new ArrayList<>();
    
    public PostController() {
        // Initialize with some sample posts
        List<UserProfile> profiles = getAllProfiles();
        if (!profiles.isEmpty()) {
            posts.add(Post.builder()
                .id(UUID.randomUUID())
                .userProfileId(profiles.get(0).getId())
                .userProfile(profiles.get(0))
                .imageUrl("https://example.com/image1.jpg")
                .caption("Beautiful sunset today!")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build()
            );
        }
    }
    
    private List<UserProfile> getAllProfiles() {
        if (userProfileController == null) {
            return new ArrayList<>();
        }
        return userProfileController.getAllProfiles();
    }
    
    @GetMapping
    public String getAllPosts(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "newest") String order,
            Model model) {
        
        List<Post> filteredPosts = new ArrayList<>(posts);
        
        // Filter by user if specified
        if (userId != null) {
            filteredPosts = filteredPosts.stream()
                .filter(post -> post.getUserProfileId().equals(userId))
                .collect(Collectors.toList());
        }
        
        // Sort by order
        if ("newest".equals(order)) {
            filteredPosts.sort(Comparator.comparing(Post::getCreatedAt).reversed());
        } else if ("oldest".equals(order)) {
            filteredPosts.sort(Comparator.comparing(Post::getCreatedAt));
        }
        
        model.addAttribute("posts", filteredPosts);
        model.addAttribute("userProfiles", getAllProfiles());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedOrder", order);
        
        return "posts/view-all";
    }
    
    @GetMapping("/{id}")
    public String getPostById(@PathVariable UUID id, Model model) {
        Optional<Post> postOptional = posts.stream()
            .filter(post -> post.getId().equals(id))
            .findFirst();
            
        if (postOptional.isEmpty()) {
            model.addAttribute("title", "Post Not Found");
            model.addAttribute("message", "Post with ID " + id + " not found.");
            return "error/404";
        }
        
        model.addAttribute("post", postOptional.get());
        return "posts/detail";
    }
    
    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("post", new Post());
        model.addAttribute("userProfiles", getAllProfiles());
        return "posts/form";
    }
    
    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        post.setId(UUID.randomUUID());
        post.setCreatedAt(LocalDateTime.now());
        
        // Set the userProfile object based on userProfileId
        if (post.getUserProfileId() != null) {
            Optional<UserProfile> userProfile = getAllProfiles().stream()
                .filter(profile -> profile.getId().equals(post.getUserProfileId()))
                .findFirst();
            userProfile.ifPresent(post::setUserProfile);
        }
        
        posts.add(post);
        redirectAttributes.addFlashAttribute("successMessage", "Successfully created new post with ID = " + post.getId());
        return "redirect:/posts";
    }
    
    @GetMapping("/update/{id}")
    public String editPostForm(@PathVariable UUID id, Model model) {
        Optional<Post> postOptional = posts.stream()
            .filter(post -> post.getId().equals(id))
            .findFirst();
            
        if (postOptional.isEmpty()) {
            model.addAttribute("title", "Post Not Found");
            model.addAttribute("message", "Post with ID " + id + " not found.");
            return "error/404";
        }
        
        model.addAttribute("isEdit", true);
        model.addAttribute("post", postOptional.get());
        model.addAttribute("userProfiles", getAllProfiles());
        return "posts/form";
    }
    
    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable UUID id, @ModelAttribute Post updatedPost, RedirectAttributes redirectAttributes) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(id)) {
                updatedPost.setId(id);
                updatedPost.setCreatedAt(posts.get(i).getCreatedAt());
                
                // Set the userProfile object based on userProfileId
                if (updatedPost.getUserProfileId() != null) {
                    Optional<UserProfile> userProfile = getAllProfiles().stream()
                        .filter(profile -> profile.getId().equals(updatedPost.getUserProfileId()))
                        .findFirst();
                    userProfile.ifPresent(updatedPost::setUserProfile);
                }
                
                posts.set(i, updatedPost);
                redirectAttributes.addFlashAttribute("successMessage", "Successfully updated post with ID = " + id);
                break;
            }
        }
        return "redirect:/posts";
    }
    
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        boolean removed = posts.removeIf(post -> post.getId().equals(id));
        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted post with ID = " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Post with ID = " + id + " not found.");
        }
        return "redirect:/posts";
    }
}
