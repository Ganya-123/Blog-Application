package com.maveric.blog.service;

import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.entity.Category;
import com.maveric.blog.entity.Post;
import com.maveric.blog.entity.User;
import com.maveric.blog.exceptions.AuthorValidationException;
import com.maveric.blog.exceptions.CategoryNotFoundException;
import com.maveric.blog.exceptions.PostNotFoundException;
import com.maveric.blog.exceptions.UserNotFoundException;
import com.maveric.blog.repository.CategoryRepository;
import com.maveric.blog.repository.PostRepository;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.utils.Constants;
import com.maveric.blog.utils.Converter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final JwtService jwtService;

  public PostResponseDto createPost(PostRequestDto postRequestDto, String token) {
    Long tokenUserId = jwtService.extractUserId(token.substring(7));
    if (postRequestDto.getAuthorId() != tokenUserId) {
      throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
    }
    User author =
        userRepository
            .findById(postRequestDto.getAuthorId())
            .orElseThrow(() -> new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

    Category category = null;
    if (postRequestDto.getCategoryId() != null) {
      category =
          categoryRepository
              .findById(postRequestDto.getCategoryId())
              .orElseThrow(() -> new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));
    }
    Post post = Converter.fromDto(postRequestDto);
    post.setAuthor(author);
    post.setCategory(category);
    post.setCreatedAt(LocalDateTime.now());
    post = postRepository.save(post);

    return Converter.toResponseDto(post);
  }

  public PostResponseDto updatePost(Long postId, PostRequestDto dto, String token) {
    Long tokenUserId = jwtService.extractUserId(token.substring(7));
    if (dto.getAuthorId() != tokenUserId) {
      throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
    }

    Post existingPost =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));
    if (!existingPost.getAuthor().getId().equals(dto.getAuthorId())) {
      throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
    }

    Category category = null;
    if (dto.getCategoryId() != null) {
      category =
          categoryRepository
              .findById(dto.getCategoryId())
              .orElseThrow(() -> new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));
    }
    existingPost.setTitle(dto.getTitle());
    existingPost.setContent(dto.getContent());
    existingPost.setCategory(category);
    existingPost.setUpdatedAt(LocalDateTime.now());
    existingPost.setDraft(true);
    existingPost.setPublished(false);
    existingPost.setFeatured(dto.isFeatured());
    Post updatedPost = postRepository.save(existingPost);
    return Converter.toResponseDto(updatedPost);
  }
}
