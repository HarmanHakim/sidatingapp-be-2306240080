package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserProfileService userProfileService;

    private UserProfile user;
    private Post post;
    private UUID postId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postService = new PostServiceImpl(postRepository, userProfileService);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        user = UserProfile.builder()
                .id(userId)
                .name("Test User")
                .birthdate(LocalDate.of(2000, 1, 1))
                .gender("MALE")
                .isActive(true)
                .build();

        post = Post.builder()
                .id(postId)
                .userProfile(user)
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Test Post")
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .isActive(true)
                .likes(new ArrayList<>())
                .build();

        // Setup mocks
        when(userProfileService.getUserProfile(userId)).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testCreatePostSuccess() {
        CreatePostDto dto = CreatePostDto.builder()
                .userProfileId(userId)
                .imageUrl("https://example.com/new-image.jpg")
                .caption("Hello World")
                .build();

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(postId);
            return savedPost;
        });

        Post created = postService.createPost(dto);
        
        assertNotNull(created);
        assertEquals(postId, created.getId());
        assertEquals(userId, created.getUserProfileId());
        assertEquals("Hello World", created.getCaption());
        assertEquals("https://example.com/new-image.jpg", created.getImageUrl());
        assertTrue(created.isActive());
        verify(postRepository, times(1)).save(any(Post.class));
        verify(userProfileService, times(1)).getUserProfile(userId);
    }

    @Test
    void testCreatePostFailIfUserNotFound() {
        CreatePostDto dto = CreatePostDto.builder()
                .userProfileId(UUID.randomUUID())
                .imageUrl("https://example.com/image.jpg")
                .caption("Hello")
                .build();

        when(userProfileService.getUserProfile(any())).thenReturn(null);
        
        Post created = postService.createPost(dto);
        assertNull(created);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testCreatePostFailIfUserDeleted() {
        user.setActive(false);
        when(userProfileService.getUserProfile(userId)).thenReturn(user);

        CreatePostDto dto = CreatePostDto.builder()
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Hello")
                .build();

        Post created = postService.createPost(dto);
        assertNull(created);
        verify(postRepository, never()).save(any(Post.class));
        verify(userProfileService, times(1)).getUserProfile(userId);
    }

    @Test
    void testGetAllPostSuccess() {
        Post post2 = Post.builder()
                .id(UUID.randomUUID())
                .userProfileId(userId)
                .imageUrl("https://example.com/image2.jpg")
                .caption("Second Post")
                .createdAt(LocalDateTime.now().plusSeconds(1))
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .isActive(true)
                .likes(new ArrayList<>())
                .build();

        List<Post> posts = Arrays.asList(post, post2);
        
        // Test without userId filter
        when(postRepository.findByDeletedAtIsNull()).thenReturn(posts);
        List<Post> result = postService.getAllPost(null, "desc");
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        
        // Test with userId filter
        when(postRepository.findByUserProfileIdAndDeletedAtIsNull(userId)).thenReturn(posts);
        result = postService.getAllPost(userId, "asc");
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isBefore(result.get(1).getCreatedAt()));
    }

    @Test
    void testGetAllPostSortingEdgeCases() {
        Post post1 = Post.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        Post post2 = Post.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();
        
        List<Post> posts = Arrays.asList(post1, post2);
        when(postRepository.findByDeletedAtIsNull()).thenReturn(posts);
        
        // Test case insensitive sorting
        List<Post> resultAsc = postService.getAllPost(null, "ASC");
        assertTrue(resultAsc.get(0).getCreatedAt().isBefore(resultAsc.get(1).getCreatedAt()));
        
        // Test default desc sorting with null sort parameter
        List<Post> resultNull = postService.getAllPost(null, null);
        assertTrue(resultNull.get(0).getCreatedAt().isAfter(resultNull.get(1).getCreatedAt()));
        
        // Test random sort value defaults to desc
        List<Post> resultRandom = postService.getAllPost(null, "random");
        assertTrue(resultRandom.get(0).getCreatedAt().isAfter(resultRandom.get(1).getCreatedAt()));
    }

    @Test
    void testGetPostSuccess() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        
        Post result = postService.getPost(postId);
        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertTrue(result.isActive());
    }

    @Test
    void testGetPostNotFound() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        
        Post result = postService.getPost(UUID.randomUUID());
        assertNull(result);
    }

    @Test
    void testGetPostDeleted() {
        post.setDeletedAt(LocalDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        
        Post result = postService.getPost(postId);
        assertNull(result);
    }

    @Test
    void testUpdatePostSuccess() {
        UpdatePostDto dto = UpdatePostDto.builder()
                .id(postId)
                .userProfileId(userId)
                .imageUrl("https://example.com/updated-image.jpg")
                .caption("Updated Caption")
                .isActive(false)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post updated = postService.updatePost(dto);
        
        assertNotNull(updated);
        assertEquals("Updated Caption", updated.getCaption());
        assertEquals("https://example.com/updated-image.jpg", updated.getImageUrl());
        assertFalse(updated.isActive());
        assertNotNull(updated.getUpdatedAt());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testUpdatePostWithNullIsActive() {
        boolean originalIsActive = post.isActive();
        
        UpdatePostDto dto = UpdatePostDto.builder()
                .id(postId)
                .userProfileId(userId)
                .imageUrl("https://example.com/updated-image.jpg")
                .caption("Updated Caption")
                .isActive(null) // Test null case
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post updated = postService.updatePost(dto);
        
        assertNotNull(updated);
        assertEquals(originalIsActive, updated.isActive()); // Should remain unchanged
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testUpdatePostFailIfNotFound() {
        UpdatePostDto dto = UpdatePostDto.builder()
                .id(UUID.randomUUID())
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Updated")
                .build();

        when(postRepository.findById(any())).thenReturn(Optional.empty());
        
        Post updated = postService.updatePost(dto);
        assertNull(updated);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testUpdatePostFailIfPostDeleted() {
        post.setDeletedAt(LocalDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        UpdatePostDto dto = UpdatePostDto.builder()
                .id(postId)
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Updated")
                .build();

        Post updated = postService.updatePost(dto);
        assertNull(updated);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testUpdatePostFailIfUserNotFound() {
        when(userProfileService.getUserProfile(userId)).thenReturn(null);

        UpdatePostDto dto = UpdatePostDto.builder()
                .id(postId)
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Updated")
                .build();

        Post updated = postService.updatePost(dto);
        assertNull(updated);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testUpdatePostFailIfUserDeleted() {
        UserProfile deletedUser = UserProfile.builder()
                .id(userId)
                .name("Deleted User")
                .deletedAt(LocalDateTime.now())
                .build();
        
        when(userProfileService.getUserProfile(userId)).thenReturn(deletedUser);

        UpdatePostDto dto = UpdatePostDto.builder()
                .id(postId)
                .userProfileId(userId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Updated")
                .build();

        Post updated = postService.updatePost(dto);
        assertNull(updated);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testDeletePostSuccess() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post deleted = postService.deletePost(postId);
        
        assertNotNull(deleted);
        assertNotNull(deleted.getDeletedAt());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testDeletePostNotFound() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        
        Post deleted = postService.deletePost(UUID.randomUUID());
        assertNull(deleted);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testDeletePostAlreadyDeleted() {
        post.setDeletedAt(LocalDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        
        Post deleted = postService.deletePost(postId);
        assertNull(deleted);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testLikePostFirstTime() {
        if (post.getLikes() == null) {
            post.setLikes(new ArrayList<>());
        }
        
        Post liked = postService.likePost(postId, userId);
        
        assertNotNull(liked);
        assertEquals(1, liked.getLikes().size());
        assertEquals(user, liked.getLikes().get(0));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testLikePostWithNullLikesList() {
        post.setLikes(null); // Explicitly set to null
        
        Post liked = postService.likePost(postId, userId);
        
        assertNotNull(liked);
        assertNotNull(liked.getLikes());
        assertEquals(1, liked.getLikes().size());
        assertEquals(user, liked.getLikes().get(0));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testLikePostToggleOff() {
        if (post.getLikes() == null) {
            post.setLikes(new ArrayList<>());
        }
        post.getLikes().add(user);
        
        Post unliked = postService.likePost(postId, userId);
        
        assertNotNull(unliked);
        assertEquals(0, unliked.getLikes().size());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testLikePostPostNotFound() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        
        Post result = postService.likePost(UUID.randomUUID(), userId);
        assertNull(result);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testLikePostUserNotFound() {
        when(userProfileService.getUserProfile(any())).thenReturn(null);
        
        Post result = postService.likePost(postId, UUID.randomUUID());
        assertNull(result);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testLikePostUserDeleted() {
        UserProfile deletedUser = UserProfile.builder()
                .id(userId)
                .name("Deleted User")
                .deletedAt(LocalDateTime.now())
                .build();
        
        when(userProfileService.getUserProfile(userId)).thenReturn(deletedUser);
        
        Post result = postService.likePost(postId, userId);
        assertNull(result);
        verify(postRepository, never()).save(any());
    }

    @Test
    void testToReadPostDto() {
        if (post.getLikes() == null) {
            post.setLikes(new ArrayList<>());
        }
        post.getLikes().add(user);
        
        ReadPostDto dto = postService.toReadPostDto(post);
        
        assertNotNull(dto);
        assertEquals(postId, dto.getId());
        assertEquals(userId, dto.getUserProfileId());
        assertEquals("Test User", dto.getUserProfileName());
        assertEquals("Test Post", dto.getCaption());
        assertEquals(1, dto.getLikeCount());
        assertEquals(1, dto.getLikes().size());
        assertEquals("Test User", dto.getLikes().get(0));
        assertNotNull(dto.getTimeAgo());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    void testToReadPostDtoWithNullLikes() {
        post.setLikes(null);
        
        ReadPostDto dto = postService.toReadPostDto(post);
        
        assertNotNull(dto);
        assertEquals(0, dto.getLikeCount());
        assertTrue(dto.getLikes().isEmpty());
    }

    @Test
    void testToReadPostDtoWithEmptyLikes() {
        post.setLikes(new ArrayList<>());
        
        ReadPostDto dto = postService.toReadPostDto(post);
        
        assertNotNull(dto);
        assertEquals(0, dto.getLikeCount());
        assertTrue(dto.getLikes().isEmpty());
    }

    @Test
    void testGetAllPostsDto() {
        Post post2 = Post.builder()
                .id(UUID.randomUUID())
                .userProfileId(userId)
                .imageUrl("https://example.com/image2.jpg")
                .caption("Second Post")
                .createdAt(LocalDateTime.now().plusSeconds(1))
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .isActive(true)
                .likes(new ArrayList<>())
                .userProfile(user)
                .build();

        List<Post> posts = Arrays.asList(post, post2);
        when(postRepository.findByDeletedAtIsNull()).thenReturn(posts);
        
        List<ReadPostDto> result = postService.getAllPostsDto(null, "desc");
        
        assertEquals(2, result.size());
        assertEquals("Second Post", result.get(0).getCaption());
        assertNotNull(result.get(0).getTimeAgo());
        assertEquals(0, result.get(0).getLikeCount());
    }

    @Test
    void testGetAllPostsDtoWithUserIdFilter() {
        post.setUserProfile(user);
        List<Post> posts = Arrays.asList(post);
        when(postRepository.findByUserProfileIdAndDeletedAtIsNull(userId)).thenReturn(posts);
        
        List<ReadPostDto> result = postService.getAllPostsDto(userId, "asc");
        
        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getCaption());
        assertEquals("Test User", result.get(0).getUserProfileName());
    }

    @Test
    void testTimeAgoCalculationComplete() {
        // Just Now (< 1 hour)
        post.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        ReadPostDto dto1 = postService.toReadPostDto(post);
        assertEquals("Just Now", dto1.getTimeAgo());

        // Hours ago
        post.setCreatedAt(LocalDateTime.now().minusHours(2));
        ReadPostDto dto2 = postService.toReadPostDto(post);
        assertEquals("2 hours ago", dto2.getTimeAgo());

        // Days ago
        post.setCreatedAt(LocalDateTime.now().minusDays(3));
        ReadPostDto dto3 = postService.toReadPostDto(post);
        assertEquals("3 days ago", dto3.getTimeAgo());

        // Weeks ago
        post.setCreatedAt(LocalDateTime.now().minusWeeks(2));
        ReadPostDto dto4 = postService.toReadPostDto(post);
        assertEquals("2 weeks ago", dto4.getTimeAgo());

        // Months ago
        post.setCreatedAt(LocalDateTime.now().minusMonths(3));
        ReadPostDto dto5 = postService.toReadPostDto(post);
        assertEquals("3 months ago", dto5.getTimeAgo());

        // Years ago
        post.setCreatedAt(LocalDateTime.now().minusYears(2));
        ReadPostDto dto6 = postService.toReadPostDto(post);
        assertEquals("2 years ago", dto6.getTimeAgo());
    }

    @Test
    void testTimeAgoBoundaryConditions() {
        // Exactly 1 hour
        post.setCreatedAt(LocalDateTime.now().minusHours(1));
        ReadPostDto dto1 = postService.toReadPostDto(post);
        assertEquals("1 hours ago", dto1.getTimeAgo());

        // Exactly 1 day
        post.setCreatedAt(LocalDateTime.now().minusDays(1));
        ReadPostDto dto2 = postService.toReadPostDto(post);
        assertEquals("1 days ago", dto2.getTimeAgo());

        // Exactly 1 week
        post.setCreatedAt(LocalDateTime.now().minusWeeks(1));
        ReadPostDto dto3 = postService.toReadPostDto(post);
        assertEquals("1 weeks ago", dto3.getTimeAgo());
    }

}