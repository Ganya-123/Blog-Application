package com.maveric.blog.dto;

import lombok.Data;

@Data
public class PostResponseDto {

  private Long postId;
  private String title;
  private String content;
  private Long authorId;
  private Long categoryId;
  private boolean featured;
}
