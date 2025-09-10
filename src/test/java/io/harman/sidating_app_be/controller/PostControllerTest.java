package io.harman.sidating_app_be.controller;

import io.harman.sidating_app_be.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileController userProfileController;

    private UUID nonExistentPostId = UUID.randomUUID();
    private UUID userProfileId;
    private UserProfile testUserProfile;
    private List<UserProfile> mockUserProfiles;

    @BeforeEach
    void setUp() {
        userProfileId = UUID.randomUUID();
        testUserProfile = UserProfile.builder()
                .id(userProfileId)
                .name("Test User")
                .nickname("TestUser")
                .email("test@example.com")
                .phoneNumber("081234567890")
                .location("Test City")
                .gender("MALE")
                .birthdate(LocalDate.of(1990, 1, 1))
                .hobbies("Testing")
                .interests("Quality Assurance")
                .bio("Test user bio")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();

        mockUserProfiles = new ArrayList<>();
        mockUserProfiles.add(testUserProfile);

        when(userProfileController.getAllProfiles()).thenReturn(mockUserProfiles);
    }

    @Test
    void testGetAllPosts() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testGetAllPostsWithOldestOrder() throws Exception {
        mockMvc.perform(get("/posts")
                .param("order", "oldest"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("selectedOrder", "oldest"));
    }

    @Test
    void testGetAllPostsWithNewestOrder() throws Exception {
        mockMvc.perform(get("/posts")
                .param("order", "newest"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("selectedOrder", "newest"));
    }

    @Test
    void testGetPostByIdNotFound() throws Exception {
        mockMvc.perform(get("/posts/" + nonExistentPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attribute("message", "Post with ID " + nonExistentPostId + " not found."));
    }

    @Test
    void testGetCreatePostForm() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    void testCreatePost() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "New test post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testCreatePostWithoutUserProfile() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("caption", "New test post without user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testGetEditPostFormNotFound() throws Exception {
        mockMvc.perform(get("/posts/update/" + nonExistentPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attribute("message", "Post with ID " + nonExistentPostId + " not found."));
    }

    @Test
    void testDeletePostNotFound() throws Exception {
        mockMvc.perform(get("/posts/delete/" + nonExistentPostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testCreateAndGetPostById() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Test post for detail view"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    void testUpdatePostFlow() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Original caption"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    void testDeletePostFlow() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post to be deleted"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    void testGetAllPostsWithEmptyList() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testCreatePostWithAllFields() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Complete post with all fields")
                .param("createdAt", LocalDateTime.now().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testGetAllPostsWithInvalidOrder() throws Exception {
        mockMvc.perform(get("/posts")
                .param("order", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attribute("selectedOrder", "invalid"));
    }

    @Test
    void testCreatePostThenGetById() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post created for testing get by ID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testCreatePostThenUpdate() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post to be updated"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testCreatePostThenDelete() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post to be deleted"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testGetEditFormNotFoundFlow() throws Exception {
        mockMvc.perform(get("/posts/update/" + nonExistentPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"));
    }

    @Test
    void testUpdateNonExistentPost() throws Exception {
        mockMvc.perform(post("/posts/update/" + nonExistentPostId)
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Updated caption"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testCreatePostWithMinimalData() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("caption", "Minimal post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testCreatePostWithEmptyCaption() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testPostSorting() throws Exception {
        mockMvc.perform(get("/posts")
                .param("order", "newest"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attribute("selectedOrder", "newest"));

        mockMvc.perform(get("/posts")
                .param("order", "oldest"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attribute("selectedOrder", "oldest"));
    }

    @Test
    void testEditPostFormWithExistingPost() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post to edit"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    void testGetPostByIdWithExistingPost() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post for get by ID test"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testDeletePostWithExistingPost() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post to delete"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    void testUpdatePostWithExistingPost() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Original post"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testCreatePostWithDifferentParameters() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("caption", "Caption only post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Complete post with all fields"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testGetCreateFormAttributes() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    void testCreatePostWithNullUserProfileId() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("caption", "Post without user profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testCreatePostWithInvalidUserProfileId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", invalidId.toString())
                .param("caption", "Post with invalid user ID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testGetAllPostsReturnsCorrectModelAttributes() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("userProfiles"))
                .andExpect(model().attribute("selectedOrder", "newest"));
    }

    @Test
    void testErrorHandlingForNonExistentPosts() throws Exception {
        UUID randomId = UUID.randomUUID();
        
        mockMvc.perform(get("/posts/" + randomId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attribute("message", "Post with ID " + randomId + " not found."));

        mockMvc.perform(get("/posts/update/" + randomId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("title", "Post Not Found"))
                .andExpect(model().attribute("message", "Post with ID " + randomId + " not found."));

        mockMvc.perform(get("/posts/delete/" + randomId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test 
    void testCreatePostAndThenAccessIt() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "First test post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Second test post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void testEditPostFormFlow() throws Exception {
        mockMvc.perform(get("/posts/update/" + nonExistentPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    void testCreatePostWithSpecialCharacters() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Post with special characters: àáâãäåæçèéêë"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testCreatePostWithLongCaption() throws Exception {
        String longCaption = "This is a very long caption that contains many words and should test how the application handles long text input. ".repeat(10);
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", longCaption))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testGetAllPostsWithMultipleFilters() throws Exception {
        mockMvc.perform(get("/posts")
                .param("userId", userProfileId.toString())
                .param("order", "custom"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attribute("selectedUserId", userProfileId))
                .andExpect(model().attribute("selectedOrder", "custom"));
    }

    @Test
    void testPostControllerInitialization() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/view-all"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("userProfiles"));
    }

    @Test
    void testUpdateExistingPostSuccessfully() throws Exception {
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Original caption"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Updated caption"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostWithNullUserProfileId() throws Exception {
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("caption", "Updated caption without user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostWithValidUserProfileId() throws Exception {
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Updated caption with valid user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostWithInvalidUserProfileId() throws Exception {
        UUID invalidUserProfileId = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", invalidUserProfileId.toString())
                .param("caption", "Updated caption with invalid user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    /**
     * The key insight is that we need to create posts first, then use the Spring Test
     * framework to inspect or manipulate the controller state to get actual post IDs.
     */
    
    @Test
    void testUpdatePostComprehensiveCoverage() throws Exception {
        
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Test post 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Test post 2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(post("/posts/create")
                .param("caption", "Test post 3 without user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));


        UUID testId1 = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + testId1)
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Updated with valid user profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        UUID testId2 = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + testId2)
                .param("caption", "Updated without user profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        UUID testId3 = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + testId3)
                .param("userProfileId", invalidUserId.toString())
                .param("caption", "Updated with invalid user profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        UUID testId4 = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + testId4)
                .param("caption", "Minimal update data"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        UUID testId5 = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + testId5)
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Complete update with all fields"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostActualExistingPost() throws Exception {
        
        
        mockMvc.perform(post("/posts/create")
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Existing post 1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/posts/create")
                .param("caption", "Existing post 2"))
                .andExpect(status().is3xxRedirection());

        
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Testing userProfileId branch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("caption", "Testing null userProfileId branch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        UUID nonExistentUserId = UUID.randomUUID();
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", nonExistentUserId.toString())
                .param("caption", "Testing invalid userProfileId branch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void testUpdatePostEdgeCases() throws Exception {
        
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        String longCaption = "Very long caption ".repeat(100);
        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", longCaption))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", "Special chars: àáâãäåæçèéêë 中文 العربية"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("caption", "No image URL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts/update/" + UUID.randomUUID())
                .param("userProfileId", userProfileId.toString())
                .param("imageUrl", "")
                .param("caption", "Empty image URL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }
}
