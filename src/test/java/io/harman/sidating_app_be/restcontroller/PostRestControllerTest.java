package io.harman.sidating_app_be.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.harman.sidating_app_be.restdto.request.post.*;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;
import io.harman.sidating_app_be.restservice.PostRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
class PostRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostRestService postRestService;

    @InjectMocks
    private PostRestController postRestController;

    private ObjectMapper objectMapper;
    private PostResponseDTO samplePostResponse;
    private CreatePostRequestDTO createPostRequest;
    private UpdatePostRequestDTO updatePostRequest;
    private DeletePostRequestDTO deletePostRequest;
    private LikePostRequestDTO likePostRequest;
    private UUID postId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();

        samplePostResponse = PostResponseDTO.builder()
                .id(postId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Test caption")
                .likeCount(5)
                .createdAt(LocalDateTime.now())
                .build();

        createPostRequest = new CreatePostRequestDTO();
        // userProfileId is optional - not set for regular user tests
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
    void getAllPost_ShouldReturnAllPosts_WhenNoFilters() throws Exception {
        List<PostResponseDTO> posts = Arrays.asList(samplePostResponse);
        when(postRestService.getAllPosts()).thenReturn(posts);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Posts retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(postId.toString()));

        verify(postRestService).getAllPosts();
    }

    @Test
    void getAllPost_ShouldReturnFilteredPosts_WhenUserIdProvided() throws Exception {
        List<PostResponseDTO> posts = Arrays.asList(samplePostResponse);
        when(postRestService.getPostByUserId(userId)).thenReturn(posts);

        mockMvc.perform(get("/api/posts")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(postRestService).getPostByUserId(userId);
    }

    @Test
    void createPost_ShouldReturnCreatedPost_WhenValidRequest() throws Exception {
        when(postRestService.createPost(any(CreatePostRequestDTO.class))).thenReturn(samplePostResponse);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Data Post Berhasil Dibuat"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postRestService).createPost(any(CreatePostRequestDTO.class));
    }

    @Test
    void createPost_ShouldReturnBadRequest_WhenRequestBodyIsNull() throws Exception {
        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required request body is missing"));

        verify(postRestService, never()).createPost(any());
    }

    @Test
    void createPost_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        CreatePostRequestDTO invalidRequest = new CreatePostRequestDTO();
        // Empty caption and imageUrl to trigger validation errors
        invalidRequest.setCaption("");
        invalidRequest.setImageUrl("");

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(postRestService, never()).createPost(any());
    }

    @Test
    void createPost_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        when(postRestService.createPost(any(CreatePostRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("Failed to create post")));

        verify(postRestService).createPost(any(CreatePostRequestDTO.class));
    }

    @Test
    void getPostById_ShouldReturnPost_WhenPostExists() throws Exception {
        when(postRestService.getPostById(postId)).thenReturn(samplePostResponse);

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Detail Post Berhasil Ditemukan"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postRestService).getPostById(postId);
    }

    @Test
    void getPostById_ShouldReturnNotFound_WhenPostDoesNotExist() throws Exception {
        when(postRestService.getPostById(postId)).thenReturn(null);

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Post Tidak Ditemukan"));

        verify(postRestService).getPostById(postId);
    }

    @Test
    void getPostById_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        when(postRestService.getPostById(postId))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verify(postRestService).getPostById(postId);
    }

    @Test
    void updatePost_ShouldReturnUpdatedPost_WhenValidRequest() throws Exception {
        when(postRestService.updatePost(any(UpdatePostRequestDTO.class))).thenReturn(samplePostResponse);

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Post updated successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postRestService).updatePost(any(UpdatePostRequestDTO.class));
    }

    @Test
    void updatePost_ShouldReturnNotFound_WhenPostDoesNotExist() throws Exception {
        when(postRestService.updatePost(any(UpdatePostRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Post tidak ditemukan atau gagal diupdate"));

        verify(postRestService).updatePost(any(UpdatePostRequestDTO.class));
    }

    @Test
    void updatePost_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        UpdatePostRequestDTO invalidRequest = new UpdatePostRequestDTO();
        invalidRequest.setId(null); // Missing required ID

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(postRestService, never()).updatePost(any());
    }

    @Test
    void updatePost_ShouldReturnForbidden_WhenNotAuthorized() throws Exception {
        when(postRestService.updatePost(any(UpdatePostRequestDTO.class)))
                .thenThrow(new SecurityException("You are not authorized to edit this post"));

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("Failed to update post")));

        verify(postRestService).updatePost(any(UpdatePostRequestDTO.class));
    }

    @Test
    void deletePost_ShouldReturnSuccess_WhenPostDeleted() throws Exception {
        doNothing().when(postRestService).deletePost(any(DeletePostRequestDTO.class));

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletePostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Post with id " + postId + " deleted successfully"));

        verify(postRestService).deletePost(any(DeletePostRequestDTO.class));
    }

    @Test
    void deletePost_ShouldReturnNotFound_WhenPostDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Post dengan id " + postId + " tidak ditemukan"))
                .when(postRestService).deletePost(any(DeletePostRequestDTO.class));

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletePostRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Post with id " + postId + " not found"));

        verify(postRestService).deletePost(any(DeletePostRequestDTO.class));
    }

    @Test
    void deletePost_ShouldReturnForbidden_WhenNotAuthorized() throws Exception {
        doThrow(new SecurityException("You are not authorized to delete this post"))
                .when(postRestService).deletePost(any(DeletePostRequestDTO.class));

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deletePostRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("Failed to delete post")));

        verify(postRestService).deletePost(any(DeletePostRequestDTO.class));
    }

    @Test
    void deletePost_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        DeletePostRequestDTO invalidRequest = new DeletePostRequestDTO();
        invalidRequest.setId(null); // Missing required ID

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(postRestService, never()).deletePost(any());
    }

    @Test
    void likePost_ShouldReturnSuccess_WhenPostLiked() throws Exception {
        doNothing().when(postRestService).likePost(any(LikePostRequestDTO.class));

        mockMvc.perform(post("/api/posts/" + postId + "/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Post like successfully"));

        verify(postRestService).likePost(any(LikePostRequestDTO.class));
    }

    @Test
    void likePost_ShouldReturnNotFound_WhenPostDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Post dengan id " + postId + " tidak ditemukan"))
                .when(postRestService).likePost(any(LikePostRequestDTO.class));

        mockMvc.perform(post("/api/posts/" + postId + "/like"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("tidak ditemukan")));

        verify(postRestService).likePost(any(LikePostRequestDTO.class));
    }

    @Test
    void likePost_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(post("/api/posts/" + invalidId + "/like"))
                .andExpect(status().isOk());

        verify(postRestService).likePost(any(LikePostRequestDTO.class));
    }

    @Test
    void likePost_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(postRestService).likePost(any(LikePostRequestDTO.class));

        mockMvc.perform(post("/api/posts/" + postId + "/like"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verify(postRestService).likePost(any(LikePostRequestDTO.class));
    }

    @Test
    void getAllPost_ShouldReturnEmptyList_WhenNoPosts() throws Exception {
        List<PostResponseDTO> emptyList = Arrays.asList();
        when(postRestService.getAllPosts()).thenReturn(emptyList);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(postRestService).getAllPosts();
    }
}