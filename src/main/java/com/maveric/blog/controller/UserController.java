package com.maveric.blog.controller;

import com.maveric.blog.dto.ChangePassword;
import com.maveric.blog.dto.RegisterResponse;
import com.maveric.blog.dto.UserUpdateRequestDto;
import com.maveric.blog.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final AuthenticationService authenticationService;

  @PutMapping("/PasswordChange")
  @PreAuthorize("hasAuthority('WRITE') or hasAuthority('READ')")
  public ResponseEntity<String> changePassword(
      @Valid @RequestBody ChangePassword changePassword,
      @RequestHeader("Authorization") String token) {
    return ResponseEntity.ok(authenticationService.changePassword(changePassword, token));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('WRITE') or hasAuthority('READ')")
  public ResponseEntity<RegisterResponse> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserUpdateRequestDto updateRequest,
      @RequestHeader("Authorization") String token) {
    RegisterResponse registerResponse = authenticationService.updateUser(id, updateRequest, token);
    return ResponseEntity.ok(registerResponse);
  }
}
