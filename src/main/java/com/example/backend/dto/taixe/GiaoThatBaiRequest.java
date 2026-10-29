package com.example.backend.dto.taixe;

import lombok.*;

/**
 * DTO nhận request giao thất bại.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GiaoThatBaiRequest {
    private String maChuyen;
    private String maKien;
    private String maNd;
    private String lyDo;
}
