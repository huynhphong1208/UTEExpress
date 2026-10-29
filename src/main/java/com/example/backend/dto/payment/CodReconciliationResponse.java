package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Báo cáo tổng hợp đối soát COD Admin (Mục 4.2.22)")
public class CodReconciliationResponse {

    @Schema(description = "Tổng cước phí vận chuyển đã thu (VNĐ)", example = "12500000")
    private BigDecimal tongCuocPhiDaThu;

    @Schema(description = "Tổng tiền COD chờ đối soát / chờ chi trả cho chủ hàng (VNĐ)", example = "35000000")
    private BigDecimal tongCodChoDoiSoat;

    @Schema(description = "Tổng tiền COD đã hoàn tất chi trả cho chủ hàng (VNĐ)", example = "28000000")
    private BigDecimal tongCodDaHoanTat;

    @Schema(description = "Tổng số lượng giao dịch COD", example = "45")
    private int tongSoGiaoDichCod;

    @Schema(description = "Danh sách chi tiết các đơn hàng và giao dịch COD")
    private List<CodTransactionDto> danhSachGiaoDich;
}
