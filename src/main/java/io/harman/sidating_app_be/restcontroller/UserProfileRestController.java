package io.harman.sidating_app_be.restcontroller;

import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.UpdateUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userprofile.UserProfileResponseDTO;

import io.harman.sidating_app_be.restservice.UserProfileRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserProfileRestController {
    @Autowired
    private UserProfileRestService userProfileRestService;

    public static final String BASE_URL = "/profile";
    public static final String VIEW_USER_PROFILE = BASE_URL + "/{id}";
    public static final String CREATE_USER_PROFILE = BASE_URL + "/create";
    public static final String UPDATE_USER_PROFILE = BASE_URL + "/update";
    public static final String DELETE_USER_PROFILE = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<UserProfileResponseDTO>>> getAllProfile(
            @RequestParam(required = false) String search) {
        var baseResponseDTO = new BaseResponseDTO<List<UserProfileResponseDTO>>();

        List<UserProfileResponseDTO> listUserProfile;

        if(search != null) {
            listUserProfile = userProfileRestService.searchUserProfilesByName(search);
        }else{
            listUserProfile = userProfileRestService.getAllUserProfile();
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listUserProfile);
        baseResponseDTO.setMessage("Data User Profile Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_USER_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> getUserProfile(@PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        UserProfileResponseDTO userProfile = userProfileRestService.getUserProfile(id);

        if (userProfile == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("User Profile Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(userProfile);
        baseResponseDTO.setMessage("Data User Profile Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_USER_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> createUserProfile(
            @Valid @RequestBody AddUserProfileRequestDTO addUserProfileRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }


        UserProfileResponseDTO userProfile = userProfileRestService.createUserProfile(addUserProfileRequestDTO);

        if (userProfile == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("username sudah digunakan. Silakan pilih username lain.");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData(userProfile);
        baseResponseDTO.setMessage("Data User Profile Berhasil Dibuat");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping(UPDATE_USER_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequestDTO updateUserProfileRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }


        try {
            UserProfileResponseDTO userProfile = userProfileRestService.updateUserProfile(updateUserProfileRequestDTO);

            if (userProfile == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User Profile Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(userProfile);
            baseResponseDTO.setMessage("Data User Profile Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_USER_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> deleteUserProfile(
            @PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        try {
            UserProfileResponseDTO userProfile = userProfileRestService.deleteUserProfile(id);

            if (userProfile == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User Profile Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(userProfile);
            baseResponseDTO.setMessage("Data User Profile Berhasil Dihapus");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}