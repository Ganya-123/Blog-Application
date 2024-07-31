package com.maveric.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class PostRequestDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Author ID cannot be null")
    @Positive(message = "Author ID must be a positive number")
    private Long authorId;

    @NotNull(message = "Category ID cannot be null")
    @Positive(message = "Category ID must be a positive number")
    private Long categoryId;

    private boolean featured;
}
