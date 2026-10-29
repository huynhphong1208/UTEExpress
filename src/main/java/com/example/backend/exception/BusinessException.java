package com.example.backend.exception;

/**
 * Exception tùy chỉnh cho các lỗi nghiệp vụ
 */
public class BusinessException extends RuntimeException {

    private final int statusCode;

    public BusinessException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
