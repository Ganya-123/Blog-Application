package com.maveric.blog.controller;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CommentNotFoundException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

    private static final String VALID_TEST_TOKEN = "Bearer valid-test-token";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    @MockBean
    private JwtService jwtService;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        UserDetails mockUserDetails = new User("testuser", "password", Collections.emptyList());

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn(mockUserDetails.getUsername());

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setAuthorId(1L);
        commentRequestDto.setContent("Test Comment");

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setAuthorId(1L);
        commentResponseDto.setContent("Test Comment");
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testCreateComment_Success() throws Exception {
        String jsonRequest = "{ \"authorId\": 1, \"postId\": 1, \"content\": \"Test Comment\" }";

        when(commentService.createComment(any(CommentRequestDto.class), anyString())).thenReturn(commentResponseDto);

        mockMvc.perform(post("/comment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testCreateComment_AuthorValidationFailed() throws Exception {
        String jsonRequest = "{ \"authorId\": 1, \"postId\": 1, \"content\": \"Test Comment\" }";

        when(commentService.createComment(any(CommentRequestDto.class), anyString()))
                .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

        mockMvc.perform(post("/comment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"" + Constants.AUTHOR_VALIDATION_FAILED + "\",\"errors\":[]}")); // Validate the message here
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testCreateComment_UserNotFound() throws Exception {
        String jsonRequest = "{ \"authorId\": 1, \"postId\": 1, \"content\": \"Test Comment\" }";

        when(commentService.createComment(any(CommentRequestDto.class), anyString()))
                .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

        mockMvc.perform(post("/comment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"NOT_FOUND\",\"message\":\"" + Constants.AUTHOR_NOT_FOUND + "\",\"errors\":[]}"));
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testCreateComment_CategoryNotFound() throws Exception {
        String jsonRequest = "{ \"authorId\": 1, \"postId\": 1, \"content\": \"Test Comment\" }";

        when(commentService.createComment(any(CommentRequestDto.class), anyString()))
                .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

        mockMvc.perform(post("/comment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"NOT_FOUND\",\"message\":\"" + Constants.CATEGORY_NOT_FOUND + "\",\"errors\":[]}"));
    }


    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testEditComment_Success() throws Exception {
        commentResponseDto.setContent("Updated Comment");
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenReturn(commentResponseDto);

        mockMvc.perform(put("/comment/{commentId}/edit", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\": 1, \"authorId\": 1, \"content\": \"Updated Comment\"}")
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"authorId\":1,\"content\":\"Updated Comment\"}"));
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testEditComment_AuthorValidationFailed() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

        mockMvc.perform(put("/comment/{commentId}/edit", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\": 1, \"authorId\": 1, \"content\": \"Updated Comment\"}")
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"" + Constants.AUTHOR_VALIDATION_FAILED + "\",\"errors\":[]}"));
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testEditComment_CommentNotFound() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new CommentNotFoundException());

        mockMvc.perform(put("/comment/{commentId}/edit", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\": 1, \"authorId\": 1, \"content\": \"Updated Comment\"}")
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testEditComment_AuthorIsDifferent() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(commentService.editComment(anyLong(), any(CommentRequestDto.class), anyString()))
                .thenThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT));

        mockMvc.perform(put("/comment/{commentId}/edit", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\": 1, \"authorId\": 1, \"content\": \"Updated Comment\"}")
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"" + Constants.AUTHOR_IS_DIFFERENT + "\",\"errors\":[]}"));
    }


    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testDeleteComment_AuthorValidationFailed() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        doThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED))
                .when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/comment/author/{authorId}/comment/{commentId}", 1L, 1L)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"" + Constants.AUTHOR_VALIDATION_FAILED + "\",\"errors\":[]}"));
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testDeleteComment_CommentNotFound() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        doThrow(new CommentNotFoundException(Constants.COMMENT_NOT_FOUND)).when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/comment/author/{authorId}/comment/{commentId}", 1L, 1L)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"NOT_FOUND\",\"message\":\"" + Constants.COMMENT_NOT_FOUND + "\",\"errors\":[]}"));
    }

    @Test
    @WithMockUser(authorities = {"WRITE"})
    void testDeleteComment_AuthorIsDifferent() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        doThrow(new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT))
                .when(commentService).deleteComment(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/comment/author/{authorId}/comment/{commentId}", 1L, 1L)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"" + Constants.AUTHOR_IS_DIFFERENT + "\",\"errors\":[]}"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testAdminDelete_Success() throws Exception {
        when(commentService.adminDelete(anyLong())).thenReturn(Constants.COMMENT_DELETE_SUCCESS);

        mockMvc.perform(delete("/comment/{commentId}", 1L)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(Constants.COMMENT_DELETE_SUCCESS));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testAdminDelete_CommentNotFound() throws Exception {
        when(commentService.adminDelete(anyLong())).thenThrow(new CommentNotFoundException(Constants.COMMENT_NOT_FOUND));

        mockMvc.perform(delete("/comment/{commentId}", 1L)
                        .header("Authorization", VALID_TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"NOT_FOUND\",\"message\":\"" + Constants.COMMENT_NOT_FOUND + "\",\"errors\":[]}"));
    }
}