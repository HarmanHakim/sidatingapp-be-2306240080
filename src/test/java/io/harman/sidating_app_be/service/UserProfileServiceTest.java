// package io.harman.sidating_app_be.service;

// import io.harman.sidating_app_be.dto.user.CreateUserDto;
// import io.harman.sidating_app_be.dto.user.UpdateUserDto;
// import io.harman.sidating_app_be.model.UserProfile;
// import io.harman.sidating_app_be.repository.UserProfileRepository;

// // import org.h2.engine.User;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.util.Optional;
// import java.util.UUID;

// import static org.hamcrest.Matchers.anEmptyMap;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class UserProfileServiceTest {

//     @Mock
//     private UserProfileRepository userProfileRepository;

//     @InjectMocks
//     private UserProfileServiceImpl service;

//     private UserProfile user1, user2;
//     private UUID user1Id, user2Id;

//     @BeforeEach
//     void setup() {
//         user1Id = UUID.randomUUID();
//         user2Id = UUID.randomUUID();
        
//         user1 = UserProfile.builder()
//                 .id(user1Id)
//                 .name("A")
//                 .gender("MALE")
//                 .birthdate(LocalDate.of(2000, 1, 1))
//                 .build();

//         user2 = UserProfile.builder()
//                 .id(user2Id)
//                 .name("B")
//                 .gender("FEMALE")
//                 .birthdate(LocalDate.of(2001, 2, 2))
//                 .build();
//     }

//     @Test
//     void testCreateAndGetUserProfile() {
//         when(userProfileRepository.save(any(UserProfile.class))).thenReturn(user1);
//         CreateUserDto dto = CreateUserDto.builder()
//                 .name("A")
//                 .gender("MALE")
//                 .birthdate(LocalDate.of(2000,1,1))
//                 .build();
        
//         UserProfile createdUser = service.createUserProfile(dto);

//         assertNotNull(createdUser);
//         assertEquals(user1.getName(), createdUser.getName());
//         assertEquals(user1.getGender(), createdUser.getGender());

//         verify(userProfileRepository, times(1)).save(any(UserProfile.class));
//     }

//     @Test
//     void testUpdateUserProfile() {
//         UUID userId = UUID.randomUUID();
//         UserProfile userProfile = UserProfile.builder()
//                 .id(userId)
//                 .name("A")
//                 .gender("MALE")
//                 .birthdate(LocalDate.of(2000,1,1))
//                 .build();
        
//         UpdateUserDto updateDto = UpdateUserDto.builder()
//                 .id(userId)
//                 .name("Updated")
//                 .gender("MALE")
//                 .birthdate(LocalDate.of(2000,1,1))
//                 .build();
        
//         when(userProfileRepository.findById(userId)).thenReturn(Optional.of(userProfile));
//         when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         UserProfile updatedUser = service.updateUserProfile(updateDto);

//         assertNotNull(updatedUser);
//         assertEquals("Updated", updatedUser.getName());

//         verify(userProfileRepository,times(1)).findById(userId);
//         verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        
//     }

//     @Test
//     void testDeleteUserProfile() {
//         when (userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));

//         UserProfile deletedProfile = service.deleteProfile(user1Id);
        
//         verify(userProfileRepository, times (1)).delete(user1);
//         assertNotNull(deletedProfile);

//         when (userProfileRepository.findById(user1Id)).thenReturn(Optional.empty());
//         assertNull(service.getUserProfile(user1Id));
//         when (userProfileRepository.findById(any())).thenReturn( Optional.empty());
//         assertNull(service.deleteProfile(UUID.randomUUID()));
//         assertNull(service.deleteProfile(null));
        
//     }

//     @Test
//     void testGetRandomMatchScoreAndMessage() {
//         when (userProfileRepository.findById(user1Id)).thenReturn(Optional.of(user1));
//         when (userProfileRepository.findById(user2Id)).thenReturn(Optional.of(user2));

//         int score = service.getRandomMatchScore(user1Id, user2Id) ;
//         assertTrue(score >= 50 && score <= 100);
//         String msg = service.getMatchMessage(score);
//         assertNotNull(msg) ;
//         assertEquals(0, service.getRandomMatchScore(null, user2Id));
//         assertEquals(0, service.getRandomMatchScore(user1Id, null));
//         assertEquals(0, service.getRandomMatchScore(user1Id, user1Id));
//         }

// }
