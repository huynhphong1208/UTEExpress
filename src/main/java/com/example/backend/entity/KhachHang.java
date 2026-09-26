package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng khach_hang
 */
@Entity
@Table(name = "khach_hang")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KhachHang {

    @Id
    @Column(name = "ma_kh", length = 20)
    private String maKh;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "sdt", nullable = false, unique = true, length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dia_chi_mac_dinh", columnDefinition = "TEXT")
    private String diaChiMacDinh;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nd", unique = true)
    private NguoiDung nguoiDung;
}
