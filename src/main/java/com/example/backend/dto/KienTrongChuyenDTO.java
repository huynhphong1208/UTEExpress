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
public class KienTrongChuyenDTO {
    private String maChuyen;
    private String maTx;
    private String maKien;
    private String maDh;
    private String tenNguoiNhan;
    private String sdtNhan;
    private String diaChiGiao;
    private BigDecimal khoiLuong;
    private String loaiHang;
    private String trangThai;
    private LocalDateTime thoiGianGan;
    private LocalDateTime thoiGianQuet;
    private LocalDateTime thoiGianGiao;
    private String ghiChu;
    private BigDecimal cod;
}
