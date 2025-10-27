package io.harman.sidating_app_be.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserProfileService userProfileService;

    public PostServiceImpl(PostRepository postRepository, UserProfileService userProfileService) {
        this.postRepository = postRepository;
        this.userProfileService = userProfileService;
    }


    @Override
public Post createPost(CreatePostDto dto) {
    UserProfile user = userProfileService.getUserProfile(dto.getUserProfileId());

    if (user == null || !user.isActive()) {
        return null;
    }

    Post post = Post.builder()
        .id(UUID.randomUUID())
            .userProfile(user)
            .userProfileId(user.getId())
            .imageUrl(dto.getImageUrl())
            .caption(dto.getCaption())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

    return postRepository.save(post);
}


    @Override
    public List<Post> getAllPost(UUID userId, String sort) {
        List<Post> posts;

        if (userId != null) {
            posts = postRepository.findByUserProfileIdAndDeletedAtIsNull(userId);
        } else {
            posts = postRepository.findByDeletedAtIsNull();
        }

        posts.sort((p1, p2) -> "asc".equalsIgnoreCase(sort)
                ? p1.getCreatedAt().compareTo(p2.getCreatedAt())
                : p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        return posts;
    }


    @Override
    public Post getPost(UUID id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.isDeleted()) return null;
        return post;
    }

    @Override
    public Post updatePost(UpdatePostDto dto) {
        Post existing = postRepository.findById(dto.getId()).orElse(null);
        if (existing == null || existing.isDeleted()) return null;

        UserProfile user = userProfileService.getUserProfile(dto.getUserProfileId());
        if (user == null || user.isDeleted()) return null;

        existing.setUserProfile(user);
        existing.setUserProfileId(dto.getUserProfileId());
        existing.setImageUrl(dto.getImageUrl());
        existing.setCaption(dto.getCaption());
        if (dto.getIsActive() != null) existing.setActive(dto.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(existing);
    }

    @Override
    public Post deletePost(UUID id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.isDeleted()) return null;

        post.setDeletedAt(LocalDateTime.now());
        return postRepository.save(post); 
    }

    @Override
    public Post likePost(UUID postId, UUID userId) {
        Post post = getPost(postId);
        if (post == null) return null;

        UserProfile user = userProfileService.getUserProfile(userId);
        if (user == null || user.isDeleted()) return null;

        if (post.getLikes() == null) post.setLikes(new ArrayList<>());

        boolean alreadyLiked = post.getLikes().stream()
                .anyMatch(u -> u.getId().equals(userId));

        if (!alreadyLiked) {
            post.getLikes().add(user);
        } else {
            post.getLikes().removeIf(u -> u.getId().equals(userId));
        }

        return postRepository.save(post);
    }

    public ReadPostDto toReadPostDto(Post post) {
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        List<String> likeNames = post.getLikes() != null
                ? post.getLikes().stream().map(UserProfile::getName).collect(Collectors.toList())
                : List.of();

        String timeAgo = getTimeAgo(post.getCreatedAt());

        return ReadPostDto.builder()
                .id(post.getId())
                .userProfileId(post.getUserProfile().getId())
                .userProfileName(post.getUserProfile().getName())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likes(likeNames)
                .likeCount(likeCount)
                .timeAgo(timeAgo)
                .build();
    }

    private String getTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 3600) return "Just Now";
        else if (seconds < 86400) return (seconds / 3600) + " hours ago";
        else if (seconds < 604800) return (seconds / 86400) + " days ago";
        else if (seconds < 2419200) return (seconds / 604800) + " weeks ago";
        else if (seconds < 29030400) return (seconds / 2419200) + " months ago";
        else return (seconds / 29030400) + " years ago";
    }

    @Override
    public List<ReadPostDto> getAllPostsDto(UUID userId, String sort) {
        return getAllPost(userId, sort).stream()
                .map(this::toReadPostDto) 
                .collect(Collectors.toList()); 
    }

}
