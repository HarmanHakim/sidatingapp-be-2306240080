package io.harman.sidating_app_be.restservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class PostRestServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private PostRestServiceImpl postRestService;

    private UUID postId;
    private UUID userId;
    private UserProfile user;
    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = UserProfile.builder()
                .id(userId)
                .name("John Doe")
                .nickname("jdoe")
                .email("john@example.com")
                .phoneNumber("123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        post = Post.builder()
                .id(postId)
                .userProfileId(userId)
                .userProfile(user)
                .imageUrl("http://img.com/pic.jpg")
                .caption("Hello world")
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now())
                .likes(new ArrayList<>())
                .isActive(true)
                .build();
    }

    @Test
    void testGetAllPosts() {
        when(postRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(post));

        List<PostResponseDTO> result = postRestService.getAllPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserProfileName()).isEqualTo("John Doe");
        verify(postRepository, times(1)).findAllByDeletedAtIsNull();
    }

    @Test
    void testGetPostByUserId() {
        when(postRepository.findByUserProfileIdAndDeletedAtIsNull(userId)).thenReturn(List.of(post));

        List<PostResponseDTO> result = postRestService.getPostByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCaption()).isEqualTo("Hello world");
        verify(postRepository).findByUserProfileIdAndDeletedAtIsNull(userId);
    }

    @Test
    void testGetPostByDate() {
        LocalDate date = LocalDate.now();
        when(postRepository.findByCreatedAtBetweenAndDeletedAtIsNull(any(), any()))
                .thenReturn(List.of(post));

        List<PostResponseDTO> result = postRestService.getPostByDate(date.toString());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImageUrl()).isEqualTo("http://img.com/pic.jpg");
    }

    @Test
    void testGetPostByDate_InvalidFormat_ThrowsException() {
        assertThrows(DateTimeParseException.class, () ->
                postRestService.getPostByDate("2025/10/07"));
    }

    @Test
    void testGetPostByUserIdAndDate() {
        LocalDate date = LocalDate.now();
        when(postRepository.findByUserProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(eq(userId), any(), any()))
                .thenReturn(List.of(post));

        List<PostResponseDTO> result = postRestService.getPostByUserIdAndDate(userId, date.toString());

        assertThat(result).hasSize(1);
        verify(postRepository).findByUserProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(eq(userId), any(), any());
    }

    @Test
    void testGetPostById_Found() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostResponseDTO result = postRestService.getPostById(postId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
    }

    @Test
    void testGetPostById_NotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.getPostById(postId);

        assertThat(result).isNull();
    }

    @Test
    void testCreatePost_Success() {
        CreatePostRequestDTO dto = new CreatePostRequestDTO(userId, "http://img.com/pic.jpg", "New Post");

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArguments()[0]);

        PostResponseDTO result = postRestService.createPost(dto);

        assertThat(result).isNotNull();
        assertThat(result.getCaption()).isEqualTo("New Post");
        verify(postRepository).save(any());
    }

    @Test
    void testCreatePost_UserNotFound() {
        CreatePostRequestDTO dto = new CreatePostRequestDTO(userId, "http://img.com/pic.jpg", "New Post");

        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.createPost(dto);

        assertThat(result).isNull();
        verify(postRepository, never()).save(any());
    }

    @Test
    void testUpdatePost_Success() {
        UpdatePostRequestDTO dto = new UpdatePostRequestDTO(postId, "http://newimg.com/img.jpg", "Updated Caption");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        PostResponseDTO result = postRestService.updatePost(dto);

        assertThat(result).isNotNull();
        assertThat(result.getCaption()).isEqualTo("Updated Caption");
    }

    @Test
    void testUpdatePost_NotFound() {
        UpdatePostRequestDTO dto = new UpdatePostRequestDTO(postId, "url", "cap");
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.updatePost(dto);

        assertThat(result).isNull();
        verify(postRepository, never()).save(any());
    }

    @Test
    void testDeletePost_Success() {
        when(postRepository.findByIdAndDeletedAtIsNull(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> {
            Post saved = (Post) i.getArguments()[0];
            assertThat(saved.getDeletedAt()).isNotNull();
            return saved;
        });

        PostResponseDTO result = postRestService.deletePost(new DeletePostRequestDTO(postId));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        verify(postRepository).save(any());
    }

    @Test
    void testDeletePost_NotFound() {
        when(postRepository.findByIdAndDeletedAtIsNull(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.deletePost(new DeletePostRequestDTO(postId));

        assertThat(result).isNull();
        verify(postRepository, never()).save(any());
    }

    @Test
    void testLikePost_ToggleLike() {
        post.setLikes(new ArrayList<>());
        when(postRepository.findByIdAndDeletedAtIsNull(postId)).thenReturn(Optional.of(post));
        when(userProfileRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        LikePostRequestDTO dto = new LikePostRequestDTO(userId, postId);

        PostResponseDTO result1 = postRestService.likePost(dto);
        assertThat(result1.getLikeCount()).isEqualTo(1);

        PostResponseDTO result2 = postRestService.likePost(dto);
        assertThat(result2.getLikeCount()).isEqualTo(0);
    }

    @Test
    void testLikePost_PostNotFound() {
        when(postRepository.findByIdAndDeletedAtIsNull(postId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.likePost(new LikePostRequestDTO(userId, postId));

        assertThat(result).isNull();
        verify(userProfileRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    void testLikePost_UserNotFound() {
        when(postRepository.findByIdAndDeletedAtIsNull(postId)).thenReturn(Optional.of(post));
        when(userProfileRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        PostResponseDTO result = postRestService.likePost(new LikePostRequestDTO(userId, postId));

        assertThat(result).isNull();
        verify(postRepository, never()).save(any());
    }
}
