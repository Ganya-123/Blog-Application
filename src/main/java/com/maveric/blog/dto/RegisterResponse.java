package com.maveric.blog.dto;

import com.maveric.blog.entity.Avatar;
import com.maveric.blog.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
  private String fullName;
  private String email;
  private String mobileNumber;
  private Role role;
  private String bio;
  private Avatar avatar;
}
