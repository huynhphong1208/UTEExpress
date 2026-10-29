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
    private final com.example.backend.repository.KhachHangRepository khachHangRepository;
    private final com.example.backend.repository.NguoiDungRepository nguoiDungRepository;
    private final com.example.backend.repository.LichSuTrangThaiRepository lichSuTrangThaiRepository;

    @Operation(
        summary = "Danh sách thông báo cập nhật đơn hàng gần đây",
        description = "Lấy các thông báo biến động trạng thái đơn hàng của khách hàng đang đăng nhập (sắp xếp mới nhất trước)"
    )
    @GetMapping("/thong-bao")
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<java.util.List<com.example.backend.dto.customer.CustomerNotificationResponse>>> getThongBaoCuaToi(
            @RequestParam(defaultValue = "15") int limit,
            Authentication authentication) {
        String username = authentication.getName();
        com.example.backend.entity.KhachHang kh = khachHangRepository.findBySdt(username)
                .or(() -> khachHangRepository.findByNguoiDung_MaNd(username))
                .or(() -> nguoiDungRepository.findByTenDangNhap(username)
                        .flatMap(nd -> khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd())
                                .or(() -> khachHangRepository.findBySdt(nd.getTenDangNhap()))))
                .orElseThrow(() -> new com.example.backend.exception.BusinessException("Không tìm thấy hồ sơ khách hàng", 404));

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, Math.min(Math.max(1, limit), 50));
        java.util.List<com.example.backend.entity.LichSuTrangThai> list = lichSuTrangThaiRepository.findTopByKhachHang(kh.getMaKh(), pageable);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm");
        java.util.List<com.example.backend.dto.customer.CustomerNotificationResponse> response = list.stream().map(ls -> {
            String timeDisplay = ls.getThoiGian() != null ? ls.getThoiGian().format(dtf) : "";
            return com.example.backend.dto.customer.CustomerNotificationResponse.builder()
                    .maLs(ls.getMaLs())
                    .maDh(ls.getDonHang() != null ? ls.getDonHang().getMaDh() : null)
                    .maTrangThai(ls.getTrangThai() != null ? ls.getTrangThai().getMaTrangThai() : null)
                    .tenTrangThai(ls.getTrangThai() != null ? ls.getTrangThai().getTenTrangThai() : "Cập nhật trạng thái")
                    .ghiChu(ls.getGhiChu())
                    .thoiGian(ls.getThoiGian())
                    .thoiGianDisplay(timeDisplay)
                    .tenNguoiNhan(ls.getDonHang() != null ? ls.getDonHang().getTenNguoiNhan() : "")
                    .build();
        }).toList();

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", response));
    }

    @Operation(
        summary = "Thông tin hồ sơ khách hàng hiện tại",
        description = "Lấy họ tên, SĐT, địa chỉ mặc định của khách hàng đang đăng nhập"
    )
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<com.example.backend.entity.KhachHang>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        com.example.backend.entity.KhachHang kh = khachHangRepository.findBySdt(username)
                .or(() -> khachHangRepository.findByNguoiDung_MaNd(username))
                .or(() -> nguoiDungRepository.findByTenDangNhap(username)
                        .flatMap(nd -> khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd())
                                .or(() -> khachHangRepository.findBySdt(nd.getTenDangNhap()))))
                .orElseThrow(() -> new com.example.backend.exception.BusinessException("Không tìm thấy hồ sơ khách hàng", 404));
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", kh));
    }

    @Operation(
        summary = "Xem đơn hàng của tôi (UC05)",
        description = "Lấy danh sách đơn hàng của khách hàng đang đăng nhập, có phân trang, lọc trạng thái và tìm kiếm theo mã đơn / SĐT / người nhận"
    )
    @GetMapping("/don-hang")
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<Page<DonHangChiTietResponse>>> getDonHangCuaToi(
            @Parameter(description = "Lọc theo mã trạng thái (TT01-TT08), bỏ trống = tất cả", example = "TT03")
            @RequestParam(required = false) String maTt,
            @Parameter(description = "Từ khóa tìm kiếm (mã đơn, người nhận, SĐT nhận)", example = "DH000001")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String maNd = authentication.getName();
        Page<DonHangChiTietResponse> result = donHangService.layDonHangCuaToi(maNd, maTt, keyword, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng thành công", result));
    }

    @Operation(
        summary = "Chi tiết một đơn hàng theo mã",
        description = "Lấy thông tin chi tiết đầy đủ của đơn hàng theo mã"
    )
    @GetMapping("/don-hang/{maDh}")
    @PreAuthorize("hasAuthority('KHACH_HANG')")
    public ResponseEntity<ApiResponse<DonHangChiTietResponse>> getChiTietDonHang(
            @PathVariable String maDh) {
        DonHangChiTietResponse result = donHangService.layChiTietDonHang(maDh);
        if (result == null) {
            throw new com.example.backend.exception.BusinessException("Không tìm thấy đơn hàng: " + maDh, 404);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", result));
    }
}
