package com.maveric.blog.controller;

import com.maveric.blog.dto.*;
import com.maveric.blog.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisterResponse registerResponse = authenticationService.register(request);
    return ResponseEntity.status(201).body(registerResponse);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticateRequest(
      @Valid @RequestBody AuthenticationRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @PutMapping("/forgotPassword")
  public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPassword forgotPassword) {
    return ResponseEntity.ok(authenticationService.forgotPassword(forgotPassword));
  }
}
