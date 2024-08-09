package com.maveric.blog.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.exception.*;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.service.PostService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTesting {

  private static final String VALID_TEST_TOKEN = "Bearer valid-test-token";
  @Autowired private MockMvc mockMvc;
  @MockBean private PostService postService;
  @MockBean private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;
  private UserDetails mockUserDetails;
  private PostRequestDto postRequestDto;

  private PostResponseDto postResponseDto;

  @BeforeEach
  public void setUp() {
    mockUserDetails = new User("testuser", "password", Collections.emptyList());
    postRequestDto = new PostRequestDto();
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
    when(jwtService.extractUsername(anyString())).thenReturn(mockUserDetails.getUsername());

    postResponseDto = new PostResponseDto();
    postResponseDto.setPostId(1L);
    postResponseDto.setTitle("Test Title");
    postResponseDto.setContent("Test Content");
    postResponseDto.setAuthorId(1L);
    postResponseDto.setCategoryId(1L);
    postResponseDto.setFeatured(true);
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testCreatePost() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Sample Title");
    postRequestDto.setContent("Sample content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    PostResponseDto postResponseDto = new PostResponseDto();
    postResponseDto.setPostId(1L);

    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenReturn(postResponseDto);

    mockMvc
        .perform(
            post("/post")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.postId").value(postResponseDto.getPostId()));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testCreatePost_AuthorValidationFailed() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Sample Title");
    postRequestDto.setContent("Sample content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            post("/post")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testCreatePost_UserNotFound() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Sample Title");
    postRequestDto.setContent("Sample content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    mockMvc
        .perform(
            post("/post")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_NOT_FOUND));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testCreatePost_CategoryNotFound() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Valid Title");
    postRequestDto.setContent("Valid Content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

    mockMvc
        .perform(
            post("/post")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.CATEGORY_NOT_FOUND));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUpdatePost_Success() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Updated Title");
    postRequestDto.setContent("Updated Content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.updatePost(1L, postRequestDto, "Bearer valid-test-token"))
        .thenReturn(postResponseDto);

    mockMvc
        .perform(
            put("/post/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.postId").value(postResponseDto.getPostId()))
        .andExpect(jsonPath("$.title").value(postResponseDto.getTitle()))
        .andExpect(jsonPath("$.content").value(postResponseDto.getContent()))
        .andExpect(jsonPath("$.authorId").value(postResponseDto.getAuthorId()))
        .andExpect(jsonPath("$.categoryId").value(postResponseDto.getCategoryId()))
        .andExpect(jsonPath("$.featured").value(postResponseDto.isFeatured()));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUpdatePost_AuthorValidationFailed() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Valid Title");
    postRequestDto.setContent("Valid Content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            put("/post/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE"})
  void testUpdatePost_PostNotFound() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Valid Title");
    postRequestDto.setContent("Valid Content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    mockMvc
        .perform(
            put("/post/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.POST_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUpdatePost_CategoryNotFound() throws Exception {
    PostRequestDto postRequestDto = new PostRequestDto();
    postRequestDto.setTitle("Valid Title");
    postRequestDto.setContent("Valid Content");
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setFeatured(false);

    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

    mockMvc
        .perform(
            put("/post/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-test-token")
                .content(objectMapper.writeValueAsString(postRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.CATEGORY_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testPublishPost_Success() throws Exception {
    when(postService.publishPost(anyLong(), anyLong(), anyString())).thenReturn(postResponseDto);

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/publish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.postId").value(postResponseDto.getPostId()))
        .andExpect(jsonPath("$.title").value(postResponseDto.getTitle()))
        .andExpect(jsonPath("$.content").value(postResponseDto.getContent()))
        .andExpect(jsonPath("$.authorId").value(postResponseDto.getAuthorId()))
        .andExpect(jsonPath("$.categoryId").value(postResponseDto.getCategoryId()))
        .andExpect(jsonPath("$.featured").value(postResponseDto.isFeatured()));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testPublishPost_AuthorValidationFailed() throws Exception {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/publish", 1L, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testPublishPost_PostNotFound() throws Exception {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/publish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value(Constants.POST_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testPublishPost_PostAlreadyPublished() throws Exception {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PublishPostException(Constants.POST_ALREADY_PUBLISHED));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/publish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value("CONFLICT"))
        .andExpect(jsonPath("$.message").value(Constants.POST_ALREADY_PUBLISHED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testPublishPost_AuthorIsDifferent() throws Exception {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/publish", 1L, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_IS_DIFFERENT))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUnpublishPost_Success() throws Exception {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString())).thenReturn(postResponseDto);

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/unpublish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.postId").value(postResponseDto.getPostId()))
        .andExpect(jsonPath("$.title").value(postResponseDto.getTitle()))
        .andExpect(jsonPath("$.content").value(postResponseDto.getContent()))
        .andExpect(jsonPath("$.authorId").value(postResponseDto.getAuthorId()))
        .andExpect(jsonPath("$.categoryId").value(postResponseDto.getCategoryId()))
        .andExpect(jsonPath("$.featured").value(postResponseDto.isFeatured()));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUnpublishPost_AuthorValidationFailed() throws Exception {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/unpublish", 1L, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUnpublishPost_PostNotFound() throws Exception {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/unpublish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value(Constants.POST_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUnpublishPost_PostAlreadyUnpublished() throws Exception {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PublishPostException(Constants.POST_ALREADY_UNPUBLISHED));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/unpublish", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value("CONFLICT"))
        .andExpect(jsonPath("$.message").value(Constants.POST_ALREADY_UNPUBLISHED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testUnpublishPost_AuthorIsDifferent() throws Exception {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

    mockMvc
        .perform(
            put("/author/{authorId}/post/{postId}/unpublish", 1L, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_IS_DIFFERENT))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testDeletePost_Success() throws Exception {
    String responseMessage = Constants.POST_DELETE_SUCCESS;

    when(postService.deletePost(anyLong(), anyLong(), anyString())).thenReturn(responseMessage);

    mockMvc
        .perform(
            delete("/author/{authorId}/post/{postId}", 1L, 2L)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string(responseMessage));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testDeletePost_AuthorValidationFailed() throws Exception {
    when(postService.deletePost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            delete("/author/{authorId}/post/{postId}", 1L, 2L)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testDeletePost_PostNotFound() throws Exception {
    when(postService.deletePost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    mockMvc
        .perform(
            delete("/author/{authorId}/post/{postId}", 1L, 2L)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value(Constants.POST_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testDeletePost_AuthorIsDifferent() throws Exception {
    when(postService.deletePost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

    mockMvc
        .perform(
            delete("/author/{authorId}/post/{postId}", 1L, 2L)
                .header("Authorization", VALID_TEST_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_IS_DIFFERENT))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"ADMIN"})
  void testAdminDeletePost_Success() throws Exception {
    String responseMessage = Constants.POST_DELETE_SUCCESS;

    when(postService.adminDeletePost(anyLong())).thenReturn(responseMessage);

    mockMvc
        .perform(delete("/{postId}", 1L).header("Authorization", VALID_TEST_TOKEN))
        .andExpect(content().string(responseMessage));
  }

  @Test
  @WithMockUser(authorities = {"ADMIN"})
  void testAdminDeletePost_PostNotFound() throws Exception {
    when(postService.adminDeletePost(anyLong()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    mockMvc
        .perform(delete("/{postId}", 1L).header("Authorization", VALID_TEST_TOKEN))
        .andExpect(jsonPath("$.message").value(Constants.POST_NOT_FOUND))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testGetAllPosts_EmptyList() throws Exception {

    when(postService.getAllPosts()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/posts").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string("[]"));
  }

  @Test
  @WithMockUser(authorities = {"WRITE", "READ"})
  void testGetAllPosts_NonEmptyList() throws Exception {

    PostResponseDto post1 = new PostResponseDto();
    post1.setPostId(1L);
    post1.setTitle("Title one");
    post1.setContent("content matters");
    post1.setAuthorId(1L);
    post1.setCategoryId(1L);
    PostResponseDto post2 = new PostResponseDto();
    post2.setPostId(1L);
    post2.setTitle("Title one");
    post2.setContent("content matters");
    post2.setAuthorId(1L);
    post2.setCategoryId(1L);

    when(postService.getAllPosts()).thenReturn(List.of(post1, post2));

    mockMvc
        .perform(get("/posts").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2));
  }
}
