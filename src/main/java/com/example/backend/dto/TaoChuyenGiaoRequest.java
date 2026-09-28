package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO nhận request tạo chuyến giao từ Nhân viên điều phối.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TaoChuyenGiaoRequest {
    private String loai;           // LIEN_KHO hoặc GIAO_CUOI
    private String maTuyen;        // bắt buộc nếu LIEN_KHO
    private String maKhoGiao;      // bắt buộc nếu GIAO_CUOI
    private String maTx;
    private String maPt;
    private LocalDateTime ngayXuatPhat;
    private String maNvDp;
    private String maNd;
    private LocalDateTime ngayDenDuKien; // bắt buộc nếu GIAO_CUOI
}
