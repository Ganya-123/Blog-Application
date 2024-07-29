package com.maveric.blog.controller;

import com.maveric.blog.dto.ChangePasswordDto;
import com.maveric.blog.dto.RegisterResponse;
import com.maveric.blog.dto.UserUpdateRequestDto;
import com.maveric.blog.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final AuthenticationService authenticationService;

  @PutMapping("/PasswordChange")
  public ResponseEntity<String> changePassword(
      @RequestBody ChangePasswordDto changePassword, @RequestHeader("Authorization") String token) {
    return ResponseEntity.ok(authenticationService.changePassword(changePassword, token));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RegisterResponse> updateUser(
      @PathVariable Long id,
      @RequestBody UserUpdateRequestDto updateRequest,
      @RequestHeader("Authorization") String token) {
    RegisterResponse registerResponse = authenticationService.updateUser(id, updateRequest, token);
    return ResponseEntity.ok(registerResponse);
  }
}
