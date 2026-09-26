package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng nguoi_dung
 */
@Entity
@Table(name = "nguoi_dung")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NguoiDung {

    @Id
    @Column(name = "ma_nd", length = 20)
    private String maNd;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_vai_tro", nullable = false)
    private VaiTro vaiTro;
}
