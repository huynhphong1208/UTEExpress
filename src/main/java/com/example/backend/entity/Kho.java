package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng kho (Bưu cục / Điểm tập kết)
 */
@Entity
@Table(name = "kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin Bưu cục / Kho hàng")
public class Kho {

    @Id
    @Column(name = "ma_kho", length = 20)
    @Schema(description = "Mã bưu cục / kho", example = "KHO01")
    private String maKho;

    @Column(name = "ten_kho", nullable = false, length = 100)
    @Schema(description = "Tên bưu cục", example = "Bưu cục Thủ Đức")
    private String tenKho;

    @Column(name = "dia_chi", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Địa chỉ bưu cục", example = "12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM")
    private String diaChi;

    @Column(name = "sdt", length = 15)
    @Schema(description = "Số điện thoại hotline bưu cục", example = "0281111111")
    private String sdt;
}
