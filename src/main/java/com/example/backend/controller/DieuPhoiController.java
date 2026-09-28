package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.GanKienRequest;
import com.example.backend.dto.TaoChuyenGiaoRequest;
import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChuyenGiao;
import com.example.backend.entity.TuyenVanChuyen;
import com.example.backend.service.DieuPhoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho vai trò Nhân viên điều phối.
 * Quản lý tuyến vận chuyển, tạo chuyến giao, gán kiện, tra cứu.
 */
@RestController
@RequestMapping("/api/dieu-phoi")
@RequiredArgsConstructor
public class DieuPhoiController {

    private final DieuPhoiService dieuPhoiService;

    // ========================== TUYẾN VẬN CHUYỂN (CRUD) ==========================

    @GetMapping("/tuyen")
    public ResponseEntity<ApiResponse<List<TuyenVanChuyen>>> getAllTuyen() {
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

    // ========================== CHUYẾN GIAO ==========================

    @PostMapping("/chuyen-giao")
    public ResponseEntity<ApiResponse<Void>> taoChuyenGiao(@RequestBody TaoChuyenGiaoRequest request) {
        dieuPhoiService.taoChuyenGiao(request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo chuyến giao thành công"));
    }

    @PostMapping("/chuyen-giao/gan-kien")
    public ResponseEntity<ApiResponse<Void>> ganKienVaoChuyen(@RequestBody GanKienRequest request) {
        dieuPhoiService.ganKienVaoChuyen(request.getMaChuyen(), request.getMaKien(), request.getMaNd());
        return ResponseEntity.ok(ApiResponse.ok("Gán kiện vào chuyến thành công"));
    }

    @GetMapping("/chuyen-giao")
    public ResponseEntity<ApiResponse<List<ChuyenGiao>>> getChuyenGiaoByNvDieuPhoi(
            @RequestParam String maNvDieuPhoi) {
        List<ChuyenGiao> list = dieuPhoiService.getChuyenGiaoByNvDieuPhoi(maNvDieuPhoi);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách chuyến giao thành công"));
    }

    @GetMapping("/chuyen-giao/{maChuyen}/chi-tiet")
    public ResponseEntity<ApiResponse<List<ChiTietChuyenGiao>>> getChiTietChuyen(
            @PathVariable String maChuyen) {
        List<ChiTietChuyenGiao> list = dieuPhoiService.getChiTietChuyen(maChuyen);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy chi tiết chuyến giao thành công"));
    }
}
