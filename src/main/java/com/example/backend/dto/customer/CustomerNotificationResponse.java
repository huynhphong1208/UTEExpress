package com.example.backend.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho một thông báo biến động đơn hàng của khách hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerNotificationResponse {
    private String maLs;
    private String maDh;
    private String tenTrangThai;
    private String maTrangThai;
    private String ghiChu;
    private LocalDateTime thoiGian;
    private String thoiGianDisplay;
    private String tenNguoiNhan;
}

