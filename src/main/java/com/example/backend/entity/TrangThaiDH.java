package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ánh xạ bảng trang_thai_dh
 */
@Entity
@Table(name = "trang_thai_dh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Trạng thái đơn hàng")
public class TrangThaiDH {

    @Id
    @Column(name = "ma_trang_thai", length = 20)
    @Schema(description = "Mã trạng thái", example = "TT01")
    private String maTrangThai;

    @Column(name = "ten_trang_thai", nullable = false, length = 50)
    @Schema(description = "Tên trạng thái", example = "Mới tạo")
    private String tenTrangThai;
}
