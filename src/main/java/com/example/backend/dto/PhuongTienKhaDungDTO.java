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
public class PhuongTienKhaDungDTO {
    private String maPt;
    private String bienSo;
    private String loaiXe;
    private BigDecimal taiTrong;
    private String trangThai;
    private String chuyenHienTai;
    private boolean khaDung;
}
