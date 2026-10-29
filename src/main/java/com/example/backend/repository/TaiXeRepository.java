package com.example.backend.repository;

import com.example.backend.entity.TaiXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiXeRepository extends JpaRepository<TaiXe, String> {

    Optional<TaiXe> findByNguoiDung_MaNd(String maNd);

    Optional<TaiXe> findByNguoiDung_TenDangNhap(String tenDangNhap);

    @Query("SELECT tx FROM TaiXe tx WHERE tx.nguoiDung.maNd = :maNd")
    Optional<TaiXe> findByMaNd(@Param("maNd") String maNd);

    boolean existsBySdt(String sdt);
    
    Optional<TaiXe> findBySdt(String sdt);
}
