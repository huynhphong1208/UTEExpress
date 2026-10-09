package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonCanDieuPhoiDTO {
    private String maKien;
    private String maDh;
    private String loaiHang;
    private BigDecimal khoiLuong;
    private String maKhoHienTai;
    private String tenKhoHienTai;
    private String diaChiGiao;
    private String maKhoNhan;
    private String tenKhoNhan;
    private String tenNguoiNhan;
    private String sdtNhan;
    private BigDecimal cod;
    private String trangThaiDon;
}
