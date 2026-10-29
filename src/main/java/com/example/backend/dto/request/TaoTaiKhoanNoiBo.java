package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO Admin tạo tài khoản nội bộ (UC15)
 */
@Data
@Schema(description = "Thông tin tạo tài khoản nội bộ")
public class TaoTaiKhoanNoiBo {

    @NotBlank(message = "Họ tên không được để trống")
    @Schema(description = "Họ và tên", example = "Lê Văn C")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    @Schema(description = "Số điện thoại", example = "0911111111")
    private String sdt;

    @Email(message = "Email không hợp lệ")
    @Schema(description = "Email", example = "nhanvien@uteexpress.vn")
    private String email;

    @NotBlank(message = "Vai trò không được để trống")
    @Pattern(regexp = "NVBC|NVDP|TAI_XE", message = "Vai trò phải là NVBC, NVDP hoặc TAI_XE")
    @Schema(description = "Vai trò: NVBC | NVDP | TAI_XE", example = "NVBC",
            allowableValues = {"NVBC", "NVDP", "TAI_XE"})
    private String maVaiTro;

    @Schema(description = "Mã kho (bắt buộc với NVBC/NVDP)", example = "KHO0001")
    private String maKho;

    @Schema(description = "Số bằng lái (bắt buộc với TAI_XE)", example = "B2-123456")
    private String bangLai;
}
