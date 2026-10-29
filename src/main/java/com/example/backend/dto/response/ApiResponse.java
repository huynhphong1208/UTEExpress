package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO response chung - bọc mọi API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chung của API")
public class ApiResponse<T> {

    @Schema(description = "Trạng thái thành công", example = "true")
    private boolean success;

    @Schema(description = "Thông điệp kết quả", example = "Thao tác thành công")
    private String message;

    @Schema(description = "Dữ liệu trả về")
    private T data;

    @Schema(description = "Mã trạng thái HTTP", example = "200")
    @Builder.Default
    private Integer status = 200;

    @Schema(description = "Tên lỗi hoặc chi tiết", example = "OK")
    private String error;

    @Schema(description = "Thời gian phản hồi")
    @Builder.Default
    private java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(400)
                .error("Bad Request")
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String error, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}
