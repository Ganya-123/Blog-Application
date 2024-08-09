package com.maveric.blog.controller;

import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PostMapping("/create")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<CommentResponseDto> createComment(
      @Valid @RequestBody CommentRequestDto requestDto,
      @RequestHeader("Authorization") String token) {
    CommentResponseDto comment = commentService.createComment(requestDto, token);
    return ResponseEntity.status(201).body(comment);
  }

  @PutMapping("/{commentId}/edit")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<CommentResponseDto> editComment(
      @PathVariable Long commentId,
      @Valid @RequestBody CommentRequestDto requestDto,
      @RequestHeader("Authorization") String token) {
    CommentResponseDto comment = commentService.editComment(commentId, requestDto, token);
    return ResponseEntity.ok(comment);
  }

  @DeleteMapping("author/{authorId}/comment/{commentId}")
  @PreAuthorize("hasAuthority('WRITE')")
  public ResponseEntity<String> deleteComment(
      @PathVariable Long commentId,
      @PathVariable Long authorId,
      @Valid @RequestHeader("Authorization") String token) {
    String result = commentService.deleteComment(commentId, authorId, token);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{commentId}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<String> adminDelete(@PathVariable Long commentId) {
    String result = commentService.adminDelete(commentId);
    return ResponseEntity.ok(result);
  }
}
