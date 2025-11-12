package io.harman.sidating_app_be.restService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.repository.PostRepository;
import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.post.*;
import io.harman.sidating_app_be.security.jwt.JwtUtils;

@Service
@Transactional
public class PostRestServiceImpl implements PostRestService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public List<PostResponseDTO> getAllPosts() {
        List<Post> posts = postRepository.findAll()
                .stream()
                .filter(post -> post.getDeletedAt() == null) // Filter posts yang belum di-delete
                .collect(Collectors.toList());
        
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByUserId(UUID userId) {
        List<Post> posts = postRepository.findByUserProfileId(userId)
                .stream()
                .filter(post -> post.getDeletedAt() == null) // Filter deleted posts
                .collect(Collectors.toList());
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);

        List<Post> posts = postRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                .stream()
                .filter(post -> post.getDeletedAt() == null) // Filter deleted posts
                .collect(Collectors.toList());
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByUserIdAndDate(UUID userId, String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);

        List<Post> posts = postRepository.findByUserProfileIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
                .stream()
                .filter(post -> post.getDeletedAt() == null) // Filter deleted posts
                .collect(Collectors.toList());
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PostResponseDTO getPostById(UUID id) {
        Post post = postRepository.findById(id).orElse(null);
        
        // Check if post exists and not deleted
        if (post == null || post.getDeletedAt() != null) {
            return null;
        }
        
        return mapToPostResponseDTO(post);
    }

    @Override
    public PostResponseDTO createPost(CreatePostRequestDTO createPostRequestDTO) {
        System.out.println("=== CREATE POST DEBUG ===");
        System.out.println("Caption: " + createPostRequestDTO.getCaption());
        System.out.println("ImageURL: " + createPostRequestDTO.getImageUrl());
        System.out.println("UserProfileId from DTO: " + createPostRequestDTO.getUserProfileId());
        
        // Get authenticated user
        UserProfile authUser = getAuthenticatedUser();
        System.out.println("Authenticated user: " + authUser.getUsername() + " (ID: " + authUser.getId() + ")");
        
        // Determine which user profile to use for the post
        UUID userProfileId;
        if (isAdmin(authUser) && createPostRequestDTO.getUserProfileId() != null) {
            // Admin can specify which user profile to create post for
            userProfileId = createPostRequestDTO.getUserProfileId();
            System.out.println("Admin creating post for user: " + userProfileId);
            
            // Verify that the specified user profile exists
            UserProfile targetUser = userProfileRepository.findById(userProfileId).orElse(null);
            if (targetUser == null) {
                throw new RuntimeException("User profile not found with id: " + userProfileId);
            }
        } else {
            // Regular users create post with their own profile
            userProfileId = authUser.getId();
            System.out.println("Regular user creating post with own ID: " + userProfileId);
        }
        
        try {
            Post post = Post.builder()
                    .id(UUID.randomUUID())
                    .userProfileId(userProfileId)
                    .imageUrl(createPostRequestDTO.getImageUrl())
                    .caption(createPostRequestDTO.getCaption())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            System.out.println("Post object created with ID: " + post.getId());
            System.out.println("Saving to database...");
            
            Post savedPost = postRepository.save(post);
            
            System.out.println("Post saved successfully! ID: " + savedPost.getId());
            System.out.println("=== END CREATE POST DEBUG ===");
            
            return mapToPostResponseDTO(savedPost);
            
        } catch (Exception e) {
            System.err.println("ERROR saving post: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save post to database: " + e.getMessage());
        }
    }

 
    @Override
    public PostResponseDTO updatePost(UpdatePostRequestDTO updatePostRequestDto) {
        Post post = postRepository.findById(updatePostRequestDto.getId()).orElse(null);
        
        // Check if post exists and not deleted
        if (post == null || post.getDeletedAt() != null) {
            return null; // Post tidak ditemukan atau sudah di-delete
        }

        // Get authenticated user and check authorization
        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(post.getUserProfileId());
        
        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("You are not authorized to edit this post");
        }

