package com.example.backend.controller;

import com.example.backend.dto.request.TaoTaiKhoanNoiBo;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.NguoiDungResponse;
import com.example.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản trị tài khoản và nhân sự (UC15) - chỉ dành cho ADMIN
 */
@Tag(name = "Admin - Quản lý người dùng",
     description = "API quản trị tài khoản - Yêu cầu role ADMIN (UC15)")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ---- Danh sách tài khoản (phân trang, tìm kiếm, lọc role) ----
    @Operation(
        summary = "Lấy danh sách tài khoản",
        description = "Phân trang, tìm kiếm theo tên/SĐT và lọc theo vai trò"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NguoiDungResponse>>> getAllUsers(
            @Parameter(description = "Từ khóa tìm kiếm (tên đăng nhập)", example = "0901")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo vai trò (ADMIN, NVBC, NVDP, TAI_XE, KHACH_HANG)")
            @RequestParam(required = false) String maVaiTro,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số mục mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<NguoiDungResponse> result = adminUserService.getAllUsers(keyword, maVaiTro, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", result));
    }

    // ---- Tạo tài khoản nội bộ ----
    @Operation(
        summary = "Tạo tài khoản nội bộ (UC15)",
        description = "Cấp tài khoản cho Nhân viên bưu cục (NVBC), Điều phối (NVDP), Tài xế (TAI_XE). " +
                "Mật khẩu mặc định: 123456@Ute"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<NguoiDungResponse>> createAccount(
            @Valid @RequestBody TaoTaiKhoanNoiBo request) {
        NguoiDungResponse result = adminUserService.createInternalAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tài khoản thành công. Mật khẩu mặc định: 123456@Ute", result));
    }

    // ---- Khóa / Mở khóa tài khoản ----
    @Operation(
        summary = "Khóa / Mở khóa tài khoản (UC15)",
        description = "Cập nhật trạng thái tài khoản. Kiểm tra công việc dở dang trước khi khóa."
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> updateStatus(
            @Parameter(description = "Mã người dùng", example = "ND000002")
            @PathVariable String id,
            @Parameter(description = "Trạng thái mới", example = "Bị khóa")
            @RequestParam String trangThai) {
        NguoiDungResponse result = adminUserService.updateStatus(id, trangThai);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái tài khoản thành công", result));
    }

    // ---- Đặt lại mật khẩu mặc định ----
    @Operation(
        summary = "Đặt lại mật khẩu mặc định (UC15)",
        description = "Đặt lại mật khẩu thành '123456@Ute' (BCrypt hash) cho nhân viên nội bộ"
    )
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Mã người dùng", example = "ND000002")
            @PathVariable String id) {
        adminUserService.resetToDefaultPassword(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt lại mật khẩu thành công. Mật khẩu mới: 123456@Ute"));
    }
}
