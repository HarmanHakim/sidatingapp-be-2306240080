package io.harman.sidating_app_be.restservice;
 
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.Reply;
import io.harman.sidating_app_be.repository.ReplyRepository;
import io.harman.sidating_app_be.restdto.external.PostDTO;
import io.harman.sidating_app_be.restdto.external.UserProfileDTO;
import io.harman.sidating_app_be.restdto.request.reply.CreateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.request.reply.UpdateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.response.reply.ReplyResponseDTO;
 
@Service
public class ReplyRestService {
 
    @Autowired
    private ReplyRepository replyRepository;
 
    @Autowired
    private ExternalApiService externalApiService;
 
    public List<ReplyResponseDTO> getAllReplies() {
        List<Reply> replies = replyRepository.findAll();
        return replies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
 
    public List<ReplyResponseDTO> getRepliesByPostId(UUID postId) {
        List<Reply> replies = replyRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return replies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
 
    public ReplyResponseDTO getReplyById(UUID id) {
        Reply reply = replyRepository.findById(id).orElse(null);
        if (reply == null) {
            return null;
        }
        return mapToResponseDTO(reply);
    }
 
    public ReplyResponseDTO createReply(CreateReplyRequestDTO requestDTO) {
        PostDTO post = externalApiService.getPost(requestDTO.getPostId());
        if (post == null) {
            throw new RuntimeException("Post with id " + requestDTO.getPostId() + " not found");
        }
 
        UserProfileDTO user = externalApiService.getUserProfile(requestDTO.getUserProfileId());
        if (user == null) {
            System.err.println("Warning: User profile not found for userId: " + requestDTO.getUserProfileId() +
                             ". Creating reply anyway.");
        }
 
        Reply reply = Reply.builder()
                .postId(requestDTO.getPostId())
                .userProfileId(requestDTO.getUserProfileId())
                .content(requestDTO.getContent())
                .build();
 
        Reply savedReply = replyRepository.save(reply);
        return mapToResponseDTO(savedReply);
    }
 
    public ReplyResponseDTO updateReply(UUID id, UpdateReplyRequestDTO requestDTO) {
        Reply reply = replyRepository.findById(id).orElse(null);
        if (reply == null) {
            throw new RuntimeException("Reply with id " + id + " not found");
        }
 
        // Check authorization
        UserProfileDTO user = externalApiService.getUserProfile(requestDTO.getUserProfileId());
        if (user == null) {
            throw new RuntimeException("User profile not found");
        }
 
        boolean isAdmin = "Admin".equals(user.getRole());
        boolean isOwner = reply.getUserProfileId().equals(requestDTO.getUserProfileId());
 
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: Only admins or the reply owner can update this reply");
        }
 
        // Update reply
        reply.setContent(requestDTO.getContent());
        Reply updatedReply = replyRepository.save(reply);
        return mapToResponseDTO(updatedReply);
    }
 
    public void deleteReply(UUID id, UUID userProfileId) {
        Reply reply = replyRepository.findById(id).orElse(null);
        if (reply == null) {
            throw new RuntimeException("Reply with id " + id + " not found");
        }
 
        // Check authorization
        UserProfileDTO user = externalApiService.getUserProfile(userProfileId);
        if (user == null) {
            throw new RuntimeException("User profile not found");
        }
 
        boolean isAdmin = "Admin".equals(user.getRole());
        boolean isOwner = reply.getUserProfileId().equals(userProfileId);
 
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: Only admins or the reply owner can delete this reply");
        }
 
        replyRepository.delete(reply);
    }
 
    private ReplyResponseDTO mapToResponseDTO(Reply reply) {
        // Fetch user and post data from BE1
        UserProfileDTO user = externalApiService.getUserProfile(reply.getUserProfileId());
        PostDTO post = externalApiService.getPost(reply.getPostId());
 
        return ReplyResponseDTO.builder()
                .id(reply.getId())
                .postId(reply.getPostId())
                .userProfileId(reply.getUserProfileId())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .userProfile(user)
                .post(post)
                .build();
    }
}