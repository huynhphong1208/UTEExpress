package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "kien_hang")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KienHang {

    @Id
    @Column(name = "ma_kien", length = 20)
    private String maKien;

    @Column(name = "ma_dh", nullable = false, length = 20)
    private String maDh;

    @Column(name = "khoi_luong", nullable = false, precision = 10, scale = 2)
    private BigDecimal khoiLuong;

    @Column(name = "dai", nullable = false, precision = 8, scale = 2)
    private BigDecimal dai;

    @Column(name = "rong", nullable = false, precision = 8, scale = 2)
    private BigDecimal rong;

    @Column(name = "cao", nullable = false, precision = 8, scale = 2)
    private BigDecimal cao;

    @Column(name = "loai_hang", length = 50)
    private String loaiHang;

    @Column(name = "ma_kho_hien_tai", length = 20)
    private String maKhoHienTai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho_hien_tai", insertable = false, updatable = false)
    private Kho khoHienTai;
}
