package io.harman.sidating_app_be.restcontroller;

import io.harman.sidating_app_be.restdto.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.request.post.*;
import io.harman.sidating_app_be.restdto.response.post.*;
import io.harman.sidating_app_be.restService.PostRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PostRestController {
    
    @Autowired
    private PostRestService postRestService;

    public static final String BASE_URL = "/posts";
    public static final String VIEW_POST_DETAIL = BASE_URL + "/{id}";
    public static final String CREATE_POST = BASE_URL + "/create";
    public static final String UPDATE_POST = BASE_URL + "/update";
    public static final String DELETE_POST = BASE_URL + "/delete";
    public static final String LIKE_POST = BASE_URL + "/{id}/like";
    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<PostResponseDTO>>> getAllPost(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String date
            ) {
        var baseResponseDTO = new BaseResponseDTO<List<PostResponseDTO>>();

        List<PostResponseDTO> posts;

        if (userId != null && date != null) {
            posts = postRestService.getPostByUserIdAndDate(userId, date);
        }

        else if (userId != null) {
            posts = postRestService.getPostByUserId(userId);
        }

        else if (date != null) {
            posts = postRestService.getPostByDate(date);
        }

        else {
            posts = postRestService.getAllPosts();
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(posts);
        baseResponseDTO.setMessage("Posts retrieved successfully");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_POST_DETAIL)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> getPostDetail(@PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

        try {
            PostResponseDTO post = postRestService.getPostById(id);

            if (post == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Post Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(post);
            baseResponseDTO.setMessage("Detail Post Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping(CREATE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> createPost(
            @RequestBody(required = false) @Valid CreatePostRequestDTO createPostRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

        // Check if request body is null
        if (createPostRequestDTO == null) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Required request body is missing");
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        // Validasi input fields
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            PostResponseDTO post = postRestService.createPost(createPostRequestDTO);

            // Kalau sukses
            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(post);
            baseResponseDTO.setMessage("Data Post Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
            
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to create post: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> updatePost(
            @Valid @RequestBody UpdatePostRequestDTO updatePostRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

        // Validasi input
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

        // Validasi apakah ada field yang akan diupdate
        if ((updatePostRequestDTO.getImageUrl() == null || updatePostRequestDTO.getImageUrl().trim().isEmpty()) &&
            (updatePostRequestDTO.getCaption() == null || updatePostRequestDTO.getCaption().trim().isEmpty())) {
            
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Setidaknya imageUrl atau caption harus diisi untuk update");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            PostResponseDTO updatedPost = postRestService.updatePost(updatePostRequestDTO);

            if (updatedPost == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Post tidak ditemukan atau gagal diupdate");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(updatedPost);
            baseResponseDTO.setMessage("Post updated successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (SecurityException ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to update post: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_POST)
    public ResponseEntity<BaseResponseDTO<String>> deletePost(
            @Valid @RequestBody DeletePostRequestDTO deletePostRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<String>();

        // Validasi input
        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            postRestService.deletePost(deletePostRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Post with id " + deletePostRequestDTO.getId() + " deleted successfully");
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (SecurityException ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to delete post: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("tidak ditemukan") || ex.getMessage().contains("not found")) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Post with id " + deletePostRequestDTO.getId() + " not found");
                baseResponseDTO.setTimestamp(new Date());
                baseResponseDTO.setData(null);
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            } else {
                baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                baseResponseDTO.setMessage("Failed to delete post: " + ex.getMessage());
                baseResponseDTO.setTimestamp(new Date());
                baseResponseDTO.setData(null);
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Unexpected error occurred while deleting post");
            baseResponseDTO.setTimestamp(new Date());
            baseResponseDTO.setData(null);
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(LIKE_POST)
    public ResponseEntity<BaseResponseDTO<String>> likePost(@PathVariable UUID id) {

        var baseResponseDTO = new BaseResponseDTO<String>();

        try {
            LikePostRequestDTO likePostRequestDTO = new LikePostRequestDTO();
            likePostRequestDTO.setId(id);
            
            postRestService.likePost(likePostRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(null);
            baseResponseDTO.setMessage("Post like successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("tidak ditemukan")) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage(ex.getMessage());
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            } else {
                baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> random() {
        Random random = new Random();
        boolean theBoolean = random.nextBoolean();

        if (theBoolean) {
            return ResponseEntity.ok().body("200 OK: Random success response");
        } else {
            return ResponseEntity.badRequest().body("400 Bad Request: Random failure response");
        }
    }


}