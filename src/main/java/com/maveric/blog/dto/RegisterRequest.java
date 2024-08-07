package com.maveric.blog.dto;

import com.maveric.blog.entity.Avatar;
import com.maveric.blog.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
      message = "Email format is invalid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Mobile number is required")
  @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits")
  private String mobileNumber;

  private Role role;
  private String bio;
  private Avatar avatar;
}
