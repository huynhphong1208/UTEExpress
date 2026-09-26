package com.example.backend.repository;

import com.example.backend.entity.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, String> {

    @Query("""
            SELECT d FROM DonHang d
            WHERE d.maKhGui = :maKh
              AND (:maTt IS NULL OR d.maTt = :maTt)
            """)
    Page<DonHang> findByMaKhGuiAndMaTt(@Param("maKh") String maKh,
                                        @Param("maTt") String maTt,
                                        Pageable pageable);
}
