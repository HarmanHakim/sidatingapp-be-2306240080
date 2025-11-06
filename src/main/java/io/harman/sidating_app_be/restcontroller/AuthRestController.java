package io.harman.sidating_app_be.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.request.security.LoginJwtRequestDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.security.LoginJwtResponseDTO;
import io.harman.sidating_app_be.restdto.response.userprofile.UserProfileResponseDTO;
import io.harman.sidating_app_be.restservice.UserProfileRestService;
import io.harman.sidating_app_be.security.jwt.JwtUtils;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthRestController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserProfileRepository userRepository;

    @Autowired
    private UserProfileRestService userProfileRestService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDTO<LoginJwtResponseDTO>> login(@RequestBody LoginJwtRequestDTO loginRequest) {
        BaseResponseDTO<LoginJwtResponseDTO> response = new BaseResponseDTO<>();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserProfile userDetails = userRepository.findByUsername(loginRequest.getUsername());

            String token = jwtUtils.generateJwtToken(userDetails.getId().toString(), 
                                                    userDetails.getUsername(), 
                                                    userDetails.getEmail(), 
                                                    userDetails.getName(), 
                                                    userDetails.getRole().getRoleName());   

            LoginJwtResponseDTO loginResponse = new LoginJwtResponseDTO();
            loginResponse.setToken(token);
            loginResponse.setId(userDetails.getId());
            loginResponse.setUsername(userDetails.getUsername());
            loginResponse.setEmail(userDetails.getEmail());
            loginResponse.setName(userDetails.getName());
            loginResponse.setRoleName(userDetails.getRole().getRoleName());
            loginResponse.setNickname(userDetails.getNickname());

            response.setData(loginResponse);
            response.setStatus(200);
            response.setMessage("Login Berhasil");
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            response.setStatus(401);
            response.setMessage("Username atau Password salah!");
            response.setTimestamp(new Date());
            response.setData(null);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> register(
            @Valid @RequestBody AddUserProfileRequestDTO registerRequest,
            BindingResult bindingResult) {
        
        BaseResponseDTO<UserProfileResponseDTO> response = new BaseResponseDTO<>();

        // Check for validation errors
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(errorMessages.toString());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Set default role to "User" if not provided
        if (registerRequest.getRoleName() == null || registerRequest.getRoleName().trim().isEmpty()) {
            registerRequest.setRoleName("User");
        }

        // Create user profile
        UserProfileResponseDTO createdProfile = userProfileRestService.createUserProfile(registerRequest);

        if (createdProfile == null) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setMessage("Username sudah digunakan. Silakan pilih username lain.");
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        response.setStatus(HttpStatus.CREATED.value());
        response.setData(createdProfile);
        response.setMessage("Registrasi berhasil! Silakan login dengan username dan password Anda.");
        response.setTimestamp(new Date());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}