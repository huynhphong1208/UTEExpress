package com.example.backend.repository;

import com.example.backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    boolean existsBySdt(String sdt);

    boolean existsByEmail(String email);

    Optional<KhachHang> findBySdt(String sdt);

    Optional<KhachHang> findByNguoiDung_MaNd(String maNd);
}
