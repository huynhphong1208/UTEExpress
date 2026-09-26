package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO Step 2 - Xác thực OTP và hoàn tất đăng ký
 */
@Data
@Schema(description = "Xác thực OTP và hoàn tất đăng ký")
public class RegisterVerifyRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    @Schema(description = "Số điện thoại", example = "0901234567")
    private String sdt;

    @Email(message = "Email không hợp lệ")
    @Schema(description = "Email", example = "user@gmail.com")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    @Schema(description = "Mật khẩu", example = "123456@Ute")
    private String matKhau;

    @NotBlank(message = "Mã OTP không được để trống")
    @Schema(description = "Mã OTP 6 chữ số nhận qua email", example = "123456")
    private String otp;
}
