package com.example.backend.repository;

import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChiTietChuyenGiaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietChuyenGiaoRepository extends JpaRepository<ChiTietChuyenGiao, ChiTietChuyenGiaoId> {

    /**
     * Lấy danh sách chi tiết kiện hàng theo mã chuyến.
     */
    List<ChiTietChuyenGiao> findByMaChuyen(String maChuyen);
}
