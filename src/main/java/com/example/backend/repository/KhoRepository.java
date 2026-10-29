package com.example.backend.repository;

import com.example.backend.entity.Kho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhoRepository extends JpaRepository<Kho, String> {
    Optional<Kho> findByMaKho(String maKho);
}
