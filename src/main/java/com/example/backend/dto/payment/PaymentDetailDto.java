package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết một lần thanh toán của đơn hàng")
public class PaymentDetailDto {

    @Schema(description = "Mã giao dịch thanh toán", example = "GD000001")
    private String maTTToan;

    @Schema(description = "Loại khoản thanh toán (PHI_VC hoặc COD)", example = "COD")
    private String loaiKhoan;

    @Schema(description = "Số tiền thanh toán", example = "250000")
    private BigDecimal soTien;

    @Schema(description = "Phương thức thanh toán", example = "TIEN_MAT")
    private String phuongThuc;

    @Schema(description = "Trạng thái thanh toán", example = "DA_THANH_TOAN")
    private String trangThai;

    @Schema(description = "Thời gian thanh toán")
    private LocalDateTime thoiGian;

    @Schema(description = "Người thanh toán / xác nhận", example = "Nguyễn Văn B")
    private String nguoiThanhToan;
}
