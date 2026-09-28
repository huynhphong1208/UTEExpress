package com.example.backend.service;

import com.example.backend.dto.TaoChuyenGiaoRequest;
import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChuyenGiao;
import com.example.backend.entity.TuyenVanChuyen;
import com.example.backend.repository.ChiTietChuyenGiaoRepository;
import com.example.backend.repository.ChuyenGiaoRepository;
import com.example.backend.repository.TuyenVanChuyenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DieuPhoiService {

    private final TuyenVanChuyenRepository tuyenVanChuyenRepository;
    private final ChuyenGiaoRepository chuyenGiaoRepository;
    private final ChiTietChuyenGiaoRepository chiTietChuyenGiaoRepository;

    // ========================== TUYẾN VẬN CHUYỂN (CRUD) ==========================

    public List<TuyenVanChuyen> getAllTuyen() {
        return tuyenVanChuyenRepository.findAll();
    }

    public TuyenVanChuyen getTuyenById(String maTuyen) {
        return tuyenVanChuyenRepository.findById(maTuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tuyến vận chuyển: " + maTuyen));
    }

    @Transactional
    public TuyenVanChuyen createTuyen(TuyenVanChuyen tuyen) {
        return tuyenVanChuyenRepository.save(tuyen);
    }

    @Transactional
    public TuyenVanChuyen updateTuyen(String maTuyen, TuyenVanChuyen tuyenUpdate) {
        TuyenVanChuyen existing = getTuyenById(maTuyen);
        existing.setMaKhoDi(tuyenUpdate.getMaKhoDi());
        existing.setMaKhoDen(tuyenUpdate.getMaKhoDen());
        existing.setKhoangCach(tuyenUpdate.getKhoangCach());
        existing.setThoiGianDuKienGio(tuyenUpdate.getThoiGianDuKienGio());
        return tuyenVanChuyenRepository.save(existing);
    }

    @Transactional
    public void deleteTuyen(String maTuyen) {
        if (!tuyenVanChuyenRepository.existsById(maTuyen)) {
            throw new RuntimeException("Không tìm thấy tuyến vận chuyển: " + maTuyen);
        }
        tuyenVanChuyenRepository.deleteById(maTuyen);
    }

    // ========================== CHUYẾN GIAO ==========================

    /**
     * Tạo chuyến giao mới bằng Stored Procedure.
     */
    @Transactional
    public void taoChuyenGiao(TaoChuyenGiaoRequest request) {
        chuyenGiaoRepository.taoChuyenGiao(
                request.getLoai(),
                request.getMaTuyen(),
                request.getMaKhoGiao(),
                request.getMaTx(),
                request.getMaPt(),
                request.getNgayXuatPhat(),
                request.getMaNvDp(),
                request.getMaNd(),
                request.getNgayDenDuKien(),
                null  // p_ma_chuyen INOUT, DB tự sinh
        );
    }

    /**
     * Gán kiện vào chuyến giao bằng Stored Procedure.
     */
    @Transactional
    public void ganKienVaoChuyen(String maChuyen, String maKien, String maNd) {
        chuyenGiaoRepository.ganKienVaoChuyen(maChuyen, maKien, maNd);
    }

    /**
     * Tra cứu danh sách chuyến giao theo nhân viên điều phối.
     */
    public List<ChuyenGiao> getChuyenGiaoByNvDieuPhoi(String maNvDieuPhoi) {
        return chuyenGiaoRepository.findByMaNvDieuPhoi(maNvDieuPhoi);
    }

    /**
     * Lấy chi tiết chuyến giao (danh sách kiện trong chuyến).
     */
    public List<ChiTietChuyenGiao> getChiTietChuyen(String maChuyen) {
        return chiTietChuyenGiaoRepository.findByMaChuyen(maChuyen);
    }
}
