package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.DonHangChiTietResponse;
import com.example.backend.service.DonHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller khách hàng - Xem đơn hàng của tôi (UC05)
 */
@Tag(name = "Khách hàng", description = "API dành riêng cho Khách hàng - Yêu cầu role KHACH_HANG")
@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class KhachHangController {

    private final DonHangService donHangService;

    @Operation(
        summary = "Xem đơn hàng của tôi (UC05)",
        description = "Lấy danh sách đơn hàng của khách hàng đang đăng nhập, có phân trang và lọc trạng thái"
    )
    @GetMapping("/don-hang")
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<Page<DonHangChiTietResponse>>> getDonHangCuaToi(
            @Parameter(description = "Lọc theo mã trạng thái (TT01-TT08), bỏ trống = tất cả", example = "TT03")
            @RequestParam(required = false) String maTt,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String maNd = authentication.getName();
        Page<DonHangChiTietResponse> result = donHangService.layDonHangCuaToi(maNd, maTt, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng thành công", result));
    }
}
