package io.harman.sidating_app_be.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.restdto.request.security.LoginJwtRequestDTO;
import io.harman.sidating_app_be.restdto.request.userprofile.AddUserProfileRequestDTO;
import io.harman.sidating_app_be.restdto.response.userprofile.UserProfileResponseDTO;
import io.harman.sidating_app_be.restservice.UserProfileRestService;
import io.harman.sidating_app_be.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileRestService userProfileRestService;

    @InjectMocks
    private AuthRestController authRestController;

    private ObjectMapper objectMapper;
    private UserProfile testUser;
    private Role testRole;
    private UUID userId;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController)
                .setValidator(validator)
                .setMessageConverters(converter)
                .build();

        userId = UUID.randomUUID();
        
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("ROLE_USER");

        testUser = new UserProfile();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setNickname("Tester");
        testUser.setRole(testRole);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Given
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        String mockToken = "mock.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userProfileRepository.findByUsername("testuser")).thenReturn(testUser);
        when(jwtUtils.generateJwtToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockToken);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login Berhasil"))
                .andExpect(jsonPath("$.data.token").value(mockToken))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userProfileRepository).findByUsername("testuser");
        verify(jwtUtils).generateJwtToken(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Given
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Username atau Password salah!"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userProfileRepository, never()).findByUsername(anyString());
    }

    @Test
    void register_ShouldCreateUser_WhenRequestIsValid() throws Exception {
        // Given
        AddUserProfileRequestDTO registerRequest = new AddUserProfileRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setName("New User");
        registerRequest.setNickname("NewNick");
        registerRequest.setBirthdate(LocalDate.of(1995, 1, 1));
        registerRequest.setGender("MALE");
        registerRequest.setPhoneNumber("08123456789");

        UserProfileResponseDTO responseDTO = new UserProfileResponseDTO();
        responseDTO.setId(userId);
        responseDTO.setName("New User");
        responseDTO.setEmail("newuser@example.com");

        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(responseDTO);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Registrasi berhasil! Silakan login dengan username dan password Anda."))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.name").value("New User"));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }

    @Test
    void register_ShouldReturnConflict_WhenUsernameExists() throws Exception {
        // Given
        AddUserProfileRequestDTO registerRequest = new AddUserProfileRequestDTO();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setName("Existing User");
        registerRequest.setNickname("ExistingNick");
        registerRequest.setBirthdate(LocalDate.of(1995, 1, 1));
        registerRequest.setGender("MALE");
        registerRequest.setPhoneNumber("08123456789");

        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(null);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username sudah digunakan. Silakan pilih username lain."));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given - Invalid request with missing required fields
        AddUserProfileRequestDTO invalidRequest = new AddUserProfileRequestDTO();
        invalidRequest.setUsername(""); // Empty username

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userProfileRestService, never()).createUserProfile(any());
    }

    @Test
    void register_ShouldSetDefaultRole_WhenRoleNotProvided() throws Exception {
        // Given
        AddUserProfileRequestDTO registerRequest = new AddUserProfileRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setName("New User");
        registerRequest.setNickname("NewNick");
        registerRequest.setBirthdate(LocalDate.of(1995, 1, 1));
        registerRequest.setGender("MALE");
        registerRequest.setPhoneNumber("08123456789");
        // No role set

        UserProfileResponseDTO responseDTO = new UserProfileResponseDTO();
        responseDTO.setId(userId);

        when(userProfileRestService.createUserProfile(any(AddUserProfileRequestDTO.class)))
                .thenReturn(responseDTO);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));

        verify(userProfileRestService).createUserProfile(any(AddUserProfileRequestDTO.class));
    }
}
