package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileServiceImplTest {

    private UserProfileServiceImpl userProfileService;
    private UserProfile userMale;
    private UserProfile userFemale;
    private UserProfile userAnotherMale;

    @BeforeEach
    void setUp() {
        // Arrange: Inisialisasi service dan data dummy sebelum setiap pengujian
        userProfileService = new UserProfileServiceImpl();

        userMale = new UserProfile();
        userMale.setId(UUID.randomUUID());
        userMale.setName("Budi");
        userMale.setGender("MALE");

        userFemale = new UserProfile();
        userFemale.setId(UUID.randomUUID());
        userFemale.setName("Cinta");
        userFemale.setGender("FEMALE");
        
        userAnotherMale = new UserProfile();
        userAnotherMale.setId(UUID.randomUUID());
        userAnotherMale.setName("Andi");
        userAnotherMale.setGender("MALE");
    }

    @Test
    void testCreateAndGetAllUserProfile() {
        // Act
        userProfileService.createUserProfile(userMale);
        userProfileService.createUserProfile(userFemale);

        // Assert
        List<UserProfile> allUsers = userProfileService.getAllUserProfile();
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size(), "Harusnya ada 2 user di dalam database");
    }

    @Test
    void testGetUserProfile() {
        // Arrange
        userProfileService.createUserProfile(userMale);

        // Act & Assert for existing user
        UserProfile foundUser = userProfileService.getUserProfile(userMale.getId());
        assertNotNull(foundUser);
        assertEquals(userMale.getId(), foundUser.getId());

        // Act & Assert for non-existing user
        UserProfile notFoundUser = userProfileService.getUserProfile(UUID.randomUUID());
        assertNull(notFoundUser, "Harusnya mengembalikan null jika user tidak ditemukan");
    }

    @Test
    void testUpdateUserProfile() {
        // Arrange
        userProfileService.createUserProfile(userMale);
        String newName = "Budi Santoso";
        userMale.setName(newName);

        // Act
        UserProfile updatedUser = userProfileService.updateUserProfile(userMale);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(newName, updatedUser.getName(), "Nama user seharusnya sudah terupdate");
        assertEquals(newName, userProfileService.getUserProfile(userMale.getId()).getName());
    }
    
    @Test
    void testUpdateUserProfile_NotFound() {
        // Arrange
        UserProfile nonExistingUser = new UserProfile();
        nonExistingUser.setId(UUID.randomUUID());
        nonExistingUser.setName("Hantu");

        // Act
        UserProfile result = userProfileService.updateUserProfile(nonExistingUser);

        // Assert
        assertNull(result, "Harusnya mengembalikan null jika user yang diupdate tidak ada");
    }


    @Test
    void testDeleteProfile() {
        // Arrange
        userProfileService.createUserProfile(userMale);
        assertEquals(1, userProfileService.getAllUserProfile().size());

        // Act
        UserProfile deletedUser = userProfileService.deleteProfile(userMale.getId());

        // Assert
        assertNotNull(deletedUser);
        assertEquals(userMale.getId(), deletedUser.getId());
        assertEquals(0, userProfileService.getAllUserProfile().size(), "Database harusnya kosong setelah user dihapus");
        assertNull(userProfileService.getUserProfile(userMale.getId()), "User yang sudah dihapus tidak boleh ditemukan");
    }
    
    @Test
    void testDeleteProfile_NotFound() {
        // Act
        UserProfile result = userProfileService.deleteProfile(UUID.randomUUID());
        
        // Assert
        assertNull(result, "Harusnya mengembalikan null jika user yang dihapus tidak ada");
    }

    // ===============================================================
    // == Unit Test untuk Fitur Match Score
    // ===============================================================

    @Test
    void testCalculateMatchScore_WhenGenderIsSame() {
        // Arrange
        userProfileService.createUserProfile(userMale);
        userProfileService.createUserProfile(userAnotherMale);

        // Act
        Map<String, Object> result = userProfileService.calculateMatchScore(userMale.getId(), userAnotherMale.getId());

        // Assert
        assertNotNull(result);
        int score = (int) result.get("score");
        assertTrue(score >= 0 && score < 50, "Skor harus antara 0-49 untuk gender yang sama");
    }

    @Test
    void testCalculateMatchScore_WhenGenderIsDifferent() {
        // Arrange
        userProfileService.createUserProfile(userMale);
        userProfileService.createUserProfile(userFemale);

        // Act
        Map<String, Object> result = userProfileService.calculateMatchScore(userMale.getId(), userFemale.getId());

        // Assert
        assertNotNull(result);
        int score = (int) result.get("score");
        assertTrue(score >= 50 && score <= 100, "Skor harus antara 50-100 untuk gender yang berbeda");
    }

    @Test
    void testCalculateMatchScore_WhenUserNotFound() {
        // Arrange
        userProfileService.createUserProfile(userMale);

        // Act
        Map<String, Object> result = userProfileService.calculateMatchScore(userMale.getId(), UUID.randomUUID());

        // Assert
        assertNull(result, "Hasil harus null jika salah satu user tidak ada");
    }
}