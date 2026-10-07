package com.example.backend.repository;

import com.example.backend.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {

    List<ThanhToan> findByDonHang_MaDhOrderByThoiGianDesc(String maDh);

    List<ThanhToan> findByDonHang_MaDhAndLoaiKhoan(String maDh, String loaiKhoan);

    Optional<ThanhToan> findFirstByDonHang_MaDhAndLoaiKhoanAndTrangThaiIn(String maDh, String loaiKhoan, List<String> trangThaiList);

    boolean existsByDonHang_MaDhAndLoaiKhoanAndTrangThaiIn(String maDh, String loaiKhoan, List<String> trangThaiList);

    List<ThanhToan> findByLoaiKhoanOrderByThoiGianDesc(String loaiKhoan);

    @Query("""
            SELECT COALESCE(SUM(t.soTien), 0)
            FROM ThanhToan t
            WHERE t.loaiKhoan = 'PHI_VC' AND (t.trangThai = 'DA_THANH_TOAN' OR t.trangThai = 'Đã thanh toán')
            """)
    BigDecimal sumPhiVanChuyenDaThu();

    @Query("""
            SELECT COALESCE(SUM(t.soTien), 0)
            FROM ThanhToan t
            WHERE t.loaiKhoan = 'COD' AND (t.trangThai = 'DA_THANH_TOAN' OR t.trangThai = 'Đã thanh toán')
            """)
    BigDecimal sumCodDaThu();
}
