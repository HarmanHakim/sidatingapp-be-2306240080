package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl service;

    private UserProfile user1, user2;
    private UUID user1Id, user2Id;

    @BeforeEach
    void setUp() {
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        user1 = UserProfile.builder()
                .id(user1Id)
                .name("John Doe")
                .nickname("johnny")
                .gender("MALE")
                .birthdate(LocalDate.of(2000, 1, 1))
                .hobbies("Reading, Gaming")
                .location("Jakarta")
                .bio("Software Developer")
                .email("john@example.com")
                .phoneNumber("081234567890")
                .interests("Technology, Books")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        user2 = UserProfile.builder()
                .id(user2Id)
                .name("Jane Smith")
                .nickname("janey")
                .gender("FEMALE")
                .birthdate(LocalDate.of(2001, 2, 2))
                .hobbies("Dancing, Singing")
                .location("Bandung")
                .bio("Artist")
                .email("jane@example.com")
                .phoneNumber("081234567891")
                .interests("Music, Art")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    void testCreateUserProfileComplete() {
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(user1);

        CreateUserDto dto = CreateUserDto.builder()
                .name("John Doe")
                .nickname("johnny")
                .gender("MALE")
                .birthdate(LocalDate.of(2000, 1, 1))
                .hobbies("Reading, Gaming")
                .location("Jakarta")
                .bio("Software Developer")
                .email("john@example.com")
                .phoneNumber("081234567890")
                .interests("Technology, Books")
                .build();

        UserProfile createdUser = service.createUserProfile(dto);

        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.getName());
        assertEquals("johnny", createdUser.getNickname());
        assertEquals("MALE", createdUser.getGender());
        assertEquals(LocalDate.of(2000, 1, 1), createdUser.getBirthdate());
        assertEquals("Reading, Gaming", createdUser.getHobbies());
        assertEquals("Jakarta", createdUser.getLocation());
        assertEquals("Software Developer", createdUser.getBio());
        assertEquals("john@example.com", createdUser.getEmail());
        assertEquals("081234567890", createdUser.getPhoneNumber());
        assertEquals("Technology, Books", createdUser.getInterests());
        assertTrue(createdUser.isActive());

        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testUpdateUserProfileComplete() {
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UpdateUserDto updateDto = UpdateUserDto.builder()
                .id(user1Id)
                .name("John Updated")
                .nickname("johnny_new")
                .gender("MALE")
                .birthdate(LocalDate.of(2000, 1, 1))
                .hobbies("Reading, Gaming, Coding")
                .location("Surabaya")
                .bio("Senior Software Developer")
                .email("john.new@example.com")
                .phoneNumber("081234567999")
                .interests("Technology, Books, AI")
                .isActive(false)
                .build();

        UserProfile updated = service.updateUserProfile(updateDto);

        assertNotNull(updated);
        assertEquals("John Updated", updated.getName());
        assertEquals("johnny_new", updated.getNickname());
        assertEquals("Reading, Gaming, Coding", updated.getHobbies());
        assertEquals("Surabaya", updated.getLocation());
        assertEquals("Senior Software Developer", updated.getBio());
        assertEquals("john.new@example.com", updated.getEmail());
        assertEquals("081234567999", updated.getPhoneNumber());
        assertEquals("Technology, Books, AI", updated.getInterests());
        assertFalse(updated.isActive());
        assertNotNull(updated.getUpdatedAt());

        verify(userProfileRepository, times(1)).findById(user1Id);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    // NEW: Test update user profile not found
    @Test
    void testUpdateUserProfileNotFound() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .id(UUID.randomUUID())
                .name("Non Existent")
                .isActive(true)
                .build();

        when(userProfileRepository.findById(any())).thenReturn(Optional.empty());

        UserProfile updated = service.updateUserProfile(updateDto);
        assertNull(updated);
        verify(userProfileRepository, never()).save(any());
    }

    // NEW: Test update user profile already deleted
    @Test
    void testUpdateUserProfileAlreadyDeleted() {
        user1.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));

        UpdateUserDto updateDto = UpdateUserDto.builder()
                .id(user1Id)
                .name("Updated Name")
                .isActive(true)
                .build();

        UserProfile updated = service.updateUserProfile(updateDto);
        assertNull(updated);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void testDeleteUserProfile() {
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile deleted = service.deleteProfile(user1Id);
        assertNotNull(deleted);
        assertNotNull(deleted.getDeletedAt());

        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testDeleteUserProfileNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userProfileRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        UserProfile deleted = service.deleteProfile(nonExistentId);
        assertNull(deleted);
        verify(userProfileRepository, never()).save(any());
    }

    // NEW: Test delete user profile already deleted
    @Test
    void testDeleteUserProfileAlreadyDeleted() {
        user1.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        
        UserProfile deleted = service.deleteProfile(user1Id);
        assertNull(deleted);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void testGetRandomMatchScoreRange() {
        // Test multiple times to ensure it's in range
        for (int i = 0; i < 100; i++) {
            int score = service.getRandomMatchScore(user1Id, user2Id);
            assertTrue(score >= 0 && score <= 100, "Score should be between 0 and 100, got: " + score);
        }
    }

    @Test
    void testGetMatchScore() {
        // Since getMatchScore calls getRandomMatchScore, test that it returns valid range
        int score = service.getMatchScore(user1Id, user2Id);
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void testGetMatchMessageAllRanges() {
        assertEquals("Sebatas Teman", service.getMatchMessage(0));
        assertEquals("Sebatas Teman", service.getMatchMessage(25));
        assertEquals("Sebatas Teman", service.getMatchMessage(50));
        assertEquals("Teman Sejati", service.getMatchMessage(51));
        assertEquals("Teman Sejati", service.getMatchMessage(65));
        assertEquals("Teman Sejati", service.getMatchMessage(70));
        assertEquals("Cocok", service.getMatchMessage(71));
        assertEquals("Cocok", service.getMatchMessage(85));
        assertEquals("Cocok", service.getMatchMessage(90));
        assertEquals("Cinta Abadi", service.getMatchMessage(91));
        assertEquals("Cinta Abadi", service.getMatchMessage(95));
        assertEquals("Cinta Abadi", service.getMatchMessage(100));
        assertEquals("Cinta Abadi", service.getMatchMessage(150)); // edge case
    }

    @Test
    void testGetMatchImageAllRanges() {
        assertEquals("https://image.idntimes.com/posts/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg",
                service.getMatchImage(0));
        assertEquals("https://image.idntimes.com/posts/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg",
                service.getMatchImage(50));
        assertEquals("https://pbs.twimg.com/media/FddMGmGVIAA1TZK.jpg", 
                service.getMatchImage(51));
        assertEquals("https://pbs.twimg.com/media/FddMGmGVIAA1TZK.jpg", 
                service.getMatchImage(70));
        assertEquals("https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg",
                service.getMatchImage(71));
        assertEquals("https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg",
                service.getMatchImage(90));
        assertEquals("https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg",
                service.getMatchImage(91));
        assertEquals("https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg",
                service.getMatchImage(100));
        assertEquals("https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg",
                service.getMatchImage(150)); // edge case
    }

    @Test
    void testGetAllUserProfile() {
        List<UserProfile> profiles = Arrays.asList(user1, user2);
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(profiles);

        List<UserProfile> result = service.getAllUserProfile();
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
    }

    // NEW: Test getAllUserProfile empty list
    @Test
    void testGetAllUserProfileEmpty() {
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(Arrays.asList());

        List<UserProfile> result = service.getAllUserProfile();
        assertTrue(result.isEmpty());
        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
    }

    @Test
    void testGetUserProfile() {
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        UserProfile result = service.getUserProfile(user1Id);
        assertNotNull(result);
        assertEquals(user1Id, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetUserProfileDeleted() {
        user1.setDeletedAt(LocalDateTime.now());
        when(userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        
        UserProfile result = service.getUserProfile(user1Id);
        assertNull(result);
    }

    @Test
    void testGetUserProfileNotFound() {
        when(userProfileRepository.findById(user2Id)).thenReturn(Optional.empty());
        
        UserProfile result = service.getUserProfile(user2Id);
        assertNull(result);
    }

    // NEW: Test toReadUserProfileDto with all age groups
    @Test
    void testToReadUserProfileDtoAllAgeGroups() {
        // Test 18-25 age group
        UserProfile young = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Young User")
                .birthdate(LocalDate.now().minusYears(20))
                .gender("MALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto youngDto = service.toReadUserProfileDto(young);
        assertEquals(20, youngDto.getAge());
        assertEquals("18-25", youngDto.getAgeGroup());

        // Test 26-35 age group
        UserProfile adult = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Adult User")
                .birthdate(LocalDate.now().minusYears(30))
                .gender("FEMALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto adultDto = service.toReadUserProfileDto(adult);
        assertEquals(30, adultDto.getAge());
        assertEquals("26-35", adultDto.getAgeGroup());

        // Test 36-45 age group
        UserProfile mature = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Mature User")
                .birthdate(LocalDate.now().minusYears(40))
                .gender("MALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto matureDto = service.toReadUserProfileDto(mature);
        assertEquals(40, matureDto.getAge());
        assertEquals("36-45", matureDto.getAgeGroup());

        // Test 45+ age group
        UserProfile senior = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Senior User")
                .birthdate(LocalDate.now().minusYears(50))
                .gender("FEMALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto seniorDto = service.toReadUserProfileDto(senior);
        assertEquals(50, seniorDto.getAge());
        assertEquals("45+", seniorDto.getAgeGroup());
    }

    // NEW: Test toReadUserProfileDto boundary conditions
    @Test
    void testToReadUserProfileDtoAgeBoundaries() {
        // Test exactly 18 years old
        UserProfile user18 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("18 Year Old")
                .birthdate(LocalDate.now().minusYears(18))
                .gender("MALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto dto18 = service.toReadUserProfileDto(user18);
        assertEquals(18, dto18.getAge());
        assertEquals("18-25", dto18.getAgeGroup());

        // Test exactly 25 years old
        UserProfile user25 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("25 Year Old")
                .birthdate(LocalDate.now().minusYears(25))
                .gender("FEMALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto dto25 = service.toReadUserProfileDto(user25);
        assertEquals(25, dto25.getAge());
        assertEquals("18-25", dto25.getAgeGroup());

        // Test exactly 26 years old
        UserProfile user26 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("26 Year Old")
                .birthdate(LocalDate.now().minusYears(26))
                .gender("MALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ReadUserProfileDto dto26 = service.toReadUserProfileDto(user26);
        assertEquals(26, dto26.getAge());
        assertEquals("26-35", dto26.getAgeGroup());
    }

    // NEW: Test toReadUserProfileDto complete mapping
    @Test
    void testToReadUserProfileDtoCompleteMapping() {
        ReadUserProfileDto dto = service.toReadUserProfileDto(user1);
        
        assertNotNull(dto);
        assertEquals(user1.getId(), dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("johnny", dto.getNickname());
        assertEquals(user1.getBirthdate(), dto.getBirthdate());
        assertTrue(dto.getAge() > 0);
        assertNotNull(dto.getAgeGroup());
        assertEquals("Reading, Gaming", dto.getHobbies());
        assertEquals("MALE", dto.getGender());
        assertEquals("Jakarta", dto.getLocation());
        assertEquals("Software Developer", dto.getBio());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("081234567890", dto.getPhoneNumber());
        assertEquals("Technology, Books", dto.getInterests());
        assertEquals(user1.getCreatedAt(), dto.getCreatedAt());
        assertEquals(user1.getUpdatedAt(), dto.getUpdatedAt());
        assertTrue(dto.isActive());
    }

    // NEW: Test getAllUserProfilesDto
    @Test
    void testGetAllUserProfilesDto() {
        List<UserProfile> profiles = Arrays.asList(user1, user2);
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(profiles);

        List<ReadUserProfileDto> result = service.getAllUserProfilesDto();
        
        assertEquals(2, result.size());
        
        ReadUserProfileDto dto1 = result.stream()
                .filter(dto -> dto.getName().equals("John Doe"))
                .findFirst()
                .orElse(null);
        assertNotNull(dto1);
        assertEquals("johnny", dto1.getNickname());
        assertEquals("MALE", dto1.getGender());
        
        ReadUserProfileDto dto2 = result.stream()
                .filter(dto -> dto.getName().equals("Jane Smith"))
                .findFirst()
                .orElse(null);
        assertNotNull(dto2);
        assertEquals("janey", dto2.getNickname());
        assertEquals("FEMALE", dto2.getGender());

        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
    }

    // NEW: Test searchProfilesByName
    @Test
    void testSearchProfilesByName() {
        String searchName = "John";
        List<UserProfile> matchingProfiles = Arrays.asList(user1);
        when(userProfileRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(searchName))
                .thenReturn(matchingProfiles);

        List<UserProfile> result = service.searchProfilesByName(searchName);
        
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(userProfileRepository, times(1))
                .findByNameContainingIgnoreCaseAndDeletedAtIsNull(searchName);
    }

    // NEW: Test searchProfilesByName with null name
    @Test
    void testSearchProfilesByNameWithNull() {
        List<UserProfile> allProfiles = Arrays.asList(user1, user2);
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(allProfiles);

        List<UserProfile> result = service.searchProfilesByName(null);
        
        assertEquals(2, result.size());
        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
        verify(userProfileRepository, never()).findByNameContainingIgnoreCaseAndDeletedAtIsNull(any());
    }

    // NEW: Test searchProfilesByName with blank name
    @Test
    void testSearchProfilesByNameWithBlank() {
        List<UserProfile> allProfiles = Arrays.asList(user1, user2);
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(allProfiles);

        List<UserProfile> result = service.searchProfilesByName("   ");
        
        assertEquals(2, result.size());
        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
        verify(userProfileRepository, never()).findByNameContainingIgnoreCaseAndDeletedAtIsNull(any());
    }

    // NEW: Test searchProfilesByName with empty string
    @Test
    void testSearchProfilesByNameWithEmpty() {
        List<UserProfile> allProfiles = Arrays.asList(user1, user2);
        when(userProfileRepository.findByDeletedAtIsNull()).thenReturn(allProfiles);

        List<UserProfile> result = service.searchProfilesByName("");
        
        assertEquals(2, result.size());
        verify(userProfileRepository, times(1)).findByDeletedAtIsNull();
        verify(userProfileRepository, never()).findByNameContainingIgnoreCaseAndDeletedAtIsNull(any());
    }

    // NEW: Test searchProfilesByName no matches
    @Test
    void testSearchProfilesByNameNoMatches() {
        String searchName = "NonExistent";
        when(userProfileRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(searchName))
                .thenReturn(Arrays.asList());

        List<UserProfile> result = service.searchProfilesByName(searchName);
        
        assertTrue(result.isEmpty());
        verify(userProfileRepository, times(1))
                .findByNameContainingIgnoreCaseAndDeletedAtIsNull(searchName);
    }
}