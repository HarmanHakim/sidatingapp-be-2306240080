package io.harman.sidating_app_be.security.service;

import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserProfile testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("ROLE_USER");

        testUser = new UserProfile();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(testRole);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(userProfileRepository.findByUsername("testuser")).thenReturn(testUser);

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword123", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        verify(userProfileRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userProfileRepository.findByUsername("nonexistent")).thenReturn(null);

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        verify(userProfileRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_WithAdminRole() {
        // Given
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName("ROLE_ADMIN");
        
        UserProfile adminUser = new UserProfile();
        adminUser.setUsername("admin");
        adminUser.setPassword("adminPassword");
        adminUser.setRole(adminRole);

        when(userProfileRepository.findByUsername("admin")).thenReturn(adminUser);

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        verify(userProfileRepository).findByUsername("admin");
    }
}
