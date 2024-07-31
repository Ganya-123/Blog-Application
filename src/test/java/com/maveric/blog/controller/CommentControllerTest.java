package com.maveric.blog.controller;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.entity.Comment;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CommentNotFoundException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CommentController commentController;

    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;
    private String token;
    private Long commentId;
    private Long authorId;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(1L);
        User author = new User();
        author.setId(1L);
        comment.setAuthor(author);
        comment.setContent("Original Comment");

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setAuthorId(1L);
        commentRequestDto.setContent("Updated Comment");

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setAuthorId(1L);
        commentResponseDto.setContent("Updated Comment");
        token = "Bearer test-token";
        commentId = 1L;
        authorId = 1L;
    }

    @Test
    void testCreateComment_Success() {
        when(commentService.createComment(any(CommentRequestDto.class), anyString())).thenReturn(commentResponseDto);

        ResponseEntity<CommentResponseDto> response = commentController.createComment(commentRequestDto, token);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(commentResponseDto, response.getBody());
        verify(commentService).createComment(any(CommentRequestDto.class), anyString());
    }

    @Test
    void testCreateComment_AuthorValidationFailed() {
        when(commentService.createComment(any(CommentRequestDto.class), anyString())).thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentController.createComment(commentRequestDto, token));

        assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testCreateComment_UserNotFound() {
        when(commentService.createComment(any(CommentRequestDto.class), anyString())).thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                commentController.createComment(commentRequestDto, token));

        assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateComment_CategoryNotFound() {
        when(commentService.createComment(any(CommentRequestDto.class), anyString())).thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () ->
                commentController.createComment(commentRequestDto, token));

        assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testEditComment_Success() {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString())).thenReturn(commentResponseDto);

        ResponseEntity<CommentResponseDto> response = commentController.editComment(1L, commentRequestDto, token);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Comment", response.getBody().getContent());

        verify(commentService, times(1)).editComment(anyLong(), any(CommentRequestDto.class), anyString());
    }

    @Test
    void testEditComment_AuthorValidationFailed() {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentController.editComment(1L, commentRequestDto, token));

        assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testEditComment_CommentNotFound() {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new CommentNotFoundException());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () ->
                commentController.editComment(1L, commentRequestDto, token));
    }

    @Test
    void testEditComment_AuthorIsDifferent() {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentController.editComment(1L, commentRequestDto, token));

        assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    }

    @Test
    void testDeleteComment_AuthorValidationFailed() {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        doThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED)).when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentController.deleteComment(1L, 1L, token));

        assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        doThrow(new CommentNotFoundException()).when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () ->
                commentController.deleteComment(1L, 1L, token));
    }

    @Test
    void testDeleteComment_AuthorIsDifferent() {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        doThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT)).when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        AuthorValidationException exception = assertThrows(AuthorValidationException.class, () ->
                commentController.deleteComment(1L, 1L, token));

        assertEquals(Constants.AUTHOR_IS_DIFFERENT, exception.getMessage());
    }

    @Test
    void testAdminDelete_Success() {

        when(commentService.adminDelete(anyLong())).thenReturn(Constants.COMMENT_DELETE_SUCCESS);

        ResponseEntity<String> response = commentController.adminDelete(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Constants.COMMENT_DELETE_SUCCESS, response.getBody());
        verify(commentService, times(1)).adminDelete(anyLong());
    }

    @Test
    void testAdminDelete_CommentNotFound() {
        when(commentService.adminDelete(anyLong())).thenThrow(new CommentNotFoundException(Constants.COMMENT_NOT_FOUND));

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () ->
                commentController.adminDelete(1L));

        assertEquals(Constants.COMMENT_NOT_FOUND, exception.getMessage());
        verify(commentService, times(1)).adminDelete(anyLong());
    }

}