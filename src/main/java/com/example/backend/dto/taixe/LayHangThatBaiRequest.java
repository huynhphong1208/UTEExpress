package com.example.backend.dto.taixe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayHangThatBaiRequest {
    private String maChuyen;
    private String maKien;
    private String maNd;
    private String lyDo;
}
