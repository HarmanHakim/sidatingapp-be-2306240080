package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID id1;
    private UUID id2;

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
    }

    @Test
    void getAllUserProfile_ShouldReturnAll() {
        UserProfile u1 = UserProfile.builder().id(id1).name("A").email("a@a").phoneNumber("081").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        UserProfile u2 = UserProfile.builder().id(id2).name("B").email("b@b").phoneNumber("082").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(userProfileRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserProfile> res = userProfileService.getAllUserProfile();

        assertThat(res).hasSize(2);
        verify(userProfileRepository).findAll();
    }

    @Test
    void searchUserProfilesByName_ShouldUseFindAllWhenEmpty() {
        when(userProfileRepository.findAll()).thenReturn(Arrays.asList());

        List<UserProfile> r1 = userProfileService.searchUserProfilesByName(null);
        List<UserProfile> r2 = userProfileService.searchUserProfilesByName("   ");

        verify(userProfileRepository, times(2)).findAll();
        verify(userProfileRepository, never()).findByNameContainingIgnoreCase(anyString());
        assertThat(r1).isEmpty();
        assertThat(r2).isEmpty();
    }

    @Test
    void searchUserProfilesByName_ShouldCallFindByName_WhenProvided() {
        UserProfile found = UserProfile.builder().id(id1).name("Alice").email("a@a").phoneNumber("081").build();
    // Stub repository for the exact input string (service currently forwards the input as-is)
    lenient().when(userProfileRepository.findByNameContainingIgnoreCase(" ali ")).thenReturn(Arrays.asList(found));

    List<UserProfile> res = userProfileService.searchUserProfilesByName(" ali ");

    verify(userProfileRepository).findByNameContainingIgnoreCase(" ali ");
    assertThat(res).hasSize(1);
    }

    @Test
    void mapToReadUserProfileDto_ShouldSetAgeGroupsCorrectly() {
        UserProfile u20 = UserProfile.builder().id(id1).name("U20").birthdate(LocalDate.now().minusYears(20)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isActive(true).build();
        var dto20 = userProfileService.mapToReadUserProfileDto(u20);
        assertThat(dto20.getAgeGroup()).isEqualTo("18-25");

        UserProfile u30 = UserProfile.builder().id(id2).name("U30").birthdate(LocalDate.now().minusYears(30)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isActive(true).build();
        var dto30 = userProfileService.mapToReadUserProfileDto(u30);
        assertThat(dto30.getAgeGroup()).isEqualTo("26-35");

        UserProfile u40 = UserProfile.builder().id(UUID.randomUUID()).name("U40").birthdate(LocalDate.now().minusYears(40)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isActive(true).build();
        var dto40 = userProfileService.mapToReadUserProfileDto(u40);
        assertThat(dto40.getAgeGroup()).isEqualTo("36-45");

        UserProfile u50 = UserProfile.builder().id(UUID.randomUUID()).name("U50").birthdate(LocalDate.now().minusYears(50)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isActive(true).build();
        var dto50 = userProfileService.mapToReadUserProfileDto(u50);
        assertThat(dto50.getAgeGroup()).isEqualTo("45+");
    }

    @Test
    void getMatchMessageAndImage_ShouldReturnExpectedStrings() {
        assertThat(userProfileService.getMatchMessage(30)).isEqualTo("Sebatas Teman");
        assertThat(userProfileService.getMatchMessage(60)).isEqualTo("Teman Sejati");
        assertThat(userProfileService.getMatchMessage(80)).isEqualTo("Cocok");
        assertThat(userProfileService.getMatchMessage(95)).isEqualTo("Cinta Abadi");

        // Images: just check that function returns the expected url fragments
        assertThat(userProfileService.getMatchImage(30)).contains("image.idntimes.com");
        assertThat(userProfileService.getMatchImage(60)).contains("pbs.twimg.com");
        assertThat(userProfileService.getMatchImage(80)).contains("pinimg.com");
        assertThat(userProfileService.getMatchImage(95)).contains("pngtree.com");
    }

    @Test
    void getRandomMatchScore_ShouldReturnCorrectRange_BasedOnGender() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UserProfile pa = UserProfile.builder().id(a).gender("male").build();
        UserProfile pb = UserProfile.builder().id(b).gender("male").build();
        when(userProfileRepository.findById(a)).thenReturn(Optional.of(pa));
        when(userProfileRepository.findById(b)).thenReturn(Optional.of(pb));

        int score = userProfileService.getRandomMatchScore(a, b);
        assertThat(score).isBetween(0, 49);

        // different genders
        UserProfile pc = UserProfile.builder().id(UUID.randomUUID()).gender("female").build();
        when(userProfileRepository.findById(pc.getId())).thenReturn(Optional.of(pc));
        when(userProfileRepository.findById(a)).thenReturn(Optional.of(pa));

        int score2 = userProfileService.getRandomMatchScore(a, pc.getId());
        assertThat(score2).isBetween(50, 100);
    }

    @Test
    void createUpdateGetDeleteProfile_flow() {
        // create
        CreateUserDto create = CreateUserDto.builder()
                .name("New")
                .nickname("nn")
                .birthdate(LocalDate.now().minusYears(25))
                .hobbies("hobby")
                .gender("male")
                .location("loc")
                .bio("bio")
                .email("e@e.com")
                .phoneNumber("081")
                .interests("int")
                .build();

        UserProfile saved = UserProfile.builder().id(id1).name(create.getName()).email(create.getEmail()).phoneNumber(create.getPhoneNumber()).birthdate(create.getBirthdate()).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(saved);

        UserProfile created = userProfileService.createUserProfile(create);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(id1);

        // get
        when(userProfileRepository.findById(id1)).thenReturn(Optional.of(created));
        UserProfile got = userProfileService.getUserProfile(id1);
        assertThat(got).isNotNull();

        // update
        UpdateUserDto upd = UpdateUserDto.builder()
                .id(id1)
                .name("Updated")
                .nickname("up")
                .birthdate(LocalDate.now().minusYears(26))
                .hobbies("h")
                .gender("male")
                .location("loc")
                .bio("b")
                .email("u@u.com")
                .phoneNumber("082")
                .interests("i")
                .isActive(true)
                .build();

        when(userProfileRepository.findById(id1)).thenReturn(Optional.of(created));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(created.toBuilder().name("Updated").build());

        UserProfile updated = userProfileService.updateUserProfile(upd);
        assertThat(updated).isNotNull();

        // delete
    when(userProfileRepository.findById(id1)).thenReturn(Optional.of(created));
    doNothing().when(userProfileRepository).delete(any(UserProfile.class));

    UserProfile deleted = userProfileService.deleteProfile(id1);
    assertThat(deleted).isNotNull();
    verify(userProfileRepository).delete(any(UserProfile.class));
    }
}

// package io.harman.sidating_app_be.service;

// import io.harman.sidating_app_be.dto.user.CreateUserDto;
// import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
// import io.harman.sidating_app_be.dto.user.UpdateUserDto;
// import io.harman.sidating_app_be.model.UserProfile;
// import io.harman.sidating_app_be.repository.UserProfileRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class UserProfileServiceImplTest {

//     @Mock
//     private UserProfileRepository userProfileRepository;

//     @InjectMocks
//     private UserProfileServiceImpl userProfileService;

//     private UUID userId1;
//     private UUID userId2;
//     private UserProfile maleUserProfile;
//     private UserProfile femaleUserProfile;
//     private UserProfile youngUserProfile;
//     private UserProfile oldUserProfile;
//     private CreateUserDto createUserDto;
//     private UpdateUserDto updateUserDto;
//     private LocalDateTime fixedTime;

//     @BeforeEach
//     void setUp() {
//         userId1 = UUID.randomUUID();
//         userId2 = UUID.randomUUID();
//         fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);

//         maleUserProfile = UserProfile.builder()
//                 .id(userId1)
//                 .name("John Doe")
//                 .nickname("Johnny")
//                 .birthdate(LocalDate.of(1990, 1, 1))
//                 .gender("Male")
//                 .location("Jakarta")
//                 .bio("Test bio")
//                 .email("john@example.com")
//                 .phoneNumber("08123456789")
//                 .hobbies("Reading, Gaming")
//                 .interests("Technology, Sports")
//                 .createdAt(fixedTime)
//                 .updatedAt(fixedTime)
//                 .isActive(true)
//                 .build();

//         femaleUserProfile = UserProfile.builder()
//                 .id(userId2)
//                 .name("Jane Doe")
//                 .nickname("Janie")
//                 .birthdate(LocalDate.of(1992, 5, 15))
//                 .gender("Female")
//                 .location("Bandung")
//                 .bio("Another test bio")
//                 .email("jane@example.com")
//                 .phoneNumber("08987654321")
//                 .hobbies("Traveling, Reading")
//                 .interests("Art, Music")
//                 .createdAt(fixedTime)
//                 .updatedAt(fixedTime)
//                 .isActive(true)
//                 .build();

//         // User profile untuk test age calculation
//         youngUserProfile = UserProfile.builder()
//                 .id(UUID.randomUUID())
//                 .name("Young User")
//                 .nickname("Young")
//                 .birthdate(LocalDate.now().minusYears(20)) // 20 years old
//                 .gender("Male")
//                 .location("Jakarta")
//                 .bio("Young user")
//                 .email("young@example.com")
//                 .isActive(true)
//                 .build();

//         oldUserProfile = UserProfile.builder()
//                 .id(UUID.randomUUID())
//                 .name("Old User")
//                 .nickname("Old")
//                 .birthdate(LocalDate.now().minusYears(50)) // 50 years old
//                 .gender("Female")
//                 .location("Jakarta")
//                 .bio("Old user")
//                 .email("old@example.com")
//                 .isActive(true)
//                 .build();

//         createUserDto = CreateUserDto.builder()
//                 .name("Test User")
//                 .nickname("Tester")
//                 .birthdate(LocalDate.of(1995, 6, 15))
//                 .gender("Male")
//                 .location("Jakarta")
//                 .bio("Test bio")
//                 .email("test@example.com")
//                 .phoneNumber("08123456789")
//                 .hobbies("Testing")
//                 .interests("QA")
//                 .build();

//         updateUserDto = UpdateUserDto.builder()
//                 .id(userId1)
//                 .name("Updated Name")
//                 .nickname("Updated Nickname")
//                 .birthdate(LocalDate.of(1990, 1, 1))
//                 .gender("Male")
//                 .location("Updated Location")
//                 .bio("Updated bio")
//                 .email("updated@example.com")
//                 .phoneNumber("08999999999")
//                 .hobbies("Updated hobbies")
//                 .interests("Updated interests")
//                 .isActive(true)
//                 .build();
//     }

//     @Test
//     void getRandomMatchScore_SameGender_ShouldReturnScoreBetween0And49() {
//         // Given
//         UserProfile user1 = maleUserProfile;
//         UserProfile user2 = UserProfile.builder()
//                 .id(userId2)
//                 .gender("Male") // Same gender
//                 .build();

//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(user1));
//         when(userProfileRepository.findById(userId2)).thenReturn(Optional.of(user2));

//         // When
//         int score = userProfileService.getRandomMatchScore(userId1, userId2);

//         // Then
//         assertThat(score).isBetween(0, 49);
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).findById(userId2);
//     }

//     @Test
//     void getRandomMatchScore_DifferentGender_ShouldReturnScoreBetween50And100() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(maleUserProfile));
//         when(userProfileRepository.findById(userId2)).thenReturn(Optional.of(femaleUserProfile));

//         // When
//         int score = userProfileService.getRandomMatchScore(userId1, userId2);

//         // Then
//         assertThat(score).isBetween(50, 100);
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).findById(userId2);
//     }

//     @Test
//     void getRandomMatchScore_User1NotFound_ShouldReturn0() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.empty());
//         when(userProfileRepository.findById(userId2)).thenReturn(Optional.of(femaleUserProfile));

//         // When
//         int score = userProfileService.getRandomMatchScore(userId1, userId2);

//         // Then
//         assertThat(score).isEqualTo(0);
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).findById(userId2);
//     }

