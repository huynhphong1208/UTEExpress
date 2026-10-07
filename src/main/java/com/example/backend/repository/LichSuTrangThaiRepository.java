package com.example.backend.repository;

import com.example.backend.entity.LichSuTrangThai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuTrangThaiRepository extends JpaRepository<LichSuTrangThai, String> {

    List<LichSuTrangThai> findByDonHang_MaDhOrderByThoiGianDesc(String maDh);

    List<LichSuTrangThai> findByDonHang_MaDhOrderByThoiGianAsc(String maDh);
}
