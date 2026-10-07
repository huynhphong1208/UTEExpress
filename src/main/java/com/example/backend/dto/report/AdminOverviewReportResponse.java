package com.example.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Báo cáo tổng quan Admin Dashboard (UC17, Mục 4.2.17)")
public class AdminOverviewReportResponse {

    @Schema(description = "Tổng số đơn hàng toàn hệ thống", example = "1250")
    private long tongSoDonHang;

    @Schema(description = "Số đơn đang vận chuyển (TT03, TT04, TT05)", example = "320")
    private long soDonDangVanChuyen;

    @Schema(description = "Số đơn hoàn thành / giao thành công (TT06)", example = "890")
    private long soDonHoanThanh;

    @Schema(description = "Số đơn hủy hoặc trả hàng (TT07, TT08)", example = "40")
    private long soDonHuyTra;

    @Schema(description = "Tổng doanh thu cước vận chuyển (VNĐ)", example = "45000000")
    private BigDecimal tongDoanhThuCuoc;

    @Schema(description = "Tổng tiền COD phát sinh trong hệ thống (VNĐ)", example = "120000000")
    private BigDecimal tongTienCod;

    @Schema(description = "Tổng tiền COD đã thu thành công (VNĐ)", example = "95000000")
    private BigDecimal tongCodDaThu;

    @Schema(description = "Tổng số kiện hàng đang lưu kho và vận chuyển", example = "1560")
    private long tongKienHang;

    @Schema(description = "Chi tiết số lượng đơn theo từng trạng thái cụ thể")
    private List<OrderStatusCountDto> thongKeTheoTrangThai;
}
