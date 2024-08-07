package com.maveric.blog.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.*;
import com.maveric.blog.entity.Avatar;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.service.AuthenticationService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticationService authenticationService;

  @MockBean private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;
  private UserDetails mockUserDetails;
  private PostRequestDto postRequestDto;
  private ChangePassword changePassword;
  private ForgotPassword forgotPassword;
  private UserUpdateRequestDto userUpdateRequestDto;

  @BeforeEach
  void setUp() {
    mockUserDetails = new User("testuser", "password", Collections.emptyList());
    postRequestDto = new PostRequestDto();
    when(jwtService.extractUserId(anyString())).thenReturn(1L);
    when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
    when(jwtService.extractUsername(anyString())).thenReturn(mockUserDetails.getUsername());
    changePassword =
        new ChangePassword("test@example.com", "oldPassword", "newPassword", "newPassword");
    forgotPassword =
        new ForgotPassword("test@example.com", "1234567890", "newPassword", "newPassword");
    userUpdateRequestDto = new UserUpdateRequestDto();
    userUpdateRequestDto.setFullName("Jane Doe");
    userUpdateRequestDto.setBio("Updated bio");
    userUpdateRequestDto.setAvatar(Avatar.DEFAULT_AVATAR);
    userUpdateRequestDto.setMobileNumber("1234567890");
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testChangePassword_Success() throws Exception {
    String token = "Bearer valid_token";
    String expectedResponse = "Password changed successfully";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenReturn(expectedResponse);

    mockMvc
        .perform(
            put("/user/PasswordChange")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(objectMapper.writeValueAsString(changePassword)))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResponse));
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testChangePassword_UserNotFound() throws Exception {
    String token = "Bearer valid_token";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    mockMvc
        .perform(
            put("/user/PasswordChange")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(objectMapper.writeValueAsString(changePassword)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_NOT_FOUND));
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testChangePassword_AuthorizationFailed() throws Exception {
    String token = "Bearer invalid_token";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            put("/user/PasswordChange")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(objectMapper.writeValueAsString(changePassword)))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED));
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testUpdateUser_Success() throws Exception {
    RegisterResponse expectedResponse = new RegisterResponse();
    expectedResponse.setFullName("John Doe");
    expectedResponse.setMobileNumber("1234567890");

    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenReturn(expectedResponse);

    mockMvc
        .perform(
            put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid_token")
                .content(objectMapper.writeValueAsString(userUpdateRequestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("John Doe"))
        .andExpect(jsonPath("$.mobileNumber").value("1234567890"));
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testUpdateUser_UserNotFound() throws Exception {
    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    mockMvc
        .perform(
            put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid_token")
                .content(objectMapper.writeValueAsString(userUpdateRequestDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_NOT_FOUND));
  }

  @Test
  @WithMockUser(authorities = "WRITE")
  void testUpdateUser_AuthorizationFailed() throws Exception {

    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenThrow(new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED));

    mockMvc
        .perform(
            put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalid_token")
                .content(objectMapper.writeValueAsString(userUpdateRequestDto)))
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_VALIDATION_FAILED));
  }
}
