package com.maveric.blog.exception;

public class CategoryPresentException extends RuntimeException {
  public CategoryPresentException() {
    super();
  }

  public CategoryPresentException(String message) {
    super(message);
  }

  public CategoryPresentException(String message, Throwable cause) {
    super(message, cause);
  }

  public CategoryPresentException(Throwable cause) {
    super(cause);
  }

  protected CategoryPresentException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
