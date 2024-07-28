package com.maveric.blog.exceptions;

public class AuthorValidationException extends RuntimeException {
    public AuthorValidationException() {
    super();
    }

    public AuthorValidationException(String message) {
        super(message);
    }

    public AuthorValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorValidationException(Throwable cause) {
        super(cause);
    }

    protected AuthorValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
