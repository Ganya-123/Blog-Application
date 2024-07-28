package com.maveric.blog.dto;

import lombok.Data;

@Data
public class PostRequestDto {

  private String title;
  private String content;
  private Long authorId;
  private Long categoryId;
  private boolean featured;
}
