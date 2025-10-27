package io.harman.sidating_app_be.restcontroller;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.harman.sidating_app_be.restService.PostRestService;
import io.harman.sidating_app_be.restdto.request.post.CreatePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.DeletePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.LikePostRequestDTO;
import io.harman.sidating_app_be.restdto.request.post.UpdatePostRequestDTO;
import io.harman.sidating_app_be.restdto.response.BaseResponseDTO;
import io.harman.sidating_app_be.restdto.response.post.PostResponseDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PostRestController {
    @Autowired
    private PostRestService postRestService;

    public static final String BASE_URL = "/posts";
    public static final String VIEW_POST_DETAIL = BASE_URL + "/{id}";
    public static final String UPDATE_POST = BASE_URL + "/update";
    public static final String CREATE_POST = BASE_URL + "/create";
    public static final String LIKE_POST = BASE_URL + "/like";
    public static final String DELETE_POST = BASE_URL + "/delete";

    @GetMapping("/random")
    public ResponseEntity<?> random() {
        Random random = new Random();
        var theBoolean = random.nextBoolean();
        if (theBoolean) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponseDTO<?>> handleEmptyBody(HttpMessageNotReadableException ex) {
        var baseResponseDTO = new BaseResponseDTO<>();
        
        baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponseDTO.setMessage("User Profile ID is required; Image URL is required; Caption is required; ");
        baseResponseDTO.setData(null);
        baseResponseDTO.setTimestamp(new Date());
        
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
    }

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
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> getPost(@PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

        PostResponseDTO post = postRestService.getPostById(id);

        if (post == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Post Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(post);
        baseResponseDTO.setMessage("Data Post Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> createPost(
            @Valid @RequestBody CreatePostRequestDTO createPostRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

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


        PostResponseDTO post = postRestService.createPost(createPostRequestDTO);

        if (post == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Post Gagal Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData(post);
        baseResponseDTO.setMessage("Data Post Berhasil Dibuat");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping(UPDATE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> updatePost(
            @Valid @RequestBody UpdatePostRequestDTO updatePostRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

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


        PostResponseDTO post = postRestService.updatePost(updatePostRequestDTO);

        if (post == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Post Gagal Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(post);
        baseResponseDTO.setMessage("Data Post Berhasil Diupdate");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping(DELETE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> deletePost(
            @Valid @RequestBody DeletePostRequestDTO deletePostRequestDTO,
            BindingResult bindingResult
    ) {
        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

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


        PostResponseDTO post = postRestService.deletePost(deletePostRequestDTO);

        if (post == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Post tidak ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(post);
        baseResponseDTO.setMessage("Post berhasil di delete");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(LIKE_POST)
    public ResponseEntity<BaseResponseDTO<PostResponseDTO>> likePost(
            @Valid @RequestBody LikePostRequestDTO likePostRequestDTO,
            BindingResult bindingResult
    ) {
        var baseResponseDTO = new BaseResponseDTO<PostResponseDTO>();

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


        PostResponseDTO post = postRestService.likePost(likePostRequestDTO);

        if (post == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Post atau UserProfile berdasarkan id Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(post);
        baseResponseDTO.setMessage("Post berhasil di like/unlike");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }



}