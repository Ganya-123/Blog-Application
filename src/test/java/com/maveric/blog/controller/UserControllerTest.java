package com.maveric.blog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.maveric.blog.dto.ChangePassword;
import com.maveric.blog.dto.RegisterResponse;
import com.maveric.blog.dto.UserUpdateRequestDto;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @InjectMocks private UserController userController;

  @Mock private AuthenticationService authenticationService;

  @Test
  void testChangePassword_Success() {
    ChangePassword changePassword = new ChangePassword();
    String token = "Bearer valid_token";
    String expectedResponse = "Password changed successfully";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenReturn(expectedResponse);

    ResponseEntity<String> response = userController.changePassword(changePassword, token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void testChangePassword_UserNotFound() {
    ChangePassword changePassword = new ChangePassword();
    String token = "Bearer valid_token";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenThrow(new UserNotFoundException("User not found"));

    Exception exception =
        assertThrows(
            UserNotFoundException.class,
            () -> {
              userController.changePassword(changePassword, token);
            });

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void testChangePassword_AuthorizationFailed() {
    ChangePassword changePassword = new ChangePassword();
    String token = "Bearer invalid_token";

    when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
        .thenThrow(new AuthorValidationException("Authorization failed"));

    Exception exception =
        assertThrows(
            AuthorValidationException.class,
            () -> {
              userController.changePassword(changePassword, token);
            });

    assertEquals("Authorization failed", exception.getMessage());
  }

  @Test
  void testUpdateUser_Success() {
    UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();
    RegisterResponse expectedResponse = new RegisterResponse();

    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenReturn(expectedResponse);

    ResponseEntity<RegisterResponse> response =
        userController.updateUser(1L, updateRequest, "Bearer valid_token");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
  }

  @Test
  void testUpdateUser_UserNotFound() {
    UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();

    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenThrow(new UserNotFoundException("User not found"));

    Exception exception =
        assertThrows(
            UserNotFoundException.class,
            () -> {
              userController.updateUser(1L, updateRequest, "Bearer valid_token");
            });

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void testUpdateUser_AuthorizationFailed() {
    UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();

    when(authenticationService.updateUser(
            anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
        .thenThrow(new AuthorValidationException("Authorization failed"));

    Exception exception =
        assertThrows(
            AuthorValidationException.class,
            () -> {
              userController.updateUser(1L, updateRequest, "Bearer invalid_token");
            });

    assertEquals("Authorization failed", exception.getMessage());
  }
}
