package com.maveric.blog.globalexcption;


import com.maveric.blog.dto.ErrorDto;
import com.maveric.blog.exception.*;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorValidationException.class)
    public ResponseEntity<ErrorDto> authorValidationException(
            AuthorValidationException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorDto> categoryNotFoundException(
            CategoryNotFoundException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorDto> commentNotFoundException(
            CommentNotFoundException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorDto> postNotFoundException(
            PostNotFoundException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(PublishPostException.class)
    public ResponseEntity<ErrorDto> publishPostException(
            PublishPostException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> userNotFoundException(
            UserNotFoundException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(CategoryPresentException.class)
    public ResponseEntity<ErrorDto> categoryPresentException(
            CategoryPresentException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> emailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorDto> authorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.UNAUTHORIZED, "Authorization failed", Collections.emptyList());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.UNAUTHORIZED, "Invalid username or password.", Collections.emptyList());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors =
                ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        ErrorDto errorResponse =
                new ErrorDto(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}