package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.PostService;
import io.harman.sidating_app_be.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private Post samplePost;
    private ReadPostDto sampleReadPostDto;
    private UserProfile sampleUserProfile;
    private List<UserProfile> sampleUserProfiles;
    private UUID postId;
    private UUID userProfileId;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");
        
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setViewResolvers(viewResolver)
                .build();

        // Setup test data
        postId = UUID.randomUUID();
        userProfileId = UUID.randomUUID();
        
        sampleUserProfile = new UserProfile();
        sampleUserProfile.setId(userProfileId);
        sampleUserProfile.setName("Test User");
        
        sampleUserProfiles = Arrays.asList(sampleUserProfile);
        
        samplePost = new Post();
        samplePost.setId(postId);
        samplePost.setCaption("Test Caption");
        samplePost.setImageUrl("test-image.jpg");
        samplePost.setUserProfileId(userProfileId);
        samplePost.setCreatedAt(LocalDateTime.now());
        
        sampleReadPostDto = new ReadPostDto();
        sampleReadPostDto.setId(postId);
        sampleReadPostDto.setCaption("Test Caption");
        sampleReadPostDto.setImageUrl("test-image.jpg");
    }

    @Test
    void getAllPosts_ShouldReturnViewWithPosts() throws Exception {
        // Given
        List<Post> posts = Arrays.asList(samplePost);
        List<ReadPostDto> readPostDtos = Arrays.asList(sampleReadPostDto);
        
        when(postService.getAllPost(null, "desc")).thenReturn(posts);
        when(postService.mapToReadPostDto(samplePost)).thenReturn(sampleReadPostDto);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("userProfiles"));
        
        verify(postService).getAllPost(null, "desc");
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void getAllPosts_WithUserIdAndSort_ShouldReturnFilteredPosts() throws Exception {
        // Given
        List<Post> posts = Arrays.asList(samplePost);
        when(postService.getAllPost(userProfileId, "asc")).thenReturn(posts);
        when(postService.mapToReadPostDto(samplePost)).thenReturn(sampleReadPostDto);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post")
                .param("userId", userProfileId.toString())
                .param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/view-all"));
        
        verify(postService).getAllPost(userProfileId, "asc");
    }

    @Test
    void createPostForm_ShouldReturnFormView() throws Exception {
        // Given
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/form"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("isEdit", false));
        
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void createPost_ValidInput_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(postService.createPost(any(CreatePostDto.class))).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(post("/post/create")
                .param("caption", "Test Caption")
                .param("imageUrl", "test-image.jpg")
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(postService).createPost(any(CreatePostDto.class));
    }

    @Test
    void createPost_ServiceReturnsNull_ShouldRedirectWithErrorMessage() throws Exception {
        // Given
        when(postService.createPost(any(CreatePostDto.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/post/create")
                .param("caption", "Test Caption")
                .param("imageUrl", "test-image.jpg")
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/create"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void getPostById_ExistingPost_ShouldReturnDetailView() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(samplePost);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
        
        verify(postService).getPost(postId);
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void getPostById_NonExistingPost_ShouldReturn404View() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/post/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attributeExists("message"));
        
        verify(postService).getPost(postId);
    }

    @Test
    void updatePostForm_ExistingPost_ShouldReturnFormView() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(samplePost);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post/update/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/form"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("isEdit", true));
        
        verify(postService).getPost(postId);
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void updatePostForm_NonExistingPost_ShouldRedirectToPosts() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/post/update/{id}", postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
        
        verify(postService).getPost(postId);
    }

    @Test
    void updatePost_ValidInput_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(postService.updatePost(any(UpdatePostDto.class))).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(put("/post/update/{id}", postId)
                .param("caption", "Updated Caption")
                .param("imageUrl", "updated-image.jpg")
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(postService).updatePost(any(UpdatePostDto.class));
    }

    @Test
    void updatePost_ServiceReturnsNull_ShouldRedirectWithErrorMessage() throws Exception {
        // Given
        when(postService.updatePost(any(UpdatePostDto.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/post/update/{id}", postId)
                .param("caption", "Updated Caption")
                .param("imageUrl", "updated-image.jpg")
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void deletePost_ExistingPost_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(postService.deletePost(postId)).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(post("/post/delete/{id}", postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(postService).deletePost(postId);
    }

    @Test
    void deletePost_NonExistingPost_ShouldRedirectWithErrorMessage() throws Exception {
        // Given
        when(postService.deletePost(postId)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/post/delete/{id}", postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"))
                .andExpect(flash().attributeExists("errorMessage"));
        
        verify(postService).deletePost(postId);
    }

    @Test
    void likePost_ValidInput_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(postService.likePost(postId, userProfileId)).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(post("/post/like/{postId}", postId)
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + postId))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(postService).likePost(postId, userProfileId);
    }

    @Test
    void likePost_ServiceReturnsNull_ShouldRedirectWithErrorMessage() throws Exception {
        // Given
        when(postService.likePost(postId, userProfileId)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/post/like/{postId}", postId)
                .param("userProfileId", userProfileId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + postId))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void getPostDetail_ExistingPost_ShouldReturnDetailView() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(samplePost);
        when(userProfileService.getAllUserProfile()).thenReturn(sampleUserProfiles);

        // When & Then
        mockMvc.perform(get("/post/post/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"));
        
        verify(postService).getPost(postId);
        verify(userProfileService).getAllUserProfile();
    }

    @Test
    void getPostDetail_NonExistingPost_ShouldRedirectToPosts() throws Exception {
        // Given
        when(postService.getPost(postId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/post/post/{id}", postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post"));
        
        verify(postService).getPost(postId);
    }
}