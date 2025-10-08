package io.harman.sidating_app_be.restService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;

@Service
public class PostRestServiceImpl implements PostRestService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public List<PostResponseDTO> getAllPosts() {
        List<Post> posts = postRepository.findAllByDeletedAtIsNull();
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByUserId(UUID userId) {
        List<Post> posts = postRepository.findByUserProfileIdAndDeletedAtIsNull(userId);
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);

        List<Post> posts = postRepository.findByCreatedAtBetweenAndDeletedAtIsNull(startOfDay, endOfDay);
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostByUserIdAndDate(UUID userId, String dateString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);

        List<Post> posts = postRepository.findByUserProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(userId, startOfDay, endOfDay);
        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(UUID id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        return mapToPostResponseDTO(post);
    }

    @Override
    public PostResponseDTO createPost(CreatePostRequestDTO dto) {

        UserProfile userProfile = userProfileRepository.findById(dto.getUserProfileId())
            .orElse(null);
        if (userProfile == null) {
            return null;
        }

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userProfile(userProfile)
                .userProfileId(userProfile.getId())
                .imageUrl(dto.getImageUrl())
                .caption(dto.getCaption())
                .isActive(true)
                .build();

        return mapToPostResponseDTO(postRepository.save(post));
    }

    @Override
    public PostResponseDTO updatePost(UpdatePostRequestDTO updatePostRequestDto) {
        Post post = postRepository.findById(updatePostRequestDto.getId()).orElse(null);

        if (post == null) return null;

        post = post.toBuilder()
                .id(post.getId())
                .userProfileId(post.getUserProfileId())
                .imageUrl(updatePostRequestDto.getImageUrl() == null ? post.getImageUrl() : updatePostRequestDto.getImageUrl())
                .caption(updatePostRequestDto.getCaption() == null ? post.getCaption() : updatePostRequestDto.getCaption())
                .build();

        return mapToPostResponseDTO(postRepository.save(post));
    }

    @Override
    public PostResponseDTO deletePost(DeletePostRequestDTO deletePostRequestDTO) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(deletePostRequestDTO.getId()).orElse(null);
        if (post == null) return null;
        post.setDeletedAt(LocalDateTime.now());
        return mapToPostResponseDTO(postRepository.save(post));
    }

    @Override
    public PostResponseDTO likePost(LikePostRequestDTO likePostRequestDTO) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(likePostRequestDTO.getId()).orElse(null);
        if (post == null) return null;
        UserProfile userProfile  = userProfileRepository.findByIdAndDeletedAtIsNull(likePostRequestDTO.getUserProfileId()).orElse(null);
        if (userProfile == null) return null;
        List<UserProfile> likeList = post.getLikes();
        boolean isLiked = likeList.stream().anyMatch(
                userProfile1 -> userProfile1.getName().equals(userProfile.getName()));
        if (isLiked) {
            likeList.remove(userProfile);
        } else {
            likeList.add(userProfile);
        }
        post.setLikes(likeList);
        return mapToPostResponseDTO(postRepository.save(post));

    }

    private PostResponseDTO mapToPostResponseDTO(Post post) {
        String userProfileName = "Unknown User";
        if (post.getUserProfile() != null) {
            userProfileName = post.getUserProfile().getName();
        }

        String timeAgo = "Unknown time";
        if (post.getCreatedAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            long hours = ChronoUnit.HOURS.between(post.getCreatedAt(), now);
            long days = ChronoUnit.DAYS.between(post.getCreatedAt(), now);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

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
}