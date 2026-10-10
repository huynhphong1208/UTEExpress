package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả tiếp nhận đơn hàng tại bưu cục")
public class ReceiveOrderResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã kiện hàng được tạo/gán", example = "KIEN000001")
    private String maKien;

    @Schema(description = "Mã bưu cục hiện tại", example = "KHO01")
    private String maKhoHienTai;

    @Schema(description = "Mã nhân viên phụ trách", example = "NV001")
    private String maNv;

    @Schema(description = "Mã trạng thái mới của đơn", example = "TT02")
    private String maTrangThai;

    @Schema(description = "Tên trạng thái mới", example = "Đã tiếp nhận")
    private String tenTrangThai;

    @Schema(description = "Thời gian tiếp nhận")
    private LocalDateTime thoiGianTiepNhan;

    @Schema(description = "Thông báo kết quả", example = "Tiếp nhận đơn hàng và lập kiện thành công")
    private String thongBao;
}
