package com.maveric.blog.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationControllerTest {

  @Mock private AuthenticationService authenticationService;

  @InjectMocks private AuthenticationController authenticationController;

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
  void testRegister_Success() {
    RegisterResponse registerResponse = new RegisterResponse();
    registerResponse.setEmail("john.doe@example.com");

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

    ResponseEntity<RegisterResponse> response = authenticationController.register(registerRequest);

    assertNotNull(response);
    assertEquals(201, response.getStatusCodeValue());
    assertEquals("john.doe@example.com", response.getBody().getEmail());
  }

  @Test
  void testRegister_EmailAlreadyExists() {
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException("Email already exists"));

    EmailAlreadyExistsException exception =
        assertThrows(
            EmailAlreadyExistsException.class,
            () -> authenticationController.register(registerRequest));

    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void testAuthenticateRequest_Success() {
    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setToken("valid-jwt-token");

    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
        .thenReturn(authenticationResponse);

    ResponseEntity<AuthenticationResponse> response =
        authenticationController.authenticateRequest(authenticationRequest);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
  }

  @Test
  void testAuthenticateRequest_UserNotFound() {
    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> authenticationController.authenticateRequest(authenticationRequest));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testForgotPassword_Success() {
    when(authenticationService.forgotPassword(any(ForgotPassword.class)))
        .thenReturn(Constants.PASSWORD_RESET_SUCCESS);

    ResponseEntity<String> response = authenticationController.forgotPassword(forgotPassword);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(Constants.PASSWORD_RESET_SUCCESS, response.getBody());
  }

  @Test
  void testForgotPassword_NewPasswordMismatch() {
    forgotPassword.setConfirmPassword("differentpassword");

    String errorMessage = "New password and confirm password do not match";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    ResponseEntity<String> response = authenticationController.forgotPassword(forgotPassword);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(errorMessage, response.getBody());
  }

  @Test
  void testForgotPassword_UserNotFound() {
    when(authenticationService.forgotPassword(any(ForgotPassword.class)))
        .thenThrow(new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> authenticationController.forgotPassword(forgotPassword));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());
  }

  @Test
  void testForgotPassword_MobileNumberMismatch() {
    forgotPassword.setMobileNumber("wrong_mobile_number");

    String errorMessage = "Mobile number does not match";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    ResponseEntity<String> response = authenticationController.forgotPassword(forgotPassword);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(errorMessage, response.getBody());
  }

  @Test
  void testForgotPassword_SameAsCurrentPassword() {
    String errorMessage = "New password cannot be the same as the current password";
    when(authenticationService.forgotPassword(any(ForgotPassword.class))).thenReturn(errorMessage);

    ResponseEntity<String> response = authenticationController.forgotPassword(forgotPassword);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(errorMessage, response.getBody());
  }
}
