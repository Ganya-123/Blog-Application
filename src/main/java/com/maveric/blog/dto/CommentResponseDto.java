package com.maveric.blog.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CommentResponseDto {
  private Long id;
  private String content;
  private Long authorId;
  private String authorName;
  private Long postId;
  private Long parentId;
  private List<CommentResponseDto> replies;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
