package com.example.backend.repository;

import com.example.backend.entity.TuyenVanChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuyenVanChuyenRepository extends JpaRepository<TuyenVanChuyen, String> {

    List<TuyenVanChuyen> findByMaKhoDi(String maKhoDi);

    List<TuyenVanChuyen> findByMaKhoDen(String maKhoDen);
    
    Optional<TuyenVanChuyen> findByMaKhoDiAndMaKhoDen(String maKhoDi, String maKhoDen);
}
