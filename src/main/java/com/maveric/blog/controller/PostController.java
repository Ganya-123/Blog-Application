package com.maveric.blog.controller;

import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {
  private final PostService postService;

  @PostMapping("/post")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<PostResponseDto> createPost(
      @Valid @RequestBody PostRequestDto postRequestDto,
      @RequestHeader("Authorization") String token) {

    PostResponseDto createdPost = postService.createPost(postRequestDto, token);
    return ResponseEntity.status(201).body(createdPost);
  }

  @PutMapping("/post/{id}")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<PostResponseDto> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody PostRequestDto post,
      @RequestHeader("Authorization") String token) {

    PostResponseDto updatedPost = postService.updatePost(id, post, token);
    return ResponseEntity.ok(updatedPost);
  }

  @PutMapping("author/{authorId}/post/{postId}/publish")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<PostResponseDto> publishPost(
      @PathVariable Long postId,
      @PathVariable Long authorId,
      @RequestHeader("Authorization") String token) {
    PostResponseDto publishedPost = postService.publishPost(postId, authorId, token);
    return ResponseEntity.ok(publishedPost);
  }

  @PutMapping("author/{authorId}/post/{postId}/unpublish")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<PostResponseDto> unpublishPost(
      @PathVariable Long postId,
      @PathVariable Long authorId,
      @RequestHeader("Authorization") String token) {
    PostResponseDto unpublishedPost = postService.unpublishPost(postId, authorId, token);
    return ResponseEntity.ok(unpublishedPost);
  }

  @DeleteMapping("author/{authorId}/post/{postId}")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<String> deletePost(
      @PathVariable Long postId,
      @PathVariable Long authorId,
      @RequestHeader("Authorization") String token) {
    String response = postService.deletePost(postId, authorId, token);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{postId}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<String> adminDeletePost(@PathVariable Long postId) {
    String response = postService.adminDeletePost(postId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/posts")
  @PreAuthorize("hasAuthority('WRITE') or hasAuthority('READ')")
  public ResponseEntity<List<PostResponseDto>> getAllPosts() {
    List<PostResponseDto> posts = postService.getAllPosts();
    return ResponseEntity.ok(posts);
  }
}
