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

    @Schema(description = "Loại token", example = "Bearer")
    private final String tokenType = "Bearer";

    @Schema(description = "Mã người dùng", example = "ND000001")
    private String maNd;

    @Schema(description = "Tên đăng nhập", example = "0901234567")
    private String tenDangNhap;

    @Schema(description = "Vai trò", example = "KHACH_HANG")
    private String vaiTro;

    @Schema(description = "Tên vai trò", example = "Khách hàng")
    private String tenVaiTro;
}
