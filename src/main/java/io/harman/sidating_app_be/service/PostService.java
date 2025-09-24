package io.harman.sidating_app_be.service;

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.post.UpdatePostDto;
import io.harman.sidating_app_be.dto.post.ReadPostDto;
import io.harman.sidating_app_be.model.Post;

import java.util.List;
import java.util.UUID;

public interface PostService {

    Post createPost(CreatePostDto createPostDto);
    List<Post> getAllPost(UUID userId, String sort);
    Post getPost(UUID id);
    Post updatePost(UpdatePostDto updatePostDto);
    Post deletePost(UUID id);
    Post likePost(UUID postId, UUID userId);

    List<ReadPostDto> getAllPostsDto(UUID userId, String sort);
    ReadPostDto toReadPostDto(Post post);
}
