package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng nhan_vien
 */
@Entity
@Table(name = "nhan_vien")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NhanVien {

    @Id
    @Column(name = "ma_nv", length = 20)
    private String maNv;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "chuc_vu", nullable = false, length = 50)
    private String chucVu;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "ma_kho", length = 20)
    private String maKho;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nd", unique = true)
    private NguoiDung nguoiDung;
}
