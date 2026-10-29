package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TraCuuDonHangResponse;
import com.example.backend.service.DonHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller tra cứu đơn hàng công khai - không cần token (UC03)
 */
@Tag(name = "Tra cứu công khai", description = "API tra cứu đơn hàng công khai (Guest, không cần đăng nhập)")
@RestController
@RequestMapping("/api/tra-cuu")
@RequiredArgsConstructor
public class TraCuuController {

    private final DonHangService donHangService;
    private final com.example.backend.repository.KhoRepository khoRepository;
    private final com.example.backend.repository.TuyenVanChuyenRepository tuyenVanChuyenRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Operation(
        summary = "Tra cứu đơn hàng theo mã (UC03)",
        description = "Public API - Không cần token. Trả về trạng thái đơn hàng, SĐT đã che và timeline vận chuyển."
    )
    @GetMapping("/{maDH}")
    public ResponseEntity<ApiResponse<TraCuuDonHangResponse>> traCuu(
            @Parameter(description = "Mã đơn hàng", example = "DH000001")
            @PathVariable String maDH) {
        TraCuuDonHangResponse result = donHangService.traCuuCongKhai(maDH);
        return ResponseEntity.ok(ApiResponse.success("Tra cứu đơn hàng thành công", result));
    }

    @Operation(
        summary = "Danh sách bưu cục toàn quốc (Public)",
        description = "Public API lấy danh mục các kho / bưu cục phục vụ khách tính cước và chọn điểm giao dịch"
    )
    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<java.util.List<com.example.backend.entity.Kho>>> getWarehouses() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bưu cục thành công", khoRepository.findAll()));
    }

    @Operation(
        summary = "Ước tính cước phí vận chuyển chính xác",
        description = "Tính toán cước phí vận chuyển tự động theo tuyến đường và kích thước/khối lượng kiện hàng"
    )
    @GetMapping("/uoc-tinh-cuoc")
    public ResponseEntity<ApiResponse<com.example.backend.dto.response.EstimateShippingFeeResponse>> estimateFee(
            @Parameter(description = "Mã kho gửi", example = "KHO01") @RequestParam String maKhoGui,
            @Parameter(description = "Mã kho nhận", example = "KHO02") @RequestParam String maKhoNhan,
            @Parameter(description = "Khối lượng (kg)", example = "1.5") @RequestParam(defaultValue = "1.0") java.math.BigDecimal khoiLuong,
            @Parameter(description = "Chiều dài (cm)", example = "20") @RequestParam(defaultValue = "10.0") java.math.BigDecimal dai,
            @Parameter(description = "Chiều rộng (cm)", example = "15") @RequestParam(defaultValue = "10.0") java.math.BigDecimal rong,
            @Parameter(description = "Chiều cao (cm)", example = "10") @RequestParam(defaultValue = "10.0") java.math.BigDecimal cao) {

        com.example.backend.entity.Kho khoGui = khoRepository.findById(maKhoGui).orElse(null);
        com.example.backend.entity.Kho khoNhan = khoRepository.findById(maKhoNhan).orElse(null);

        boolean cungKho = maKhoGui != null && maKhoGui.equalsIgnoreCase(maKhoNhan);
        java.math.BigDecimal khoangCach = java.math.BigDecimal.ZERO;
        java.math.BigDecimal thoiGianGio = java.math.BigDecimal.valueOf(2.0);

        if (!cungKho) {
            // Ưu tiên tra cứu khoảng cách trực tiếp từ DB fn_khoang_cach_don
            try {
                java.math.BigDecimal dbDist = jdbcTemplate.queryForObject(
                        "SELECT fn_khoang_cach_don(?, ?)",
                        java.math.BigDecimal.class,
                        maKhoGui, maKhoNhan);
                if (dbDist != null && dbDist.compareTo(java.math.BigDecimal.ZERO) >= 0) {
                    khoangCach = dbDist;
                }
            } catch (Exception ignored) {
            }

            var routeOpt = tuyenVanChuyenRepository.findByMaKhoDiAndMaKhoDen(maKhoGui, maKhoNhan)
                    .or(() -> tuyenVanChuyenRepository.findByMaKhoDiAndMaKhoDen(maKhoNhan, maKhoGui));
            if (routeOpt.isPresent()) {
                var route = routeOpt.get();
                if (khoangCach.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    khoangCach = route.getKhoangCach() != null ? route.getKhoangCach() : java.math.BigDecimal.valueOf(15.0);
                }
                thoiGianGio = route.getThoiGianDuKienGio() != null ? route.getThoiGianDuKienGio() : java.math.BigDecimal.valueOf(24.0);
            } else if (khoangCach.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                khoangCach = java.math.BigDecimal.valueOf(300.0);
                thoiGianGio = java.math.BigDecimal.valueOf(24.0);
            }
        }

        // Tính khối lượng quy đổi thể tích: (D * R * C) / 5000
        double volWeightDouble = (dai.doubleValue() * rong.doubleValue() * cao.doubleValue()) / 5000.0;
        java.math.BigDecimal volWeight = java.math.BigDecimal.valueOf(Math.round(volWeightDouble * 100.0) / 100.0);
        java.math.BigDecimal chargeWeight = khoiLuong.max(volWeight);

        // Gọi trực tiếp hàm Database fn_tinh_phi_van_chuyen để đảm bảo đồng bộ 100% với hệ thống tạo đơn
        java.math.BigDecimal totalFee;
        try {
            totalFee = jdbcTemplate.queryForObject(
                    "SELECT fn_tinh_phi_van_chuyen(?, ?, ?, ?, ?)",
                    java.math.BigDecimal.class,
                    khoiLuong, dai, rong, cao, khoangCach);
        } catch (Exception ex) {
            // Fallback công thức chuẩn Database: 15.000 + max(0, chargeWeight - 0.5) * 5.000 + khoangCach * 300
            double extraKg = Math.max(0.0, chargeWeight.doubleValue() - 0.5);
            double fee = 15000.0 + (extraKg * 5000.0) + (khoangCach.doubleValue() * 300.0);
            totalFee = java.math.BigDecimal.valueOf(Math.round(fee / 100.0) * 100);
        }

        var resp = com.example.backend.dto.response.EstimateShippingFeeResponse.builder()
                .phiVanChuyen(totalFee)
                .khoangCachKm(khoangCach)
                .khoiLuongTinhCuoc(chargeWeight)
                .khoiLuongQuyDoi(volWeight)
                .cungKho(cungKho)
                .thoiGianDuKienGio(thoiGianGio)
                .tenKhoGui(khoGui != null ? khoGui.getTenKho() : maKhoGui)
                .tenKhoNhan(khoNhan != null ? khoNhan.getTenKho() : maKhoNhan)
                .ghiChu("Cước tính theo chuẩn Database UTE Express: Cước cơ bản 15.000đ + 5.000đ/kg vượt (trên 0.5kg) + 300đ/km")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Ước tính cước phí thành công", resp));
    }
}
