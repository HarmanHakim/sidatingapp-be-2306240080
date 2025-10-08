package io.harman.sidating_app_be.restService;

import java.util.List;
import java.util.UUID;

import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;

public interface PostRestService {

    List<PostResponseDTO> getAllPosts();

    List<PostResponseDTO> getPostByUserId(UUID userId);

    List<PostResponseDTO> getPostByDate(String dateString);

    List<PostResponseDTO> getPostByUserIdAndDate(UUID userId, String dateString);

    PostResponseDTO getPostById(UUID id);

    PostResponseDTO createPost(CreatePostRequestDTO createPostRequestDTO);

    PostResponseDTO updatePost(UpdatePostRequestDTO updatePostRequestDTO);

    PostResponseDTO deletePost(DeletePostRequestDTO deletePostRequestDTO);

    PostResponseDTO likePost(LikePostRequestDTO likePostRequestDTO);
}
