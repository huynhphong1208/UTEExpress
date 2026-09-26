package com.example.backend.repository;

import com.example.backend.entity.TaiXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiXeRepository extends JpaRepository<TaiXe, String> {

    Optional<TaiXe> findByNguoiDung_MaNd(String maNd);

    boolean existsBySdt(String sdt);
}
