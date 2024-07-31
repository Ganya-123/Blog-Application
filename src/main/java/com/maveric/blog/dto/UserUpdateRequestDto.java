package com.maveric.blog.dto;

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
    private String mobileNumber;
}