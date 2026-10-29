package com.example.backend.dto.dieuphoi;

import com.example.backend.dto.chuyengiao.ChuyenGiaoChiTietDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DieuPhoiDashboardDTO {
    private long tongSoChuyen;
    private long chuyenChuaDi;
    private long chuyenDangDi;
    private long chuyenHoanThanh;
    private long donCanDieuPhoi;
    private long chuyenDangHoatDong;
    private long taiXeKhaDung;
    private long phuongTienKhaDung;
    private List<ChuyenGiaoChiTietDTO> chuyenGiaoGanDay;
}
