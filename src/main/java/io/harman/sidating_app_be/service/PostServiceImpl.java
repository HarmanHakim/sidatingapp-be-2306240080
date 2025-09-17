package io.harman.sidating_app_be.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;

@Service
public class PostServiceImpl implements PostService {

    private final UserProfileService userProfileService;

    public PostServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private final List<Post> postDB = new ArrayList<>();

    @Override
    public Post createPost(Post post) {
        Optional<UserProfile> optionalUserProfile = userProfileService.getAllUserProfile().stream()
                .filter(user -> user.getId().equals(post.getUserProfileId()))
                .findFirst();
        if (optionalUserProfile.isEmpty()) {
            return null;
        }

        post.setUserProfile(optionalUserProfile.get());
        post.setId(UUID.randomUUID());
        post.setCreatedAt(LocalDateTime.now());
        postDB.add(post);

        return post;
    }

    @Override
    public List<Post> getAllPost(UUID userId, String sort) {
        List<Post> filteredPosts = new ArrayList<>(postDB);

        if (userId != null) {
            filteredPosts = filteredPosts.stream()
                    .filter(p -> p.getUserProfile() != null && p.getUserProfile().getId().equals(userId))
                    .toList();
        }

        filteredPosts = filteredPosts.stream()
                .sorted((p1, p2) -> {
                    if ("asc".equalsIgnoreCase(sort)) {
                        return p1.getCreatedAt().compareTo(p2.getCreatedAt());
                    } else {
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    }
                })
                .toList();
        
        return filteredPosts;
    }

    @Override
    public Post getPost(UUID id) {
        for (Post post : postDB) {
            if (id.equals(post.getId())) {
                return post;
            }
        }
        return null;
    }

    @Override
    public Post updatePost(Post updatedPost) {
        for (int i = 0; i < postDB.size(); i++) {
            if (updatedPost.getId().equals(postDB.get(i).getId())) {
                Optional<UserProfile> optionalUserProfile = userProfileService.getAllUserProfile().stream()
                        .filter(user -> user.getId().equals(updatedPost.getUserProfileId()))
                        .findFirst();
                if (optionalUserProfile.isEmpty()) {
                    return null;
                }
                updatedPost.setUserProfile(optionalUserProfile.get());
                updatedPost.setId(postDB.get(i).getId());
                updatedPost.setCreatedAt(postDB.get(i).getCreatedAt());
                postDB.set(i, updatedPost);

                return updatedPost;
            }
        }
        return null;
    }

    @Override
    public Post deletePost(UUID id) {
        for (Post post : postDB) {
            if (id.equals(post.getId())) {
                Post deletedPost = post;
                postDB.remove(post);
                return deletedPost;
            }
        }
        return null;
    }
    @Override
    public Post likePost(UUID postId, UUID userId) {
        Post post = getPost(postId);
        if (post == null) return null;

        UserProfile user = userProfileService.getUserProfile(userId);
        if (user == null) return null;

        if (post.getLikes() == null) {
            post.setLikes(new ArrayList<>());
        }

        boolean alreadyLiked = post.getLikes().stream()
                .anyMatch(u -> u.getId().equals(userId));
        if (!alreadyLiked) {
            post.getLikes().add(user);
        } else {
            post.getLikes().removeIf(u -> u.getId().equals(userId));
        }

        return post;
    }
}