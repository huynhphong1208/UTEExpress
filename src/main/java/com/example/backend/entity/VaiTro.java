package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng vai_tro
 */
@Entity
@Table(name = "vai_tro")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VaiTro {

    @Id
    @Column(name = "ma_vai_tro", length = 20)
    private String maVaiTro;

    @Column(name = "ten_vai_tro", nullable = false, length = 50)
    private String tenVaiTro;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
}
