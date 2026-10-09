package com.example.backend.repository;

import com.example.backend.entity.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, String> {

    @Query("""
            SELECT d FROM DonHang d
            WHERE d.maKhGui = :maKh
              AND (:maTt IS NULL OR d.trangThai.maTrangThai = :maTt)
            """)
    Page<DonHang> findByMaKhGuiAndMaTt(@Param("maKh") String maKh,
                                        @Param("maTt") String maTt,
                                        Pageable pageable);

    /**
     * Tìm danh sách đơn hàng theo kho gửi và mã trạng thái (ví dụ đơn chờ tiếp nhận TT01)
     */
    List<DonHang> findByMaKhoGuiAndTrangThai_MaTrangThai(String maKhoGui, String maTrangThai);

    /**
     * Đếm số đơn theo mã trạng thái
     */
    long countByTrangThai_MaTrangThai(String maTrangThai);

    /**
     * Đếm số đơn theo danh sách mã trạng thái
     */
    long countByTrangThai_MaTrangThaiIn(List<String> maTrangThaiList);

    /**
     * Đếm số đơn tiếp nhận của một bưu cục
     */
    long countByMaKhoGui(String maKhoGui);

    /**
     * Đếm số đơn giao thành công của bưu cục gửi hoặc bưu cục nhận
     */
    long countByMaKhoGuiAndTrangThai_MaTrangThai(String maKhoGui, String maTrangThai);

    /**
     * Tổng phí vận chuyển của các đơn hàng trong hệ thống
     */
    @Query("SELECT COALESCE(SUM(d.phiVanChuyen), 0) FROM DonHang d")
    BigDecimal sumTongPhiVanChuyen();

    /**
     * Tổng COD của các đơn hàng trong hệ thống
     */
    @Query("SELECT COALESCE(SUM(d.cod), 0) FROM DonHang d")
    BigDecimal sumTongCod();

    List<DonHang> findByMaKhGuiOrderByNgayTaoDesc(String maKhGui);

    List<DonHang> findAllByOrderByNgayTaoDesc();
}

