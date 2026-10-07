package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng lich_su_trang_thai
 */
@Entity
@Table(name = "lich_su_trang_thai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Lịch sử chuyển trạng thái của đơn hàng")
public class LichSuTrangThai {

    @Id
    @Column(name = "ma_ls", length = 20)
    @Schema(description = "Mã lịch sử trạng thái", example = "LS00000001")
    private String maLs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dh", nullable = false)
    @Schema(description = "Đơn hàng liên quan")
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_tt", nullable = false)
    @Schema(description = "Trạng thái được cập nhật")
    private TrangThaiDH trangThai;

    @Column(name = "thoi_gian", nullable = false)
    @Schema(description = "Thời gian ghi nhận cập nhật")
    private LocalDateTime thoiGian;

    @Column(name = "ma_nd", length = 20)
    @Schema(description = "Mã tài khoản người thực hiện cập nhật", example = "ND001")
    private String maNd;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    @Schema(description = "Ghi chú cập nhật", example = "Tiếp nhận đơn tại bưu cục Thủ Đức")
    private String ghiChu;
}
