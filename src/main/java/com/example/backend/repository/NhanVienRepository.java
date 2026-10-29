package com.example.backend.repository;

import com.example.backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    Optional<NhanVien> findByNguoiDung_MaNd(String maNd);

    Optional<NhanVien> findByNguoiDung_TenDangNhap(String tenDangNhap);

    boolean existsBySdt(String sdt);

    boolean existsByEmail(String email);

    Optional<NhanVien> findBySdt(String sdt);

    java.util.List<NhanVien> findByMaKho(String maKho);

    Optional<NhanVien> findFirstByMaKhoAndChucVu(String maKho, String chucVu);

    Optional<NhanVien> findFirstByChucVu(String chucVu);
}
