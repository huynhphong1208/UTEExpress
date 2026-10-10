package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng thanh_toan
 */
@Entity
@Table(name = "thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin Giao dịch Thanh toán / Thu hộ COD")
public class ThanhToan {

    @Id
    @Column(name = "ma_tt_toan", length = 20)
    @Schema(description = "Mã giao dịch thanh toán", example = "GD000001")
    private String maTtToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dh", nullable = false)
    @Schema(description = "Đơn hàng thanh toán")
    private DonHang donHang;

    @Column(name = "loai_khoan", length = 10, nullable = false)
    @Schema(description = "Loại khoản thanh toán (PHI_VC hoặc COD)", example = "COD")
    @Builder.Default
    private String loaiKhoan = "COD";

    @Column(name = "so_tien", nullable = false, precision = 12, scale = 2)
    @Schema(description = "Số tiền thanh toán", example = "250000")
    private BigDecimal soTien;

    @Column(name = "phuong_thuc", length = 50)
    @Schema(description = "Phương thức thanh toán (TIEN_MAT, CHUYEN_KHOAN, COD hoặc Tiền mặt, Chuyển khoản)", example = "TIEN_MAT")
    private String phuongThuc;

    @Column(name = "trang_thai", length = 50, nullable = false)
    @Schema(description = "Trạng thái thanh toán (DA_THANH_TOAN, CHUA_THANH_TOAN)", example = "DA_THANH_TOAN")
    @Builder.Default
    private String trangThai = "DA_THANH_TOAN";

    @Column(name = "thoi_gian", nullable = false)
    @Schema(description = "Thời gian thực hiện thanh toán")
    private LocalDateTime thoiGian;

    @Column(name = "nguoi_thanh_toan", length = 100)
    @Schema(description = "Người thanh toán / người xác nhận", example = "Nguyễn Văn B")
    private String nguoiThanhToan;
}
