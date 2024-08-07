package com.maveric.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.*;
import com.maveric.blog.entity.Avatar;
import com.maveric.blog.entity.Role;
import com.maveric.blog.exception.EmailAlreadyExistsException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticationService authenticationService;

  @Autowired private ObjectMapper objectMapper;

  private RegisterRequest registerRequest;
  private AuthenticationRequest authenticationRequest;
  private ForgotPassword forgotPassword;

  @BeforeEach
  void setUp() {
    registerRequest = new RegisterRequest();
    registerRequest.setFullName("John Doe");
    registerRequest.setEmail("john.doe@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setRole(Role.WRITE);
    registerRequest.setBio("Hello, I'm John");
    registerRequest.setAvatar(Avatar.ALIEN);
    registerRequest.setMobileNumber("1234567890");

    authenticationRequest = new AuthenticationRequest();
    authenticationRequest.setEmail("john.doe@example.com");
    authenticationRequest.setPassword("password123");

    forgotPassword = new ForgotPassword();
    forgotPassword.setEmailId("john.doe@example.com");
    forgotPassword.setMobileNumber("1234567890");
    forgotPassword.setNewPassword("newpassword123");
    forgotPassword.setConfirmPassword("newpassword123");
  }

  @Test
  void testRegister_Success() throws Exception {
    RegisterResponse registerResponse = new RegisterResponse();
    registerResponse.setEmail("john.doe@example.com");

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("john.doe@example.com"));
  }

  @Test
  void testRegister_EmailAlreadyExists() throws Exception {
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException(Constants.EMAIL_EXISTS));

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(jsonPath("$.message").value(Constants.EMAIL_EXISTS));
  }

  @Test
  void testAuthenticateRequest_Success() throws Exception {
    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setToken("valid-jwt-token");
    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
        .thenReturn(authenticationResponse);

    mockMvc
        .perform(
            post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("valid-jwt-token"));
  }

  @Test
  void testAuthenticateRequest_UserNotFound() throws Exception {
    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));
    mockMvc
        .perform(
            post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_NOT_FOUND));
  }

  @Test
  void testForgotPassword_Success() throws Exception {
    when(authenticationService.forgotPassword(any(ForgotPassword.class)))
        .thenReturn(Constants.PASSWORD_RESET_SUCCESS);
    mockMvc
        .perform(
            put("/api/v1/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPassword)))
        .andExpect(status().isOk())
        .andExpect(content().string(Constants.PASSWORD_RESET_SUCCESS));
  }

  @Test
  void testForgotPassword_UserNotFound() throws Exception {
    when(authenticationService.forgotPassword(any(ForgotPassword.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    mockMvc
        .perform(
            put("/api/v1/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPassword)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(Constants.AUTHOR_NOT_FOUND));
  }

  @Test
  void testForgotPassword_MobileNumberMismatch() throws Exception {
    forgotPassword.setMobileNumber("wrong_mobile_number");

    String errorMessage = "Mobile number does not match";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    mockMvc
        .perform(
            put("/api/v1/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPassword)))
        .andExpect(status().isOk())
        .andExpect(content().string(errorMessage));
  }

  @Test
  void testForgotPassword_NewPasswordMismatch() throws Exception {
    forgotPassword.setConfirmPassword("differentpassword");

    String errorMessage = "New password and confirm password do not match";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    mockMvc
        .perform(
            put("/api/v1/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPassword)))
        .andExpect(status().isOk())
        .andExpect(content().string(errorMessage));
  }

  @Test
  void testForgotPassword_SameAsCurrentPassword() throws Exception {
    String errorMessage = "New password cannot be the same as the current password";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    mockMvc
        .perform(
            put("/api/v1/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPassword)))
        .andExpect(status().isOk())
        .andExpect(content().string(errorMessage));
  }
}
