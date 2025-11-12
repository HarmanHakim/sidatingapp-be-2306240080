package io.harman.sidating_app_be.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.harman.sidating_app_be.restdto.request.reply.CreateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.request.reply.UpdateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.response.reply.ReplyResponseDTO;
import io.harman.sidating_app_be.restservice.ReplyRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReplyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReplyRestService replyRestService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID replyId;
    private UUID postId;
    private UUID userProfileId;
    private ReplyResponseDTO mockReply;

    @BeforeEach
    void setUp() {
        replyId = UUID.randomUUID();
        postId = UUID.randomUUID();
        userProfileId = UUID.randomUUID();

        mockReply = ReplyResponseDTO.builder()
                .id(replyId)
                .postId(postId)
                .userProfileId(userProfileId)
                .content("This is a reply")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // === GET /api/replies ===
    @Test
    void testGetAllReplies_Success() throws Exception {
        when(replyRestService.getAllReplies()).thenReturn(List.of(mockReply));

        mockMvc.perform(get("/api/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content").value("This is a reply"))
                .andExpect(jsonPath("$.message").value("Replies retrieved successfully"));
    }

    // === GET /api/replies?postId=... ===
    @Test
    void testGetRepliesByPostId_Success() throws Exception {
        when(replyRestService.getRepliesByPostId(postId)).thenReturn(List.of(mockReply));

        mockMvc.perform(get("/api/replies")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].postId").value(postId.toString()));
    }

    // === GET /api/replies/{id} ===
    @Test
    void testGetReplyById_Found() throws Exception {
        when(replyRestService.getReplyById(replyId)).thenReturn(mockReply);

        mockMvc.perform(get("/api/replies/{id}", replyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(replyId.toString()))
                .andExpect(jsonPath("$.message").value("Reply retrieved successfully"));
    }

    @Test
    void testGetReplyById_NotFound() throws Exception {
        when(replyRestService.getReplyById(replyId)).thenReturn(null);

        mockMvc.perform(get("/api/replies/{id}", replyId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // === POST /api/replies/create ===
    @Test
    void testCreateReply_Success() throws Exception {
        CreateReplyRequestDTO request = new CreateReplyRequestDTO(postId, userProfileId, "Nice reply!");
        when(replyRestService.createReply(any(CreateReplyRequestDTO.class))).thenReturn(mockReply);

        mockMvc.perform(post("/api/replies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.content").value("This is a reply"))
                .andExpect(jsonPath("$.message").value("Reply created successfully"));
    }

    @Test
    void testCreateReply_BadRequest() throws Exception {
        CreateReplyRequestDTO invalidRequest = new CreateReplyRequestDTO(null, null, "");

        mockMvc.perform(post("/api/replies/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("required")));
    }

    // === PUT /api/replies/{id} ===
    @Test
    void testUpdateReply_Success() throws Exception {
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(userProfileId)
                .content("Updated reply!")
                .build();
        
        when(replyRestService.updateReply(eq(replyId), any(UpdateReplyRequestDTO.class)))
                .thenReturn(mockReply);

        mockMvc.perform(put("/api/replies/{id}", replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reply updated successfully"));
    }

    @Test
    void testUpdateReply_Unauthorized() throws Exception {
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(userProfileId)
                .content("Updated reply!")
                .build();
        
        when(replyRestService.updateReply(eq(replyId), any(UpdateReplyRequestDTO.class)))
                .thenThrow(new RuntimeException("Unauthorized: Only admins or the reply owner can update this reply"));

        mockMvc.perform(put("/api/replies/{id}", replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    @Test
    void testUpdateReply_NotFound() throws Exception {
        UpdateReplyRequestDTO request = UpdateReplyRequestDTO.builder()
                .userProfileId(userProfileId)
                .content("Updated reply!")
                .build();
        
        when(replyRestService.updateReply(eq(replyId), any(UpdateReplyRequestDTO.class)))
                .thenThrow(new RuntimeException("Reply with id " + replyId + " not found"));

        mockMvc.perform(put("/api/replies/{id}", replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // === DELETE /api/replies/{id} ===
    @Test
    void testDeleteReply_Success() throws Exception {
        doNothing().when(replyRestService).deleteReply(replyId, userProfileId);

        mockMvc.perform(delete("/api/replies/{id}", replyId)
                        .param("userProfileId", userProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reply deleted successfully"));
        
        verify(replyRestService, times(1)).deleteReply(replyId, userProfileId);
    }

    @Test
    void testDeleteReply_Unauthorized() throws Exception {
        doThrow(new RuntimeException("Unauthorized: Only admins or the reply owner can delete this reply"))
                .when(replyRestService).deleteReply(replyId, userProfileId);

        mockMvc.perform(delete("/api/replies/{id}", replyId)
                        .param("userProfileId", userProfileId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    @Test
    void testDeleteReply_NotFound() throws Exception {
        doThrow(new RuntimeException("Reply with id " + replyId + " not found"))
                .when(replyRestService).deleteReply(replyId, userProfileId);

        mockMvc.perform(delete("/api/replies/{id}", replyId)
                        .param("userProfileId", userProfileId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}
