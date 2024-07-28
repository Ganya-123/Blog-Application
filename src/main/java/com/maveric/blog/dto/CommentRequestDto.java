package com.maveric.blog.dto;

import lombok.Data;

@Data
public class CommentRequestDto {
  private String content;
  private Long authorId;
  private Long postId;
  private Long parentId;
}
