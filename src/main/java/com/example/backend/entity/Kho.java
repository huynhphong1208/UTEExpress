package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kho")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Kho {

    @Id
    @Column(name = "ma_kho", length = 20)
    private String maKho;

    @Column(name = "ten_kho", nullable = false, length = 100)
    private String tenKho;

    @Column(name = "dia_chi", nullable = false, columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "sdt", length = 15)
    private String sdt;
}
