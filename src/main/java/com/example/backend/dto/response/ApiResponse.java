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

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
