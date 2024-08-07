package com.maveric.blog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.entity.Category;
import com.maveric.blog.entity.Post;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.*;
import com.maveric.blog.repository.CategoryRepository;
import com.maveric.blog.repository.PostRepository;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostServiceTest {

  @Mock private PostRepository postRepository;

  @Mock private UserRepository userRepository;

  @Mock private CategoryRepository categoryRepository;

  @Mock private JwtService jwtService;

  @Mock private ModelMapper mapper;

  @InjectMocks private PostService postService;

  private PostRequestDto postRequestDto;
  private Post post;
  private User author;
  private Category category;
  private String token;

  @BeforeEach
  public void setUp() {
    author = new User();
    author.setId(1L);

    category = new Category();
    category.setId(1L);

    postRequestDto = new PostRequestDto();
    postRequestDto.setAuthorId(1L);
    postRequestDto.setCategoryId(1L);
    postRequestDto.setTitle("Test Title");
    postRequestDto.setContent("Test Content");

    post = new Post();
    post.setPostId(1L);
    post.setAuthor(author);
    post.setCategory(category);
    post.setCreatedAt(LocalDateTime.now());

    token = "Bearer dummyTokenString";
  }

  @Test
  void testCreatePost_Success() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDto response = postService.createPost(postRequestDto, token);

    assertEquals(post.getPostId(), response.getPostId());
    assertEquals(post.getTitle(), response.getTitle());
    assertEquals(post.getContent(), response.getContent());
  }

  @Test
  void testCreatePost_AuthorValidationFailed() {
    when(jwtService.extractUserId(anyString())).thenReturn(2L);

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postService.createPost(postRequestDto, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testCreatePost_UserNotFound() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> postService.createPost(postRequestDto, token));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testCreatePost_CategoryNotFound() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

    CategoryNotFoundException exception =
        assertThrows(
            CategoryNotFoundException.class, () -> postService.createPost(postRequestDto, token));

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUpdatePost_Success() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDto response = postService.updatePost(1L, postRequestDto, token);

    assertEquals(post.getPostId(), response.getPostId());
    assertEquals(post.getTitle(), response.getTitle());
    assertEquals(post.getContent(), response.getContent());
    assertEquals(post.getCategory().getId(), response.getCategoryId());
    assertTrue(post.isDraft());
    assertFalse(post.isPublished());
  }

  @Test
  void testUpdatePost_AuthorValidationFailed() {
    when(jwtService.extractUserId(anyString())).thenReturn(2L);

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postService.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testUpdatePost_PostNotFound() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

    PostNotFoundException exception =
        assertThrows(
            PostNotFoundException.class, () -> postService.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUpdatePost_CategoryNotFound() {
    postRequestDto.setCategoryId(2L);
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

    CategoryNotFoundException exception =
        assertThrows(
            CategoryNotFoundException.class,
            () -> postService.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testUpdatePost_AuthorMismatch() {
    post.getAuthor().setId(2L);
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> postService.updatePost(1L, postRequestDto, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void testPublishPost_Success() {
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDto response = postService.publishPost(1L, 1L, token);

    assertNotNull(response);
    assertTrue(post.isPublished());
    assertNotNull(post.getPublishedAt());
  }

  @Test
  void testPublishPost_AuthorValidationFailed() {
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(2L);

    AuthorValidationException exception =
        assertThrows(AuthorValidationException.class, () -> postService.publishPost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testPublishPost_PostNotFound() {
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postService.publishPost(1L, 1L, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testPublishPost_AlreadyPublished() {
    post.setPublished(true);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    PublishPostException exception =
        assertThrows(PublishPostException.class, () -> postService.publishPost(1L, 1L, token));

    assertEquals(Constants.POST_ALREADY_PUBLISHED, exception.getMessage());
  }

  @Test
  void testPublishPost_AuthorMismatch() {
    post.getAuthor().setId(2L);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    AuthorValidationException exception =
        assertThrows(AuthorValidationException.class, () -> postService.publishPost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void test_unPublishPost_Success() {
    post.setPublished(true);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDto response = postService.unpublishPost(1L, 1L, token);

    assertNotNull(response);
    assertFalse(post.isPublished());
  }

  @Test
  void test_unPublishPost_AuthorValidationFailed() {
    post.setPublished(true);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(2L);

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postService.unpublishPost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void test_unPublishPost_PostNotFound() {
    post.setPublished(true);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postService.unpublishPost(1L, 1L, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void test_unPublishPost_AlreadyPublished() {
    post.setPublished(false);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    PublishPostException exception =
        assertThrows(PublishPostException.class, () -> postService.unpublishPost(1L, 1L, token));

    assertEquals(Constants.POST_ALREADY_UNPUBLISHED, exception.getMessage());
  }

  @Test
  void test_unPublishPost_AuthorMismatch() {
    post.setPublished(true);
    post.getAuthor().setId(2L);
    String token = "dummyTokenString";
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class, () -> postService.unpublishPost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void testDeletePost_Success() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    postService.deletePost(1L, 1L, token);

    verify(postRepository, times(1)).deleteById(anyLong());
  }

  @Test
  void testDeletePost_AuthorValidationFailed() {
    when(jwtService.extractUserId(anyString())).thenReturn(2L);

    AuthorValidationException exception =
        assertThrows(AuthorValidationException.class, () -> postService.deletePost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
  }

  @Test
  void testDeletePost_PostNotFound() {
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postService.deletePost(1L, 1L, token));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testDeletePost_AuthorMismatch() {
    post.getAuthor().setId(2L);
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

    AuthorValidationException exception =
        assertThrows(AuthorValidationException.class, () -> postService.deletePost(1L, 1L, token));

    assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
  }

  @Test
  void testAdminDeletePost_Success() {

    when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
    String result = postService.adminDeletePost(1L);
    assertEquals(Constants.POST_DELETE_SUCCESS, result);
    verify(postRepository, times(1)).deleteById(anyLong());
  }

  @Test
  void testAdminDeletePost_PostNotFound() {

    when(postRepository.findById(anyLong())).thenReturn(Optional.empty());
    PostNotFoundException exception =
        assertThrows(PostNotFoundException.class, () -> postService.adminDeletePost(1L));

    assertEquals(Constants.POST_NOT_FOUND, exception.getMessage());
    verify(postRepository, times(0)).deleteById(anyLong());
  }

  @Test
  void testGetAllPosts_EmptyList() {

    when(postRepository.findAll()).thenReturn(Collections.emptyList());

    List<PostResponseDto> result = postService.getAllPosts();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetAllPosts_NonEmptyList() {

    Post post1 = new Post();
    Post post2 = new Post();
    when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

    List<PostResponseDto> result = postService.getAllPosts();

    assertNotNull(result);
    assertEquals(2, result.size());
  }
}
