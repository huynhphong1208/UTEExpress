package com.example.backend.dto.taixe;

import com.example.backend.dto.chuyengiao.ChuyenGiaoChiTietDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaiXeDashboardDTO {
    private String maTx;
    private String tenTaiXe;
    private String bienSoXe;
    private String loaiXe;
    private long chuyenPhanCong;
    private long chuyenDangThucHien;
    private long kienCanGiao;
    private long kienDaGiao;
    private ChuyenGiaoChiTietDTO chuyenHienTai;
}
