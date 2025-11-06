package io.harman.sidating_app_be.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String testSecret = "TestSecretKeyForJWTTokenGenerationAndValidation123456789";
    private int testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void testGenerateJwtToken() {
        String id = "123";
        String username = "testuser";
        String email = "test@example.com";
        String name = "Test User";
        String role = "USER";

        String token = jwtUtils.generateJwtToken(id, username, email, name, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGetUserNameFromJwtToken() {
        String username = "testuser";
        String token = jwtUtils.generateJwtToken("123", username, "test@example.com", "Test User", "USER");

        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetIdFromJwtToken() {
        String id = "123";
        String token = jwtUtils.generateJwtToken(id, "testuser", "test@example.com", "Test User", "USER");

        String extractedId = jwtUtils.getIdFromJwtToken(token);

        assertEquals(id, extractedId);
    }

    @Test
    void testGetEmailFromJwtToken() {
        String email = "test@example.com";
        String token = jwtUtils.generateJwtToken("123", "testuser", email, "Test User", "USER");

        String extractedEmail = jwtUtils.getEmailFromJwtToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void testGetNameFromJwtToken() {
        String name = "Test User";
        String token = jwtUtils.generateJwtToken("123", "testuser", "test@example.com", name, "USER");

        String extractedName = jwtUtils.getNameFromJwtToken(token);

        assertEquals(name, extractedName);
    }

    @Test
    void testGetRoleFromJwtToken() {
        String role = "ADMIN";
        String token = jwtUtils.generateJwtToken("123", "testuser", "test@example.com", "Test User", role);

        String extractedRole = jwtUtils.getRoleFromJwtToken(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void testValidateJwtToken_ValidToken() {
        String token = jwtUtils.generateJwtToken("123", "testuser", "test@example.com", "Test User", "USER");

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateJwtToken_InvalidSignature() {
        String token = Jwts.builder()
            .subject("testuser")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
            .signWith(Keys.hmacShaKeyFor("WrongSecretKeyForJWTTokenGenerationAndValidation123456789".getBytes()))
            .compact();

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_MalformedToken() {
        String malformedToken = "this.is.not.a.valid.token";

        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_ExpiredToken() {
        // Create an expired token
        JwtUtils expiredJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtExpirationMs", -1000); // Negative expiration

        String expiredToken = expiredJwtUtils.generateJwtToken("123", "testuser", "test@example.com", "Test User", "USER");

        // Wait a moment to ensure token is expired
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_EmptyToken() {
        boolean isValid = jwtUtils.validateJwtToken("");

        assertFalse(isValid);
    }

    @Test
    void testGetCurrentUsername_WithAuthentication() {
        User user = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = jwtUtils.getCurrentUsername();

        assertEquals("testuser", username);

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUsername_NoAuthentication() {
        SecurityContextHolder.clearContext();

        String username = jwtUtils.getCurrentUsername();

        assertNull(username);
    }

    @Test
    void testGetCurrentUsername_NonUserPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("stringPrincipal", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = jwtUtils.getCurrentUsername();

        assertNull(username);

        // Clean up
        SecurityContextHolder.clearContext();
    }
}
