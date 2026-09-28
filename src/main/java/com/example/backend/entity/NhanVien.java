package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    @Column(name = "ma_nd", length = 20)
    private String maNd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho", insertable = false, updatable = false)
    private Kho kho;
}
