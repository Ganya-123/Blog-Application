package com.maveric.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthenticationRequest {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
