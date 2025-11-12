package io.harman.sidating_app_be.restservice;

import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.external.PostDTO;
import io.harman.sidating_app_be.restdto.external.UserProfileDTO;
import io.harman.sidating_app_be.restservice.ExternalApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private ExternalApiService externalApiService;

    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        // Set URL config (karena @Value tidak dijalankan di unit test)
        ReflectionTestUtils.setField(externalApiService, "be1Url", "http://mock-be");

        // Mock RequestContext agar Authorization header bisa diambil
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer testtoken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @Test
    void testGetUserProfile_Success() {
        // Arrange
        UserProfileDTO mockProfile = new UserProfileDTO();
        BaseResponseDTO<UserProfileDTO> responseBody = new BaseResponseDTO<>();
        responseBody.setData(mockProfile);
        ResponseEntity<BaseResponseDTO<UserProfileDTO>> responseEntity =
                new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://mock-be/api/profile/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<BaseResponseDTO<UserProfileDTO>>>any()
        )).thenReturn(responseEntity);

        // Act
        UserProfileDTO result = externalApiService.getUserProfile(userId);

        // Assert
        assertNotNull(result);
        assertEquals(mockProfile, result);
    }

    @Test
    void testGetUserProfile_NullBody() {
        ResponseEntity<BaseResponseDTO<UserProfileDTO>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<BaseResponseDTO<UserProfileDTO>>>any()
        )).thenReturn(responseEntity);

        UserProfileDTO result = externalApiService.getUserProfile(userId);
        assertNull(result);
    }

    @Test
    void testGetUserProfile_HttpClientErrorException() {
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<BaseResponseDTO<UserProfileDTO>>>any()
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        UserProfileDTO result = externalApiService.getUserProfile(userId);
        assertNull(result);
    }

    @Test
    void testGetPost_Success() {
        PostDTO mockPost = new PostDTO();
        BaseResponseDTO<PostDTO> responseBody = new BaseResponseDTO<>();
        responseBody.setData(mockPost);
        ResponseEntity<BaseResponseDTO<PostDTO>> responseEntity =
                new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://mock-be/api/posts/" + postId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<BaseResponseDTO<PostDTO>>>any()
        )).thenReturn(responseEntity);

        PostDTO result = externalApiService.getPost(postId);
        assertNotNull(result);
        assertEquals(mockPost, result);
    }

    @Test
    void testGetPost_Exception() {
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<BaseResponseDTO<PostDTO>>>any()
        )).thenThrow(new RuntimeException("Connection timeout"));

        PostDTO result = externalApiService.getPost(postId);
        assertNull(result);
    }
}
