package com.maveric.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
  private Long postId;

  private String title;

  private String content;

  private Long authorId;

  private Long categoryId;
  private boolean featured;
}
