package com.maveric.blog.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class CustomLogoutHandler implements LogoutHandler {
  private final JwtTokenService jwtTokenService;

  public CustomLogoutHandler(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
      jwtTokenService.invalidateToken(token);
    }
  }
}