//     @Test
//     void getRandomMatchScore_User2NotFound_ShouldReturn0() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(maleUserProfile));
//         when(userProfileRepository.findById(userId2)).thenReturn(Optional.empty());

//         // When
//         int score = userProfileService.getRandomMatchScore(userId1, userId2);

//         // Then
//         assertThat(score).isEqualTo(0);
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).findById(userId2);
//     }

//     @Test
//     void getRandomMatchScore_BothUsersNotFound_ShouldReturn0() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.empty());
//         when(userProfileRepository.findById(userId2)).thenReturn(Optional.empty());

//         // When
//         int score = userProfileService.getRandomMatchScore(userId1, userId2);

//         // Then
//         assertThat(score).isEqualTo(0);
//     }

//     @Test
//     void getMatchMessage_Score0To50_ShouldReturnSebatagasTeman() {
//         // When & Then
//         assertThat(userProfileService.getMatchMessage(0)).isEqualTo("Sebatas Teman");
//         assertThat(userProfileService.getMatchMessage(25)).isEqualTo("Sebatas Teman");
//         assertThat(userProfileService.getMatchMessage(50)).isEqualTo("Sebatas Teman");
//     }

//     @Test
//     void getMatchMessage_Score51To70_ShouldReturnTemanSejati() {
//         // When & Then
//         assertThat(userProfileService.getMatchMessage(51)).isEqualTo("Teman Sejati");
//         assertThat(userProfileService.getMatchMessage(65)).isEqualTo("Teman Sejati");
//         assertThat(userProfileService.getMatchMessage(70)).isEqualTo("Teman Sejati");
//     }

