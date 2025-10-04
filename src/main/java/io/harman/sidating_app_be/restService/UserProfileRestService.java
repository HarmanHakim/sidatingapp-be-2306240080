package io.harman.sidating_app_be.restService;

import java.util.List;
import java.util.UUID;

import io.harman.sidating_app_be.restdto.request.userProfile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userProfile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userProfile.UserProfileResponseDTO;

public interface UserProfileRestService {
    
    UserProfileResponseDTO createUserProfile(AddUserProfileRequestDTO addUserProfileRequestDTO);
    
    List<UserProfileResponseDTO> getAllUserProfile();
    
    List<UserProfileResponseDTO> searchUserProfilesByName(String name);
    
    UserProfileResponseDTO getUserProfile(UUID id);
    
    UserProfileResponseDTO updateUserProfile(UpdateUserProfileRequestDTO updateUserDto);
    
    UserProfileResponseDTO deleteUserProfile(UUID id);
}
