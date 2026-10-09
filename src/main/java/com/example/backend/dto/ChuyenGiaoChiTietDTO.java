package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenGiaoChiTietDTO {
    private String maChuyen;
    private String loaiChuyen;
    private String trangThai;
    private String maTuyen;
    private String maKhoXuatPhat;
    private String tenKhoXuatPhat;
    private String maKhoDen;
    private String tenDiemDen;
    private String maTx;
    private String tenTaiXe;
    private String maPt;
    private String bienSo;
    private BigDecimal taiTrong;
    private String maNvDieuPhoi;
    private LocalDateTime ngayXuatPhat;
    private LocalDateTime ngayDenDuKien;
    private Long soKien;
    private BigDecimal tongKg;
    private BigDecimal tyLeTai;
}
