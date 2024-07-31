package com.maveric.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDto {

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotNull(message = "Post ID is required")
    private Long postId;

    private Long parentId;
}
