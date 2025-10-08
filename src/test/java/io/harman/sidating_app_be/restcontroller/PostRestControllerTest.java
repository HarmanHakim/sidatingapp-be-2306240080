package io.harman.sidating_app_be.restcontroller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.harman.sidating_app_be.restService.PostRestService;
import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;

@SpringBootTest
@AutoConfigureMockMvc
class PostRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostRestService postRestService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID postId;
    private UUID userId;
    private PostResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();
        responseDTO = PostResponseDTO.builder()
                .id(postId)
                .userProfileId(userId)
                .userProfileName("Tester")
                .caption("Test Caption")
                .imageUrl("https://img")
                .createdAt(LocalDateTime.now())
                .likeCount(1)
                .likes(List.of("user1"))
                .timeAgo("1m ago")
                .build();
    }

    @Test
    void testGetAllPosts_Default() throws Exception {
        when(postRestService.getAllPosts()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(postId.toString()));
    }

    @Test
    void testGetAllPosts_ByUserIdAndDate() throws Exception {
        when(postRestService.getPostByUserIdAndDate(any(), any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/posts")
                        .param("userId", userId.toString())
                        .param("date", "2025-10-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userProfileName").value("Tester"));
    }

    @Test
    void testGetAllPosts_ByUserIdOnly() throws Exception {
        when(postRestService.getPostByUserId(any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/posts")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userProfileId").value(userId.toString()));
    }

    @Test
    void testGetAllPosts_ByDateOnly() throws Exception {
        when(postRestService.getPostByDate(any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/posts")
                        .param("date", "2025-10-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].caption").value("Test Caption"));
    }

    @Test
    void testGetPost_Success() throws Exception {
        when(postRestService.getPostById(eq(postId))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId.toString()));
    }

    @Test
    void testGetPost_NotFound() throws Exception {
        when(postRestService.getPostById(eq(postId))).thenReturn(null);

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Post Tidak Ditemukan"));
    }

    @Test
    void testCreatePost_Success() throws Exception {
        var dto = new CreatePostRequestDTO(userId, "https://img", "caption");
        when(postRestService.createPost(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(postId.toString()));
    }

    @Test
    void testCreatePost_Fail() throws Exception {
        var dto = new CreatePostRequestDTO(userId, "https://img", "caption");
        when(postRestService.createPost(any())).thenReturn(null);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Post Gagal Dibuat"));
    }

    @Test
    void testUpdatePost_Success() throws Exception {
        var dto = new UpdatePostRequestDTO(postId, "url", "cap");
        when(postRestService.updatePost(any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId.toString()));
    }

    @Test
    void testUpdatePost_Fail() throws Exception {
        var dto = new UpdatePostRequestDTO(postId, "url", "cap");
        when(postRestService.updatePost(any())).thenReturn(null);

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Post Gagal Diupdate"));
    }

    @Test
    void testDeletePost_Success() throws Exception {
        when(postRestService.deletePost(any())).thenReturn(responseDTO);

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeletePostRequestDTO(postId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId.toString()));
    }

    @Test
    void testDeletePost_NotFound() throws Exception {
        when(postRestService.deletePost(any())).thenReturn(null);

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeletePostRequestDTO(postId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Post tidak ditemukan"));
    }

    @Test
    void testLikePost_Success() throws Exception {
        when(postRestService.likePost(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/posts/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LikePostRequestDTO(userId, postId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1));
    }

    @Test
    void testLikePost_NotFound() throws Exception {
        when(postRestService.likePost(any())).thenReturn(null);

        mockMvc.perform(post("/api/posts/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LikePostRequestDTO(userId, postId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHandleEmptyBody() throws Exception {
        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User Profile ID is required")));
    }

    // 🆕 Tambahan: validasi field error untuk Create
    @Test
    void testCreatePost_FieldValidation() throws Exception {
        var dto = new CreatePostRequestDTO(null, "", "");

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // 🆕 Tambahan: validasi field error untuk Update
    @Test
    void testUpdatePost_FieldValidation() throws Exception {
        var dto = new UpdatePostRequestDTO(null, "", "");

        mockMvc.perform(put("/api/posts/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // 🆕 Tambahan: validasi field error untuk Delete
    @Test
    void testDeletePost_FieldValidation() throws Exception {
        var dto = new DeletePostRequestDTO(null);

        mockMvc.perform(delete("/api/posts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // 🆕 Tambahan: validasi field error untuk Like
    @Test
    void testLikePost_FieldValidation() throws Exception {
        var dto = new LikePostRequestDTO(null, null);

        mockMvc.perform(post("/api/posts/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
