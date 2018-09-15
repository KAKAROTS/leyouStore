package com.leyou.common.exception;

import org.springframework.http.HttpStatus;

public class LyException extends RuntimeException {
    private HttpStatus httpStatus;

    public LyException(String message) {
        super(message);
    }

    public LyException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public LyException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
