package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả điều chuyển kho cho kiện hàng")
public class TransferWarehouseResponse {

    @Schema(description = "Mã kiện hàng", example = "KIEN000001")
    private String maKien;

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã kho cũ", example = "KHO01")
    private String maKhoCu;

    @Schema(description = "Mã kho mới", example = "KHO02")
    private String maKhoMoi;

    @Schema(description = "Tên kho mới", example = "Bưu cục Cầu Giấy")
    private String tenKhoMoi;

    @Schema(description = "Thời gian chuyển kho")
    private LocalDateTime thoiGian;

    @Schema(description = "Thông báo kết quả", example = "Chuyển kho cho kiện hàng thành công")
    private String thongBao;
}
