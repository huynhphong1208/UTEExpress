package com.example.backend.dto.taixe;

import lombok.*;

/**
 * DTO nhận request xuất phát chuyến giao.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class XuatPhatChuyenRequest {
    private String maChuyen;
    private String maNd;
}
