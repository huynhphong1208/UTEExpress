package com.example.backend.repository;

import com.example.backend.entity.KienHang;
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

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE KienHang k SET k.khoHienTai = :kho WHERE k.maKien = :maKien")
    int updateKhoHienTai(@Param("maKien") String maKien, @Param("kho") com.example.backend.entity.Kho kho);
}
