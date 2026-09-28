package com.example.backend.repository;

import com.example.backend.entity.TuyenVanChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuyenVanChuyenRepository extends JpaRepository<TuyenVanChuyen, String> {

    List<TuyenVanChuyen> findByMaKhoDi(String maKhoDi);

    List<TuyenVanChuyen> findByMaKhoDen(String maKhoDen);
}
