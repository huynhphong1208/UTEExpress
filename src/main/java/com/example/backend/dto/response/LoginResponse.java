package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO response sau khi đăng nhập thành công
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin trả về sau đăng nhập")
public class LoginResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Builder.Default
    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Mã người dùng", example = "ND000001")
    private String maNd;

    @Schema(description = "Tên đăng nhập", example = "0901234567")
    private String tenDangNhap;

    @Schema(description = "Vai trò", example = "KHACH_HANG")
    private String vaiTro;

    @Schema(description = "Tên vai trò", example = "Khách hàng")
    private String tenVaiTro;

    @Schema(description = "Họ và tên người dùng", example = "Nguyễn Văn An")
    private String hoTen;

    @Schema(description = "Ảnh đại diện người dùng")
    private String avatarUrl;

    @Schema(description = "Mã đối tượng theo vai trò (maTx, maNv, maKh)", example = "TX001")
    private String maDoiTuong;
}
