package com.example.backend.controller;

import com.example.backend.dto.common.*;
import com.example.backend.dto.chuyengiao.*;
import com.example.backend.dto.dieuphoi.*;
import com.example.backend.entity.*;
import com.example.backend.service.DieuPhoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho vai trò Nhân viên điều phối.
 * Quản lý tuyến vận chuyển, tạo chuyến giao, gán kiện, tra cứu và dashboard.
 */
@RestController
@RequestMapping("/api/dieu-phoi")
@RequiredArgsConstructor
public class DieuPhoiController {

    private final DieuPhoiService dieuPhoiService;

    // ========================== DASHBOARD ==========================

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DieuPhoiDashboardDTO>> getDashboard() {
        DieuPhoiDashboardDTO stats = dieuPhoiService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats, "Lấy dữ liệu Dashboard thành công"));
    }

    @GetMapping("/don-can-dieu-phoi")
    public ResponseEntity<ApiResponse<?>> getDonCanDieuPhoi(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String maKho,
            @RequestParam(required = false) String loaiChuyen,
            @RequestParam(required = false) String maTuyen,
            @RequestParam(required = false) String maKhoNhan) {
        if (page != null && size != null) {
            PageResponse<DonCanDieuPhoiDTO> paged = dieuPhoiService.getDonCanDieuPhoiPaged(page, size, maKho, loaiChuyen, maTuyen, maKhoNhan);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách đơn cần điều phối phân trang thành công"));
        }
        List<DonCanDieuPhoiDTO> list = (maKhoNhan != null && !maKhoNhan.isBlank()) 
                ? dieuPhoiService.getAvailableKienHangDTOList(maKho, loaiChuyen, maTuyen, maKhoNhan)
                : dieuPhoiService.getDonCanDieuPhoiList();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách đơn cần điều phối thành công"));
    }

    // ========================== ĐIỀU PHỐI LẤY HÀNG ==========================

    @GetMapping("/don-can-lay")
    public ResponseEntity<ApiResponse<List<DonHang>>> getDonCanLay(
            @RequestParam(required = false) String maKho) {
        List<DonHang> list = dieuPhoiService.getDonHangCanLay(maKho);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách đơn cần lấy thành công"));
    }

    @PostMapping("/assign-pickup")
    public ResponseEntity<ApiResponse<DonHang>> assignPickup(@RequestBody java.util.Map<String, String> body) {
        String maDh = body.get("maDh");
        String maTx = body.get("maTx");
        if (maDh == null || maDh.isBlank() || maTx == null || maTx.isBlank()) {
            throw new RuntimeException("Vui lòng cung cấp đầy đủ maDh và maTx");
        }
        DonHang updated = dieuPhoiService.assignTaiXeLayHang(maDh, maTx);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Phân công tài xế " + maTx + " lấy đơn " + maDh + " thành công"));
    }

    @GetMapping("/chuyen-dang-chay")
    public ResponseEntity<ApiResponse<?>> getChuyenDangChay(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<ChuyenGiaoChiTietDTO> paged = dieuPhoiService.getChuyenDangChayPaged(page, size);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách chuyến đang chạy phân trang thành công"));
        }
        List<ChuyenGiaoChiTietDTO> list = dieuPhoiService.getChuyenDangChayList();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến đang chạy thành công"));
    }

    @GetMapping("/tai-xe-kha-dung")
    public ResponseEntity<ApiResponse<?>> getTaiXeKhaDung(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<TaiXeKhaDungDTO> paged = dieuPhoiService.getTaiXeKhaDungPaged(page, size);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách tài xế khả dụng phân trang thành công"));
        }
        List<TaiXeKhaDungDTO> list = dieuPhoiService.getTaiXeKhaDungList();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách tài xế khả dụng thành công"));
    }

    @GetMapping("/phuong-tien-kha-dung")
    public ResponseEntity<ApiResponse<?>> getPhuongTienKhaDung(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<PhuongTienKhaDungDTO> paged = dieuPhoiService.getPhuongTienKhaDungPaged(page, size);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách phương tiện khả dụng phân trang thành công"));
        }
        List<PhuongTienKhaDungDTO> list = dieuPhoiService.getPhuongTienKhaDungList();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách phương tiện khả dụng thành công"));
    }

    // ========================== TUYẾN VẬN CHUYỂN (CRUD) ==========================

    @GetMapping("/tuyen")
    public ResponseEntity<ApiResponse<?>> getAllTuyen(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        if (page != null && size != null) {
            PageResponse<TuyenVanChuyen> paged = dieuPhoiService.getTuyenPaged(page, size, search);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách tuyến phân trang thành công"));
        }
        List<TuyenVanChuyen> list = dieuPhoiService.getAllTuyen();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách tuyến thành công"));
    }

    @GetMapping("/tuyen/{maTuyen}")
    public ResponseEntity<ApiResponse<TuyenVanChuyen>> getTuyenById(@PathVariable String maTuyen) {
        TuyenVanChuyen tuyen = dieuPhoiService.getTuyenById(maTuyen);
        return ResponseEntity.ok(ApiResponse.ok(tuyen));
    }

    @PostMapping("/tuyen")
    public ResponseEntity<ApiResponse<TuyenVanChuyen>> createTuyen(@RequestBody TuyenVanChuyen tuyen) {
        TuyenVanChuyen created = dieuPhoiService.createTuyen(tuyen);
        return ResponseEntity.ok(ApiResponse.ok(created, "Tạo tuyến vận chuyển thành công"));
    }

    @PutMapping("/tuyen/{maTuyen}")
    public ResponseEntity<ApiResponse<TuyenVanChuyen>> updateTuyen(
            @PathVariable String maTuyen,
            @RequestBody TuyenVanChuyen tuyen) {
        TuyenVanChuyen updated = dieuPhoiService.updateTuyen(maTuyen, tuyen);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật tuyến thành công"));
    }

    @DeleteMapping("/tuyen/{maTuyen}")
    public ResponseEntity<ApiResponse<Void>> deleteTuyen(@PathVariable String maTuyen) {
        dieuPhoiService.deleteTuyen(maTuyen);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tuyến vận chuyển thành công"));
    }

    // ========================== DANH MỤC HỖ TRỢ ĐIỀU PHỐI ==========================

    @GetMapping("/kho")
    public ResponseEntity<ApiResponse<List<Kho>>> getAllKho() {
        List<Kho> list = dieuPhoiService.getAllKho();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách kho thành công"));
    }

    @GetMapping("/tai-xe")
    public ResponseEntity<ApiResponse<List<TaiXe>>> getAllTaiXe() {
        List<TaiXe> list = dieuPhoiService.getAllTaiXe();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách tài xế thành công"));
    }

    @GetMapping("/phuong-tien")
    public ResponseEntity<ApiResponse<List<PhuongTien>>> getAllPhuongTien() {
        List<PhuongTien> list = dieuPhoiService.getAllPhuongTien();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách phương tiện thành công"));
    }

    @GetMapping("/kien-hang-kha-dung")
    public ResponseEntity<ApiResponse<List<DonCanDieuPhoiDTO>>> getAvailableKienHang(
            @RequestParam(required = false) String maKho,
            @RequestParam(required = false) String loaiChuyen,
            @RequestParam(required = false) String maTuyen) {
        List<DonCanDieuPhoiDTO> list = dieuPhoiService.getAvailableKienHangDTOList(maKho, loaiChuyen, maTuyen);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách kiện hàng khả dụng thành công"));
    }

    // ========================== CHUYẾN GIAO ==========================

    @PostMapping("/chuyen-giao")
    public ResponseEntity<ApiResponse<String>> taoChuyenGiao(@RequestBody TaoChuyenGiaoRequest request) {
        String maChuyen = dieuPhoiService.taoChuyenGiao(request);
        return ResponseEntity.ok(ApiResponse.ok(maChuyen, "Tạo chuyến giao thành công"));
    }

    @PostMapping("/chuyen-giao/gan-kien")
    public ResponseEntity<ApiResponse<Void>> ganKienVaoChuyen(@RequestBody GanKienRequest request) {
        dieuPhoiService.ganKienVaoChuyen(request.getMaChuyen(), request.getMaKien(), request.getMaNd());
        return ResponseEntity.ok(ApiResponse.ok("Gán kiện vào chuyến thành công"));
    }

    @GetMapping("/chuyen-giao")
    public ResponseEntity<ApiResponse<?>> getChuyenGiao(
            @RequestParam(required = false) String maNvDieuPhoi,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<ChuyenGiao> paged = dieuPhoiService.getChuyenGiaoPaged(maNvDieuPhoi, page, size);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách chuyến giao phân trang thành công"));
        }
        List<ChuyenGiao> list = dieuPhoiService.getChuyenGiaoByNvDieuPhoi(maNvDieuPhoi);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến giao thành công"));
    }

    @GetMapping("/chuyen-giao-chi-tiet")
    public ResponseEntity<ApiResponse<?>> getChuyenGiaoChiTiet(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String search) {
        if (page != null && size != null) {
            PageResponse<ChuyenGiaoChiTietDTO> paged = dieuPhoiService.getChuyenGiaoChiTietPaged(page, size, trangThai, search);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy danh sách chuyến giao chi tiết phân trang thành công"));
        }
        List<ChuyenGiaoChiTietDTO> list = dieuPhoiService.getChuyenGiaoChiTietList();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến giao chi tiết thành công"));
    }

    @GetMapping("/chuyen-giao/{maChuyen}/chi-tiet")
    public ResponseEntity<ApiResponse<?>> getChiTietChuyen(
            @PathVariable String maChuyen,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PageResponse<KienTrongChuyenDTO> paged = dieuPhoiService.getKienTrongChuyenPaged(maChuyen, page, size);
            return ResponseEntity.ok(ApiResponse.ok(paged, "Lấy chi tiết kiện hàng trong chuyến phân trang thành công"));
        }
        List<KienTrongChuyenDTO> list = dieuPhoiService.getKienTrongChuyen(maChuyen);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy chi tiết kiện hàng trong chuyến thành công"));
    }

    @PutMapping("/chuyen-giao/{maChuyen}")
    public ResponseEntity<ApiResponse<ChuyenGiao>> capNhatChuyenGiao(
            @PathVariable String maChuyen,
            @RequestBody CapNhatChuyenGiaoRequest request) {
        ChuyenGiao cg = dieuPhoiService.capNhatChuyenGiao(maChuyen, request);
        return ResponseEntity.ok(ApiResponse.ok(cg, "Cập nhật phân công chuyến giao thành công"));
    }

    @PostMapping("/chuyen-giao/{maChuyen}/huy")
    public ResponseEntity<ApiResponse<Void>> huyChuyenGiao(
            @PathVariable String maChuyen,
            @RequestParam(required = false, defaultValue = "ND004") String maNd) {
        dieuPhoiService.huyChuyenGiao(maChuyen, maNd);
        return ResponseEntity.ok(ApiResponse.ok("Hủy chuyến giao thành công"));
    }

    @DeleteMapping("/chuyen-giao/{maChuyen}")
    public ResponseEntity<ApiResponse<Void>> xoaChuyenGiao(
            @PathVariable String maChuyen,
            @RequestParam(required = false, defaultValue = "ND004") String maNd) {
        dieuPhoiService.xoaChuyenGiao(maChuyen, maNd);
        return ResponseEntity.ok(ApiResponse.ok("Xóa chuyến giao thành công"));
    }

    @PostMapping("/chuyen-giao/{maChuyen}/hoan-thanh")
    public ResponseEntity<ApiResponse<Void>> hoanThanhChuyen(
            @PathVariable String maChuyen,
            @RequestParam(required = false, defaultValue = "ND004") String maNd) {
        dieuPhoiService.hoanThanhChuyen(maChuyen, maNd);
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận hoàn thành chuyến giao thành công"));
    }
}
