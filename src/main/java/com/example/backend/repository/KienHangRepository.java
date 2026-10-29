package com.example.backend.repository;

import com.example.backend.entity.KienHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KienHangRepository extends JpaRepository<KienHang, String> {

    Optional<KienHang> findByMaKien(String maKien);

    List<KienHang> findByDonHang_MaDh(String maDh);

    long countByKhoHienTai_MaKho(String maKho);

    @Query("SELECT COUNT(k) FROM KienHang k WHERE k.khoHienTai IS NOT NULL")
    long countTongKienLuuKho();

    @Query("SELECT k FROM KienHang k LEFT JOIN FETCH k.donHang LEFT JOIN FETCH k.khoHienTai")
    List<KienHang> findAllWithDonHangAndKho();

    @Query(value = "SELECT k FROM KienHang k LEFT JOIN FETCH k.donHang LEFT JOIN FETCH k.khoHienTai",
           countQuery = "SELECT COUNT(k) FROM KienHang k")
    Page<KienHang> findAllWithDonHangAndKho(Pageable pageable);

    @Query(value = "SELECT k FROM KienHang k LEFT JOIN FETCH k.donHang LEFT JOIN FETCH k.khoHienTai WHERE k.khoHienTai.maKho = :maKho",
           countQuery = "SELECT COUNT(k) FROM KienHang k WHERE k.khoHienTai.maKho = :maKho")
    Page<KienHang> findByKhoHienTaiWithDonHangAndKho(@Param("maKho") String maKho, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE KienHang k SET k.khoHienTai = :kho WHERE k.maKien = :maKien")
    int updateKhoHienTai(@Param("maKien") String maKien, @Param("kho") com.example.backend.entity.Kho kho);

    @Query("SELECT k FROM KienHang k WHERE k.khoHienTai.maKho = :maKhoHienTai")
    List<KienHang> findByMaKhoHienTai(@Param("maKhoHienTai") String maKhoHienTai);

    List<KienHang> findByKhoHienTai_MaKho(String maKhoHienTai);

    /**
     * Lấy các kiện hàng đang ở trong kho và chưa được gán vào chuyến nào đang chạy/chưa đi
     */
    @Query("SELECT k FROM KienHang k WHERE k.khoHienTai IS NOT NULL " +
           "AND k.maKien NOT IN (SELECT c.id.maKien FROM ChiTietChuyenGiao c " +
           "  JOIN ChuyenGiao cg ON cg.maChuyen = c.id.maChuyen WHERE cg.trangThai IN ('Chưa đi', 'Đang đi'))")
    List<KienHang> findAvailableKienHang();

    @Query("SELECT k FROM KienHang k WHERE k.khoHienTai.maKho = :maKho " +
           "AND k.maKien NOT IN (SELECT c.id.maKien FROM ChiTietChuyenGiao c " +
           "  JOIN ChuyenGiao cg ON cg.maChuyen = c.id.maChuyen WHERE cg.trangThai IN ('Chưa đi', 'Đang đi'))")
    List<KienHang> findAvailableKienHangByKho(@Param("maKho") String maKho);
}
