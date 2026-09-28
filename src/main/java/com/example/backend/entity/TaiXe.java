package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tai_xe")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TaiXe {

    @Id
    @Column(name = "ma_tx", length = 20)
    private String maTx;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "sdt", nullable = false, length = 15)
    private String sdt;

    @Column(name = "bang_lai", length = 20)
    private String bangLai;

    @Column(name = "ma_nd", length = 20)
    private String maNd;
}
