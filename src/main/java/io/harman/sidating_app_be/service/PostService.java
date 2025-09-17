package io.harman.sidating_app_be.service;

import java.util.List;
import java.util.UUID;

import io.harman.sidating_app_be.model.Post;

public interface PostService {

    Post createPost(Post post);

    List<Post> getAllPost(UUID userId, String sort);

    Post getPost(UUID id);

    Post updatePost(Post post);

    Post deletePost(UUID id);

    Post likePost(UUID postId, UUID userId);
}