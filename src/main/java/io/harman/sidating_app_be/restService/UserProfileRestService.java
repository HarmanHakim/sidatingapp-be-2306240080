package io.harman.sidating_app_be.restservice;

import io.harman.sidating_app_be.restdto.request.userprofile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userprofile.UserProfileResponseDTO;
import java.util.List;
import java.util.UUID;

public interface UserProfileRestService {
	UserProfileResponseDTO createUserProfile(AddUserProfileRequestDTO addUserProfileRequestDTO);
	List<UserProfileResponseDTO> getAllUserProfile();
	List<UserProfileResponseDTO> searchUserProfilesByName(String name);
	UserProfileResponseDTO getUserProfile(UUID id);
	UserProfileResponseDTO updateUserProfile(UpdateUserProfileRequestDTO updateUserDto);
    UserProfileResponseDTO deleteUserProfile(UUID id);

	
}