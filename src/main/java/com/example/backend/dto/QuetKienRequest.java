package com.example.backend.dto;

import lombok.*;

/**
 * DTO nhận request quét kiện từ tài xế.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class QuetKienRequest {
    private String maChuyen;
    private String maKien;
    private String maNd;
    private String ghiChu;
}
