package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.model.Post;
import java.util.List;
import java.util.UUID;

public interface PostService {
    Post createPost(CreatePostDto dto);
    List<Post> getAllPost(UUID userId, String sort);
    Post getPost(UUID id);
    Post updatePost(UpdatePostDto dto);
    Post deletePost(UUID id);
    Post likePost(UUID postId, UUID userId);

    ReadPostDto mapToReadPostDto(Post post);
} 