package com.maveric.blog.controller;

import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.PostResponseDto;
import com.maveric.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {
  @Autowired private final PostService postService;

  @PostMapping("/post")
  public ResponseEntity<PostResponseDto> createPost(
      @RequestBody PostRequestDto postRequestDto, @RequestHeader("Authorization") String token) {
    PostResponseDto createdPost = postService.createPost(postRequestDto, token);
    return ResponseEntity.status(201).body(createdPost);
  }

  @PutMapping("/{id}/post")
  public ResponseEntity<PostResponseDto> updatePost(
      @PathVariable Long id,
      @RequestBody PostRequestDto post,
      @RequestHeader("Authorization") String token) {

    PostResponseDto updatedPost = postService.updatePost(id, post, token);
    return ResponseEntity.ok(updatedPost);
  }
}
