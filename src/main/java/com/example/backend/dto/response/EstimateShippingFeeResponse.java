package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO phản hồi tính cước phí ước tính cho khách hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả ước tính cước phí vận chuyển")
public class EstimateShippingFeeResponse {

    @Schema(description = "Tổng cước phí ước tính (VNĐ)", example = "35000")
    private BigDecimal phiVanChuyen;

    @Schema(description = "Khoảng cách vận chuyển (km)", example = "1700")
    private BigDecimal khoangCachKm;

    @Schema(description = "Khối lượng tính cước (kg)", example = "1.5")
    private BigDecimal khoiLuongTinhCuoc;

    @Schema(description = "Khối lượng quy đổi theo thể tích (kg)", example = "0.6")
    private BigDecimal khoiLuongQuyDoi;

    @Schema(description = "Cùng bưu cục gửi nhận không", example = "false")
    private boolean cungKho;

    @Schema(description = "Thời gian vận chuyển dự kiến (giờ)", example = "36.0")
    private BigDecimal thoiGianDuKienGio;

    @Schema(description = "Tên kho gửi", example = "Bưu cục Thủ Đức (TP.HCM)")
    private String tenKhoGui;

    @Schema(description = "Tên kho nhận", example = "Bưu cục Cầu Giấy (Hà Nội)")
    private String tenKhoNhan;

    @Schema(description = "Ghi chú giải thích cách tính")
    private String ghiChu;
}
