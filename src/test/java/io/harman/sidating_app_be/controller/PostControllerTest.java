package io.harman.sidating_app_be.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.PostService;
import io.harman.sidating_app_be.service.UserProfileService;

@WebMvcTest(PostController.class)
@ContextConfiguration(classes = PostController.class)
@Import(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private UserProfileService userProfileService;

    private UUID existingUserId;
    private UUID fakeUserId;
    private UUID existingPostId;
    private Post post;
    private UserProfile user;
    private ReadPostDto readPostDto;

    @BeforeEach
    void setUp() {
        existingUserId = UUID.randomUUID();
        fakeUserId = UUID.randomUUID();
        existingPostId = UUID.randomUUID();

        user = UserProfile.builder()
                .id(existingUserId)
                .name("Muhammad Hafiz")
                .nickname("Hafiz")
                .birthdate(LocalDate.of(2004, 8, 16))
                .hobbies("Coding")
                .gender("MALE")
                .location("Jakarta")
                .bio("Information System Student.")
                .email("hafiz@example.com")
                .phoneNumber("081234567890")
                .interests("Technology, Startups, AI")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        post = Post.builder()
                .id(existingPostId)
                .userProfile(user)
                .userProfileId(existingUserId)
                .imageUrl("https://example.com/image.jpg")
                .caption("Initial Post")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        readPostDto = ReadPostDto.builder()
                .id(existingPostId)
                .userProfileId(existingUserId)
                .userProfileName("Muhammad Hafiz")
                .imageUrl("https://example.com/image.jpg")
                .caption("Initial Post")
                .createdAt(LocalDateTime.now())
                .likes(List.of())
                .likeCount(0)
                .timeAgo("Just Now")
                .build();

        List<UserProfile> profiles = List.of(user);
        List<ReadPostDto> postsDto = List.of(readPostDto);

        when(userProfileService.getAllUserProfile()).thenReturn(profiles);
        when(userProfileService.getUserProfile(existingUserId)).thenReturn(user);
        when(postService.getAllPostsDto(any(UUID.class), any(String.class))).thenReturn(postsDto);
        when(postService.getPost(existingPostId)).thenReturn(post);
        when(postService.getPost(fakeUserId)).thenReturn(null);
        when(postService.createPost(any(CreatePostDto.class))).thenReturn(post);
        when(postService.updatePost(any(UpdatePostDto.class))).thenReturn(post);
        when(postService.deletePost(existingPostId)).thenReturn(post);
        when(postService.likePost(existingPostId, existingUserId)).thenReturn(post);
        when(postService.likePost(existingPostId, fakeUserId)).thenReturn(null);
    }

    // --- View All Posts ---
    @Test
    void testGetAllPostsDefaultSort() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/view-all"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testViewAllPostsEmptyList() throws Exception {
        when(postService.getAllPostsDto(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/view-all"))
                .andExpect(model().attribute("posts", List.of()));
    }

    // --- View Single Post ---
    @Test
    void testGetPostByIdFound() throws Exception {
        mockMvc.perform(get("/posts/{id}", existingPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists( "userProfiles"));
    }

    @Test
    void testGetPostByIdNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        when(postService.getPost(fakePostId)).thenReturn(null);

        mockMvc.perform(get("/posts/{id}", fakePostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testViewPostWithoutUserProfile() throws Exception {
        Post orphanPost = Post.builder()
                .id(UUID.randomUUID())
                .userProfile(null)
                .imageUrl("https://example.com/image.jpg")
                .caption("Orphan Post")
                .isActive(true)
                .build();

        when(postService.getPost(orphanPost.getId())).thenReturn(orphanPost);

        mockMvc.perform(get("/posts/{id}", orphanPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    // --- Create Post ---
    @Test
    void testFormCreatePost() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/form"))
                .andExpect(model().attribute("isEdit", Boolean.FALSE))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .param("userProfileId", existingUserId.toString())
                        .param("imageUrl", "https://example.com/new-image.jpg")
                        .param("caption", "Test Post")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).createPost(any(CreatePostDto.class));
    }

    @Test
    void testCreatePostWithMissingImageUrl() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .param("userProfileId", existingUserId.toString())
                        .param("caption", "Test Post")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/create"));
    }

    @Test
void testCreatePostWithEmptyCaption() throws Exception {
    mockMvc.perform(post("/posts/create")
                    .param("userProfileId", existingUserId.toString())
                    .param("imageUrl", "https://example.com/image.jpg")
                    .param("caption", "") // kosong
                    .contentType("application/x-www-form-urlencoded"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/create"))
            .andExpect(flash().attributeExists("errorMessage"));
}


    // --- Update Post ---
    @Test
    void testFormUpdatePostFound() throws Exception {
        mockMvc.perform(get("/posts/update/{id}", existingPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/form"))
                .andExpect(model().attribute("isEdit", Boolean.TRUE))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("postId", existingPostId));
    }

    @Test
    void testFormUpdatePostNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        when(postService.getPost(fakePostId)).thenReturn(null);

        mockMvc.perform(get("/posts/update/{id}", fakePostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testUpdatePostSuccess() throws Exception {
        mockMvc.perform(put("/posts/update/{id}", existingPostId)
                        .param("id", existingPostId.toString())
                        .param("userProfileId", existingUserId.toString())
                        .param("imageUrl", "https://example.com/updated-image.jpg")
                        .param("caption", "Updated Post")
                        .param("isActive", "true")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostWithInvalidData() throws Exception {
        mockMvc.perform(put("/posts/update/{id}", existingPostId)
                        .param("id", existingPostId.toString())
                        .param("userProfileId", existingUserId.toString())
                        .param("imageUrl", "")
                        .param("caption", "")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/update/" + existingPostId))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // --- Delete Post ---
    @Test
    void testDeletePostFound() throws Exception {
        mockMvc.perform(delete("/posts/delete/{id}", existingPostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testDeletePostNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        when(postService.deletePost(fakePostId)).thenReturn(null);

        mockMvc.perform(delete("/posts/delete/{id}", fakePostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    // --- Like Post ---
    @Test
    void testLikePostSuccess() throws Exception {
        mockMvc.perform(post("/posts/{id}/like", existingPostId)
                        .param("userId", existingUserId.toString())
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testLikePostFail() throws Exception {
        mockMvc.perform(post("/posts/{id}/like", existingPostId)
                        .param("userId", fakeUserId.toString())
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