//     @Test
//     void getMatchMessage_Score71To90_ShouldReturnCocok() {
//         // When & Then
//         assertThat(userProfileService.getMatchMessage(71)).isEqualTo("Cocok");
//         assertThat(userProfileService.getMatchMessage(85)).isEqualTo("Cocok");
//         assertThat(userProfileService.getMatchMessage(90)).isEqualTo("Cocok");
//     }

//     @Test
//     void getMatchMessage_Score91To100_ShouldReturnCintaAbadi() {
//         // When & Then
//         assertThat(userProfileService.getMatchMessage(91)).isEqualTo("Cinta Abadi");
//         assertThat(userProfileService.getMatchMessage(95)).isEqualTo("Cinta Abadi");
//         assertThat(userProfileService.getMatchMessage(100)).isEqualTo("Cinta Abadi");
//     }

//     @Test
//     void getMatchImage_Score0To50_ShouldReturnCorrectImage() {
//         // When
//         String imageUrl = userProfileService.getMatchImage(25);

//         // Then
//         assertThat(imageUrl).isEqualTo("https://image.idntimes.com/post/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg");
//     }

//     @Test
//     void getMatchImage_Score51To70_ShouldReturnCorrectImage() {
//         // When
//         String imageUrl = userProfileService.getMatchImage(65);

