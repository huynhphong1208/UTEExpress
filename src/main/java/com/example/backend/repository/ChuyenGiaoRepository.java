package com.example.backend.repository;

import com.example.backend.entity.ChuyenGiao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChuyenGiaoRepository extends JpaRepository<ChuyenGiao, String> {

    /**
     * Tra cứu chuyến giao theo nhân viên điều phối.
     */
    List<ChuyenGiao> findByMaNvDieuPhoi(String maNvDieuPhoi);

    /**
     * Tra cứu chuyến giao theo tài xế.
     */
    List<ChuyenGiao> findByMaTx(String maTx);

    /**
     * Tra cứu chuyến giao theo tài xế và trạng thái.
     */
    List<ChuyenGiao> findByMaTxAndTrangThai(String maTx, String trangThai);



    /**
     * Gán kiện vào chuyến giao.
     */
    @Modifying
    @Query(value = "CALL sp_gan_kien_vao_chuyen(:maChuyen, :maKien, :maNd)", nativeQuery = true)
    void ganKienVaoChuyen(
            @Param("maChuyen") String maChuyen,
            @Param("maKien") String maKien,
            @Param("maNd") String maNd
    );

    /**
     * Tài xế quét kiện lên xe.
     */
    @Modifying
    @Query(value = "CALL sp_quet_kien(:maChuyen, :maKien, :maNd, :ghiChu)", nativeQuery = true)
    void quetKien(
            @Param("maChuyen") String maChuyen,
            @Param("maKien") String maKien,
            @Param("maNd") String maNd,
            @Param("ghiChu") String ghiChu
    );

    /**
     * Tài xế xuất phát chuyến.
     */
    @Modifying
    @Query(value = "CALL sp_xuat_phat_chuyen(:maChuyen, :maNd)", nativeQuery = true)
    void xuatPhatChuyen(
            @Param("maChuyen") String maChuyen,
            @Param("maNd") String maNd
    );

    /**
     * Giao thành công.
     */
    @Modifying
    @Query(value = "CALL sp_giao_thanh_cong(:maChuyen, :maKien, :maNd, :ghiChu, :nguoiThu)", nativeQuery = true)
    void giaoThanhCong(
            @Param("maChuyen") String maChuyen,
            @Param("maKien") String maKien,
            @Param("maNd") String maNd,
            @Param("ghiChu") String ghiChu,
            @Param("nguoiThu") String nguoiThu
    );

    /**
     * Giao thất bại.
     */
    @Modifying
    @Query(value = "CALL sp_giao_that_bai(:maChuyen, :maKien, :maNd, :lyDo)", nativeQuery = true)
    void giaoThatBai(
            @Param("maChuyen") String maChuyen,
            @Param("maKien") String maKien,
            @Param("maNd") String maNd,
            @Param("lyDo") String lyDo
    );
}
