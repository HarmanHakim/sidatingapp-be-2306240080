package io.harman.sidating_app_be.service;

import java.util.List;
import java.util.UUID;

// import org.h2.engine.User;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
public interface UserProfileService {
    
    UserProfile createUserProfile(CreateUserDto createUserDto);

    List<UserProfile> getAllUserProfile();

    UserProfile getUserProfile(UUID id);

    UserProfile updateUserProfile(UpdateUserDto updateUserDto);

    UserProfile deleteProfile(UUID id);

    int getRandomMatchScore(UUID userId1, UUID userId2);

    String getMatchMessage(int score);

    String getMatchImage(int score);

    ReadUserProfileDto mapToReadUserProfileDto(UserProfile userProfile);

    List<UserProfile> searchUserProfilesByName(String name);

}


