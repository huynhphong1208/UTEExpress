package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu xử lý giao dịch thanh toán hoặc thu tiền COD (UC06, UC07)")
public class ProcessPaymentRequest {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    @Schema(description = "Mã đơn hàng cần thanh toán", example = "DH000001")
    private String maDH;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
    @Schema(description = "Số tiền thanh toán (VNĐ)", example = "250000")
    private BigDecimal soTien;

    @Schema(description = "Loại khoản thanh toán (COD hoặc PHI_VC)", example = "COD")
    private String loaiKhoan;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Schema(description = "Phương thức thanh toán (TIEN_MAT, CHUYEN_KHOAN, COD)", example = "TIEN_MAT")
    private String phuongThuc;

    @Schema(description = "Tên người thanh toán / người nhận nộp tiền", example = "Nguyễn Văn B")
    private String nguoiThanhToan;
}
