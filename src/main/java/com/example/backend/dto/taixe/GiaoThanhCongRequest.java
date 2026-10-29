package com.example.backend.dto.taixe;

import lombok.*;

/**
 * DTO nhận request giao thành công.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GiaoThanhCongRequest {
    private String maChuyen;
    private String maKien;
    private String maNd;
    private String ghiChu;
    private String nguoiThu;
}
