package com.example.backend.dto;

import lombok.*;

/**
 * DTO nhận request gán kiện vào chuyến giao.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GanKienRequest {
    private String maChuyen;
    private String maKien;
    private String maNd;
}
