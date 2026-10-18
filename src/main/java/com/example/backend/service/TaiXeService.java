package com.example.backend.service;

import com.example.backend.dto.common.*;
import com.example.backend.dto.chuyengiao.*;
import com.example.backend.dto.taixe.*;
import com.example.backend.entity.ChiTietChuyenGiao;
import com.example.backend.entity.ChuyenGiao;
import com.example.backend.entity.TaiXe;
import com.example.backend.repository.ChiTietChuyenGiaoRepository;
import com.example.backend.repository.ChuyenGiaoRepository;
import com.example.backend.repository.TaiXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaiXeService {

    private final ChuyenGiaoRepository chuyenGiaoRepository;
    private final ChiTietChuyenGiaoRepository chiTietChuyenGiaoRepository;
    private final TaiXeRepository taiXeRepository;
    private final JdbcTemplate jdbcTemplate;

    // ========================== TRA CỨU ==========================

    public String resolveMaTx(String maTx) {
        if (maTx == null || maTx.isBlank()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String currentUsername = auth.getName();
                try {
                    List<String> list = jdbcTemplate.query(
                        "SELECT tx.ma_tx FROM tai_xe tx JOIN nguoi_dung nd ON tx.ma_nd = nd.ma_nd WHERE nd.ten_dang_nhap = ?",
                        (rs, rowNum) -> rs.getString("ma_tx"),
                        currentUsername
                    );
                    if (!list.isEmpty()) {
                        return list.get(0);
                    }
                } catch (Exception ignored) {}
            }
            return "";
        }
        String clean = maTx.trim();
        // 1. Kiểm tra trực tiếp theo mã TX trong database (chính xác hoặc chữ hoa)
        if (taiXeRepository.existsById(clean)) {
            return clean;
        }
        if (taiXeRepository.existsById(clean.toUpperCase())) {
            return clean.toUpperCase();
        }
        // 2. Tìm theo tên đăng nhập (nguoi_dung.ten_dang_nhap)
        try {
            List<String> list = jdbcTemplate.query(
                "SELECT tx.ma_tx FROM tai_xe tx JOIN nguoi_dung nd ON tx.ma_nd = nd.ma_nd WHERE nd.ten_dang_nhap = ?",
                (rs, rowNum) -> rs.getString("ma_tx"),
                clean
            );
            if (!list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception ignored) {}
        // 3. Tìm theo mã người dùng (nguoi_dung.ma_nd)
        var byMaNd = taiXeRepository.findByMaNd(clean);
        if (byMaNd.isPresent()) {
            return byMaNd.get().getMaTx();
        }
        return clean.toUpperCase();
    }

    /**
     * Lấy danh sách chuyến giao của tài xế.
     */
    public List<ChuyenGiao> getChuyenGiaoByTaiXe(String maTx) {
        return chuyenGiaoRepository.findByMaTx(resolveMaTx(maTx));
    }

    /**
     * Lấy danh sách chuyến giao theo tài xế và trạng thái.
     */
    public List<ChuyenGiao> getChuyenGiaoByTaiXeAndTrangThai(String maTx, String trangThai) {
        return chuyenGiaoRepository.findByMaTxAndTrangThai(resolveMaTx(maTx), trangThai);
    }

    /**
     * Lấy chi tiết chuyến giao (entity thô).
     */
    public List<ChiTietChuyenGiao> getChiTietChuyen(String maChuyen) {
        return chiTietChuyenGiaoRepository.findByMaChuyen(maChuyen);
    }

    /**
     * Lấy danh sách kiện hàng trong chuyến với đầy đủ thông tin:
     * Mã kiện, Mã đơn, Tên người nhận, SĐT, Địa chỉ giao, COD, Khối lượng, Trạng thái.
     */
    public List<KienTrongChuyenDTO> getKienTrongChuyen(String maChuyen) {
        String sql = """
            SELECT x.ma_chuyen, c.ma_tx, x.ma_kien, k.ma_dh,
                   COALESCE(d.ten_nguoi_nhan, 'Khách hàng') AS ten_nguoi_nhan,
                   COALESCE(d.sdt_nhan, '') AS sdt_nhan,
                   COALESCE(d.dia_chi_giao, '') AS dia_chi_giao,
                   k.khoi_luong, k.loai_hang, x.trang_thai,
                   x.thoi_gian_gan, x.thoi_gian_quet, x.ghi_chu,
                   COALESCE(d.cod, 0) AS cod
            FROM chi_tiet_chuyen_giao x
            JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen
            JOIN kien_hang k ON k.ma_kien = x.ma_kien
            LEFT JOIN don_hang d ON d.ma_dh = k.ma_dh
            WHERE x.ma_chuyen = ?
            ORDER BY x.thoi_gian_gan ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp tsGan = rs.getTimestamp("thoi_gian_gan");
            Timestamp tsQuet = rs.getTimestamp("thoi_gian_quet");

            return KienTrongChuyenDTO.builder()
                    .maChuyen(rs.getString("ma_chuyen"))
                    .maTx(rs.getString("ma_tx"))
                    .maKien(rs.getString("ma_kien"))
                    .maDh(rs.getString("ma_dh"))
                    .tenNguoiNhan(rs.getString("ten_nguoi_nhan"))
                    .sdtNhan(rs.getString("sdt_nhan"))
                    .diaChiGiao(rs.getString("dia_chi_giao"))
                    .khoiLuong(rs.getBigDecimal("khoi_luong"))
                    .loaiHang(rs.getString("loai_hang"))
                    .trangThai(rs.getString("trang_thai"))
                    .thoiGianGan(tsGan != null ? tsGan.toLocalDateTime() : null)
                    .thoiGianQuet(tsQuet != null ? tsQuet.toLocalDateTime() : null)
                    .ghiChu(rs.getString("ghi_chu"))
                    .cod(rs.getBigDecimal("cod"))
                    .build();
        }, maChuyen);
    }

    /**
     * Lấy thông tin chuyến giao chi tiết (cho tài xế xem trên trang chi tiết chuyến).
     */
    public ChuyenGiaoChiTietDTO getChuyenGiaoChiTiet(String maChuyen) {
        String sql = """
            SELECT c.ma_chuyen, c.loai_chuyen, c.trang_thai, c.ma_tuyen,
                   COALESCE(t.ma_kho_di, c.ma_kho_giao) AS ma_kho_xuat_phat,
                   COALESCE(kdi.ten_kho, 'Kho xuất phát') AS ten_kho_xuat_phat,
                   t.ma_kho_den,
                   COALESCE(kden.ten_kho, 'Địa chỉ khách hàng') AS ten_diem_den,
                   c.ma_tx, tx.ho_ten AS ten_tai_xe, c.ma_pt, pt.bien_so, pt.tai_trong,
                   c.ma_nv_dieu_phoi, c.ngay_xuat_phat, c.ngay_den_du_kien,
                   COALESCE(ct.so_kien, 0) AS so_kien, COALESCE(ct.tong_kg, 0) AS tong_kg,
                   CASE WHEN pt.tai_trong > 0 THEN ROUND(100.0 * COALESCE(ct.tong_kg, 0) / pt.tai_trong, 1) ELSE 0 END AS ty_le_tai
            FROM chuyen_giao c
            JOIN tai_xe tx ON tx.ma_tx = c.ma_tx
            JOIN phuong_tien pt ON pt.ma_pt = c.ma_pt
            LEFT JOIN tuyen_van_chuyen t ON t.ma_tuyen = c.ma_tuyen
            LEFT JOIN kho kdi  ON kdi.ma_kho  = COALESCE(t.ma_kho_di, c.ma_kho_giao)
            LEFT JOIN kho kden ON kden.ma_kho = t.ma_kho_den
            LEFT JOIN (
                SELECT x.ma_chuyen, COUNT(*) AS so_kien, SUM(k.khoi_luong) AS tong_kg
                FROM chi_tiet_chuyen_giao x
                JOIN kien_hang k ON k.ma_kien = x.ma_kien
                GROUP BY x.ma_chuyen
            ) ct ON ct.ma_chuyen = c.ma_chuyen
            WHERE c.ma_chuyen = ?
        """;

        List<ChuyenGiaoChiTietDTO> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp tsXuatPhat = rs.getTimestamp("ngay_xuat_phat");
            Timestamp tsDenDuKien = rs.getTimestamp("ngay_den_du_kien");

            return ChuyenGiaoChiTietDTO.builder()
                    .maChuyen(rs.getString("ma_chuyen"))
                    .loaiChuyen(rs.getString("loai_chuyen"))
                    .trangThai(rs.getString("trang_thai"))
                    .maTuyen(rs.getString("ma_tuyen"))
                    .maKhoXuatPhat(rs.getString("ma_kho_xuat_phat"))
                    .tenKhoXuatPhat(rs.getString("ten_kho_xuat_phat"))
                    .maKhoDen(rs.getString("ma_kho_den"))
                    .tenDiemDen(rs.getString("ten_diem_den"))
                    .maTx(rs.getString("ma_tx"))
                    .tenTaiXe(rs.getString("ten_tai_xe"))
                    .maPt(rs.getString("ma_pt"))
                    .bienSo(rs.getString("bien_so"))
                    .taiTrong(rs.getBigDecimal("tai_trong"))
                    .maNvDieuPhoi(rs.getString("ma_nv_dieu_phoi"))
                    .ngayXuatPhat(tsXuatPhat != null ? tsXuatPhat.toLocalDateTime() : null)
                    .ngayDenDuKien(tsDenDuKien != null ? tsDenDuKien.toLocalDateTime() : null)
                    .soKien(rs.getLong("so_kien"))
                    .tongKg(rs.getBigDecimal("tong_kg"))
                    .tyLeTai(rs.getBigDecimal("ty_le_tai"))
                    .build();
        }, maChuyen);

        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Lấy toàn bộ danh sách chuyến giao được phân công cho tài xế (thẻ 1 & thẻ 2 trên Dashboard).
     */
    public List<ChuyenGiaoChiTietDTO> getChuyenPhanCongByTaiXe(String maTx) {
        maTx = resolveMaTx(maTx);
        String sql = """
            SELECT c.ma_chuyen, c.loai_chuyen, c.trang_thai, c.ma_tuyen,
                   COALESCE(t.ma_kho_di, c.ma_kho_giao) AS ma_kho_xuat_phat,
                   COALESCE(kdi.ten_kho, 'Kho xuất phát') AS ten_kho_xuat_phat,
                   t.ma_kho_den,
                   COALESCE(kden.ten_kho, 'Địa chỉ khách hàng') AS ten_diem_den,
                   c.ma_tx, tx.ho_ten AS ten_tai_xe, c.ma_pt, pt.bien_so, pt.tai_trong,
                   c.ma_nv_dieu_phoi, c.ngay_xuat_phat, c.ngay_den_du_kien,
                   COALESCE(ct.so_kien, 0) AS so_kien, COALESCE(ct.tong_kg, 0) AS tong_kg,
                   CASE WHEN pt.tai_trong > 0 THEN ROUND(100.0 * COALESCE(ct.tong_kg, 0) / pt.tai_trong, 1) ELSE 0 END AS ty_le_tai
            FROM chuyen_giao c
            JOIN tai_xe tx ON tx.ma_tx = c.ma_tx
            JOIN phuong_tien pt ON pt.ma_pt = c.ma_pt
            LEFT JOIN tuyen_van_chuyen t ON t.ma_tuyen = c.ma_tuyen
            LEFT JOIN kho kdi  ON kdi.ma_kho  = COALESCE(t.ma_kho_di, c.ma_kho_giao)
            LEFT JOIN kho kden ON kden.ma_kho = t.ma_kho_den
            LEFT JOIN (
                SELECT x.ma_chuyen, COUNT(*) AS so_kien, SUM(k.khoi_luong) AS tong_kg
                FROM chi_tiet_chuyen_giao x
                JOIN kien_hang k ON k.ma_kien = x.ma_kien
                GROUP BY x.ma_chuyen
            ) ct ON ct.ma_chuyen = c.ma_chuyen
            WHERE c.ma_tx = ? AND c.trang_thai <> 'Đã hủy'
            ORDER BY 
                CASE WHEN c.trang_thai = 'Đang đi' THEN 1 
                     WHEN c.trang_thai = 'Chưa đi' THEN 2 
                     ELSE 3 END,
                c.ngay_xuat_phat DESC, c.ma_chuyen DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp tsXuatPhat = rs.getTimestamp("ngay_xuat_phat");
            Timestamp tsDenDuKien = rs.getTimestamp("ngay_den_du_kien");

            return ChuyenGiaoChiTietDTO.builder()
                    .maChuyen(rs.getString("ma_chuyen"))
                    .loaiChuyen(rs.getString("loai_chuyen"))
                    .trangThai(rs.getString("trang_thai"))
                    .maTuyen(rs.getString("ma_tuyen"))
                    .maKhoXuatPhat(rs.getString("ma_kho_xuat_phat"))
                    .tenKhoXuatPhat(rs.getString("ten_kho_xuat_phat"))
                    .maKhoDen(rs.getString("ma_kho_den"))
                    .tenDiemDen(rs.getString("ten_diem_den"))
                    .maTx(rs.getString("ma_tx"))
                    .tenTaiXe(rs.getString("ten_tai_xe"))
                    .maPt(rs.getString("ma_pt"))
                    .bienSo(rs.getString("bien_so"))
                    .taiTrong(rs.getBigDecimal("tai_trong"))
                    .maNvDieuPhoi(rs.getString("ma_nv_dieu_phoi"))
                    .ngayXuatPhat(tsXuatPhat != null ? tsXuatPhat.toLocalDateTime() : null)
                    .ngayDenDuKien(tsDenDuKien != null ? tsDenDuKien.toLocalDateTime() : null)
                    .soKien(rs.getLong("so_kien"))
                    .tongKg(rs.getBigDecimal("tong_kg"))
                    .tyLeTai(rs.getBigDecimal("ty_le_tai"))
                    .build();
        }, maTx);
    }

    /**
     * Lấy danh sách kiện hàng cần giao thuộc trách nhiệm của tài xế (thẻ 3).
     */
    public List<KienTrongChuyenDTO> getKienCanGiaoByTaiXe(String maTx) {
        maTx = resolveMaTx(maTx);
        String sql = """
            SELECT x.ma_chuyen, c.ma_tx, x.ma_kien, k.ma_dh,
                   COALESCE(d.ten_nguoi_nhan, 'Khách hàng') AS ten_nguoi_nhan,
                   COALESCE(d.sdt_nhan, '') AS sdt_nhan,
                   COALESCE(d.dia_chi_giao, '') AS dia_chi_giao,
                   k.khoi_luong, k.loai_hang, x.trang_thai,
                   x.thoi_gian_gan, x.thoi_gian_quet, x.ghi_chu,
                   COALESCE(d.cod, 0) AS cod
            FROM chi_tiet_chuyen_giao x
            JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen
            JOIN kien_hang k ON k.ma_kien = x.ma_kien
            LEFT JOIN don_hang d ON d.ma_dh = k.ma_dh
            WHERE c.ma_tx = ? 
              AND c.trang_thai IN ('Chưa đi', 'Đang đi')
              AND x.trang_thai IN ('Đã gán', 'Đã quét')
            ORDER BY c.ma_chuyen ASC, x.thoi_gian_gan ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp tsGan = rs.getTimestamp("thoi_gian_gan");
            Timestamp tsQuet = rs.getTimestamp("thoi_gian_quet");

            return KienTrongChuyenDTO.builder()
                    .maChuyen(rs.getString("ma_chuyen"))
                    .maTx(rs.getString("ma_tx"))
                    .maKien(rs.getString("ma_kien"))
                    .maDh(rs.getString("ma_dh"))
                    .tenNguoiNhan(rs.getString("ten_nguoi_nhan"))
                    .sdtNhan(rs.getString("sdt_nhan"))
                    .diaChiGiao(rs.getString("dia_chi_giao"))
                    .khoiLuong(rs.getBigDecimal("khoi_luong"))
                    .loaiHang(rs.getString("loai_hang"))
                    .trangThai(rs.getString("trang_thai"))
                    .thoiGianGan(tsGan != null ? tsGan.toLocalDateTime() : null)
                    .thoiGianQuet(tsQuet != null ? tsQuet.toLocalDateTime() : null)
                    .ghiChu(rs.getString("ghi_chu"))
                    .cod(rs.getBigDecimal("cod"))
                    .build();
        }, maTx);
    }

    /**
     * Lấy danh sách kiện hàng đã giao thành công bởi tài xế (thẻ 4).
     */
    public List<KienTrongChuyenDTO> getKienDaGiaoByTaiXe(String maTx) {
        maTx = resolveMaTx(maTx);
        String sql = """
            SELECT x.ma_chuyen, c.ma_tx, x.ma_kien, k.ma_dh,
                   COALESCE(d.ten_nguoi_nhan, 'Khách hàng') AS ten_nguoi_nhan,
                   COALESCE(d.sdt_nhan, '') AS sdt_nhan,
                   COALESCE(d.dia_chi_giao, '') AS dia_chi_giao,
                   k.khoi_luong, k.loai_hang, x.trang_thai,
                   x.thoi_gian_gan, x.thoi_gian_quet, x.ghi_chu,
                   COALESCE(d.cod, 0) AS cod,
                   COALESCE(ls.thoi_gian, x.thoi_gian_quet, x.thoi_gian_gan) AS thoi_gian_giao
            FROM chi_tiet_chuyen_giao x
            JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen
            JOIN kien_hang k ON k.ma_kien = x.ma_kien
            LEFT JOIN don_hang d ON d.ma_dh = k.ma_dh
            LEFT JOIN LATERAL (
                SELECT ls_inner.thoi_gian 
                FROM lich_su_trang_thai ls_inner 
                WHERE ls_inner.ma_dh = k.ma_dh AND ls_inner.ma_tt = 'TT06'
                ORDER BY ls_inner.thoi_gian DESC LIMIT 1
            ) ls ON true
            WHERE c.ma_tx = ? AND x.trang_thai = 'Đã giao'
            ORDER BY thoi_gian_giao DESC, x.thoi_gian_gan DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp tsGan = rs.getTimestamp("thoi_gian_gan");
            Timestamp tsQuet = rs.getTimestamp("thoi_gian_quet");
            Timestamp tsGiao = rs.getTimestamp("thoi_gian_giao");

            return KienTrongChuyenDTO.builder()
                    .maChuyen(rs.getString("ma_chuyen"))
                    .maTx(rs.getString("ma_tx"))
                    .maKien(rs.getString("ma_kien"))
                    .maDh(rs.getString("ma_dh"))
                    .tenNguoiNhan(rs.getString("ten_nguoi_nhan"))
                    .sdtNhan(rs.getString("sdt_nhan"))
                    .diaChiGiao(rs.getString("dia_chi_giao"))
                    .khoiLuong(rs.getBigDecimal("khoi_luong"))
                    .loaiHang(rs.getString("loai_hang"))
                    .trangThai(rs.getString("trang_thai"))
                    .thoiGianGan(tsGan != null ? tsGan.toLocalDateTime() : null)
                    .thoiGianQuet(tsQuet != null ? tsQuet.toLocalDateTime() : null)
                    .thoiGianGiao(tsGiao != null ? tsGiao.toLocalDateTime() : null)
                    .ghiChu(rs.getString("ghi_chu"))
                    .cod(rs.getBigDecimal("cod"))
                    .build();
        }, maTx);
    }

    /**
     * Thống kê Dashboard Tài xế
     */
    public TaiXeDashboardDTO getDashboardStats(String maTx) {
        maTx = resolveMaTx(maTx);
        if (maTx == null || maTx.isBlank()) {
            return TaiXeDashboardDTO.builder()
                    .maTx("")
                    .tenTaiXe("Tài xế")
                    .bienSoXe("---")
                    .loaiXe("Xe tải")
                    .chuyenPhanCong(0L)
                    .chuyenDangThucHien(0L)
                    .kienCanGiao(0L)
                    .kienDaGiao(0L)
                    .chuyenHienTai(null)
                    .build();
        }

        TaiXe tx = taiXeRepository.findById(maTx).orElse(null);
        String tenTx = tx != null ? tx.getHoTen() : "Tài xế";

        Long chuyenPhanCong = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE ma_tx = ? AND trang_thai <> 'Đã hủy'",
                Long.class, maTx);

        Long chuyenDangThucHien = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE ma_tx = ? AND trang_thai = 'Đang đi'",
                Long.class, maTx);

        Long kienCanGiao = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chi_tiet_chuyen_giao x " +
                "JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen " +
                "WHERE c.ma_tx = ? AND c.trang_thai IN ('Chưa đi', 'Đang đi') AND x.trang_thai <> 'Đã giao'",
                Long.class, maTx);

        Long kienDaGiao = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chi_tiet_chuyen_giao x " +
                "JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen " +
                "WHERE c.ma_tx = ? AND x.trang_thai = 'Đã giao'",
                Long.class, maTx);

        // Lấy chuyến đang chạy hoặc chuyến mới nhất của tài xế
        List<String> tripIds = jdbcTemplate.query(
                "SELECT ma_chuyen FROM chuyen_giao WHERE ma_tx = ? " +
                "ORDER BY CASE WHEN trang_thai = 'Đang đi' THEN 1 WHEN trang_thai = 'Chưa đi' THEN 2 ELSE 3 END, ngay_xuat_phat DESC LIMIT 1",
                (rs, rowNum) -> rs.getString("ma_chuyen"), maTx);

        ChuyenGiaoChiTietDTO chuyenHienTai = null;
        if (!tripIds.isEmpty()) {
            chuyenHienTai = getChuyenGiaoChiTiet(tripIds.get(0));
        }

        return TaiXeDashboardDTO.builder()
                .maTx(maTx)
                .tenTaiXe(tenTx)
                .bienSoXe(chuyenHienTai != null ? chuyenHienTai.getBienSo() : "Chưa gán")
                .loaiXe(chuyenHienTai != null ? ("Xe tải " + chuyenHienTai.getTaiTrong() + " kg") : "Xe tải")
                .chuyenPhanCong(chuyenPhanCong != null ? chuyenPhanCong : 0)
                .chuyenDangThucHien(chuyenDangThucHien != null ? chuyenDangThucHien : 0)
                .kienCanGiao(kienCanGiao != null ? kienCanGiao : 0)
                .kienDaGiao(kienDaGiao != null ? kienDaGiao : 0)
                .chuyenHienTai(chuyenHienTai)
                .build();
    }

    // ========================== THAO TÁC (STORED PROCEDURE) ==========================

    /**
     * Quét kiện hàng lên xe.
     */
    @Transactional
    public void quetKien(String maChuyen, String maKien, String maNd, String ghiChu) {
        chuyenGiaoRepository.quetKien(maChuyen, maKien, maNd, ghiChu);
    }

    /**
     * Xuất phát chuyến.
     */
    @Transactional
    public void xuatPhatChuyen(String maChuyen, String maNd) {
        chuyenGiaoRepository.xuatPhatChuyen(maChuyen, maNd);
    }

    /**
     * Giao kiện thành công.
     * Nếu tất cả kiện trong chuyến đều đã có kết quả (thành công hoặc thất bại), tự động hoàn thành chuyến.
     */
    @Transactional
    public void giaoThanhCong(String maChuyen, String maKien, String maNd, String ghiChu, String nguoiThu) {
        chuyenGiaoRepository.giaoThanhCong(maChuyen, maKien, maNd, ghiChu, nguoiThu);
        kiemTraVaTuDongHoanThanhChuyen(maChuyen, maNd);
    }

    /**
     * Giao kiện thất bại.
     * Nếu tất cả kiện trong chuyến đều đã có kết quả (thành công hoặc thất bại), tự động hoàn thành chuyến.
     */
    @Transactional
    public void giaoThatBai(String maChuyen, String maKien, String maNd, String lyDo) {
        chuyenGiaoRepository.giaoThatBai(maChuyen, maKien, maNd, lyDo);
        kiemTraVaTuDongHoanThanhChuyen(maChuyen, maNd);
    }

    private void kiemTraVaTuDongHoanThanhChuyen(String maChuyen, String maNd) {
        ChuyenGiao cg = chuyenGiaoRepository.findById(maChuyen).orElse(null);
        if (cg == null || !"Đang đi".equals(cg.getTrangThai())) {
            return;
        }

        // Nghiệp vụ: Chuyến đang giao mà chưa có kết quả hết các kiện -> Đang đi
        // Có kiện thành công, có kiện thất bại HOẶC Tất cả kiện đều giao thất bại -> Hoàn thành
        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chi_tiet_chuyen_giao WHERE ma_chuyen = ? AND trang_thai NOT IN ('Đã giao', 'Thất bại')",
                Integer.class, maChuyen);

        if (remaining != null && remaining == 0) {
            try {
                jdbcTemplate.execute((java.sql.Connection con) -> {
                    try (java.sql.CallableStatement cs = con.prepareCall("CALL sp_hoan_thanh_chuyen(?, ?)")) {
                        cs.setString(1, maChuyen);
                        cs.setString(2, (maNd != null && !maNd.isBlank()) ? maNd : "ND0003");
                        cs.execute();
                        return null;
                    }
                });
                log.info("Chuyến giao {} đã tự động hoàn thành vì tất cả kiện hàng đều đã có kết quả.", maChuyen);
            } catch (Exception ex) {
                log.warn("Lỗi khi tự động hoàn thành chuyến {}: {}", maChuyen, ex.getMessage());
            }
        }
    }

    /**
     * Xác nhận hoàn thành chuyến giao (gọi Stored Procedure sp_hoan_thanh_chuyen).
     * Đặc biệt quan trọng với chuyến LIEN_KHO: khi xe đến kho đích, hoàn thành chuyến sẽ tự động
     * chuyển vị trí tất cả kiện hàng sang kho đích (ma_kho_den) và cập nhật đơn hàng sang TT04.
     */
    @Transactional
    public void hoanThanhChuyen(String maChuyen, String maNd) {
        ChuyenGiao cg = chuyenGiaoRepository.findById(maChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao: " + maChuyen));

        if (!"Đang đi".equals(cg.getTrangThai())) {
            throw new RuntimeException("Chuyến giao đang ở trạng thái [" + cg.getTrangThai() + "]. Chỉ có thể xác nhận hoàn thành khi chuyến đang chạy.");
        }

        try {
            jdbcTemplate.execute((java.sql.Connection con) -> {
                try (java.sql.CallableStatement cs = con.prepareCall("CALL sp_hoan_thanh_chuyen(?, ?)")) {
                    cs.setString(1, maChuyen);
                    cs.setString(2, (maNd != null && !maNd.isBlank()) ? maNd : "ND0003");
                    cs.execute();
                    return null;
                }
            });
        } catch (org.springframework.dao.DataAccessException ex) {
            Throwable root = ex.getMostSpecificCause();
            String msg = (root != null && root.getMessage() != null) ? root.getMessage() : ex.getMessage();
            int nl = msg.indexOf('\n');
            if (nl > 0) msg = msg.substring(0, nl);
            if (msg.startsWith("ERROR: ")) msg = msg.substring(7);
            throw new RuntimeException(msg.trim());
        }
    }

    // ========================== PHÂN TRANG BACKEND (SERVER-SIDE PAGINATION) ==========================

    /**
     * Lấy danh sách chuyến phân công cho tài xế có phân trang.
     */
    public PageResponse<ChuyenGiaoChiTietDTO> getChuyenPhanCongPaged(String maTx, int page, int size, String trangThai) {
        List<ChuyenGiaoChiTietDTO> all = getChuyenPhanCongByTaiXe(maTx);
        if (trangThai != null && !trangThai.isBlank() && !trangThai.equalsIgnoreCase("TAT_CA")) {
            all = all.stream().filter(c -> trangThai.equalsIgnoreCase(c.getTrangThai())).toList();
        }
        return PageResponse.of(all, page, size);
    }

    /**
     * Lấy danh sách chuyến entity của tài xế có phân trang (Spring Data Pageable).
     */
    public PageResponse<ChuyenGiao> getChuyenGiaoByTaiXePaged(String maTx, String trangThai, int page, int size) {
        maTx = resolveMaTx(maTx);
        int p = Math.max(0, page - 1);
        int s = (size > 0) ? size : 5;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(p, s);
        if (trangThai != null && !trangThai.isBlank()) {
            return PageResponse.from(chuyenGiaoRepository.findByMaTxAndTrangThai(maTx, trangThai, pageable));
        }
        return PageResponse.from(chuyenGiaoRepository.findByMaTx(maTx, pageable));
    }

    /**
     * Lấy danh sách kiện trong chuyến của tài xế có phân trang.
     */
    public PageResponse<KienTrongChuyenDTO> getKienTrongChuyenPaged(String maChuyen, int page, int size) {
        List<KienTrongChuyenDTO> all = getKienTrongChuyen(maChuyen);
        return PageResponse.of(all, page, size);
    }

    /**
     * Lấy danh sách kiện cần giao của tài xế có phân trang (Server-side).
     */
    public PageResponse<KienTrongChuyenDTO> getKienCanGiaoPaged(String maTx, int page, int size) {
        List<KienTrongChuyenDTO> all = getKienCanGiaoByTaiXe(maTx);
        return PageResponse.of(all, page, size);
    }

    /**
     * Lấy danh sách kiện đã giao của tài xế có phân trang (Server-side).
     */
    public PageResponse<KienTrongChuyenDTO> getKienDaGiaoPaged(String maTx, int page, int size) {
        List<KienTrongChuyenDTO> all = getKienDaGiaoByTaiXe(maTx);
        return PageResponse.of(all, page, size);
    }
}

