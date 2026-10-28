package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu cập nhật thông tin cá nhân (Họ tên, SĐT, Email, Địa chỉ)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin cá nhân")
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Schema(description = "Họ và tên", example = "Nguyễn Văn An")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    @Schema(description = "Số điện thoại (10 chữ số)", example = "0901234567")
    private String sdt;

    @Schema(description = "Email / Gmail", example = "user@gmail.com")
    private String email;

    @Schema(description = "Địa chỉ (Dành cho khách hàng)", example = "12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM")
    private String diaChi;
}
