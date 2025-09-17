package io.harman.sidating_app_be.controller;


import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.PostService;
import io.harman.sidating_app_be.service.UserProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileController userProfileController;

    @MockBean
    private PostService postService;

    @MockBean
    private UserProfileService userProfileService;

    private UUID existingUserId;
    private UUID fakeUserId = UUID.randomUUID();
    private UUID existingPostId;
    private Post post;
    private UserProfile user;

    @BeforeEach
    void setup() {
        existingUserId = UUID.randomUUID();
        existingPostId = UUID.randomUUID();

        user = UserProfile.builder()
                .id(existingUserId)
                .name("Muhamad Hafiz")
                .nickname("hafiz")
                .birthdate(LocalDate.of(2004, 8, 16))
                .hobbies("Coding")
                .gender("MALE")
                .location("Jakarta")
                .bio("Information System Student.")
                .email("hafiz@example.com")
                .phoneNumber("081234567890")
                .interests("Technology, Startups, AI")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();

        post = Post.builder()
                .id(existingPostId)
                .userProfile(user)
                .userProfileId(existingUserId)
                .caption("Initial Post")
                .createdAt(LocalDateTime.now())
                .build();

        List<UserProfile> profiles = List.of(user);
        List<Post> posts = List.of(post);

        when(userProfileService.getAllUserProfile()).thenReturn(profiles);
        when(postService.getAllPost(eq(existingUserId), any(String.class))).thenReturn(posts);
        when(postService.getPost(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (id.equals(existingPostId)) return post;
            return null;
        });
        when(postService.createPost(any(Post.class))).thenReturn(post);
        when(postService.updatePost(any(Post.class))).thenReturn(post);
        when(postService.deletePost(existingPostId)).thenReturn(post);
        when(postService.likePost(eq(existingPostId), eq(existingUserId))).thenReturn(post);
        when(postService.likePost(eq(existingPostId), eq(fakeUserId))).thenReturn(null);
        when(postService.createPost(argThat(p -> fakeUserId.equals(p.getUserProfileId())))).thenReturn(null);
        when(postService.updatePost(argThat(p -> fakeUserId.equals(p.getUserProfileId())))).thenReturn(null);
    }

    @Test
    void testGetAllPosts() throws Exception {
        mockMvc.perform(get("/posts").param("userId", existingUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attribute("posts", hasSize(1)))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attributeExists("selectedUser"))
                .andExpect(model().attributeExists("selectedSort"));
    }

    @Test
    void testGetPostByIdFound() throws Exception {
        mockMvc.perform(get("/posts/" + existingPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testGetPostByIdNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        mockMvc.perform(get("/posts/" + fakePostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testFormCreatePost() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", existingUserId.toString())
                .param("caption", "Test Post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testTailcallPostSortedDesc() throws Exception {
        mockMvc.perform(get("/posts")
                .param("userId", existingUserId.toString())
                .param("sort", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attributeExists("selectedUser"))
                .andExpect(model().attributeExists("selectedSort"));
    }

    @Test
    void testCreatePostFail() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", fakeUserId.toString())
                .param("caption", "Invalid Post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testFormUpdatePostFound() throws Exception {
        mockMvc.perform(get("/posts/update/" + existingPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("postId", existingPostId));
    }

    @Test
    void testFormUpdatePostNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        mockMvc.perform(get("/posts/update/" + fakePostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("message"));
    }
    
    @Test
    void testUpdatePostSuccess() throws Exception {
        mockMvc.perform(put("/posts/update/" + existingPostId)
                .param("userProfileId", existingUserId.toString())
                .param("caption", "Updated Post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostFail() throws Exception {
        mockMvc.perform(put("/posts/update/" + existingPostId)
                .param("userProfileId", fakeUserId.toString())
                .param("caption", "Invalid Update"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testDeletePostFound() throws Exception {
        mockMvc.perform(delete("/posts/delete/" + existingPostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testDeletePostNotFound() throws Exception {
        UUID fakePostId = UUID.randomUUID();
        mockMvc.perform(delete("/posts/delete/" + fakePostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testLikePostSuccess() throws Exception {
        mockMvc.perform(post("/posts/" + existingPostId + "/like")
                .param("userId", existingUserId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));
    }

    @Test
    void testLikePostFail() throws Exception {
        mockMvc.perform(post("/posts/" + existingPostId + "/like")
                .param("userId", fakeUserId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));
    }
}