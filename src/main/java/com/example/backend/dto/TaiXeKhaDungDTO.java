package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaiXeKhaDungDTO {
    private String maTx;
    private String hoTen;
    private String sdt;
    private String bangLai;
    private String trangThaiTaiKhoan;
    private String chuyenHienTai;
    private String thongTinPhanCong;
    private boolean khaDung;
}
