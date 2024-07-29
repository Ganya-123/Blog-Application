package com.maveric.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

  private String emailId;
  private String currentPassword;
  private String newPassword;
  private String confirmPassword;
}
