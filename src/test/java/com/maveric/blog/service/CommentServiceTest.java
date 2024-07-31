package com.maveric.blog.service;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.entity.Comment;
import com.maveric.blog.entity.Post;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.*;
import com.maveric.blog.repository.CommentRepository;
import com.maveric.blog.repository.PostRepository;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private Post post;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        author = new User();
        author.setId(1L);
        author.setFullName("Test User");

        post = new Post();
        post.setPostId(1L);
        post.setAuthor(author);
        post.setPublished(true);

        comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setContent("Test Comment");
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateComment_Success() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        CommentResponseDto responseDto = commentService.createComment(requestDto, token);

        assertNotNull(responseDto);
        assertEquals(comment.getContent(), responseDto.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateComment_AuthorValidationFailed() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(2L);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.createComment(requestDto, token));

        Assertions.assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testCreateComment_UserNotFound() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        assertThrows(UserNotFoundException.class, () ->
                commentService.createComment(requestDto, token));
    }

    @Test
    void testCreateComment_PostNotFound() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        assertThrows(PostNotFoundException.class, () ->
                commentService.createComment(requestDto, token));
    }

    @Test
    void testCreateComment_AuthorIsDifferent() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));

        User differentAuthor = new User();
        differentAuthor.setId(2L);
        post.setAuthor(differentAuthor);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.createComment(requestDto, token));

        assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    }


    @Test
    void testCreateComment_PostNotPublished() {
        post.setPublished(false);
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");

        PublishPostException exception = assertThrows(PublishPostException.class, () ->
                commentService.createComment(requestDto, token));

        assertEquals(Constants.POST_NOT_PUBLISHED, exception.getMessage());
    }

    @Test
    void testCreateComment_ParentCommentNotFound() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");
        requestDto.setParentId(2L);

        assertThrows(CommentNotFoundException.class, () ->
                commentService.createComment(requestDto, token));
    }

    @Test
    void testCreateComment_ParentCommentDoesNotBelongToPost() {
        String token = "Bearer valid.token";
        Comment parentComment = new Comment();
        parentComment.setId(2L);
        parentComment.setPost(new Post() {{
            setPostId(2L);
        }});

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(parentComment));

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setPostId(1L);
        requestDto.setContent("Test Comment");
        requestDto.setParentId(2L);

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () ->
                commentService.createComment(requestDto, token));

        assertEquals(Constants.PARENT_COMMENT_NOT_BELONGS_TO_POST, exception.getMessage());
    }

    @Test
    void testEditComment_Success() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setContent("Updated Comment");

        CommentResponseDto responseDto = commentService.editComment(1L, requestDto, token);

        assertNotNull(responseDto);
        assertEquals("Updated Comment", responseDto.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testEditComment_AuthorValidationFailed() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(2L);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setContent("Updated Comment");

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.editComment(1L, requestDto, token));

        assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testEditComment_CommentNotFound() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setContent("Updated Comment");

        assertThrows(CommentNotFoundException.class, () ->
                commentService.editComment(1L, requestDto, token));
    }

    @Test
    void testEditComment_AuthorIsDifferent() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        Comment differentAuthorComment = new Comment();
        User differentAuthor = new User();
        differentAuthor.setId(2L);
        differentAuthorComment.setAuthor(differentAuthor);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(differentAuthorComment));

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setAuthorId(1L);
        requestDto.setContent("Updated Comment");

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.editComment(1L, requestDto, token));

        assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    }

    @Test
    void testDeleteComment_Success() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 1L, token);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_AuthorValidationFailed() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(2L);

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.deleteComment(1L, 1L, token));

        assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () ->
                commentService.deleteComment(1L, 1L, token));
    }

    @Test
    void testDeleteComment_AuthorIsDifferent() {
        String token = "Bearer valid.token";
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        Comment differentAuthorComment = new Comment();
        User differentAuthor = new User();
        differentAuthor.setId(2L);
        differentAuthorComment.setAuthor(differentAuthor);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(differentAuthorComment));

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentService.deleteComment(1L, 1L, token));

        assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    }

    @Test
    void testAdminDelete_Success() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        String result = commentService.adminDelete(1L);

        assertEquals(Constants.COMMENT_DELETE_SUCCESS, result);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testAdminDelete_CommentNotFound() {

        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () ->
                commentService.adminDelete(1L));

        assertEquals(Constants.COMMENT_NOT_FOUND, exception.getMessage());
    }
}
