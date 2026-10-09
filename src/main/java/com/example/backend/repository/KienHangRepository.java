package com.example.backend.repository;

import com.example.backend.entity.KienHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KienHangRepository extends JpaRepository<KienHang, String> {

    List<KienHang> findByMaKhoHienTai(String maKhoHienTai);

    /**
     * Lấy các kiện hàng đang ở trong kho và chưa được gán vào chuyến nào đang chạy/chưa đi
     */
    @Query("SELECT k FROM KienHang k WHERE k.maKhoHienTai IS NOT NULL " +
           "AND k.maKien NOT IN (SELECT c.id.maKien FROM ChiTietChuyenGiao c " +
           "  JOIN ChuyenGiao cg ON cg.maChuyen = c.id.maChuyen WHERE cg.trangThai IN ('Chưa đi', 'Đang đi'))")
    List<KienHang> findAvailableKienHang();

    @Query("SELECT k FROM KienHang k WHERE k.maKhoHienTai = :maKho " +
           "AND k.maKien NOT IN (SELECT c.id.maKien FROM ChiTietChuyenGiao c " +
           "  JOIN ChuyenGiao cg ON cg.maChuyen = c.id.maChuyen WHERE cg.trangThai IN ('Chưa đi', 'Đang đi'))")
    List<KienHang> findAvailableKienHangByKho(@Param("maKho") String maKho);
}
