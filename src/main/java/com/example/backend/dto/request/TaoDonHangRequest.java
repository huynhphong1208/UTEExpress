package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tạo đơn hàng mới (UC04)
 */
@Data
@Schema(description = "Thông tin tạo đơn hàng")
public class TaoDonHangRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Schema(description = "Họ tên người nhận", example = "Trần Thị B")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "SĐT người nhận phải có đúng 10 chữ số")
    @Schema(description = "SĐT người nhận", example = "0987654321")
    private String sdtNhan;

    @NotBlank(message = "Địa chỉ lấy hàng không được để trống")
    @Schema(description = "Địa chỉ lấy hàng", example = "123 Võ Văn Ngân, Thủ Đức, TP.HCM")
    private String diaChiLay;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Schema(description = "Địa chỉ giao hàng", example = "456 Nguyễn Văn Cừ, Q5, TP.HCM")
    private String diaChiGiao;

    @NotBlank(message = "Mã kho gửi không được để trống")
    @Schema(description = "Mã bưu cục gửi", example = "KHO0001")
    private String maKhoGui;

    @NotBlank(message = "Mã kho nhận không được để trống")
    @Schema(description = "Mã bưu cục nhận", example = "KHO0002")
    private String maKhoNhan;

    @DecimalMin(value = "0", message = "Tiền COD không được âm")
    @Schema(description = "Tiền COD (0 nếu không có)", example = "150000")
    private BigDecimal cod = BigDecimal.ZERO;

    @NotNull(message = "Danh sách kiện hàng không được để trống")
    @Size(min = 1, message = "Đơn hàng phải có ít nhất một kiện hàng")
    @Valid
    @Schema(description = "Danh sách kiện hàng")
    private List<KienHangRequest> danhSachKien;

    @Data
    @Schema(description = "Thông tin một kiện hàng")
    public static class KienHangRequest {

        @DecimalMin(value = "0.01", message = "Khối lượng phải lớn hơn 0")
        @NotNull(message = "Khối lượng không được để trống")
        @Schema(description = "Khối lượng (kg)", example = "2.5")
        private BigDecimal khoiLuong;

        @DecimalMin(value = "0.01", message = "Chiều dài phải lớn hơn 0")
        @NotNull(message = "Chiều dài không được để trống")
        @Schema(description = "Chiều dài (cm)", example = "30")
        private BigDecimal dai;

        @DecimalMin(value = "0.01", message = "Chiều rộng phải lớn hơn 0")
        @NotNull(message = "Chiều rộng không được để trống")
        @Schema(description = "Chiều rộng (cm)", example = "20")
        private BigDecimal rong;

        @DecimalMin(value = "0.01", message = "Chiều cao phải lớn hơn 0")
        @NotNull(message = "Chiều cao không được để trống")
        @Schema(description = "Chiều cao (cm)", example = "15")
        private BigDecimal cao;

        @Schema(description = "Loại hàng hoá", example = "Điện tử")
        private String loaiHang;
    }
}
