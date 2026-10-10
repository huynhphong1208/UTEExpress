package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bảng kê tổng hợp thanh toán và đối soát đơn hàng (UC06)")
public class PaymentSummaryResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Cước phí vận chuyển", example = "35000")
    private BigDecimal phiVanChuyen;

    @Schema(description = "Tiền thu hộ COD", example = "250000")
    private BigDecimal cod;

    @Schema(description = "Tổng số tiền cần thu (Cước VC + COD)", example = "285000")
    private BigDecimal tongSoTienCanThu;

    @Schema(description = "Tổng số tiền đã thanh toán", example = "285000")
    private BigDecimal tongSoTienDaThu;

    @Schema(description = "Cước vận chuyển đã thanh toán chưa", example = "true")
    private boolean phiDaThanhToan;

    @Schema(description = "Tiền COD đã thu chưa", example = "true")
    private boolean codDaThu;

    @Schema(description = "Tình trạng thanh toán tổng thể", example = "Đã thanh toán đủ")
    private String tinhTrangThanhToan;

    @Schema(description = "Mã trạng thái đơn hàng hiện tại", example = "TT06")
    private String maTrangThai;

    @Schema(description = "Tên trạng thái đơn hàng hiện tại", example = "Giao thành công")
    private String tenTrangThai;

    @Schema(description = "Lịch sử các giao dịch thanh toán của đơn hàng")
    private List<PaymentDetailDto> lichSuThanhToan;
}
