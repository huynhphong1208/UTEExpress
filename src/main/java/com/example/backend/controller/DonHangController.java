package com.example.backend.controller;

import com.example.backend.dto.request.TaoDonHangRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.DonHangChiTietResponse;
import com.example.backend.service.DonHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller tạo đơn hàng (UC04) - yêu cầu role KHACH_HANG
 */
@Tag(name = "Đơn hàng", description = "API Tạo đơn hàng - Yêu cầu đăng nhập với vai trò KHACH_HANG")
@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DonHangController {

    private final DonHangService donHangService;

    @Operation(
        summary = "Tạo đơn hàng mới (UC04)",
        description = "Lấy mã khách hàng từ JWT → Gọi stored procedure sp_tao_don_hang → Trả về mã đơn hàng"
    )
    @PostMapping
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<String>> taoDonHang(
            @Valid @RequestBody TaoDonHangRequest request,
            Authentication authentication) {

        String maNd = authentication.getName(); // username = ten_dang_nhap = SĐT
        String maDh = donHangService.taoDonHang(maNd, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn hàng thành công", maDh));
    }
}
