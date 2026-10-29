package com.example.backend.repository;

import com.example.backend.entity.LichSuTrangThai;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuTrangThaiRepository extends JpaRepository<LichSuTrangThai, String> {

    List<LichSuTrangThai> findByDonHang_MaDhOrderByThoiGianDesc(String maDh);

    List<LichSuTrangThai> findByDonHang_MaDhOrderByThoiGianAsc(String maDh);

    @Query("SELECT ls FROM LichSuTrangThai ls WHERE ls.donHang.maKhGui = :maKh OR ls.donHang.maKhNhan = :maKh ORDER BY ls.thoiGian DESC")
    List<LichSuTrangThai> findTopByKhachHang(@Param("maKh") String maKh, Pageable pageable);
}
