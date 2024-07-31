package com.maveric.blog.exception;

public class PublishPostException extends RuntimeException {
  public PublishPostException() {
    super();
  }

  public PublishPostException(String message) {
    super(message);
  }

  public PublishPostException(String message, Throwable cause) {
    super(message, cause);
  }

  public PublishPostException(Throwable cause) {
    super(cause);
  }

  protected PublishPostException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
