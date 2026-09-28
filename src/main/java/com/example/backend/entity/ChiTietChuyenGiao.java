package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_chuyen_giao")
@IdClass(ChiTietChuyenGiaoId.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChiTietChuyenGiao {

    @Id
    @Column(name = "ma_chuyen", length = 20)
    private String maChuyen;

    @Id
    @Column(name = "ma_kien", length = 20)
    private String maKien;

    @Column(name = "thoi_gian_gan", nullable = false)
    private LocalDateTime thoiGianGan;

    @Column(name = "thoi_gian_quet")
    private LocalDateTime thoiGianQuet;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuyen", insertable = false, updatable = false)
    private ChuyenGiao chuyenGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kien", insertable = false, updatable = false)
    private KienHang kienHang;
}
