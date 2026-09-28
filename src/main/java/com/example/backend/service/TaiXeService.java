package com.example.backend.service;

import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChuyenGiao;
import com.example.backend.repository.ChiTietChuyenGiaoRepository;
import com.example.backend.repository.ChuyenGiaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiXeService {

    private final ChuyenGiaoRepository chuyenGiaoRepository;
    private final ChiTietChuyenGiaoRepository chiTietChuyenGiaoRepository;

    // ========================== TRA CỨU ==========================

    /**
     * Lấy danh sách chuyến giao của tài xế.
     */
    public List<ChuyenGiao> getChuyenGiaoByTaiXe(String maTx) {
        return chuyenGiaoRepository.findByMaTx(maTx);
    }

    /**
     * Lấy danh sách chuyến giao theo tài xế và trạng thái.
     */
    public List<ChuyenGiao> getChuyenGiaoByTaiXeAndTrangThai(String maTx, String trangThai) {
        return chuyenGiaoRepository.findByMaTxAndTrangThai(maTx, trangThai);
    }

    /**
     * Lấy chi tiết chuyến giao (danh sách kiện trong chuyến).
     */
    public List<ChiTietChuyenGiao> getChiTietChuyen(String maChuyen) {
        return chiTietChuyenGiaoRepository.findByMaChuyen(maChuyen);
    }

    // ========================== THAO TÁC (STORED PROCEDURE) ==========================

    /**
     * Quét kiện hàng lên xe.
     */
    @Transactional
    public void quetKien(String maChuyen, String maKien, String maNd, String ghiChu) {
        chuyenGiaoRepository.quetKien(maChuyen, maKien, maNd, ghiChu);
    }

    /**
     * Xuất phát chuyến.
     */
    @Transactional
    public void xuatPhatChuyen(String maChuyen, String maNd) {
        chuyenGiaoRepository.xuatPhatChuyen(maChuyen, maNd);
    }

    /**
     * Giao kiện thành công.
     */
    @Transactional
    public void giaoThanhCong(String maChuyen, String maKien, String maNd, String ghiChu, String nguoiThu) {
        chuyenGiaoRepository.giaoThanhCong(maChuyen, maKien, maNd, ghiChu, nguoiThu);
    }

    /**
     * Giao kiện thất bại.
     */
    @Transactional
    public void giaoThatBai(String maChuyen, String maKien, String maNd, String lyDo) {
        chuyenGiaoRepository.giaoThatBai(maChuyen, maKien, maNd, lyDo);
    }
}
