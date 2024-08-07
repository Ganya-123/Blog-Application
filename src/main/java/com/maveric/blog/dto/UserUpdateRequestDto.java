package com.maveric.blog.dto;

import com.maveric.blog.entity.Avatar;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

  private String fullName;
  private String bio;
  private Avatar avatar;

  @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits")
  private String mobileNumber;
}
