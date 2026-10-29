package com.example.backend.exception;

import com.example.backend.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý exception tập trung cho toàn bộ ứng dụng:
 * Hỗ trợ validation, bảo mật (JWT/Role), Business logic và lỗi Stored Procedure / Trigger PostgreSQL.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // --- Validation @Valid ---
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Dữ liệu đầu vào không hợp lệ")
                .data(errors)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // --- EntityNotFoundException (404) ---
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(jakarta.persistence.EntityNotFoundException ex) {
        log.warn("EntityNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage()));
    }

    // --- BusinessException ---
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getStatusCode(), "Business Error", ex.getMessage()));
    }

    // --- Spring Security Authentication ---
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        String message;
        if (ex instanceof BadCredentialsException) {
            message = "Tên đăng nhập hoặc mật khẩu không chính xác";
        } else if (ex instanceof LockedException) {
            message = "Tài khoản đã bị khóa";
        } else if (ex instanceof DisabledException) {
            message = "Tài khoản đã bị vô hiệu hóa";
        } else {
            message = "Xác thực thất bại: " + ex.getMessage();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", message));
    }

    // --- AccessDeniedException (403) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Forbidden", "Bạn không có quyền thực hiện thao tác này"));
    }

    // --- DataIntegrityViolationException (Trigger / Procedure PostgreSQL) ---
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = extractDatabaseMessage(ex);
        log.warn("DataIntegrityViolationException: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    // --- SQLException trực tiếp ---
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException ex) {
        String message = extractMessage(ex.getMessage());
        log.warn("SQLException: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    // --- JpaSystemException & DataAccessException (Stored Procedure qua JPA/Jdbc) ---
    @ExceptionHandler({JpaSystemException.class, DataAccessException.class})
    public ResponseEntity<ApiResponse<Void>> handleJpaAndDataAccessException(Exception ex) {
        String message = extractMessage(ex.getMessage());
        if (ex.getCause() != null) {
            String causeMsg = extractMessage(ex.getCause().getMessage());
            if (causeMsg != null && !causeMsg.isBlank()) {
                message = causeMsg;
            }
        }
        log.warn("DataAccessException: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    // --- IllegalArgumentException ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage()));
    }

    // --- Tất cả exception còn lại ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Lỗi hệ thống không xác định: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "Lỗi hệ thống: " + extractMessage(ex.getMessage())));
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
