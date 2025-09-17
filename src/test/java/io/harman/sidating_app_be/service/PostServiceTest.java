package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostServiceTest {

    private PostServiceImpl postService;
    private UserProfileServiceImpl userService;
    private UserProfile user;
    private UserProfile user1;
    private Post post;
    private Post post1;
    private Post noUserPost;
    private Post fakePost;

    @BeforeEach
    void setUp() {
        userService = new UserProfileServiceImpl();
        postService = new PostServiceImpl(userService);

        user = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("User")
                .gender("MALE")
                .birthdate(LocalDate.of(2000, 1, 1))
                .build();
        userService.createUserProfile(user);

        user1 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("User1")
                .gender("MALE")
                .birthdate(LocalDate.of(2000, 1, 1))
                .build();
        userService.createUserProfile(user1);

        post = Post.builder()
                .userProfileId(user.getId())
                .caption("Hello")
                .build();
        
        post1 = Post.builder()
                .userProfileId(user.getId())
                .caption("World")
                .build();

        noUserPost = Post.builder()
                .caption("World")
                .build();

        fakePost = Post.builder()
                .id(UUID.randomUUID())
                .userProfileId(user.getId())
                .caption("Fake")
                .build();
    }

    @Test
    void testCreateAndGetPost() {
        Post created = postService.createPost(post);
        Post created1 = postService.createPost(post1);
        Post noUserPostCreated = postService.createPost(noUserPost);
        
        assertNotNull(created);
        assertNotNull(created1);
        assertNull(noUserPostCreated);
        
        assertEquals(post.getCaption(), postService.getPost(created.getId()).getCaption());
        assertNull(postService.getPost(UUID.randomUUID()));
        assertEquals(2, postService.getAllPost(null, "desc").size());
    }

    @Test
    void testGetAllPostWithFilterAndSort() throws InterruptedException {
        Post created1 = postService.createPost(post);
        Thread.sleep(10); // Membedakan createdAt
        Post created2 = postService.createPost(post1);

        assertEquals(2, postService.getAllPost(null, "desc").size());
        assertEquals(created2.getId(), postService.getAllPost(null, "desc").get(0).getId());
        assertEquals(created1.getId(), postService.getAllPost(null, "asc").get(0).getId());

        assertEquals(2, postService.getAllPost(user.getId(), "desc").size());
        assertEquals(0, postService.getAllPost(user1.getId(), "desc").size());
    }

    @Test
    void testUpdatePost() {
        Post created = postService.createPost(post);
        created.setCaption("Updated");
        Post updated = postService.updatePost(created);
        
        Post fakePostUpdate = postService.updatePost(fakePost);

        Post noProfileCreated = postService.createPost(post1);
        userService.deleteProfile(user.getId());
        Post noProfileUpdated = postService.updatePost(noProfileCreated);

        assertNotNull(updated);
        assertNull(fakePostUpdate);
        assertNull(noProfileUpdated);
    }

    @Test
    void testDeletePost() {
        Post created = postService.createPost(post);
        Post deletedNoPost = postService.deletePost(UUID.randomUUID());
        Post deleted = postService.deletePost(created.getId());

        assertNotNull(deleted);
        assertNull(postService.getPost(created.getId()));
        assertNull(deletedNoPost);
    }

    @Test
    void testLikePost() {
        Post created = postService.createPost(post);
        Post liked = postService.likePost(created.getId(), user.getId());

        assertNull(postService.likePost(UUID.randomUUID(), user.getId()));
        assertNotNull(liked);
        assertEquals(1, liked.getLikes().size());
        assertNull(postService.likePost(created.getId(), UUID.randomUUID()));

        Post unliked = postService.likePost(created.getId(), user.getId());
        assertNotNull(unliked);
        assertEquals(0, unliked.getLikes().size());
    }
}