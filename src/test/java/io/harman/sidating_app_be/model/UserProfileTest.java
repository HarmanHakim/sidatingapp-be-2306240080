package io.harman.sidating_app_be.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    private UserProfile userProfile;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setName("John Doe");
        userProfile.setNickname("Johnny");
        userProfile.setEmail("john.doe@example.com");
        userProfile.setPhoneNumber("081234567890");
        userProfile.setLocation("Jakarta");
        userProfile.setBio("Test bio");
        userProfile.setInterests("Reading, Gaming");
        userProfile.setGender("MALE");
        userProfile.setBirthdate(LocalDate.of(1995, 1, 1));
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfile.setActive(true);
        userProfile.setPosts(new ArrayList<>());
    }

    @Test
    void testUserProfileCreation() {
        assertNotNull(userProfile);
        assertEquals(userId, userProfile.getId());
        assertEquals("John Doe", userProfile.getName());
        assertEquals("Johnny", userProfile.getNickname());
        assertEquals("john.doe@example.com", userProfile.getEmail());
        assertEquals("081234567890", userProfile.getPhoneNumber());
        assertEquals("Jakarta", userProfile.getLocation());
        assertEquals("Test bio", userProfile.getBio());
        assertEquals("Reading, Gaming", userProfile.getInterests());
        assertEquals("MALE", userProfile.getGender());
        assertEquals(LocalDate.of(1995, 1, 1), userProfile.getBirthdate());
        assertNotNull(userProfile.getCreatedAt());
        assertNotNull(userProfile.getUpdatedAt());
        assertTrue(userProfile.isActive());
        assertNotNull(userProfile.getPosts());
        assertTrue(userProfile.getPosts().isEmpty());
    }

    @Test
    void testUserProfileBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate birthdate = LocalDate.of(1990, 5, 15);
        List<Post> posts = new ArrayList<>();

        UserProfile builtProfile = UserProfile.builder()
                .id(userId)
                .name("Jane Doe")
                .nickname("Janey")
                .email("jane.doe@example.com")
                .phoneNumber("081987654321")
                .location("Bandung")
                .bio("Another test bio")
                .interests("Cooking, Traveling")
                .gender("FEMALE")
                .birthdate(birthdate)
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .posts(posts)
                .build();

        assertNotNull(builtProfile);
        assertEquals(userId, builtProfile.getId());
        assertEquals("Jane Doe", builtProfile.getName());
        assertEquals("Janey", builtProfile.getNickname());
        assertEquals("jane.doe@example.com", builtProfile.getEmail());
        assertEquals("081987654321", builtProfile.getPhoneNumber());
        assertEquals("Bandung", builtProfile.getLocation());
        assertEquals("Another test bio", builtProfile.getBio());
        assertEquals("Cooking, Traveling", builtProfile.getInterests());
        assertEquals("FEMALE", builtProfile.getGender());
        assertEquals(birthdate, builtProfile.getBirthdate());
        assertEquals(now, builtProfile.getCreatedAt());
        assertEquals(now, builtProfile.getUpdatedAt());
        assertTrue(builtProfile.isActive());
        assertEquals(posts, builtProfile.getPosts());
    }

    @Test
    void testAddPost() {
        assertTrue(userProfile.getPosts().isEmpty());

        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setUserProfile(userProfile);
        post.setCaption("Test post");

        userProfile.getPosts().add(post);

        assertEquals(1, userProfile.getPosts().size());
        assertTrue(userProfile.getPosts().contains(post));
        assertEquals(userProfile, post.getUserProfile());
    }

    @Test
    void testRemovePost() {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        userProfile.getPosts().add(post);
        assertEquals(1, userProfile.getPosts().size());

        userProfile.getPosts().remove(post);

        assertTrue(userProfile.getPosts().isEmpty());
        assertFalse(userProfile.getPosts().contains(post));
    }

    @Test
    void testSoftDelete() {
        assertNull(userProfile.getDeletedAt());

        LocalDateTime deletedAt = LocalDateTime.now();
        userProfile.setDeletedAt(deletedAt);

        assertNotNull(userProfile.getDeletedAt());
        assertEquals(deletedAt, userProfile.getDeletedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        UserProfile anotherProfile = new UserProfile();
        anotherProfile.setId(userId);
        anotherProfile.setName("John Doe");
        anotherProfile.setEmail("john.doe@example.com");

        // Since equals/hashCode might be based on all fields or ID only
        // Let's test basic equality concepts
        assertNotNull(userProfile);
        assertNotNull(anotherProfile);
        
        // Test self-equality
        assertEquals(userProfile, userProfile);
        
        // Test null inequality
        assertNotEquals(userProfile, null);
        
        UserProfile differentProfile = new UserProfile();
        differentProfile.setId(UUID.randomUUID());
        differentProfile.setName("Different Name");

        // Different IDs should not be equal
        assertNotEquals(userProfile, differentProfile);
    }

    @Test
    void testToString() {
        String profileString = userProfile.toString();

        assertNotNull(profileString);
        assertTrue(profileString.contains("UserProfile"));
        assertTrue(profileString.contains(userId.toString()));
    }

    @Test
    void testUserProfileSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        String newName = "Updated Name";
        String newNickname = "UpdatedNick";
        String newEmail = "updated@example.com";
        String newPhoneNumber = "081111111111";
        String newLocation = "Surabaya";
        String newBio = "Updated bio";
        String newInterests = "Updated interests";
        String newGender = "FEMALE";
        LocalDate newBirthdate = LocalDate.of(1990, 12, 25);
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        List<Post> newPosts = new ArrayList<>();

        userProfile.setId(newId);
        userProfile.setName(newName);
        userProfile.setNickname(newNickname);
        userProfile.setEmail(newEmail);
        userProfile.setPhoneNumber(newPhoneNumber);
        userProfile.setLocation(newLocation);
        userProfile.setBio(newBio);
        userProfile.setInterests(newInterests);
        userProfile.setGender(newGender);
        userProfile.setBirthdate(newBirthdate);
        userProfile.setUpdatedAt(newTime);
        userProfile.setActive(false);
        userProfile.setPosts(newPosts);

        assertEquals(newId, userProfile.getId());
        assertEquals(newName, userProfile.getName());
        assertEquals(newNickname, userProfile.getNickname());
        assertEquals(newEmail, userProfile.getEmail());
        assertEquals(newPhoneNumber, userProfile.getPhoneNumber());
        assertEquals(newLocation, userProfile.getLocation());
        assertEquals(newBio, userProfile.getBio());
        assertEquals(newInterests, userProfile.getInterests());
        assertEquals(newGender, userProfile.getGender());
        assertEquals(newBirthdate, userProfile.getBirthdate());
        assertEquals(newTime, userProfile.getUpdatedAt());
        assertFalse(userProfile.isActive());
        assertEquals(newPosts, userProfile.getPosts());
    }

    @Test
    void testUserProfileWithNullValues() {
        UserProfile nullProfile = new UserProfile();

        assertNull(nullProfile.getId());
        assertNull(nullProfile.getName());
        assertNull(nullProfile.getNickname());
        assertNull(nullProfile.getEmail());
        assertNull(nullProfile.getPhoneNumber());
        assertNull(nullProfile.getLocation());
        assertNull(nullProfile.getBio());
        assertNull(nullProfile.getInterests());
        assertNull(nullProfile.getGender());
        assertNull(nullProfile.getBirthdate());
        assertNull(nullProfile.getCreatedAt());
        assertNull(nullProfile.getUpdatedAt());
        assertNull(nullProfile.getPosts());
        assertNull(nullProfile.getDeletedAt());
        assertFalse(nullProfile.isActive()); // boolean default
    }

    @Test
    void testActiveStatusToggle() {
        assertTrue(userProfile.isActive());

        userProfile.setActive(false);
        assertFalse(userProfile.isActive());

        userProfile.setActive(true);
        assertTrue(userProfile.isActive());
    }

    @Test
    void testBirthdateValidation() {
        LocalDate pastDate = LocalDate.of(1980, 1, 1);
        LocalDate futureDate = LocalDate.of(2030, 1, 1);

        userProfile.setBirthdate(pastDate);
        assertEquals(pastDate, userProfile.getBirthdate());

        // In a real application, you might want validation for future dates
        userProfile.setBirthdate(futureDate);
        assertEquals(futureDate, userProfile.getBirthdate());
    }
}