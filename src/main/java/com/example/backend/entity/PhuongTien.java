package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "phuong_tien")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PhuongTien {

    @Id
    @Column(name = "ma_pt", length = 20)
    private String maPt;

    @Column(name = "bien_so", nullable = false, unique = true, length = 20)
    private String bienSo;

    @Column(name = "loai_xe", length = 50)
    private String loaiXe;

    @Column(name = "tai_trong", nullable = false, precision = 10, scale = 2)
    private BigDecimal taiTrong;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;
}
