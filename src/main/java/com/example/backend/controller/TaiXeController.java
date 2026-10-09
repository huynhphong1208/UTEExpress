package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChuyenGiao;
import com.example.backend.service.TaiXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho vai trò Tài xế.
 * Nhận/tra cứu chuyến, quét kiện, xuất phát, giao thành công/thất bại và dashboard.
 */
@RestController
@RequestMapping("/api/tai-xe")
@RequiredArgsConstructor
public class TaiXeController {

    private final TaiXeService taiXeService;

    // ========================== DASHBOARD ==========================

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<TaiXeDashboardDTO>> getDashboard(
            @RequestParam(required = false) String maTx) {
        TaiXeDashboardDTO stats = taiXeService.getDashboardStats(maTx);
        return ResponseEntity.ok(ApiResponse.ok(stats, "Lấy dữ liệu Dashboard Tài xế thành công"));
    }

    // ========================== TRA CỨU ==========================

    @GetMapping("/chuyen-giao")
    public ResponseEntity<ApiResponse<List<ChuyenGiao>>> getChuyenGiao(
            @RequestParam String maTx,
            @RequestParam(required = false) String trangThai) {
        List<ChuyenGiao> list;
        if (trangThai != null && !trangThai.isBlank()) {
            list = taiXeService.getChuyenGiaoByTaiXeAndTrangThai(maTx, trangThai);
        } else {
            list = taiXeService.getChuyenGiaoByTaiXe(maTx);
        }
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến giao thành công"));
    }

    @GetMapping("/chuyen-phan-cong")
    public ResponseEntity<ApiResponse<List<ChuyenGiaoChiTietDTO>>> getChuyenPhanCong(
            @RequestParam String maTx) {
        List<ChuyenGiaoChiTietDTO> list = taiXeService.getChuyenPhanCongByTaiXe(maTx);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến phân công thành công"));
    }

    @GetMapping("/kien-can-giao")
    public ResponseEntity<ApiResponse<List<KienTrongChuyenDTO>>> getKienCanGiao(
            @RequestParam String maTx) {
        List<KienTrongChuyenDTO> list = taiXeService.getKienCanGiaoByTaiXe(maTx);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách kiện cần giao thành công"));
    }

    @GetMapping("/kien-da-giao")
    public ResponseEntity<ApiResponse<List<KienTrongChuyenDTO>>> getKienDaGiao(
            @RequestParam String maTx) {
        List<KienTrongChuyenDTO> list = taiXeService.getKienDaGiaoByTaiXe(maTx);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách kiện đã giao thành công"));
    }

    @GetMapping("/chuyen-giao/{maChuyen}/chi-tiet")
    public ResponseEntity<ApiResponse<List<ChiTietChuyenGiao>>> getChiTietChuyen(
            @PathVariable String maChuyen) {
        List<ChiTietChuyenGiao> list = taiXeService.getChiTietChuyen(maChuyen);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy chi tiết chuyến giao thành công"));
    }

    @GetMapping("/chuyen-giao/{maChuyen}/kien-hang")
    public ResponseEntity<ApiResponse<List<KienTrongChuyenDTO>>> getKienTrongChuyen(
            @PathVariable String maChuyen) {
        List<KienTrongChuyenDTO> list = taiXeService.getKienTrongChuyen(maChuyen);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách kiện hàng trong chuyến thành công"));
    }

    @GetMapping("/chuyen-giao/{maChuyen}/chi-tiet-day-du")
    public ResponseEntity<ApiResponse<ChuyenGiaoChiTietDTO>> getChuyenGiaoChiTiet(
            @PathVariable String maChuyen) {
        ChuyenGiaoChiTietDTO dto = taiXeService.getChuyenGiaoChiTiet(maChuyen);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Lấy thông tin chuyến giao chi tiết thành công"));
    }

    // ========================== THAO TÁC ==========================

    @PostMapping("/quet-kien")
    public ResponseEntity<ApiResponse<Void>> quetKien(@RequestBody QuetKienRequest request) {
        taiXeService.quetKien(
                request.getMaChuyen(), request.getMaKien(),
                request.getMaNd(), request.getGhiChu());
        return ResponseEntity.ok(ApiResponse.ok("Quét kiện thành công"));
    }

    @PostMapping("/xuat-phat")
    public ResponseEntity<ApiResponse<Void>> xuatPhatChuyen(@RequestBody XuatPhatChuyenRequest request) {
        taiXeService.xuatPhatChuyen(request.getMaChuyen(), request.getMaNd());
        return ResponseEntity.ok(ApiResponse.ok("Xuất phát chuyến thành công"));
    }

    @PostMapping("/giao-thanh-cong")
    public ResponseEntity<ApiResponse<Void>> giaoThanhCong(@RequestBody GiaoThanhCongRequest request) {
        taiXeService.giaoThanhCong(
                request.getMaChuyen(), request.getMaKien(),
                request.getMaNd(), request.getGhiChu(), request.getNguoiThu());
        return ResponseEntity.ok(ApiResponse.ok("Giao hàng thành công"));
    }

    @PostMapping("/giao-that-bai")
    public ResponseEntity<ApiResponse<Void>> giaoThatBai(@RequestBody GiaoThatBaiRequest request) {
        taiXeService.giaoThatBai(
                request.getMaChuyen(), request.getMaKien(),
                request.getMaNd(), request.getLyDo());
        return ResponseEntity.ok(ApiResponse.ok("Đã ghi nhận giao thất bại"));
    }

    @PostMapping("/chuyen-giao/{maChuyen}/hoan-thanh")
    public ResponseEntity<ApiResponse<Void>> hoanThanhChuyen(
            @PathVariable String maChuyen,
            @RequestParam(required = false) String maNd) {
        taiXeService.hoanThanhChuyen(maChuyen, maNd);
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận hoàn thành chuyến giao thành công"));
    }
}
