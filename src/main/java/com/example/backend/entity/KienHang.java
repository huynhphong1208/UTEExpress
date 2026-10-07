package com.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entity ánh xạ bảng kien_hang
 * Kiểu dữ liệu khoi_luong, dai, rong, cao thống nhất dùng BigDecimal khớp chính xác kiểu NUMERIC trong PostgreSQL.
 */
@Entity
@Table(name = "kien_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin Kiện hàng")
public class KienHang {

    @Id
    @Column(name = "ma_kien", length = 20)
    @Schema(description = "Mã kiện hàng", example = "KIEN000001")
    private String maKien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dh", nullable = false)
    @Schema(description = "Đơn hàng chứa kiện này")
    private DonHang donHang;

    @Column(name = "khoi_luong", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Khối lượng kiện hàng (kg)", example = "2.50")
    private BigDecimal khoiLuong;

    @Column(name = "dai", nullable = false, precision = 8, scale = 2)
    @Schema(description = "Chiều dài (cm)", example = "30.00")
    @Builder.Default
    private BigDecimal dai = new BigDecimal("10.00");

    @Column(name = "rong", nullable = false, precision = 8, scale = 2)
    @Schema(description = "Chiều rộng (cm)", example = "20.00")
    @Builder.Default
    private BigDecimal rong = new BigDecimal("10.00");

    @Column(name = "cao", nullable = false, precision = 8, scale = 2)
    @Schema(description = "Chiều cao (cm)", example = "15.00")
    @Builder.Default
    private BigDecimal cao = new BigDecimal("10.00");

    @Column(name = "loai_hang", length = 50)
    @Schema(description = "Loại hàng hóa", example = "Điện tử")
    private String loaiHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_kho_hien_tai")
    @Schema(description = "Kho / Bưu cục hiện tại đang lưu giữ kiện hàng")
    private Kho khoHienTai;

    @Transient
    @Schema(description = "Kích thước dạng Dài x Rộng x Cao (cm)", example = "30x20x15")
    private String kichThuoc;

    public String getKichThuoc() {
        if (kichThuoc != null && !kichThuoc.isBlank()) {
            return kichThuoc;
        }
        if (dai != null && rong != null && cao != null) {
            return String.format("%.0fx%.0fx%.0f", dai.doubleValue(), rong.doubleValue(), cao.doubleValue());
        }
        return "10x10x10";
    }

    public void setKichThuoc(String kichThuoc) {
        this.kichThuoc = kichThuoc;
        if (kichThuoc != null && kichThuoc.contains("x")) {
            try {
                String[] parts = kichThuoc.toLowerCase().split("x");
                if (parts.length >= 3) {
                    this.dai = new BigDecimal(parts[0].trim()).setScale(2, RoundingMode.HALF_UP);
                    this.rong = new BigDecimal(parts[1].trim()).setScale(2, RoundingMode.HALF_UP);
                    this.cao = new BigDecimal(parts[2].trim()).setScale(2, RoundingMode.HALF_UP);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
