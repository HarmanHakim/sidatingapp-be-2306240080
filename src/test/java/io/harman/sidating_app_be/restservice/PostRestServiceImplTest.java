package io.harman.sidating_app_be.restservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restService.PostRestServiceImpl;
import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;
import io.harman.sidating_app_be.security.jwt.JwtUtils;

@ExtendWith(MockitoExtension.class)
class PostRestServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private PostRestServiceImpl postRestService;

    private Post samplePost;
    private UserProfile sampleUserProfile;
    private CreatePostRequestDTO createPostRequest;
    private UpdatePostRequestDTO updatePostRequest;
    private DeletePostRequestDTO deletePostRequest;
    private LikePostRequestDTO likePostRequest;
    private UUID postId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();

        sampleUserProfile = new UserProfile();
        sampleUserProfile.setId(userId);
        sampleUserProfile.setName("John Doe");
        sampleUserProfile.setNickname("Johnny");
        sampleUserProfile.setEmail("john@example.com");
        sampleUserProfile.setBirthdate(LocalDate.of(1995, 1, 1));
        sampleUserProfile.setGender("MALE");

        samplePost = new Post();
        samplePost.setId(postId);
        samplePost.setUserProfileId(userId); // Set userProfileId for ownership check
        samplePost.setUserProfile(sampleUserProfile);
        samplePost.setImageUrl("https://example.com/image.jpg");
        samplePost.setCaption("Test caption");
        samplePost.setCreatedAt(LocalDateTime.now());
        samplePost.setLikes(new ArrayList<>());

        createPostRequest = new CreatePostRequestDTO();
        // userProfileId is optional - will be determined by service from authenticated user
        createPostRequest.setImageUrl("https://example.com/image.jpg");
        createPostRequest.setCaption("Test caption");

        updatePostRequest = new UpdatePostRequestDTO();
        updatePostRequest.setId(postId);
        updatePostRequest.setImageUrl("https://example.com/updated-image.jpg");
        updatePostRequest.setCaption("Updated caption");

        deletePostRequest = new DeletePostRequestDTO();
        deletePostRequest.setId(postId);

        likePostRequest = new LikePostRequestDTO();
        likePostRequest.setId(postId);
        // userProfileId is no longer set here - it comes from authenticated user
    }

    @Test
    void getAllPosts_ShouldReturnAllNonDeletedPosts() {
        List<Post> posts = Arrays.asList(samplePost);
        when(postRepository.findAll()).thenReturn(posts);

        List<PostResponseDTO> result = postRestService.getAllPosts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(postId, result.get(0).getId());
        verify(postRepository).findAll();
    }

    @Test
    void getAllPosts_ShouldFilterDeletedPosts() {
        Post deletedPost = new Post();
        deletedPost.setId(UUID.randomUUID());
        deletedPost.setDeletedAt(LocalDateTime.now());
        
        List<Post> posts = Arrays.asList(samplePost, deletedPost);
        when(postRepository.findAll()).thenReturn(posts);

        List<PostResponseDTO> result = postRestService.getAllPosts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(postId, result.get(0).getId());
    }

    @Test
    void getPostById_ShouldReturnPost_WhenPostExists() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        PostResponseDTO result = postRestService.getPostById(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals("Test caption", result.getCaption());
        verify(postRepository).findById(postId);
    }

    @Test
    void getPostById_ShouldReturnNull_WhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.getPostById(postId);

        assertNull(result);
        verify(postRepository).findById(postId);
    }

    @Test
    void getPostById_ShouldReturnNull_WhenPostIsDeleted() {
        samplePost.setDeletedAt(LocalDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        PostResponseDTO result = postRestService.getPostById(postId);

        assertNull(result);
        verify(postRepository).findById(postId);
    }

    @Test
    void createPost_ShouldCreatePost_WhenValidRequest() {
        // Mock authenticated user
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.save(any(Post.class))).thenReturn(samplePost);

        // Don't set userProfileId for regular user test
        createPostRequest.setUserProfileId(null);

        PostResponseDTO result = postRestService.createPost(createPostRequest);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals("Test caption", result.getCaption());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_ShouldThrowException_WhenUserNotAuthenticated() {
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            postRestService.createPost(createPostRequest);
        });

        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_ShouldUpdatePost_WhenValidRequest() {
        // Mock authenticated user (owner of the post)
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));
        when(postRepository.save(any(Post.class))).thenReturn(samplePost);

        PostResponseDTO result = postRestService.updatePost(updatePostRequest);

        assertNotNull(result);
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).findById(postId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_ShouldReturnNull_WhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.updatePost(updatePostRequest);

        assertNull(result);
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_ShouldReturnNull_WhenPostIsDeleted() {
        samplePost.setDeletedAt(LocalDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        PostResponseDTO result = postRestService.updatePost(updatePostRequest);

        assertNull(result);
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_ShouldMarkPostAsDeleted_WhenPostExists() {
        // Mock authenticated user (owner of the post)
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        assertDoesNotThrow(() -> postRestService.deletePost(deletePostRequest));

        assertNotNull(samplePost.getDeletedAt());
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).findById(postId);
        verify(postRepository).save(samplePost);
    }

    @Test
    void deletePost_ShouldThrowException_WhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postRestService.deletePost(deletePostRequest);
        });

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_ShouldMarkAsDeleted_WhenPostAlreadyDeleted() {
        samplePost.setDeletedAt(LocalDateTime.now());
        // Mock authenticated user (owner of the post)
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        // The service should still mark it as deleted even if already deleted
        assertDoesNotThrow(() -> postRestService.deletePost(deletePostRequest));

        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).findById(postId);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_ShouldAddLike_WhenPostAndUserExist() {
        // Mock authenticated user
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        assertDoesNotThrow(() -> postRestService.likePost(likePostRequest));

        assertTrue(samplePost.getLikes().contains(sampleUserProfile));
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).findById(postId);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_ShouldRemoveLike_WhenUserAlreadyLikedPost() {
        samplePost.getLikes().add(sampleUserProfile);
        // Mock authenticated user
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(sampleUserProfile);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        assertDoesNotThrow(() -> postRestService.likePost(likePostRequest));

        assertFalse(samplePost.getLikes().contains(sampleUserProfile));
        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
        verify(postRepository).findById(postId);
        verify(postRepository).save(samplePost);
    }

    @Test
    void likePost_ShouldThrowException_WhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postRestService.likePost(likePostRequest);
        });

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(postRepository).findById(postId);
        verify(userProfileRepository, never()).findById(any());
    }

    @Test
    void likePost_ShouldThrowException_WhenUserNotFound() {
        // Mock authenticated user not found
        when(jwtUtils.getCurrentUsername()).thenReturn("testuser");
        when(userProfileRepository.findByUsername("testuser")).thenReturn(null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost));

        assertThrows(RuntimeException.class, () -> {
            postRestService.likePost(likePostRequest);
        });

        verify(jwtUtils).getCurrentUsername();
        verify(userProfileRepository).findByUsername("testuser");
    }

    @Test
    void getPostByUserId_ShouldReturnUserPosts() {
        List<Post> posts = Arrays.asList(samplePost);
        when(postRepository.findByUserProfileId(userId)).thenReturn(posts);

        List<PostResponseDTO> result = postRestService.getPostByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(postId, result.get(0).getId());
        verify(postRepository).findByUserProfileId(userId);
    }

    @Test
    void getPostByDate_ShouldReturnPostsForSpecificDate() {
        String dateString = "2024-01-01";
        List<Post> posts = Arrays.asList(samplePost);
        when(postRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(posts);

        List<PostResponseDTO> result = postRestService.getPostByDate(dateString);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postRepository).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getPostByUserIdAndDate_ShouldReturnFilteredPosts() {
        String dateString = "2024-01-01";
        List<Post> posts = Arrays.asList(samplePost);
        when(postRepository.findByUserProfileIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(posts);

        List<PostResponseDTO> result = postRestService.getPostByUserIdAndDate(userId, dateString);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postRepository).findByUserProfileIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}