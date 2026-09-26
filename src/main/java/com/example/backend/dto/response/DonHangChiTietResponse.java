package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO chi tiết đơn hàng cho khách hàng (UC05)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết đơn hàng của khách hàng")
public class DonHangChiTietResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Ngày tạo")
    private LocalDateTime ngayTao;

    @Schema(description = "Tên người nhận", example = "Trần Thị B")
    private String tenNguoiNhan;

    @Schema(description = "SĐT người nhận", example = "0987654321")
    private String sdtNhan;

    @Schema(description = "Địa chỉ lấy hàng")
    private String diaChiLay;

    @Schema(description = "Địa chỉ giao hàng")
    private String diaChiGiao;

    @Schema(description = "Bưu cục gửi")
    private String tenKhoGui;

    @Schema(description = "Bưu cục nhận")
    private String tenKhoNhan;

    @Schema(description = "Mã trạng thái", example = "TT03")
    private String maTt;

    @Schema(description = "Trạng thái", example = "Đang vận chuyển")
    private String tenTrangThai;

    @Schema(description = "Số kiện hàng", example = "2")
    private Long soKien;

    @Schema(description = "Tổng khối lượng (kg)", example = "5.0")
    private BigDecimal tongKg;

    @Schema(description = "Phí vận chuyển (VNĐ)", example = "45000")
    private BigDecimal phiVanChuyen;

    @Schema(description = "Tiền COD (VNĐ)", example = "150000")
    private BigDecimal cod;

    @Schema(description = "Tổng thu (VNĐ)", example = "195000")
    private BigDecimal tongThu;
}
