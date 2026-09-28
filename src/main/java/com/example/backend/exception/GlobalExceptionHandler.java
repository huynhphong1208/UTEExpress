package com.example.backend.exception;

import com.example.backend.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * Global Exception Handler.
 * Bắt các lỗi từ PostgreSQL RAISE EXCEPTION (trong Stored Procedure / Trigger)
 * và trả về JSON thân thiện cho Frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt DataIntegrityViolationException - phát sinh từ constraint violations
     * và RAISE EXCEPTION trong trigger / stored procedure.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = extractDatabaseMessage(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Bắt SQLException trực tiếp.
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException ex) {
        String message = extractMessage(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Bắt RuntimeException (bao gồm lỗi nghiệp vụ từ Service).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không xác định";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Bắt tất cả các Exception chưa xử lý.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống: " + ex.getMessage()));
    }

    // ========================== PRIVATE HELPERS ==========================

    /**
     * Trích xuất thông báo lỗi thân thiện từ PostgreSQL exception.
     * PostgreSQL format: "ERROR: <message tiếng Việt>\n  Where: ..."
     */
    private String extractDatabaseMessage(DataIntegrityViolationException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        if (rootCause instanceof SQLException sqlEx) {
            return extractMessage(sqlEx.getMessage());
        }
        return ex.getMessage() != null ? ex.getMessage() : "Lỗi ràng buộc dữ liệu";
    }

    /**
     * Loại bỏ prefix "ERROR:" và các dòng stack trace/context do PostgreSQL thêm vào.
     */
    private String extractMessage(String rawMessage) {
        if (rawMessage == null) return "Lỗi không xác định";

        // PostgreSQL message thường có format: "ERROR: <message>\n  Where: ..."
        String message = rawMessage;

        // Lấy dòng đầu tiên (loại bỏ Where, Context...)
        int newlineIdx = message.indexOf('\n');
        if (newlineIdx > 0) {
            message = message.substring(0, newlineIdx);
        }

        // Loại bỏ prefix "ERROR: " nếu có
        if (message.startsWith("ERROR: ")) {
            message = message.substring(7);
        }

        return message.trim();
    }
}
