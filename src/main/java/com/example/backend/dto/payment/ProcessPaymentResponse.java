package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả xử lý giao dịch thanh toán")
public class ProcessPaymentResponse {

    @Schema(description = "Mã giao dịch được sinh", example = "GD000001")
    private String maTTToan;

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Loại khoản đã thanh toán", example = "COD")
    private String loaiKhoan;

    @Schema(description = "Số tiền thanh toán", example = "250000")
    private BigDecimal soTien;

    @Schema(description = "Phương thức thanh toán", example = "TIEN_MAT")
    private String phuongThuc;

    @Schema(description = "Trạng thái giao dịch", example = "DA_THANH_TOAN")
    private String trangThai;

    @Schema(description = "Thời gian ghi nhận giao dịch")
    private LocalDateTime thoiGian;

    @Schema(description = "Người thanh toán / xác nhận", example = "Nguyễn Văn B")
    private String nguoiThanhToan;

    @Schema(description = "Thông báo kết quả", example = "Xử lý thanh toán thành công")
    private String thongBao;
}
