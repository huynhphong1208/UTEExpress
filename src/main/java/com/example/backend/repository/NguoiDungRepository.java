package com.example.backend.repository;

import com.example.backend.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    @Query("""
            SELECT n FROM NguoiDung n
            WHERE (:keyword IS NULL OR
                   LOWER(n.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:maVaiTro IS NULL OR n.vaiTro.maVaiTro = :maVaiTro)
            """)
    Page<NguoiDung> searchUsers(@Param("keyword") String keyword,
                                @Param("maVaiTro") String maVaiTro,
                                Pageable pageable);
}
