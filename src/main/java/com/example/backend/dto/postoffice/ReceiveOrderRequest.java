package com.example.backend.dto.postoffice;

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
@Schema(description = "Yêu cầu tiếp nhận đơn hàng và lập kiện tại bưu cục (UC08)")
public class ReceiveOrderRequest {

    @NotNull(message = "Khối lượng kiện hàng không được để trống")
    @DecimalMin(value = "0.01", message = "Khối lượng phải lớn hơn 0")
    @Schema(description = "Khối lượng kiện hàng (kg)", example = "2.5")
    private BigDecimal khoiLuong;

    @Schema(description = "Kích thước Dài x Rộng x Cao (cm)", example = "30x20x15")
    private String kichThuoc;

    @Schema(description = "Chiều dài (cm)", example = "30.0")
    private BigDecimal dai;

    @Schema(description = "Chiều rộng (cm)", example = "20.0")
    private BigDecimal rong;

    @Schema(description = "Chiều cao (cm)", example = "15.0")
    private BigDecimal cao;

    @Schema(description = "Loại hàng hóa", example = "Điện tử gia dụng")
    private String loaiHang;

    @NotBlank(message = "Mã kho tiếp nhận không được để trống")
    @Schema(description = "Mã bưu cục tiếp nhận", example = "KHO01")
    private String maKho;

    @Schema(description = "Mã nhân viên tiếp nhận", example = "NV001")
    private String maNV;

    @Schema(description = "Ghi chú tiếp nhận", example = "Tiếp nhận và cân đo kiện hàng tại quầy")
    private String ghiChu;
}
