package com.maveric.blog.service;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CommentRequestDto;
import com.maveric.blog.dto.CommentResponseDto;
import com.maveric.blog.entity.Comment;
import com.maveric.blog.entity.Post;
import com.maveric.blog.entity.User;
import com.maveric.blog.exception.*;
import com.maveric.blog.repository.CommentRepository;
import com.maveric.blog.repository.PostRepository;
import com.maveric.blog.repository.UserRepository;
import com.maveric.blog.security.JwtService;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {


    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtService jwtService;

    public CommentResponseDto createComment(CommentRequestDto requestDto, String token) {
        Long tokenUserId = jwtService.extractUserId(token.substring(7));
        if (!requestDto.getAuthorId().equals(tokenUserId)) {
            throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
        }
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException(Constants.AUTHOR_NOT_FOUND));

        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(Constants.POST_NOT_FOUND));

        if (!post.getAuthor().getId().equals(requestDto.getAuthorId())) {
            throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
        }

        if (!post.isPublished()) {
            throw new PublishPostException(Constants.POST_NOT_PUBLISHED);
        }

        Comment parentComment = null;
        if (requestDto.getParentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException(Constants.PARENT_COMMENT_NOT_FOUND));

            if (!parentComment.getPost().getPostId().equals(requestDto.getPostId())) {
                throw new CommentNotFoundException(Constants.PARENT_COMMENT_NOT_BELONGS_TO_POST);
            }
        }

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setContent(requestDto.getContent());
        comment.setParent(parentComment);
        comment.setCreatedAt(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return convertToResponseDto(savedComment);
    }


    @Transactional
    public CommentResponseDto editComment(Long commentId, CommentRequestDto requestDto, String token) {
        Long tokenUserId = jwtService.extractUserId(token.substring(7));
        if (!requestDto.getAuthorId().equals(tokenUserId)) {
            throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!comment.getAuthor().getId().equals(tokenUserId)) {
            throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
        }
        comment.setContent(requestDto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return convertToResponseDto(updatedComment);
    }

    public String deleteComment(Long commentId, Long authorId, String token) {
        Long tokenUserId = jwtService.extractUserId(token.substring(7));

        if (!authorId.equals(tokenUserId)) {
            throw new AuthorValidationException(Constants.AUTHOR_VALIDATION_FAILED);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(Constants.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getId().equals(tokenUserId)) {
            throw new AuthorValidationException(Constants.AUTHOR_IS_DIFFERENT);
        }

        commentRepository.delete(comment);

        return Constants.COMMENT_DELETE_SUCCESS;
    }

    public String adminDelete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(Constants.COMMENT_NOT_FOUND));
        commentRepository.delete(comment);

        return Constants.COMMENT_DELETE_SUCCESS;
    }

    private CommentResponseDto convertToResponseDto(Comment comment) {
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(comment.getId());
        responseDto.setContent(comment.getContent());
        responseDto.setAuthorId(comment.getAuthor().getId());
        responseDto.setAuthorName(comment.getAuthor().getFullName());
        responseDto.setPostId(comment.getPost().getPostId());
        responseDto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        responseDto.setReplies(comment.getReplies() != null ?
                comment.getReplies().stream().map(this::convertToResponseDto).collect(Collectors.toList()) : null);
        responseDto.setCreatedAt(comment.getCreatedAt());
        responseDto.setUpdatedAt(comment.getUpdatedAt());
        return responseDto;
    }
}
