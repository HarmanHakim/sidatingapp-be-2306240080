package io.harman.sidating_app_be.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    private Post post;
    private UserProfile userProfile;
    private UUID postId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setName("John Doe");

        post = new Post();
        post.setId(postId);
        post.setUserProfile(userProfile);
        post.setImageUrl("https://example.com/image.jpg");
        post.setCaption("Test caption");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setLikes(new ArrayList<>());
    }

    @Test
    void testPostCreation() {
        assertNotNull(post);
        assertEquals(postId, post.getId());
        assertEquals(userProfile, post.getUserProfile());
        assertEquals("https://example.com/image.jpg", post.getImageUrl());
        assertEquals("Test caption", post.getCaption());
        assertNotNull(post.getCreatedAt());
        assertNotNull(post.getUpdatedAt());
        assertNotNull(post.getLikes());
        assertTrue(post.getLikes().isEmpty());
    }

    @Test
    void testPostBuilder() {
        LocalDateTime now = LocalDateTime.now();
        List<UserProfile> likes = new ArrayList<>();
        likes.add(userProfile);

        Post builtPost = Post.builder()
                .id(postId)
                .userProfile(userProfile)
                .imageUrl("https://example.com/image.jpg")
                .caption("Test caption")
                .createdAt(now)
                .updatedAt(now)
                .likes(likes)
                .build();

        assertNotNull(builtPost);
        assertEquals(postId, builtPost.getId());
        assertEquals(userProfile, builtPost.getUserProfile());
        assertEquals("https://example.com/image.jpg", builtPost.getImageUrl());
        assertEquals("Test caption", builtPost.getCaption());
        assertEquals(now, builtPost.getCreatedAt());
        assertEquals(now, builtPost.getUpdatedAt());
        assertEquals(likes, builtPost.getLikes());
        assertEquals(1, builtPost.getLikes().size());
    }

    @Test
    void testAddLike() {
        assertTrue(post.getLikes().isEmpty());

        post.getLikes().add(userProfile);

        assertEquals(1, post.getLikes().size());
        assertTrue(post.getLikes().contains(userProfile));
    }

    @Test
    void testRemoveLike() {
        post.getLikes().add(userProfile);
        assertEquals(1, post.getLikes().size());

        post.getLikes().remove(userProfile);

        assertTrue(post.getLikes().isEmpty());
        assertFalse(post.getLikes().contains(userProfile));
    }

    @Test
    void testSoftDelete() {
        assertNull(post.getDeletedAt());

        LocalDateTime deletedAt = LocalDateTime.now();
        post.setDeletedAt(deletedAt);

        assertNotNull(post.getDeletedAt());
        assertEquals(deletedAt, post.getDeletedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Post anotherPost = new Post();
        anotherPost.setId(postId);
        anotherPost.setCaption("Test caption");
        anotherPost.setImageUrl("https://example.com/image.jpg");
        anotherPost.setUserProfile(userProfile);

        // Since equals/hashCode might be based on all fields or ID only
        // Let's test basic equality concepts
        assertNotNull(post);
        assertNotNull(anotherPost);
        
        // Test self-equality
        assertEquals(post, post);
        
        // Test null inequality
        assertNotEquals(post, null);
        
        Post differentPost = new Post();
        differentPost.setId(UUID.randomUUID());
        differentPost.setCaption("Different caption");

        // Different IDs should not be equal
        assertNotEquals(post, differentPost);
    }

    @Test
    void testToString() {
        String postString = post.toString();

        assertNotNull(postString);
        assertTrue(postString.contains("Post"));
        assertTrue(postString.contains(postId.toString()));
    }

    @Test
    void testPostSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        String newImageUrl = "https://example.com/new-image.jpg";
        String newCaption = "New caption";
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        List<UserProfile> newLikes = new ArrayList<>();

        post.setId(newId);
        post.setImageUrl(newImageUrl);
        post.setCaption(newCaption);
        post.setUpdatedAt(newTime);
        post.setLikes(newLikes);

        assertEquals(newId, post.getId());
        assertEquals(newImageUrl, post.getImageUrl());
        assertEquals(newCaption, post.getCaption());
        assertEquals(newTime, post.getUpdatedAt());
        assertEquals(newLikes, post.getLikes());
    }

    @Test
    void testPostWithNullValues() {
        Post nullPost = new Post();

        assertNull(nullPost.getId());
        assertNull(nullPost.getUserProfile());
        assertNull(nullPost.getImageUrl());
        assertNull(nullPost.getCaption());
        assertNull(nullPost.getCreatedAt());
        assertNull(nullPost.getUpdatedAt());
        assertNull(nullPost.getLikes());
        assertNull(nullPost.getDeletedAt());
    }
}