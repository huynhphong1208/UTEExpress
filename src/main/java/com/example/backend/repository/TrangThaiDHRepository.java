package com.example.backend.repository;

import com.example.backend.entity.TrangThaiDH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrangThaiDHRepository extends JpaRepository<TrangThaiDH, String> {
    Optional<TrangThaiDH> findByMaTrangThai(String maTrangThai);
    Optional<TrangThaiDH> findByTenTrangThai(String tenTrangThai);
}
