package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng don_hang
 */
@Entity
@Table(name = "don_hang")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonHang {

    @Id
    @Column(name = "ma_dh", length = 20)
    private String maDh;

    @Column(name = "ma_kh_gui", nullable = false, length = 20)
    private String maKhGui;

    @Column(name = "ma_kh_nhan", length = 20)
    private String maKhNhan;

    @Column(name = "sdt_nhan", nullable = false, length = 15)
    private String sdtNhan;

    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(name = "dia_chi_lay", nullable = false, columnDefinition = "TEXT")
    private String diaChiLay;

    @Column(name = "dia_chi_giao", nullable = false, columnDefinition = "TEXT")
    private String diaChiGiao;

    @Column(name = "ma_kho_gui", nullable = false, length = 20)
    private String maKhoGui;

    @Column(name = "ma_kho_nhan", nullable = false, length = 20)
    private String maKhoNhan;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "phi_van_chuyen", nullable = false, precision = 12, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "cod", nullable = false, precision = 12, scale = 2)
    private BigDecimal cod;

    @Column(name = "ma_tt", nullable = false, length = 20)
    private String maTt;

    @Column(name = "ma_nv", length = 20)
    private String maNv;
}
