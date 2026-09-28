package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tuyen_van_chuyen")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TuyenVanChuyen {

    @Id
    @Column(name = "ma_tuyen", length = 20)
    private String maTuyen;

    @Column(name = "ma_kho_di", nullable = false, length = 20)
    private String maKhoDi;

    @Column(name = "ma_kho_den", nullable = false, length = 20)
    private String maKhoDen;

    @Column(name = "khoang_cach", nullable = false, precision = 10, scale = 2)
    private BigDecimal khoangCach;

    @Column(name = "thoi_gian_du_kien_gio", nullable = false, precision = 6, scale = 2)
    private BigDecimal thoiGianDuKienGio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho_di", insertable = false, updatable = false)
    private Kho khoDi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho_den", insertable = false, updatable = false)
    private Kho khoDen;
}