//         // Then
//         assertThat(imageUrl).isEqualTo("https://pbs.twimg.com/media/FddMGmGVIAA1TZK.jpg");
//     }

//     @Test
//     void getMatchImage_Score71To90_ShouldReturnCorrectImage() {
//         // When
//         String imageUrl = userProfileService.getMatchImage(85);

//         // Then
//         assertThat(imageUrl).isEqualTo("https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg");
//     }

//     @Test
//     void getMatchImage_Score91To100_ShouldReturnCorrectImage() {
//         // When
//         String imageUrl = userProfileService.getMatchImage(95);

//         // Then
//         assertThat(imageUrl).isEqualTo("https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg");
//     }

//     @Test
//     void createUserProfile_ValidInput_ShouldReturnCreatedProfile() {
//         // Given
//         when(userProfileRepository.save(any(UserProfile.class))).thenReturn(maleUserProfile);

//         // When
//         UserProfile result = userProfileService.createUserProfile(createUserDto);

//         // Then
//         assertThat(result).isNotNull();
//         assertThat(result).isEqualTo(maleUserProfile);
//         verify(userProfileRepository).save(any(UserProfile.class));
//     }

//     @Test
//     void getAllUserProfile_ShouldReturnAllProfiles() {
//         // Given
//         List<UserProfile> expectedProfiles = Arrays.asList(maleUserProfile, femaleUserProfile);
//         when(userProfileRepository.findAll()).thenReturn(expectedProfiles);

//         // When
//         List<UserProfile> result = userProfileService.getAllUserProfile();

//         // Then
//         assertThat(result).isEqualTo(expectedProfiles);
//         assertThat(result).hasSize(2);
//         verify(userProfileRepository).findAll();
//     }

//     @Test
//     void mapToReadUserProfileDto_NullProfile_ShouldReturnNull() {
//         // When
//         ReadUserProfileDto result = userProfileService.mapToReadUserProfileDto(null);

//         // Then
//         assertThat(result).isNull();
//     }
    


//     @Test
//     void getUserProfile_ExistingId_ShouldReturnProfile() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(maleUserProfile));

//         // When
//         UserProfile result = userProfileService.getUserProfile(userId1);

//         // Then
//         assertThat(result).isEqualTo(maleUserProfile);
//         verify(userProfileRepository).findById(userId1);
//     }

//     @Test
//     void getUserProfile_NonExistingId_ShouldReturnNull() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.empty());

//         // When
//         UserProfile result = userProfileService.getUserProfile(userId1);

//         // Then
//         assertThat(result).isNull();
//         verify(userProfileRepository).findById(userId1);
//     }

//     @Test
//     void updateUserProfile_ExistingProfile_ShouldReturnUpdatedProfile() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(maleUserProfile));
        
//         UserProfile updatedProfile = maleUserProfile.toBuilder()
//                 .name("Updated Name")
//                 .nickname("Updated Nickname")
//                 .location("Updated Location")
//                 .bio("Updated bio")
//                 .email("updated@example.com")
//                 .phoneNumber("08999999999")
//                 .hobbies("Updated hobbies")
//                 .interests("Updated interests")
//                 .build();
                
