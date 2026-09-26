package com.example.backend.repository;

import com.example.backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    Optional<NhanVien> findByNguoiDung_MaNd(String maNd);

    boolean existsBySdt(String sdt);

    boolean existsByEmail(String email);
}
