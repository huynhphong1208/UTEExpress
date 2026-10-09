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
     * Bắt JpaSystemException và DataAccessException - phát sinh từ Stored Procedure gọi qua Spring Data JPA.
     */
    @ExceptionHandler({org.springframework.orm.jpa.JpaSystemException.class, org.springframework.dao.DataAccessException.class})
    public ResponseEntity<ApiResponse<Void>> handleJpaAndDataAccessException(Exception ex) {
        String message = extractMessage(ex.getMessage());
        if (ex.getCause() != null) {
            String causeMsg = extractMessage(ex.getCause().getMessage());
            if (causeMsg != null && !causeMsg.isBlank()) {
                message = causeMsg;
            }
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Bắt RuntimeException (bao gồm lỗi nghiệp vụ từ Service).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        String message = extractMessage(ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không xác định");
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
                .body(ApiResponse.error("Lỗi hệ thống: " + extractMessage(ex.getMessage())));
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

        String message = rawMessage;
        int errorIdx = message.indexOf("ERROR: ");
        if (errorIdx >= 0) {
            message = message.substring(errorIdx + 7);
        }

        int newlineIdx = message.indexOf('\n');
        if (newlineIdx > 0) {
            message = message.substring(0, newlineIdx);
        }

        int eolIdx = message.indexOf("<EOL>");
        if (eolIdx > 0) {
            message = message.substring(0, eolIdx);
        }

        if (message.endsWith("]")) {
            message = message.substring(0, message.length() - 1);
        }

        return message.trim();
    }
}
