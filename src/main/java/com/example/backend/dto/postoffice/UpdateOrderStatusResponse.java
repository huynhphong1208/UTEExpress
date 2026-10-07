package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả cập nhật trạng thái đơn hàng")
public class UpdateOrderStatusResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã trạng thái cũ", example = "TT02")
    private String maTrangThaiCu;

    @Schema(description = "Tên trạng thái cũ", example = "Đã tiếp nhận")
    private String tenTrangThaiCu;

    @Schema(description = "Mã trạng thái mới", example = "TT03")
    private String maTrangThaiMoi;

    @Schema(description = "Tên trạng thái mới", example = "Đang vận chuyển")
    private String tenTrangThaiMoi;

    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime thoiGian;

    @Schema(description = "Thông báo kết quả", example = "Cập nhật trạng thái luân chuyển đơn hàng thành công")
    private String thongBao;
}
