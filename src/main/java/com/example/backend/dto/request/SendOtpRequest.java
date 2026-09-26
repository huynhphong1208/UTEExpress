package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO Step 1 - Gửi OTP đăng ký (kiểm tra trùng trước khi đăng ký)
 */
@Data
@Schema(description = "Thông tin gửi OTP đăng ký tài khoản")
public class SendOtpRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    @Schema(description = "Số điện thoại (dùng làm tên đăng nhập)", example = "0901234567")
    private String sdt;

    @Email(message = "Email không hợp lệ")
    @Schema(description = "Địa chỉ email nhận OTP", example = "user@gmail.com")
    private String email;
}
