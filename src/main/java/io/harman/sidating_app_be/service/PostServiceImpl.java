package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Duration;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserProfileService userProfileService;

    @Override
    public Post createPost(CreatePostDto dto) {
        UserProfile userProfile = userProfileService.getUserProfile(dto.getUserProfileId());
        if (userProfile == null) {
            return null;
        }

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userProfile(userProfile)
                .userProfileId(dto.getUserProfileId())
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
        if (userId != null) {
            if ("asc".equalsIgnoreCase(sort)) {
                return postRepository.findByUserProfileIdOrderByCreatedAtAsc(userId);
            } else {
                return postRepository.findByUserProfileIdOrderByCreatedAtDesc(userId);
            }
        } else {
            if ("asc".equalsIgnoreCase(sort)) {
                return postRepository.findAllByOrderByCreatedAtAsc();
            } else {
                return postRepository.findAllByOrderByCreatedAtDesc();
            }
        }
    }

   @Override
    public ReadPostDto mapToReadPostDto(Post post) {
        if (post == null) {
            return null;
        }

        // Menghitung 'timeAgo'
        Duration duration = Duration.between(post.getCreatedAt(), LocalDateTime.now());
        String timeAgo;
        if (duration.toHours() < 1) {
            timeAgo = "Just Now";
        } else if (duration.toDays() < 1) {
            timeAgo = duration.toHours() + " hours ago";
        } else if (duration.toDays() < 7) {
            timeAgo = duration.toDays() + " days ago";
        } else if (duration.toDays() < 28) {
            timeAgo = (duration.toDays() / 7) + " weeks ago";
        } else if (duration.toDays() < 335) {
            timeAgo = (duration.toDays() / 30) + " months ago";
        } else {
            timeAgo = (duration.toDays() / 365) + " years ago";
        }
        
        Integer likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        String userProfileName = post.getUserProfile() != null ? post.getUserProfile().getName() : "Unknown User";

        return ReadPostDto.builder()
                .id(post.getId())
                .userProfileId(post.getUserProfileId())
                .userProfileName(userProfileName)
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likes(post.getLikes() != null ? post.getLikes().stream().map(UserProfile::getName).toList() : null)
                .likeCount(likeCount)
                .timeAgo(timeAgo)
                .isLikedByCurrentUser(false) // Selalu false karena tidak ada info user
                .build();
    }

    @Override
    public Post getPost(UUID id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public Post updatePost(UpdatePostDto dto) {
        Optional<Post> optionalPost = postRepository.findById(dto.getId());
        if (optionalPost.isEmpty()) {
            return null;
        }

        Post post = optionalPost.get();
        post.setCaption(dto.getCaption());
        post.setImageUrl(dto.getImageUrl());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Override
    public Post deletePost(UUID id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            return null;
        }

        postRepository.delete(optionalPost.get());
        return optionalPost.get();
    }


    // @Override
    // public UserProfile deleteProfile(UUID id) {
    //     Optional<UserProfile> optionalUserProfile = userProfileRepository.findById(id);
    //     if (optionalUserProfile.isEmpty()) {
    //         return null;
    //     }
    //     UserProfile userProfile = optionalUserProfile.get();
    //     // Hibernate akan menjalankan SQLDelete, tidak menghapus permanen
    //     userProfileRepository.delete(userProfile);
    //     return userProfile;
    // }

    @Override
    public Post likePost(UUID postId, UUID userId) {
        Post post = getPost(postId);
        if (post == null) {
            return null;
        }

        UserProfile user = userProfileService.getUserProfile(userId);
        if (user == null) {
            return null;
        }
        
        // Inisialisasi list likes jika masih null
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

        return postRepository.save(post);
    }
}
