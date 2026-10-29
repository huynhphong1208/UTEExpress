package com.example.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Báo cáo chỉ số chất lượng dịch vụ SLA và xếp hạng bưu cục (Mục 4.2.23)")
public class SlaPerformanceReportResponse {

    @Schema(description = "Tỷ lệ giao hàng thành công toàn hệ thống (%)", example = "96.5")
    private Double tyLeGiaoThanhCong;

    @Schema(description = "Tỷ lệ giao hàng đúng cam kết SLA (%)", example = "94.2")
    private Double tyLeGiaoDungHan;

    @Schema(description = "Tổng số đơn giao thành công (TT06)", example = "890")
    private long tongDonHoanThanh;

    @Schema(description = "Tổng số đơn giao thất bại hoặc hủy (TT07, TT08)", example = "40")
    private long tongDonThatBai;

    @Schema(description = "Danh sách Top các bưu cục có lưu lượng xử lý cao nhất")
    private List<PostOfficePerformanceResponse> topPostOffices;
}
