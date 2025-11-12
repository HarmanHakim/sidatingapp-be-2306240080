package io.harman.sidating_app_be.restcontroller;
 
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.request.reply.CreateReplyRequestDTO;
import io.harman.sidating_app_be.restdto.response.reply.ReplyResponseDTO;
import io.harman.sidating_app_be.restservice.ReplyRestService;
import jakarta.validation.Valid;
 
@RestController
@RequestMapping("/api/replies")
public class ReplyRestController {
 
    @Autowired
    private ReplyRestService replyRestService;
 
    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<ReplyResponseDTO>>> getAllReplies(
            @RequestParam(required = false) UUID postId) {
 
        var baseResponseDTO = new BaseResponseDTO<List<ReplyResponseDTO>>();
 
        List<ReplyResponseDTO> replies;
 
        if (postId != null) {
            replies = replyRestService.getRepliesByPostId(postId);
        } else {
            replies = replyRestService.getAllReplies();
        }
 
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(replies);
        baseResponseDTO.setMessage("Replies retrieved successfully");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ReplyResponseDTO>> getReplyById(@PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<ReplyResponseDTO>();
 
        ReplyResponseDTO reply = replyRestService.getReplyById(id);
 
        if (reply == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Reply with id " + id + " not found");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
 
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(reply);
        baseResponseDTO.setMessage("Reply retrieved successfully");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }
 
    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<ReplyResponseDTO>> createReply(
            @Valid @RequestBody CreateReplyRequestDTO createReplyRequestDTO,
            BindingResult bindingResult) {
 
        var baseResponseDTO = new BaseResponseDTO<ReplyResponseDTO>();
 
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
 
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
 
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
 
        try {
            ReplyResponseDTO reply = replyRestService.createReply(createReplyRequestDTO);
 
            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(reply);
            baseResponseDTO.setMessage("Reply created successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
 
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to create reply: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
 