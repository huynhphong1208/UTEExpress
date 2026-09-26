package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận request đăng nhập
 */
@Data
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Schema(description = "Tên đăng nhập (SĐT)", example = "0901234567")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu", example = "123456@Ute")
    private String matKhau;
}
