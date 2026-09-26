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
}
