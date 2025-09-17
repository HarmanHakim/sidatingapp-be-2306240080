package io.harman.sidating_app_be.service;

import java.util.List;
import java.util.Map; 
import java.util.UUID;

import io.harman.sidating_app_be.model.UserProfile;

public interface UserProfileService {

    UserProfile createUserProfile(UserProfile userProfile);

    List<UserProfile> getAllUserProfile();

    UserProfile getUserProfile(UUID id);

    UserProfile updateUserProfile(UserProfile userProfile);

    UserProfile deleteProfile(UUID id);
    
    Map<String, Object> calculateMatchScore(UUID userId1, UUID userId2);
}