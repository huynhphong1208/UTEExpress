package com.example.backend.dto.chuyengiao;

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

    // Thông tin thanh toán & thu tiền của tài xế
    private BigDecimal phiVanChuyen;
    private String phuongThucThanhToan; // "Chuyển khoản" hoặc "Tiền mặt"
    private Boolean phiDaThanhToan;     // true nếu cước đã thanh toán (chuyển khoản)
    private BigDecimal tongTienThu;     // Số tiền tài xế cần thu khi giao hàng (chuyển khoản -> cod; tiền mặt -> cước + cod)
    private String hinhThucThu;         // "Thu COD", "Thu Cước + COD", "Thu Cước", "Không thu"
    private String chiTietThu;          // Mô tả chi tiết phân tích cước và COD

    public void applyPaymentInfo(BigDecimal phiVC, BigDecimal codVal, String rawPhuongThuc, String rawTrangThai) {
        if (phiVC == null) phiVC = BigDecimal.ZERO;
        if (codVal == null) codVal = BigDecimal.ZERO;

        boolean isCk = rawPhuongThuc != null && (
                rawPhuongThuc.toUpperCase().contains("CHUYEN") ||
                rawPhuongThuc.toUpperCase().contains("CHUYỂN") ||
                rawPhuongThuc.toUpperCase().contains("BANK") ||
                rawPhuongThuc.toUpperCase().contains("QR")
        );
        boolean isDaTT = rawTrangThai != null && (
                rawTrangThai.equalsIgnoreCase("Đã thanh toán") ||
                rawTrangThai.equalsIgnoreCase("DA_THANH_TOAN")
        );

        boolean daThanhToanChuyenKhoan = isCk && isDaTT;

        this.phiVanChuyen = phiVC;
        this.cod = codVal;

        if (daThanhToanChuyenKhoan) {
            // Đã thanh toán bằng chuyển khoản -> chỉ thu COD
            this.phuongThucThanhToan = "Chuyển khoản";
            this.phiDaThanhToan = true;
            this.tongTienThu = codVal;
            if (codVal.compareTo(BigDecimal.ZERO) > 0) {
                this.hinhThucThu = "Thu COD";
                this.chiTietThu = "Cước đã TT chuyển khoản. Chỉ thu COD: " + formatMoney(codVal);
            } else {
                this.hinhThucThu = "Không thu";
                this.chiTietThu = "Cước đã TT chuyển khoản (Không có COD)";
            }
        } else {
            // Thanh toán tiền mặt -> thu lần tiền cước + tiền COD
            BigDecimal tong = phiVC.add(codVal);
            this.phuongThucThanhToan = "Tiền mặt";
            this.phiDaThanhToan = false;
            this.tongTienThu = tong;
            if (phiVC.compareTo(BigDecimal.ZERO) > 0 && codVal.compareTo(BigDecimal.ZERO) > 0) {
                this.hinhThucThu = "Thu Cước + COD";
                this.chiTietThu = "Cước: " + formatMoney(phiVC) + " + COD: " + formatMoney(codVal);
            } else if (phiVC.compareTo(BigDecimal.ZERO) > 0) {
                this.hinhThucThu = "Thu Cước";
                this.chiTietThu = "Cước tiền mặt: " + formatMoney(phiVC);
            } else if (codVal.compareTo(BigDecimal.ZERO) > 0) {
                this.hinhThucThu = "Thu COD";
                this.chiTietThu = "Thu COD: " + formatMoney(codVal);
            } else {
                this.hinhThucThu = "Không thu";
                this.chiTietThu = "Không thu (0 đ)";
            }
        }
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,d đ", amount.longValue()).replace(',', '.');
    }
}
