package com.maveric.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPassword {

  @NotBlank(message = "Email ID is required")
  private String emailId;

  @NotBlank(message = "Mobile number is required")
  private String mobileNumber;

  @NotBlank(message = "New password is required")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;
}