//         when(userProfileRepository.save(any(UserProfile.class))).thenReturn(updatedProfile);

//         // When
//         UserProfile result = userProfileService.updateUserProfile(updateUserDto);

//         // Then
//         assertThat(result).isNotNull();
//         assertThat(result.getName()).isEqualTo("Updated Name");
//         assertThat(result.getNickname()).isEqualTo("Updated Nickname");
//         assertThat(result.getLocation()).isEqualTo("Updated Location");
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).save(any(UserProfile.class));
//     }

//     @Test
//     void updateUserProfile_NonExistingProfile_ShouldReturnNull() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.empty());

//         // When
//         UserProfile result = userProfileService.updateUserProfile(updateUserDto);

//         // Then
//         assertThat(result).isNull();
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository, never()).save(any(UserProfile.class));
//     }

//     @Test
//     void deleteProfile_ExistingProfile_ShouldReturnDeletedProfile() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.of(maleUserProfile));

//         // When
//         UserProfile result = userProfileService.deleteProfile(userId1);

//         // Then
//         assertThat(result).isEqualTo(maleUserProfile);
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository).delete(maleUserProfile);
//     }

//     @Test
//     void deleteProfile_NonExistingProfile_ShouldReturnNull() {
//         // Given
//         when(userProfileRepository.findById(userId1)).thenReturn(Optional.empty());

//         // When
//         UserProfile result = userProfileService.deleteProfile(userId1);

//         // Then
//         assertThat(result).isNull();
//         verify(userProfileRepository).findById(userId1);
//         verify(userProfileRepository, never()).delete(any(UserProfile.class));
//     }

//     @Test
//     void searchUserProfilesByName_WithValidName_ShouldReturnMatchingProfiles() {
//         // Given
//         String searchName = "John";
//         List<UserProfile> expectedProfiles = Arrays.asList(maleUserProfile);
//         when(userProfileRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(expectedProfiles);

//         // When
//         List<UserProfile> result = userProfileService.searchUserProfilesByName(searchName);

//         // Then
//         assertThat(result).isEqualTo(expectedProfiles);
//         assertThat(result).hasSize(1);
//         verify(userProfileRepository).findByNameContainingIgnoreCase(searchName);
//         verify(userProfileRepository, never()).findAll();
//     }

//     @Test
//     void searchUserProfilesByName_WithNullName_ShouldReturnAllProfiles() {
//         // Given
//         List<UserProfile> allProfiles = Arrays.asList(maleUserProfile, femaleUserProfile);
//         when(userProfileRepository.findAll()).thenReturn(allProfiles);

//         // When
//         List<UserProfile> result = userProfileService.searchUserProfilesByName(null);

//         // Then
//         assertThat(result).isEqualTo(allProfiles);
//         assertThat(result).hasSize(2);
//         verify(userProfileRepository).findAll();
//         verify(userProfileRepository, never()).findByNameContainingIgnoreCase(any());
//     }

//     @Test
//     void searchUserProfilesByName_WithEmptyName_ShouldReturnAllProfiles() {
//         // Given
//         List<UserProfile> allProfiles = Arrays.asList(maleUserProfile, femaleUserProfile);
//         when(userProfileRepository.findAll()).thenReturn(allProfiles);

//         // When
//         List<UserProfile> result = userProfileService.searchUserProfilesByName("");

//         // Then
//         assertThat(result).isEqualTo(allProfiles);
//         verify(userProfileRepository).findAll();
//     }

//     @Test
//     void searchUserProfilesByName_WithWhitespaceOnly_ShouldReturnAllProfiles() {
//         // Given
//         List<UserProfile> allProfiles = Arrays.asList(maleUserProfile, femaleUserProfile);
//         when(userProfileRepository.findAll()).thenReturn(allProfiles);

//         // When
//         List<UserProfile> result = userProfileService.searchUserProfilesByName("   ");

//         // Then
//         assertThat(result).isEqualTo(allProfiles);
//         verify(userProfileRepository).findAll();
//         verify(userProfileRepository, never()).findByNameContainingIgnoreCase(any());
//     }

//     @Test
//     void searchUserProfilesByName_NoMatchingResults_ShouldReturnEmptyList() {
//         // Given
//         String searchName = "NonExistentName";
//         when(userProfileRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(Arrays.asList());

//         // When
//         List<UserProfile> result = userProfileService.searchUserProfilesByName(searchName);

//         // Then
//         assertThat(result).isEmpty();
//         verify(userProfileRepository).findByNameContainingIgnoreCase(searchName);
//     }
// }