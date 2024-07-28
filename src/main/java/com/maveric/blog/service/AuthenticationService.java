package com.maveric.blog.service;

import com.maveric.blog.dto.AuthenticationRequest;
import com.maveric.blog.dto.AuthenticationResponse;
import com.maveric.blog.dto.RegisterRequest;
import com.maveric.blog.dto.RegisterResponse;
import com.maveric.blog.entity.User;
import com.maveric.blog.repository.UserRepository;
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
}
