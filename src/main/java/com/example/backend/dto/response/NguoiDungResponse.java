package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO thông tin tài khoản người dùng (cho Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tài khoản người dùng")
public class NguoiDungResponse {

    @Schema(description = "Mã người dùng", example = "ND000001")
    private String maNd;

    @Schema(description = "Tên đăng nhập", example = "0901234567")
    private String tenDangNhap;

    @Schema(description = "Mã vai trò", example = "KHACH_HANG")
    private String maVaiTro;

    @Schema(description = "Tên vai trò", example = "Khách hàng")
    private String tenVaiTro;

    @Schema(description = "Trạng thái tài khoản", example = "Hoạt động")
    private String trangThai;

    @Schema(description = "Họ và tên (nếu có)")
    private String hoTen;

    @Schema(description = "Email (nếu có)")
    private String email;

    @Schema(description = "SĐT (nếu có)")
    private String sdt;
}
