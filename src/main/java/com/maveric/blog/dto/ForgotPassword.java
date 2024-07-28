package com.maveric.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPassword {

  private String emailId;
  private String mobileNumber;
  private String newPassword;
  private String confirmPassword;
}
