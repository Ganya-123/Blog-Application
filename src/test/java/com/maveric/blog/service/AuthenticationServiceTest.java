package com.maveric.blog.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.*;
import com.maveric.blog.entity.Avatar;
import com.maveric.blog.entity.Role;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.EmailAlreadyExistsException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtService jwtService;

  @Mock private ModelMapper mapper;

  @InjectMocks private AuthenticationService authenticationService;

  private RegisterRequest registerRequest;
  private User user;
  private RegisterResponse registerResponse;
  private AuthenticationRequest authenticationRequest;
  private String token;
  private Long tokenUserId;
  private ChangePassword changePassword;
  private ForgotPassword forgotPassword;
  private UserUpdateRequestDto updateRequest;

  @BeforeEach
  public void setUp() {
    changePassword =
        new ChangePassword("test@example.com", "oldPassword", "newPassword", "newPassword");
    forgotPassword =
        new ForgotPassword("test@example.com", "1234567890", "newPassword", "newPassword");
    registerRequest = createRegisterRequest();
    authenticationRequest = createAuthenticationRequest();
    user = createUser();
    registerResponse = createRegisterResponse();
    token = "Bearer testtoken";
    tokenUserId = 1L;
    updateRequest = createUpdateRequest();
  }

  private UserUpdateRequestDto createUpdateRequest() {
    UserUpdateRequestDto request = new UserUpdateRequestDto();
    request.setFullName("Jane Doe");
    request.setBio("Updated bio");
    request.setAvatar(Avatar.DEFAULT_AVATAR);
    request.setMobileNumber("987-654-3210");
    return request;
  }

  private RegisterRequest createRegisterRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setFullName("John Doe");
    request.setEmail("john.doe@example.com");
    request.setPassword("password123");
    request.setRole(Role.READ);
    request.setBio("Sample bio");
    request.setAvatar(Avatar.DEFAULT_AVATAR);
    request.setMobileNumber("1234567890");
    return request;
  }

  private AuthenticationRequest createAuthenticationRequest() {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");
    return request;
  }

  private User createUser() {
    User user = new User();
    user.setId(1L);
    user.setFullName("John Doe");
    user.setEmail("john.doe@example.com");
    user.setPassword("encodedPassword");
    user.setRole(Role.READ);
    user.setBio("Sample bio");
    user.setAvatar(Avatar.DEFAULT_AVATAR);
    user.setMobileNumber("1234567890");
    return user;
  }

  private RegisterResponse createRegisterResponse() {
    RegisterResponse response = new RegisterResponse();
    response.setFullName("John Doe");
    response.setEmail("john.doe@example.com");
    return response;
  }

  @Test
  void shouldRegisterUserWhenEmailDoesNotExist() {
    when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(mapper.map(any(User.class), eq(RegisterResponse.class))).thenReturn(registerResponse);

    RegisterResponse response = authenticationService.register(registerRequest);

    assertEquals("John Doe", response.getFullName());
    assertEquals("john.doe@example.com", response.getEmail());

    verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
    verify(mapper, times(1)).map(any(User.class), eq(RegisterResponse.class));
  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {
    when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

    EmailAlreadyExistsException exception =
        assertThrows(
            EmailAlreadyExistsException.class,
            () -> authenticationService.register(registerRequest));

    Assertions.assertEquals(Constants.EMAIL_EXISTS, exception.getMessage());

    verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
    verify(userRepository, times(0)).save(any(User.class));
    verify(mapper, times(0)).map(any(User.class), eq(RegisterResponse.class));
  }

  @Test
  void shouldReturnTokenWhenCredentialsAreValid() {
    when(userRepository.findByEmail(authenticationRequest.getEmail()))
        .thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, times(1)).findByEmail(authenticationRequest.getEmail());
    verify(jwtService, times(1)).generateToken(user);
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> authenticationService.authenticate(authenticationRequest));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, times(1)).findByEmail(authenticationRequest.getEmail());
    verify(jwtService, times(0)).generateToken(any(User.class));
  }

  @Test
  void shouldThrowExceptionWhenCredentialsAreInvalid() {
    doThrow(new AuthenticationException("Invalid credentials") {})
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));

    AuthenticationException exception =
        assertThrows(
            AuthenticationException.class,
            () -> authenticationService.authenticate(authenticationRequest));

    assertEquals("Invalid credentials", exception.getMessage());

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, times(0)).findByEmail(authenticationRequest.getEmail());
    verify(jwtService, times(0)).generateToken(any(User.class));
  }

  @Test
  void testChangePassword_Success() {
    when(jwtService.extractUserId(anyString())).thenReturn(tokenUserId);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

    String result = authenticationService.changePassword(changePassword, token);

    assertEquals(Constants.PASSWORD_CHANGE_SUCCESS, result);
    verify(userRepository).save(user);
  }

  @Test
  void testChangePassword_UserNotFound() {
    when(jwtService.extractUserId(anyString())).thenReturn(tokenUserId);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> authenticationService.changePassword(changePassword, token));
  }

  @Test
  void testChangePassword_AuthorValidationFailed() {
    when(jwtService.extractUserId(anyString())).thenReturn(2L);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    assertThrows(
        AuthorValidationException.class,
        () -> authenticationService.changePassword(changePassword, token));
  }

  @Test
  void testChangePassword_NewPasswordMismatch() {
    changePassword.setConfirmPassword("differentNewPassword");
    when(jwtService.extractUserId(anyString())).thenReturn(tokenUserId);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    String result = authenticationService.changePassword(changePassword, token);

    assertEquals("New password and confirm password do not match", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testChangePassword_CurrentPasswordIncorrect() {
    when(jwtService.extractUserId(anyString())).thenReturn(tokenUserId);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    String result = authenticationService.changePassword(changePassword, token);

    assertEquals("Current password is incorrect", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testChangePassword_NewPasswordSameAsCurrent() {
    when(jwtService.extractUserId(anyString())).thenReturn(tokenUserId);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, true);

    String result = authenticationService.changePassword(changePassword, token);

    assertEquals("New password cannot be the same as the current password", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testForgotPassword_Success() {

    forgotPassword.setMobileNumber("1234567890");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

    String result = authenticationService.forgotPassword(forgotPassword);

    assertEquals(Constants.PASSWORD_RESET_SUCCESS, result);
    verify(userRepository).save(user);
    verify(passwordEncoder).encode(anyString());
  }

  @Test
  void testForgotPassword_NewPasswordMismatch() {
    forgotPassword.setConfirmPassword("differentNewPassword");

    String result = authenticationService.forgotPassword(forgotPassword);

    assertEquals("New password and confirm password do not match", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testForgotPassword_UserNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> authenticationService.forgotPassword(forgotPassword));
  }

  @Test
  void testForgotPassword_MobileNumberMismatch() {
    user.setMobileNumber("0987654321");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    String result = authenticationService.forgotPassword(forgotPassword);

    assertEquals("Mobile number does not match", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testForgotPassword_NewPasswordSameAsCurrent() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    String result = authenticationService.forgotPassword(forgotPassword);

    assertEquals("New password cannot be the same as the current password", result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldUpdateUserDetailsSuccessfully() {
    String token = "Bearer token";
    Long userId = 1L;
    user.setFullName("Jane Doe");
    registerResponse.setFullName("Jane Doe");
    when(jwtService.extractUserId(token.substring(7))).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(mapper.map(any(User.class), eq(RegisterResponse.class))).thenReturn(registerResponse);

    RegisterResponse response = authenticationService.updateUser(userId, updateRequest, token);

    assertEquals("Jane Doe", response.getFullName());
    assertEquals("john.doe@example.com", response.getEmail());

    verify(jwtService, times(1)).extractUserId(token.substring(7));
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(user);
    verify(mapper, times(1)).map(user, RegisterResponse.class);
  }

  @Test
  void shouldThrowExceptionWhenTokenUserIdDoesNotMatch() {
    String token = "Bearer token";
    Long userId = 1L;
    Long tokenUserId = 2L;
    when(jwtService.extractUserId(token.substring(7))).thenReturn(tokenUserId);

    AuthorValidationException exception =
        assertThrows(
            AuthorValidationException.class,
            () -> authenticationService.updateUser(userId, updateRequest, token));

    assertEquals(Constants.AUTHOR_VALIDATION_FAILED, exception.getMessage());

    verify(jwtService, times(1)).extractUserId(token.substring(7));
    verify(userRepository, times(0)).findById(userId);
    verify(userRepository, times(0)).save(any(User.class));
    verify(mapper, times(0)).map(any(User.class), eq(RegisterResponse.class));
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound_update() {
    String token = "Bearer token";
    Long userId = 1L;
    when(jwtService.extractUserId(token.substring(7))).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> authenticationService.updateUser(userId, updateRequest, token));

    assertEquals(Constants.AUTHOR_NOT_FOUND, exception.getMessage());

    verify(jwtService, times(1)).extractUserId(token.substring(7));
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(0)).save(any(User.class));
    verify(mapper, times(0)).map(any(User.class), eq(RegisterResponse.class));
  }
}
