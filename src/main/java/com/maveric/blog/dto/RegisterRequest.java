package com.maveric.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
  private String fullName;
  private String email;
  private String password;
  private String mobileNumber;
  private Role role;
  private String bio;
  private Avatar avatar;
}
