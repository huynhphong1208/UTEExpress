package com.example.backend.controller;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xác thực: Đăng nhập, Đăng ký (OTP), Quên mật khẩu
 */
@Tag(name = "Auth", description = "API Xác thực - Đăng nhập, Đăng ký, Quên mật khẩu")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    // ---- UC02: Đăng nhập ----
    @Operation(summary = "Đăng nhập", description = "Nhận username + password, trả về JWT Token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", resp));
    }

    // ---- UC01 Step 1: Gửi OTP đăng ký ----
    @Operation(summary = "Gửi OTP đăng ký",
               description = "Kiểm tra SĐT / Email chưa trùng → Sinh OTP → Gửi về Email khách hàng")
    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        authService.sendRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đã được gửi đến email " + request.getEmail() + ". Vui lòng kiểm tra hộp thư."));
    }

    // ---- UC01 Step 2: Xác thực OTP và hoàn tất đăng ký ----
    @Operation(summary = "Xác thực OTP & hoàn tất đăng ký",
               description = "Nhận form đăng ký + mã OTP → Tạo tài khoản KHACH_HANG")
    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<String>> registerVerify(
            @Valid @RequestBody RegisterVerifyRequest request) {
        String maKh = authService.registerVerify(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", maKh));
    }

    // ---- Quên mật khẩu: Gửi OTP ----
    @Operation(summary = "Quên mật khẩu - Gửi OTP",
               description = "Gửi OTP về email để đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn"));
    }

    // ---- Quên mật khẩu: Xác thực OTP và đặt lại mật khẩu ----
    @Operation(summary = "Đặt lại mật khẩu",
               description = "Xác thực OTP và cập nhật mật khẩu mới")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam @NotBlank String sdt,
            @RequestParam @NotBlank String otp,
            @RequestParam @NotBlank @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự") String matKhauMoi) {
        authService.resetPassword(sdt, otp, matKhauMoi);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."));
    }
}
