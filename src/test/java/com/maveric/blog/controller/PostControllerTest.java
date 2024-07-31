package com.maveric.blog.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

  @Mock private PostService postService;

  @Mock private JwtService jwtService;

  @InjectMocks private PostController postController;

  private PostRequestDto postRequestDto;
  private PostResponseDto postResponseDto;
  private String token;
  private Long postId;
  private Long authorId;

  @BeforeEach
  void setUp() {
    postRequestDto = new PostRequestDto();
    postResponseDto = new PostResponseDto();
    token = "Bearer test-token";
    postId = 1L;
    authorId = 1L;
  }

  @Test
  void testCreatePost_Success() {
    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenReturn(postResponseDto);

    ResponseEntity<PostResponseDto> response = postController.createPost(postRequestDto, token);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(postResponseDto, response.getBody());
    verify(postService).createPost(any(PostRequestDto.class), anyString());
  }

  @Test
  void testCreatePost_AuthorValidationFailed() {
    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postController.createPost(postRequestDto, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testCreatePost_UserNotFound() {
    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> postController.createPost(postRequestDto, token));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testCreatePost_CategoryNotFound() {
    when(postService.createPost(any(PostRequestDto.class), anyString()))
        .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

    CategoryNotFoundException exception =
        assertThrows(
            CategoryNotFoundException.class,
            () -> postController.createPost(postRequestDto, token));

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUpdatePost_Success() {
    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenReturn(postResponseDto);

    ResponseEntity<PostResponseDto> response = postController.updatePost(1L, postRequestDto, token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(postResponseDto, response.getBody());
    verify(postService).updatePost(anyLong(), any(PostRequestDto.class), anyString());
  }

  @Test
  void testUpdatePost_AuthorValidationFailed() {
    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postController.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testUpdatePost_PostNotFound() {
    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    PostNotFoundException exception =
        assertThrows(
            PostNotFoundException.class,
            () -> postController.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUpdatePost_CategoryNotFound() {
    when(postService.updatePost(anyLong(), any(PostRequestDto.class), anyString()))
        .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

    CategoryNotFoundException exception =
        assertThrows(
            CategoryNotFoundException.class,
            () -> postController.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testPublishPost_Success() {
    when(postService.publishPost(anyLong(), anyLong(), anyString())).thenReturn(postResponseDto);

    ResponseEntity<PostResponseDto> response = postController.publishPost(1L, 1L, token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(postResponseDto, response.getBody());
    verify(postService).publishPost(anyLong(), anyLong(), anyString());
  }

  @Test
  void testPublishPost_AuthorValidationFailed() {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postController.publishPost(1L, 2L, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testPublishPost_PostNotFound() {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postController.publishPost(1L, 1L, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testPublishPost_PostAlreadyPublished() {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PublishPostException(Constants.POST_ALREADY_PUBLISHED));

    PublishPostException exception =
        assertThrows(PublishPostException.class, () -> postController.publishPost(1L, 1L, token));

    assertEquals(Constants.POST_ALREADY_PUBLISHED, exception.getMessage());
  }

  @Test
  void testPublishPost_AuthorIsDifferent() {
    when(postService.publishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postController.publishPost(1L, 2L, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void testUnpublishPost_Success() {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString())).thenReturn(postResponseDto);

    ResponseEntity<PostResponseDto> response = postController.unpublishPost(1L, 1L, token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(postResponseDto, response.getBody());
    verify(postService).unpublishPost(anyLong(), anyLong(), anyString());
  }

  @Test
  void testUnpublishPost_AuthorValidationFailed() {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postController.unpublishPost(1L, 2L, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testUnpublishPost_PostNotFound() {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));

    PostNotFoundException exception =
        assertThrows(
            PostNotFoundException.class, () -> postController.unpublishPost(1L, 1L, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUnpublishPost_PostAlreadyUnpublished() {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new PublishPostException(Constants.POST_ALREADY_UNPUBLISHED));

    PublishPostException exception =
        assertThrows(PublishPostException.class, () -> postController.unpublishPost(1L, 1L, token));

    assertEquals(Constants.POST_ALREADY_UNPUBLISHED, exception.getMessage());
  }

  @Test
  void testUnpublishPost_AuthorIsDifferent() {
    when(postService.unpublishPost(anyLong(), anyLong(), anyString()))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postController.unpublishPost(1L, 2L, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void testDeletePost_Success() {
    when(postService.deletePost(anyLong(), anyLong(), anyString()))
        .thenReturn(Constants.POST_DELETE_SUCCESS);

    ResponseEntity<String> response = postController.deletePost(postId, authorId, token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Constants.POST_DELETE_SUCCESS, response.getBody());
    verify(postService).deletePost(postId, authorId, token);
  }

  @Test
  void testDeletePost_AuthorValidationFailed() {
    doThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED))
        .when(postService)
        .deletePost(anyLong(), anyLong(), anyString());

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postController.deletePost(postId, authorId, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    verify(postService).deletePost(postId, authorId, token);
  }

  @Test
  void testDeletePost_PostNotFound() {
    doThrow(new PostNotFoundException(Constants.POST_NOT_FOUND))
        .when(postService)
        .deletePost(anyLong(), anyLong(), anyString());

    PostNotFoundException exception =
        assertThrows(
            PostNotFoundException.class, () -> postController.deletePost(postId, authorId, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
    verify(postService).deletePost(postId, authorId, token);
  }

  @Test
  void testDeletePost_AuthorIsDifferent() {
    doThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT))
        .when(postService)
        .deletePost(anyLong(), anyLong(), anyString());

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postController.deletePost(postId, authorId, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    verify(postService).deletePost(postId, authorId, token);
  }

  @Test
  void testAdminDeletePost_Success() {
    when(postService.adminDeletePost(anyLong())).thenReturn(Constants.POST_DELETE_SUCCESS);
    ResponseEntity<String> response = postController.adminDeletePost(1L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Constants.POST_DELETE_SUCCESS, response.getBody());
    verify(postService, times(1)).adminDeletePost(anyLong());
  }

  @Test
  void testAdminDeletePost_PostNotFound() {
    when(postService.adminDeletePost(anyLong()))
        .thenThrow(new PostNotFoundException(Constants.POST_NOT_FOUND));
    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postController.adminDeletePost(1L));
    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
    verify(postService, times(1)).adminDeletePost(anyLong());
  }
  @Test
  void testGetAllPosts_EmptyList() {
    when(postService.getAllPosts()).thenReturn(Collections.emptyList());
    ResponseEntity<List<PostResponseDto>> responseEntity = postController.getAllPosts();
    List<PostResponseDto> result = responseEntity.getBody();
    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    verify(postService, times(1)).getAllPosts();
  }

  @Test
  void testGetAllPosts_NonEmptyList() {

    PostResponseDto post1 = new PostResponseDto();
    post1.setPostId(1L);
    post1.setTitle("Title one");
    post1.setContent("content matters");
    post1.setAuthorId(1L);
    post1.setCategoryId(1L);

    PostResponseDto post2 = new PostResponseDto();
    post2.setPostId(2L);
    post2.setTitle("Title two");
    post2.setContent("more content matters");
    post2.setAuthorId(2L);
    post2.setCategoryId(2L);

    when(postService.getAllPosts()).thenReturn(List.of(post1, post2));

    ResponseEntity<List<PostResponseDto>> responseEntity = postController.getAllPosts();
    List<PostResponseDto> result = responseEntity.getBody();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    verify(postService, times(1)).getAllPosts();
  }
}
