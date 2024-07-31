package com.maveric.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassword {

  @NotBlank(message = "Email ID is required")
  private String emailId;

  @NotBlank(message = "Current password is required")
  private String currentPassword;

  @NotBlank(message = "New password is required")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;
}