        post = post.toBuilder()
                .id(updatePostRequestDto.getId())
                .caption(updatePostRequestDto.getCaption())
                .imageUrl(updatePostRequestDto.getImageUrl())
                .updatedAt(LocalDateTime.now())
                .build();
        return mapToPostResponseDTO(postRepository.save(post));
    }

    @Override
    public void deletePost(DeletePostRequestDTO deletePostRequestDTO) {
        Post post = postRepository.findById(deletePostRequestDTO.getId()).orElse(null);
        
        if (post == null) {
            throw new RuntimeException("Post dengan id " + deletePostRequestDTO.getId() + " tidak ditemukan");
        }

        // Get authenticated user and check authorization
        UserProfile authUser = getAuthenticatedUser();
        boolean isOwner = authUser.getId().equals(post.getUserProfileId());
        
        if (!isOwner && !isAdmin(authUser)) {
            throw new SecurityException("You are not authorized to delete this post");
        }
        
        // Soft delete: set deletedAt timestamp
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void likePost(LikePostRequestDTO likePostRequestDTO) {
        // Cek apakah post exists
        Post existingPost = postRepository.findById(likePostRequestDTO.getId()).orElse(null);
        
        if (existingPost == null || existingPost.getDeletedAt() != null) {
            throw new RuntimeException("Post tidak ditemukan atau sudah dihapus");
        }

        // Get authenticated user
        UserProfile authUser = getAuthenticatedUser();

        // Initialize likes list if null
        if (existingPost.getLikes() == null) {
            existingPost.setLikes(new ArrayList<>());
        }

        // Check if user already liked this post
        boolean alreadyLiked = existingPost.getLikes().stream()
                .anyMatch(user -> user.getId().equals(authUser.getId()));

        if (alreadyLiked) {
            // Unlike: remove user from likes
            existingPost.getLikes().removeIf(user -> user.getId().equals(authUser.getId()));
        } else {
            // Like: add user to likes
            existingPost.getLikes().add(authUser);
        }

        // Save the updated post
        postRepository.save(existingPost);
    }

    


    private PostResponseDTO mapToPostResponseDTO(Post post) {
        String userProfileName = "Unknown User";
        // Fetch user profile berdasarkan userProfileId
        if (post.getUserProfileId() != null) {
            UserProfile userProfile = userProfileRepository.findById(post.getUserProfileId()).orElse(null);
            if (userProfile != null) {
                userProfileName = userProfile.getName();
            }
        }

        String timeAgo = "Unknown time";
        if (post.getCreatedAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            long hours = ChronoUnit.HOURS.between(post.getCreatedAt(), now);
            long days = ChronoUnit.DAYS.between(post.getCreatedAt(), now);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            // ini yang dinamis
            if (hours < 1) {
                timeAgo = "Just Now";
            } else if (hours < 24) {
                timeAgo = hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (days < 7) {
                timeAgo = days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (weeks < 4) {
                timeAgo = weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
            } else if (months < 12) {
                timeAgo = months + " month" + (months > 1 ? "s" : "") + " ago";
            } else {
                timeAgo = years + " year" + (years > 1 ? "s" : "") + " ago";
            }
        }

        List<String> likes = new ArrayList<>();
        if (post.getLikes() != null) {
            for (UserProfile user : post.getLikes()) {
                likes.add(user.getNickname());
            }
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .userProfileId(post.getUserProfileId())
                .userProfileName(userProfileName)
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likes(likes)
                .likeCount(likes.size())
                .timeAgo(timeAgo)
                .build();
    }

    private UserProfile getAuthenticatedUser() {
        String currentUsername = jwtUtils.getCurrentUsername();
        UserProfile authUser = userProfileRepository.findByUsername(currentUsername);
        if (authUser == null) {
            throw new UsernameNotFoundException("Authenticated user not found.");
        }
        return authUser;
    }

    private boolean isAdmin(UserProfile user) {
        return user.getRole() != null && "Admin".equalsIgnoreCase(user.getRole().getRoleName());
    }
}