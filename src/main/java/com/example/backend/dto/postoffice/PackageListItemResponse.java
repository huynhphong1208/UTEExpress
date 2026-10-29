package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin kiện hàng phân trang cho nhân viên bưu cục (UC09)")
public class PackageListItemResponse {
    @Schema(description = "Mã kiện hàng", example = "KIEN000001")
    private String maKien;

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã kho lưu hiện tại", example = "KHO01")
    private String maKhoHienTai;

    @Schema(description = "Tên kho lưu hiện tại", example = "Bưu cục Thủ Đức")
    private String tenKhoHienTai;

    @Schema(description = "Khối lượng (kg)", example = "1.50")
    private BigDecimal khoiLuong;

    @Schema(description = "Kích thước (Dài x Rộng x Cao)", example = "20x15x10 cm")
    private String kichThuoc;

    private BigDecimal dai;
    private BigDecimal rong;
    private BigDecimal cao;

    @Schema(description = "Loại hàng", example = "Hàng tiêu chuẩn")
    private String loaiHang;
}
