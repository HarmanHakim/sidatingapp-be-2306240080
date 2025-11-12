package io.harman.sidating_app_be.restservice;

import io.harman.sidating_app_be.model.Reply;
import io.harman.sidating_app_be.repository.ReplyRepository;
import io.harman.sidating_app_be.restdto.external.PostDTO;
import io.harman.sidating_app_be.restdto.external.UserProfileDTO;
import io.harman.sidating_app_be.restdto.request.reply.CreateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.request.reply.UpdateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.response.reply.ReplyResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyRestServiceTest {

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private ExternalApiService externalApiService;

    @InjectMocks
    private ReplyRestService replyRestService;

    private UUID replyId;
    private UUID postId;
    private UUID userProfileId;
    private Reply mockReply;
    private UserProfileDTO mockUser;
    private PostDTO mockPost;

    @BeforeEach
    void setUp() {
        replyId = UUID.randomUUID();
        postId = UUID.randomUUID();
        userProfileId = UUID.randomUUID();

        mockUser = UserProfileDTO.builder()
                .id(userProfileId)
                .username("testuser")
                .role("User")
                .build();
        
        mockPost = PostDTO.builder()
                .id(postId)
                .build();

        mockReply = Reply.builder()
                .id(replyId)
                .postId(postId)
                .userProfileId(userProfileId)
                .content("Test content")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testGetAllReplies_Success() {
        when(replyRepository.findAll()).thenReturn(List.of(mockReply));
        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        when(externalApiService.getPost(postId)).thenReturn(mockPost);

        List<ReplyResponseDTO> result = replyRestService.getAllReplies();

        assertEquals(1, result.size());
        assertEquals(mockReply.getId(), result.get(0).getId());
        assertEquals(mockUser, result.get(0).getUserProfile());
        assertEquals(mockPost, result.get(0).getPost());
    }

    @Test
    void testGetRepliesByPostId_Success() {
        when(replyRepository.findByPostIdOrderByCreatedAtDesc(postId))
                .thenReturn(List.of(mockReply));
        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        when(externalApiService.getPost(postId)).thenReturn(mockPost);

        List<ReplyResponseDTO> result = replyRestService.getRepliesByPostId(postId);

        assertEquals(1, result.size());
        assertEquals(mockReply.getContent(), result.get(0).getContent());
    }

    @Test
    void testGetReplyById_Success() {
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        when(externalApiService.getPost(postId)).thenReturn(mockPost);

        ReplyResponseDTO result = replyRestService.getReplyById(replyId);

        assertNotNull(result);
        assertEquals(mockReply.getId(), result.getId());
    }

    @Test
    void testCreateReply_Success() {
        CreateReplyRequestDTO request = new CreateReplyRequestDTO(postId, userProfileId, "New reply content");

        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        when(externalApiService.getPost(postId)).thenReturn(mockPost);
        when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> {
            Reply r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReplyResponseDTO result = replyRestService.createReply(request);

        assertNotNull(result);
        assertEquals(request.getContent(), result.getContent());
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    void testUpdateReply_Success() {
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(userProfileId)
                .content("Updated content")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        when(replyRepository.save(any(Reply.class))).thenAnswer(i -> i.getArgument(0));
        when(externalApiService.getPost(postId)).thenReturn(mockPost);

        ReplyResponseDTO result = replyRestService.updateReply(replyId, request);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    void testUpdateReply_AsAdmin() {
        UUID differentUserId = UUID.randomUUID();
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(differentUserId)
                .content("Updated by admin")
                .build();

        UserProfileDTO adminUser = UserProfileDTO.builder()
                .id(differentUserId)
                .username("admin")
                .role("Admin")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(differentUserId)).thenReturn(adminUser);
        when(replyRepository.save(any(Reply.class))).thenAnswer(i -> i.getArgument(0));
        when(externalApiService.getPost(postId)).thenReturn(mockPost);

        ReplyResponseDTO result = replyRestService.updateReply(replyId, request);

        assertNotNull(result);
        assertEquals("Updated by admin", result.getContent());
    }

    @Test
    void testUpdateReply_Unauthorized() {
        UUID differentUserId = UUID.randomUUID();
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(differentUserId)
                .content("Unauthorized update")
                .build();

        UserProfileDTO nonOwnerUser = UserProfileDTO.builder()
                .id(differentUserId)
                .username("other")
                .role("User")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(differentUserId)).thenReturn(nonOwnerUser);

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> replyRestService.updateReply(replyId, request));
        
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testUpdateReply_NotFound() {
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(userProfileId)
                .content("Update non-existent")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> replyRestService.updateReply(replyId, request));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testDeleteReply_Success() {
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(userProfileId)).thenReturn(mockUser);
        doNothing().when(replyRepository).delete(any(Reply.class));

        replyRestService.deleteReply(replyId, userProfileId);

        verify(replyRepository, times(1)).delete(any(Reply.class));
    }

    @Test
    void testDeleteReply_AsAdmin() {
        UUID adminUserId = UUID.randomUUID();
        UserProfileDTO adminUser = UserProfileDTO.builder()
                .id(adminUserId)
                .username("admin")
                .role("Admin")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(adminUserId)).thenReturn(adminUser);
        doNothing().when(replyRepository).delete(any(Reply.class));

        replyRestService.deleteReply(replyId, adminUserId);

        verify(replyRepository, times(1)).delete(any(Reply.class));
    }

    @Test
    void testDeleteReply_Unauthorized() {
        UUID differentUserId = UUID.randomUUID();
        UserProfileDTO nonOwnerUser = UserProfileDTO.builder()
                .id(differentUserId)
                .username("other")
                .role("User")
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(mockReply));
        when(externalApiService.getUserProfile(differentUserId)).thenReturn(nonOwnerUser);

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> replyRestService.deleteReply(replyId, differentUserId));
        
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testDeleteReply_NotFound() {
        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> replyRestService.deleteReply(replyId, userProfileId));
        
        assertTrue(exception.getMessage().contains("not found"));
    }
}

