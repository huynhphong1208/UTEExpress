package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO phản hồi sau khi tạo đơn hàng mới (UC04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả tạo đơn hàng thành công")
public class TaoDonHangResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Cước phí vận chuyển", example = "35000")
    private BigDecimal phiVanChuyen;

    @Schema(description = "Tiền thu hộ COD", example = "150000")
    private BigDecimal cod;

    @Schema(description = "Tổng tiền người gửi cần thanh toán ngay", example = "35000")
    private BigDecimal tongCuocCanThanhToan;

    @Schema(description = "Phương thức thanh toán cước", example = "Chuyển khoản")
    private String phuongThucThanhToan;

    @Schema(description = "Trạng thái thanh toán cước", example = "Đã thanh toán")
    private String trangThaiThanhToan;

    @Schema(description = "Thời gian tạo đơn")
    private LocalDateTime thoiGianTao;

    @Schema(description = "Thông báo hướng dẫn thanh toán", example = "Đã ghi nhận thanh toán chuyển khoản thành công")
    private String thongBaoThanhToan;
}
