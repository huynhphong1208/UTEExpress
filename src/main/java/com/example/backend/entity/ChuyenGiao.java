package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chuyen_giao")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChuyenGiao {

    @Id
    @Column(name = "ma_chuyen", length = 20)
    private String maChuyen;

    @Column(name = "loai_chuyen", nullable = false, length = 20)
    private String loaiChuyen;

    @Column(name = "ma_tuyen", length = 20)
    private String maTuyen;

    @Column(name = "ma_kho_giao", length = 20)
    private String maKhoGiao;

    @Column(name = "ma_tx", nullable = false, length = 20)
    private String maTx;

    @Column(name = "ma_pt", nullable = false, length = 20)
    private String maPt;

    @Column(name = "ma_nv_dieu_phoi", length = 20)
    private String maNvDieuPhoi;

    @Column(name = "ngay_xuat_phat", nullable = false)
    private LocalDateTime ngayXuatPhat;

    @Column(name = "ngay_den_du_kien", nullable = false)
    private LocalDateTime ngayDenDuKien;

    @Column(name = "ngay_xuat_phat_thuc")
    private LocalDateTime ngayXuatPhatThuc;

    @Column(name = "ngay_den")
    private LocalDateTime ngayDen;

    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tuyen", insertable = false, updatable = false)
    private TuyenVanChuyen tuyenVanChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho_giao", insertable = false, updatable = false)
    private Kho khoGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tx", insertable = false, updatable = false)
    private TaiXe taiXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_pt", insertable = false, updatable = false)
    private PhuongTien phuongTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nv_dieu_phoi", insertable = false, updatable = false)
    private NhanVien nhanVienDieuPhoi;
}
