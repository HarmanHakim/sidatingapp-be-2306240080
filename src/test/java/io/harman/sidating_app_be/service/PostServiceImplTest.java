package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private PostServiceImpl postService;

    private UUID postId;
    private UUID userProfileId;
    private UUID anotherUserId;
    private Post samplePost;
    private UserProfile sampleUserProfile;
    private UserProfile anotherUserProfile;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userProfileId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        sampleUserProfile = new UserProfile();
        sampleUserProfile.setId(userProfileId);
        sampleUserProfile.setName("Test User");

        anotherUserProfile = new UserProfile();
        anotherUserProfile.setId(anotherUserId);
        anotherUserProfile.setName("Another User");

        samplePost = Post.builder()
                .id(postId)
                .userProfile(sampleUserProfile)
                .userProfileId(userProfileId)
                .imageUrl("test-image.jpg")
                .caption("Test Caption")
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .isActive(true)
                .likes(new ArrayList<>())
                .build();
    }

    @Test
    void createPost_ValidInput_ShouldReturnCreatedPost() {
        // Given
        CreatePostDto createPostDto = new CreatePostDto();
        createPostDto.setUserProfileId(userProfileId);
        createPostDto.setImageUrl("test-image.jpg");
        createPostDto.setCaption("Test Caption");

        when(userProfileService.getUserProfile(userProfileId)).thenReturn(sampleUserProfile);
        when(postRepository.save(any(Post.class))).thenReturn(samplePost);

        // When
        Post result = postService.createPost(createPostDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCaption()).isEqualTo("Test Caption");
        assertThat(result.getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(result.getUserProfileId()).isEqualTo(userProfileId);
        assertThat(result.getUserProfile()).isEqualTo(sampleUserProfile);
        assertThat(result.isActive()).isTrue();

        verify(userProfileService).getUserProfile(userProfileId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_UserProfileNotFound_ShouldReturnNull() {
        // Given
        CreatePostDto createPostDto = new CreatePostDto();
        createPostDto.setUserProfileId(userProfileId);
        createPostDto.setImageUrl("test-image.jpg");
        createPostDto.setCaption("Test Caption");

        when(userProfileService.getUserProfile(userProfileId)).thenReturn(null);

        // When
        Post result = postService.createPost(createPostDto);

        // Then
        assertThat(result).isNull();
        verify(userProfileService).getUserProfile(userProfileId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void getAllPost_WithoutUserIdDescSort_ShouldReturnAllPostsDescending() {
        // Given
        List<Post> expectedPosts = Arrays.asList(samplePost);
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedPosts);

        // When
        List<Post> result = postService.getAllPost(null, "desc");

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(postRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllPost_WithoutUserIdAscSort_ShouldReturnAllPostsAscending() {
        // Given
        List<Post> expectedPosts = Arrays.asList(samplePost);
        when(postRepository.findAllByOrderByCreatedAtAsc()).thenReturn(expectedPosts);

        // When
        List<Post> result = postService.getAllPost(null, "asc");

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(postRepository).findAllByOrderByCreatedAtAsc();
    }

    @Test
    void getAllPost_WithUserIdDescSort_ShouldReturnUserPostsDescending() {
        // Given
        List<Post> expectedPosts = Arrays.asList(samplePost);
        when(postRepository.findByUserProfileIdOrderByCreatedAtDesc(userProfileId)).thenReturn(expectedPosts);

        // When
        List<Post> result = postService.getAllPost(userProfileId, "desc");

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(postRepository).findByUserProfileIdOrderByCreatedAtDesc(userProfileId);
    }

    @Test
    void getAllPost_WithUserIdAscSort_ShouldReturnUserPostsAscending() {
        // Given
        List<Post> expectedPosts = Arrays.asList(samplePost);
        when(postRepository.findByUserProfileIdOrderByCreatedAtAsc(userProfileId)).thenReturn(expectedPosts);

        // When
        List<Post> result = postService.getAllPost(userProfileId, "asc");

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(postRepository).findByUserProfileIdOrderByCreatedAtAsc(userProfileId);
    }


    @Test
    void mapToReadPostDto_NullPost_ShouldReturnNull() {
        // When
        ReadPostDto result = postService.mapToReadPostDto(null);

        // Then
        assertThat(result).isNull();
    }


    @Test
    void getPost_ExistingId_ShouldReturnPost() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        // When
        Post result = postService.getPost(postId);

        // Then
        assertThat(result).isEqualTo(samplePost);
        verify(postRepository).findById(postId);
    }

    @Test
    void getPost_NonExistingId_ShouldReturnNull() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When
        Post result = postService.getPost(postId);

        // Then
        assertThat(result).isNull();
        verify(postRepository).findById(postId);
    }

    @Test
    void updatePost_ExistingPost_ShouldReturnUpdatedPost() {
        // Given
        UpdatePostDto updatePostDto = new UpdatePostDto();
        updatePostDto.setId(postId);
        updatePostDto.setCaption("Updated Caption");
        updatePostDto.setImageUrl("updated-image.jpg");
        updatePostDto.setUserProfileId(userProfileId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(postRepository.save(any(Post.class))).thenReturn(samplePost);

        // When
        Post result = postService.updatePost(updatePostDto);

        // Then
        assertThat(result).isNotNull();
        verify(postRepository).findById(postId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_NonExistingPost_ShouldReturnNull() {
        // Given
        UpdatePostDto updatePostDto = new UpdatePostDto();
        updatePostDto.setId(postId);
        updatePostDto.setCaption("Updated Caption");
        updatePostDto.setImageUrl("updated-image.jpg");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When
        Post result = postService.updatePost(updatePostDto);

        // Then
        assertThat(result).isNull();
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_ExistingPost_ShouldReturnDeletedPost() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        // When
        Post result = postService.deletePost(postId);

        // Then
        assertThat(result).isEqualTo(samplePost);
        verify(postRepository).findById(postId);
        verify(postRepository).delete(samplePost);
    }

    @Test
    void deletePost_NonExistingPost_ShouldReturnNull() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When
        Post result = postService.deletePost(postId);

        // Then
        assertThat(result).isNull();
        verify(postRepository).findById(postId);
        verify(postRepository, never()).deleteById(postId);
    }

    @Test
    void likePost_ValidPostAndUser_NotYetLiked_ShouldAddLike() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(userProfileService.getUserProfile(userProfileId)).thenReturn(sampleUserProfile);
        when(postRepository.save(samplePost)).thenReturn(samplePost);

        // When
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(samplePost);
        assertThat(samplePost.getLikes()).contains(sampleUserProfile);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_ValidPostAndUser_AlreadyLiked_ShouldRemoveLike() {
        // Given
        samplePost.getLikes().add(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(userProfileService.getUserProfile(userProfileId)).thenReturn(sampleUserProfile);
        when(postRepository.save(samplePost)).thenReturn(samplePost);

        // When
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(samplePost);
        assertThat(samplePost.getLikes()).doesNotContain(sampleUserProfile);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_PostWithNullLikes_ShouldInitializeAndAddLike() {
        // Given
        samplePost.setLikes(null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(userProfileService.getUserProfile(userProfileId)).thenReturn(sampleUserProfile);
        when(postRepository.save(samplePost)).thenReturn(samplePost);

        // When
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(samplePost);
        assertThat(samplePost.getLikes()).isNotNull();
        assertThat(samplePost.getLikes()).contains(sampleUserProfile);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_PostNotFound_ShouldReturnNull() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNull();
        verify(userProfileService, never()).getUserProfile(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void likePost_UserNotFound_ShouldReturnNull() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(userProfileService.getUserProfile(userProfileId)).thenReturn(null);

        // When
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNull();
        verify(postRepository, never()).save(any());
    }

    @Test
    void likePost_MultipleUsers_ShouldHandleCorrectly() {
        // Given
        samplePost.getLikes().add(anotherUserProfile); // Another user already liked
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(userProfileService.getUserProfile(userProfileId)).thenReturn(sampleUserProfile);
        when(postRepository.save(samplePost)).thenReturn(samplePost);

        // When - First user likes the post
        Post result = postService.likePost(postId, userProfileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(samplePost);
        assertThat(samplePost.getLikes()).hasSize(2);
        assertThat(samplePost.getLikes()).contains(sampleUserProfile, anotherUserProfile);
    }

    @Test
    void testMapToReadPostDto_WithValidPost() {
        UUID userId = UUID.randomUUID();
        UserProfile user = UserProfile.builder()
                .id(userId)
                .name("Alice")
                .build();

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userProfile(user)
                .userProfileId(userId)
                .caption("Hello World")
                .imageUrl("http://example.com/img.png")
                .createdAt(LocalDateTime.now().minusHours(2))
                .likes(List.of(user))
                .build();

        ReadPostDto dto = postService.mapToReadPostDto(post);

        assertNotNull(dto);
        assertEquals(post.getId(), dto.getId());
        assertEquals(userId, dto.getUserProfileId());
        assertEquals("Alice", dto.getUserProfileName());
        assertEquals("Hello World", dto.getCaption());
        assertEquals("http://example.com/img.png", dto.getImageUrl());
        assertEquals(1, dto.getLikeCount());
        assertTrue(dto.getLikes().contains("Alice"));
        assertTrue(dto.getTimeAgo().contains("hours ago"));
        assertFalse(dto.getIsLikedByCurrentUser()); // default false
    }

    @Test
    void testMapToReadPostDto_NullPost() {
        ReadPostDto dto = postService.mapToReadPostDto(null);
        assertNull(dto);
    }

    @Test
    void testMapToReadPostDto_NoLikesAndNoUserProfile() {
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userProfile(null)
                .userProfileId(UUID.randomUUID())
                .caption("No user")
                .createdAt(LocalDateTime.now().minusDays(3))
                .likes(null)
                .build();

        ReadPostDto dto = postService.mapToReadPostDto(post);

        assertNotNull(dto);
        assertEquals("Unknown User", dto.getUserProfileName());
        assertEquals(0, dto.getLikeCount());
        assertTrue(dto.getTimeAgo().contains("days ago"));
    }
}