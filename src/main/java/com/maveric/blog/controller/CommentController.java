package com.maveric.blog.controller;

import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

  @Autowired private CommentService commentService;

  @PostMapping("/create")
  public ResponseEntity<CommentResponseDto> createComment(
      @RequestBody CommentRequestDto requestDto, @RequestHeader("Authorization") String token) {
    CommentResponseDto comment = commentService.createComment(requestDto, token);
    return ResponseEntity.ok(comment);
  }

  @PutMapping("/{commentId}/edit")
  public ResponseEntity<CommentResponseDto> editComment(
      @PathVariable Long commentId,
      @RequestBody CommentRequestDto requestDto,
      @RequestHeader("Authorization") String token) {
    CommentResponseDto comment = commentService.editComment(commentId, requestDto, token);
    return ResponseEntity.ok(comment);
  }

  @DeleteMapping("author/{authorId}/comment/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long commentId,
      @PathVariable Long authorId,
      @RequestHeader("Authorization") String token) {
    commentService.deleteComment(commentId, authorId, token);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentResponseDto> getCommentWithReplies(
      @PathVariable Long commentId, @RequestHeader("Authorization") String token) {
    CommentResponseDto comment = commentService.getCommentWithReplies(commentId);
    return ResponseEntity.ok(comment);
  }
}
