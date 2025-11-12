package io.harman.sidating_app_be.restservice;
 
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.external.PostDTO;
import io.harman.sidating_app_be.restdto.external.UserProfileDTO;

import java.util.UUID;
 
@Service
public class ExternalApiService {
 
    @Autowired
    private RestTemplate restTemplate;
 
    @Value("${external.sidating-app-be-url}")
    private String be1Url;
 
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
 
        // Get token from current HTTP request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                headers.set("Authorization", authHeader);
            }
        }
 
        return headers;
    }
 
    public UserProfileDTO getUserProfile(UUID userId) {
        try {
            String url = be1Url + "/api/profile/" + userId;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
 
            ResponseEntity<BaseResponseDTO<UserProfileDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<BaseResponseDTO<UserProfileDTO>>() {}
            );
 
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
 
            System.err.println("User profile response body is null for userId: " + userId);
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching user profile from BE1 for userId " + userId + ": " + e.getMessage());
            return null;
        }
    }
 
    public PostDTO getPost(UUID postId) {
        try {
            String url = be1Url + "/api/posts/" + postId;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
 
            ResponseEntity<BaseResponseDTO<PostDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<BaseResponseDTO<PostDTO>>() {}
            );
 
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
 
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching post from BE1: " + e.getMessage());
            return null;
        }
    }
}
 
