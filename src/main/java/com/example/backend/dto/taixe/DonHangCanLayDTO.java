package com.example.backend.dto.taixe;

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
public class DonHangCanLayDTO {
    private String maDh;
    private String tenNguoiGui;
    private String sdtGui;
    private String diaChiLay;
    private String tenNguoiNhan;
    private String sdtNhan;
    private String diaChiGiao;
    private BigDecimal phiVanChuyen;
    private BigDecimal cod;
    private String trangThaiLay;
    private LocalDateTime ngayTao;
    private String phuongThucThanhToan; // "Chuyển khoản" hoặc "Tiền mặt"
    private Boolean phiDaThanhToan;     // true nếu cước đã thanh toán
    private BigDecimal tienThuKhiLay;   // Nếu chuyển khoản -> 0, nếu tiền mặt -> phiVanChuyen
    private String ghiChuThu;           // Ghi chú thu tiền khi lấy hàng
}

