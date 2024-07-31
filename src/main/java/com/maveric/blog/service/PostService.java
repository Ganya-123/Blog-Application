package com.maveric.blog.service;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.entity.Category;
import com.maveric.blog.entity.Post;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.*;
import com.maveric.blog.repository.CategoryRepository;
import com.maveric.blog.repository.PostRepository;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.util.Converter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;
  private final ModelMapper mapper;
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

  public PostResponseDto publishPost(Long postId, Long authorId, String token) {
    Long tokenUserId = jwtService.extractUserId(token.substring(7));
    if (authorId != tokenUserId) {
      throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
    }
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));
    if (post.isPublished()) {
      throw new PublishPostException(Constants.POST_ALREADY_PUBLISHED);
    }
    if (post.getAuthor().getId() != authorId) {
      throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
    }
    post.setPublished(true);
    post.setPublishedAt(LocalDateTime.now());
    post.setDraft(false);
    Post publishedPost = postRepository.save(post);
    return Converter.toResponseDto(post);
  }

  public PostResponseDto unpublishPost(Long postId, Long authorId, String token) {
    Long tokenUserId = jwtService.extractUserId(token.substring(7));
    if (authorId != tokenUserId) {
      throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
    }
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));
    if (!post.isPublished()) {
      throw new PublishPostException(Constants.POST_ALREADY_UNPUBLISHED);
    }
    if (post.getAuthor().getId() != authorId) {
      throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
    }
    post.setPublished(false);
    post.setPublishedAt(null);
    Post unpublishedPost = postRepository.save(post);
    return Converter.toResponseDto(post);
  }

  public String deletePost(Long postId, Long authorId, String token) {
    Long tokenUserId = jwtService.extractUserId(token.substring(7));
    if (authorId != tokenUserId) {
      throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
    }
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));
    if (post.getAuthor().getId() != authorId) {
      throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
    }
    postRepository.deleteById(postId);
    return Constants.POST_DELETE_SUCCESS;
  }

  public String adminDeletePost(Long postId) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));

    postRepository.deleteById(postId);
    return Constants.POST_DELETE_SUCCESS;
  }

  public List<PostResponseDto> getAllPosts() {
    List<Post> posts = postRepository.findAll();
    return posts.stream().map(Converter::toResponseDto).collect(Collectors.toList());
  }
}
