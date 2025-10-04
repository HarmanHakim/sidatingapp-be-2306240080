package io.harman.sidating_app_be.restService;

import java.util.List;
import java.util.UUID;

import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;

public interface PostRestService {
    
    List<PostResponseDTO> getAllPosts();
    
    List<PostResponseDTO> getPostsByUserId(UUID userId);
    
    List<PostResponseDTO> getPostsByDate(String dateString);
    
    List<PostResponseDTO> getPostsByUserIdAndDate(UUID userId, String dateString);
    
    // PostResponseDTO getPostById(UUID id);
    
    // PostResponseDTO createPost(CreatePostRequestDTO createPostRequestDTO);
    
    // PostResponseDTO updatePost(UpdatePostRequestDTO updatePostRequestDTO);
    
    // void deletePost(DeletePostRequestDTO deletePostRequestDTO);
    
    // void likePost(LikePostRequestDTO likePostRequestDTO);
}