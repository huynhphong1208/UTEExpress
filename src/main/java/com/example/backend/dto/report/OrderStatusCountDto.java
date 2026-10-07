package com.example.backend.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thống kê số lượng đơn hàng theo trạng thái")
public class OrderStatusCountDto {

    @Schema(description = "Mã trạng thái", example = "TT01")
    private String maTrangThai;

    @Schema(description = "Tên trạng thái", example = "Mới tạo")
    private String tenTrangThai;

    @Schema(description = "Số lượng đơn hàng", example = "12")
    private long soLuong;
}
