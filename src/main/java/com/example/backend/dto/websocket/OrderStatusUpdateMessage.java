package com.example.backend.dto.websocket;

import lombok.*;

/**
 * DTO payload gửi qua WebSocket khi trạng thái đơn hàng thay đổi.
 *
 * Được broadcast tới channel "/topic/orders/{maDh}" để tất cả client
 * đang theo dõi đơn hàng đó nhận được cập nhật real-time.
 *
 * Các field ánh xạ trực tiếp từ dữ liệu DB:
 * - DonHang (maDh, tenNguoiNhan)
 * - TrangThaiDH (maTrangThai, tenTrangThai)
 * - LichSuTrangThai (thoiGian, ghiChu, maNd)
 * - Kho (maKho, tenKho) - vị trí hiện tại của kiện hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateMessage {

    // === Thông tin đơn hàng ===
    /** Mã đơn hàng (DonHang.maDh) - VD: "DH000001" */
    private String maDh;

    /** Tên người nhận (DonHang.tenNguoiNhan) */
    private String tenNguoiNhan;

    // === Trạng thái cũ ===
    /** Mã trạng thái trước khi cập nhật - VD: "TT01" */
    private String maTrangThaiCu;

    /** Tên trạng thái cũ - VD: "Mới tạo" */
    private String tenTrangThaiCu;

    // === Trạng thái mới ===
    /** Mã trạng thái sau khi cập nhật - VD: "TT02" */
    private String maTrangThaiMoi;

    /** Tên trạng thái mới - VD: "Đã tiếp nhận" */
    private String tenTrangThaiMoi;

    // === Lịch sử (LichSuTrangThai) ===
    /** Thời gian ghi nhận cập nhật, ISO format: "2026-10-29T07:30:00" */
    private String thoiGian;

    /** Ghi chú chi tiết cập nhật trạng thái */
    private String ghiChu;

    /** Mã tài khoản người thực hiện cập nhật (NguoiDung.maNd) */
    private String nguoiCapNhat;

    // === Vị trí kiện hàng (nếu có) ===
    /** Mã kho/bưu cục hiện tại của kiện hàng */
    private String maKhoHienTai;

    /** Tên kho/bưu cục hiện tại */
    private String tenKhoHienTai;
}
