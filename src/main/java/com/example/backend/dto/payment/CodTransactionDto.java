package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bản ghi giao dịch COD phục vụ đối soát")
public class CodTransactionDto {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã khách hàng gửi (chủ hàng nhận tiền COD)", example = "KH000001")
    private String maKhGui;

    @Schema(description = "Tên người nhận", example = "Lê Văn Cường")
    private String tenNguoiNhan;

    @Schema(description = "Số tiền COD cần thu", example = "500000")
    private BigDecimal tienCod;

    @Schema(description = "Số tiền COD thực tế đã thu", example = "500000")
    private BigDecimal codDaThu;

    @Schema(description = "Mã trạng thái đơn hàng", example = "TT06")
    private String maTrangThai;

    @Schema(description = "Tên trạng thái đơn hàng", example = "Giao thành công")
    private String tenTrangThai;

    @Schema(description = "Tình trạng đối soát COD (Chờ đối soát, Đã thu, Đã hoàn tất chi trả, Không thu do hủy)", example = "Chờ đối soát")
    private String tinhTrangCod;

    @Schema(description = "Thời gian thu tiền COD")
    private LocalDateTime thoiGianThu;

    @Schema(description = "Người xác nhận thu tiền COD", example = "Tài xế Huỳnh Văn Tài")
    private String nguoiThu;

    @Schema(description = "Mã giao dịch thu tiền", example = "GD000001")
    private String maGiaoDich;
}
