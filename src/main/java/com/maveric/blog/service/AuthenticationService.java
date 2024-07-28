package com.maveric.blog.service;

import com.maveric.blog.dto.*;
import com.maveric.blog.entity.User;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final ModelMapper mapper;

  public RegisterResponse register(RegisterRequest request) {
    userRepository
        .findByEmail(request.getEmail())
        .ifPresent(
            user -> {
              throw new RuntimeException("Email already exists");
            });
    User user =
        User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .bio(request.getBio())
            .avatar(request.getAvatar())
            .mobileNumber(request.getMobileNumber())
            .build();
    user = userRepository.save(user);
    return mapper.map(user, RegisterResponse.class);
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      String jwtoken = jwtService.generateToken(user);
      return AuthenticationResponse.builder().token(jwtoken).build();
    } else {
      throw new RuntimeException("User not found");
    }
  }

  public String forgotPassword(ForgotPassword forgotPassword) {
    if (!forgotPassword.getNewPassword().equals(forgotPassword.getConfirmPassword())) {
      return "New password and confirm password do not match";
    }

    User user =
        userRepository
            .findByEmail(forgotPassword.getEmailId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getMobileNumber().equals(forgotPassword.getMobileNumber())) {
      return "Mobile number does not match";
    }

    if (passwordEncoder.matches(forgotPassword.getNewPassword(), user.getPassword())) {
      return "New password cannot be the same as the current password";
    }

    String encodedNewPassword = passwordEncoder.encode(forgotPassword.getNewPassword());
    user.setPassword(encodedNewPassword);
    userRepository.save(user);

    return "Password reset successful";
  }
}
