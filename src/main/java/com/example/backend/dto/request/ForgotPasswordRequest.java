package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO quên mật khẩu - gửi OTP qua email
 */
@Data
@Schema(description = "Yêu cầu quên mật khẩu - gửi OTP")
public class ForgotPasswordRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    @Schema(description = "Số điện thoại tài khoản", example = "0901234567")
    private String sdt;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Schema(description = "Email xác thực (phải khớp với email đã đăng ký)", example = "user@gmail.com")
    private String email;
}
