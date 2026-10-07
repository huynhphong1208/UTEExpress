package com.example.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hiệu suất hoạt động của từng bưu cục / kho hàng")
public class PostOfficePerformanceResponse {

    @Schema(description = "Mã bưu cục", example = "KHO01")
    private String maKho;

    @Schema(description = "Tên bưu cục", example = "Bưu cục Thủ Đức")
    private String tenKho;

    @Schema(description = "Địa chỉ bưu cục", example = "12 Võ Văn Ngân, TP. Thủ Đức")
    private String diaChi;

    @Schema(description = "Số lượng đơn tiếp nhận", example = "450")
    private long soDonTiepNhan;

    @Schema(description = "Số đơn giao thành công", example = "430")
    private long soDonGiaoThanhCong;

    @Schema(description = "Tỷ lệ xử lý thành công (%)", example = "95.5")
    private Double tyLeThanhCong;

    @Schema(description = "Số kiện hàng đang lưu kho hiện tại", example = "35")
    private long soKienTonKho;
}
