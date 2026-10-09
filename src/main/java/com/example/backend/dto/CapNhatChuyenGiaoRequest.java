package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO nhận request cập nhật phân công chuyến giao từ Nhân viên điều phối.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CapNhatChuyenGiaoRequest {
    private String loai;              // LIEN_KHO hoặc GIAO_CUOI
    private String maTuyen;           // nếu LIEN_KHO
    private String maKhoGiao;         // nếu GIAO_CUOI
    private String maTx;              // tài xế phân công mới
    private String maPt;              // phương tiện phân công mới
    private LocalDateTime ngayXuatPhat;
    private LocalDateTime ngayDenDuKien;
    private String maNd;              // mã tài khoản người dùng điều phối
}
