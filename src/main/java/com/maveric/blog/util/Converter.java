package com.maveric.blog.util;

import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.entity.Post;

public class Converter {
    public static Post fromDto(PostRequestDto postRequestDto) {
        if (postRequestDto == null) {
            return null;
        }

        Post post = new Post();
        post.setTitle(postRequestDto.getTitle());
        post.setContent(postRequestDto.getContent());
        post.setFeatured(postRequestDto.isFeatured());
        return post;
    }

    public static PostRequestDto toRequestDto(Post post) {
        if (post == null) {
            return null;
        }

        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTitle(post.getTitle());
        postRequestDto.setContent(post.getContent());
        postRequestDto.setFeatured(post.isFeatured());
        postRequestDto.setAuthorId(post.getAuthor() != null ? post.getAuthor().getId() : null);
        postRequestDto.setCategoryId(post.getCategory() != null ? post.getCategory().getId() : null);

        return postRequestDto;
    }

    public static PostResponseDto toResponseDto(Post post) {
        if (post == null) {
            return null;
        }

        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setPostId(post.getPostId());
        postResponseDto.setTitle(post.getTitle());
        postResponseDto.setContent(post.getContent());
        postResponseDto.setFeatured(post.isFeatured());
        postResponseDto.setAuthorId(post.getAuthor() != null ? post.getAuthor().getId() : null);
        postResponseDto.setCategoryId(post.getCategory() != null ? post.getCategory().getId() : null);

        return postResponseDto;
    }
}