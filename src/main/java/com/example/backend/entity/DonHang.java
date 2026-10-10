package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng don_hang
 */
@Entity
@Table(name = "don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin Đơn hàng")
public class DonHang {

    @Id
    @Column(name = "ma_dh", length = 20)
    @Schema(description = "Mã đơn hàng", example = "DH000001")
    private String maDh;

    @Column(name = "ma_kh_gui", nullable = false, length = 20)
    @Schema(description = "Mã khách hàng gửi", example = "KH000001")
    private String maKhGui;

    @Column(name = "ma_kh_nhan", length = 20)
    @Schema(description = "Mã khách hàng nhận (nếu có tài khoản)", example = "KH000002")
    private String maKhNhan;

    @Column(name = "sdt_nhan", nullable = false, length = 15)
    @Schema(description = "Số điện thoại người nhận", example = "0901234567")
    private String sdtNhan;

    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    @Schema(description = "Họ tên người nhận", example = "Nguyễn Văn B")
    private String tenNguoiNhan;

    @Column(name = "dia_chi_lay", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Địa chỉ lấy hàng", example = "12 Võ Văn Ngân, Thủ Đức, TP.HCM")
    private String diaChiLay;

    @Column(name = "dia_chi_giao", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Địa chỉ giao hàng", example = "45 Nguyễn Văn Linh, Đà Nẵng")
    private String diaChiGiao;

    @Column(name = "ma_kho_gui", nullable = false, length = 20)
    @Schema(description = "Mã bưu cục gửi tiếp nhận", example = "KHO01")
    private String maKhoGui;

    @Column(name = "ma_kho_nhan", nullable = false, length = 20)
    @Schema(description = "Mã bưu cục nhận phát hàng", example = "KHO02")
    private String maKhoNhan;

    @Column(name = "ngay_tao", nullable = false)
    @Schema(description = "Thời gian tạo đơn")
    private LocalDateTime ngayTao;

    @Column(name = "phi_van_chuyen", nullable = false, precision = 12, scale = 2)
    @Schema(description = "Cước phí vận chuyển", example = "35000")
    private BigDecimal phiVanChuyen;

    @Column(name = "cod", nullable = false, precision = 12, scale = 2)
    @Schema(description = "Tiền thu hộ COD", example = "250000")
    private BigDecimal cod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_tt", referencedColumnName = "ma_trang_thai", nullable = false)
    @Schema(description = "Trạng thái đơn hàng hiện tại")
    private TrangThaiDH trangThai;

    @Column(name = "ma_nv", length = 20)
    @Schema(description = "Mã nhân viên phụ trách", example = "NV001")
    private String maNv;

    /**
     * Getter tương thích ngược cho maTt
     */
    public String getMaTt() {
        return trangThai != null ? trangThai.getMaTrangThai() : null;
    }

    /**
     * Setter tương thích ngược cho maTt
     */
    public void setMaTt(String maTt) {
        if (this.trangThai == null) {
            this.trangThai = new TrangThaiDH();
        }
        this.trangThai.setMaTrangThai(maTt);
    }
}
