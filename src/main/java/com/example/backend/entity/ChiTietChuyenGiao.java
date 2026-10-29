package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_chuyen_giao")
@IdClass(ChiTietChuyenGiaoId.class)
public class ChiTietChuyenGiao {

    @Id
    @Column(name = "ma_chuyen", length = 20)
    private String maChuyen;

    @Id
    @Column(name = "ma_kien", length = 20)
    private String maKien;

    @Column(name = "thoi_gian_gan", nullable = false)
    private LocalDateTime thoiGianGan;

    @Column(name = "thoi_gian_quet")
    private LocalDateTime thoiGianQuet;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuyen", insertable = false, updatable = false)
    private ChuyenGiao chuyenGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kien", insertable = false, updatable = false)
    private KienHang kienHang;

    public ChiTietChuyenGiao() {
    }

    public ChiTietChuyenGiao(String maChuyen, String maKien, LocalDateTime thoiGianGan, LocalDateTime thoiGianQuet, String trangThai, String ghiChu, ChuyenGiao chuyenGiao, KienHang kienHang) {
        this.maChuyen = maChuyen;
        this.maKien = maKien;
        this.thoiGianGan = thoiGianGan;
        this.thoiGianQuet = thoiGianQuet;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.chuyenGiao = chuyenGiao;
        this.kienHang = kienHang;
    }

    public String getMaChuyen() {
        return maChuyen;
    }

    public void setMaChuyen(String maChuyen) {
        this.maChuyen = maChuyen;
    }

    public String getMaKien() {
        return maKien;
    }

    public void setMaKien(String maKien) {
        this.maKien = maKien;
    }

    public LocalDateTime getThoiGianGan() {
        return thoiGianGan;
    }

    public void setThoiGianGan(LocalDateTime thoiGianGan) {
        this.thoiGianGan = thoiGianGan;
    }

    public LocalDateTime getThoiGianQuet() {
        return thoiGianQuet;
    }

    public void setThoiGianQuet(LocalDateTime thoiGianQuet) {
        this.thoiGianQuet = thoiGianQuet;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public ChuyenGiao getChuyenGiao() {
        return chuyenGiao;
    }

    public void setChuyenGiao(ChuyenGiao chuyenGiao) {
        this.chuyenGiao = chuyenGiao;
    }

    public KienHang getKienHang() {
        return kienHang;
    }

    public void setKienHang(KienHang kienHang) {
        this.kienHang = kienHang;
    }
}
