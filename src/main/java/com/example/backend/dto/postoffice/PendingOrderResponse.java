package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đơn hàng chờ bưu cục tiếp nhận")
public class PendingOrderResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Mã khách hàng gửi", example = "KH000001")
    private String maKhGui;

    @Schema(description = "Họ tên người nhận", example = "Lê Văn Cường")
    private String tenNguoiNhan;

    @Schema(description = "Số điện thoại người nhận", example = "0901000003")
    private String sdtNhan;

    @Schema(description = "Địa chỉ lấy hàng", example = "12 Võ Văn Ngân, Thủ Đức, TP.HCM")
    private String diaChiLay;

    @Schema(description = "Địa chỉ giao hàng", example = "45 Nguyễn Văn Linh, Đà Nẵng")
    private String diaChiGiao;

    @Schema(description = "Mã bưu cục gửi", example = "KHO01")
    private String maKhoGui;

    @Schema(description = "Mã bưu cục nhận", example = "KHO02")
    private String maKhoNhan;

    @Schema(description = "Thời gian tạo đơn")
    private LocalDateTime ngayTao;

    @Schema(description = "Cước phí vận chuyển", example = "618500")
    private BigDecimal phiVanChuyen;

    @Schema(description = "Tiền thu hộ COD", example = "500000")
    private BigDecimal cod;

    @Schema(description = "Mã trạng thái", example = "TT01")
    private String maTrangThai;

    @Schema(description = "Tên trạng thái", example = "Mới tạo")
    private String tenTrangThai;

    @Schema(description = "Cước phí đã thanh toán chưa", example = "true")
    private Boolean phiDaThanhToan;

    @Schema(description = "Tình trạng thanh toán cước", example = "Đã thanh toán")
    private String tinhTrangThanhToan;

    @Schema(description = "Phương thức thanh toán cước", example = "Chuyển khoản")
    private String phuongThucThanhToan;
}
