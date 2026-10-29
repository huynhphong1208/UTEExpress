package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO kết quả tra cứu công khai đơn hàng (UC03)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tra cứu công khai đơn hàng")
public class TraCuuDonHangResponse {

    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Schema(description = "Ngày tạo đơn")
    private LocalDateTime ngayTao;

    @Schema(description = "Trạng thái hiện tại", example = "Đang vận chuyển")
    private String trangThai;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime capNhatLuc;

    @Schema(description = "SĐT người nhận (đã che)", example = "090***567")
    private String sdtNhanChe;

    @Schema(description = "Số kiện hàng", example = "2")
    private Long soKien;

    @Schema(description = "Mã trạng thái", example = "TT03")
    private String maTt;

    @Schema(description = "Mã bưu cục gửi", example = "KHO01")
    private String maKhoGui;

    @Schema(description = "Tên bưu cục gửi", example = "Bưu cục Thủ Đức")
    private String tenKhoGui;

    @Schema(description = "Mã bưu cục nhận", example = "KHO02")
    private String maKhoNhan;

    @Schema(description = "Tên bưu cục nhận", example = "Bưu cục Cầu Giấy")
    private String tenKhoNhan;

    @Schema(description = "Mã kho hiện tại của kiện hàng", example = "KHO01")
    private String maKhoHienTai;

    @Schema(description = "Tên kho hiện tại", example = "Bưu cục Thủ Đức")
    private String tenKhoHienTai;

    @Schema(description = "Lịch sử vận chuyển")
    private List<LichSuTrangThai> lichSu;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Một mốc lịch sử vận chuyển")
    public static class LichSuTrangThai {
        private String maTt;
        private String tenTrangThai;
        private LocalDateTime thoiGian;
        private String ghiChu;
    }
}
