package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DieuPhoiService {

    private final TuyenVanChuyenRepository tuyenVanChuyenRepository;
    private final ChuyenGiaoRepository chuyenGiaoRepository;
    private final ChiTietChuyenGiaoRepository chiTietChuyenGiaoRepository;
    private final KhoRepository khoRepository;
    private final TaiXeRepository taiXeRepository;
    private final PhuongTienRepository phuongTienRepository;
    private final KienHangRepository kienHangRepository;
    private final JdbcTemplate jdbcTemplate;

    // ========================== TUYẾN VẬN CHUYỂN (CRUD) ==========================

    public List<TuyenVanChuyen> getAllTuyen() {
        return tuyenVanChuyenRepository.findAll();
    }

    public TuyenVanChuyen getTuyenById(String maTuyen) {
        return tuyenVanChuyenRepository.findById(maTuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tuyến vận chuyển: " + maTuyen));
    }

    @Transactional
    public TuyenVanChuyen createTuyen(TuyenVanChuyen tuyen) {
        return tuyenVanChuyenRepository.save(tuyen);
    }

    @Transactional
    public TuyenVanChuyen updateTuyen(String maTuyen, TuyenVanChuyen tuyenUpdate) {
        TuyenVanChuyen existing = getTuyenById(maTuyen);
        existing.setMaKhoDi(tuyenUpdate.getMaKhoDi());
        existing.setMaKhoDen(tuyenUpdate.getMaKhoDen());
        existing.setKhoangCach(tuyenUpdate.getKhoangCach());
        existing.setThoiGianDuKienGio(tuyenUpdate.getThoiGianDuKienGio());
        return tuyenVanChuyenRepository.save(existing);
    }

    @Transactional
    public void deleteTuyen(String maTuyen) {
        if (!tuyenVanChuyenRepository.existsById(maTuyen)) {
            throw new RuntimeException("Không tìm thấy tuyến vận chuyển: " + maTuyen);
        }
        tuyenVanChuyenRepository.deleteById(maTuyen);
    }

    // ========================== DANH MỤC PHỤC VỤ ĐIỀU PHỐI ==========================

    public List<Kho> getAllKho() {
        return khoRepository.findAll();
    }

    public List<TaiXe> getAllTaiXe() {
        return taiXeRepository.findAll();
    }

    public List<PhuongTien> getAllPhuongTien() {
        return phuongTienRepository.findAll();
    }

    public List<KienHang> getAvailableKienHang(String maKho) {
        if (maKho != null && !maKho.isBlank()) {
            return kienHangRepository.findAvailableKienHangByKho(maKho);
        }
        return kienHangRepository.findAvailableKienHang();
    }

    // ========================== CHUYẾN GIAO ==========================

    /**
     * Tạo chuyến giao mới bằng Stored Procedure sp_tao_chuyen_giao.
     * Sử dụng CallableStatement để nhận giá trị trả về từ tham số INOUT p_ma_chuyen.
     */
    @Transactional
    public String taoChuyenGiao(TaoChuyenGiaoRequest request) {
        try {
            return jdbcTemplate.execute((java.sql.Connection con) -> {
                try (java.sql.CallableStatement cs = con.prepareCall("CALL sp_tao_chuyen_giao(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    cs.setString(1, request.getLoai());
                    cs.setString(2, request.getMaTuyen());
                    cs.setString(3, request.getMaKhoGiao());
                    cs.setString(4, request.getMaTx());
                    cs.setString(5, request.getMaPt());
                    cs.setTimestamp(6, request.getNgayXuatPhat() != null ? java.sql.Timestamp.valueOf(request.getNgayXuatPhat()) : null);
                    cs.setString(7, request.getMaNvDp());
                    cs.setString(8, request.getMaNd());
                    cs.setTimestamp(9, request.getNgayDenDuKien() != null ? java.sql.Timestamp.valueOf(request.getNgayDenDuKien()) : null);
                    cs.registerOutParameter(10, java.sql.Types.VARCHAR);
                    cs.execute();
                    return cs.getString(10);
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

    /**
     * Gán kiện vào chuyến giao bằng Stored Procedure.
     */
    @Transactional
    public void ganKienVaoChuyen(String maChuyen, String maKien, String maNd) {
        chuyenGiaoRepository.ganKienVaoChuyen(maChuyen, maKien, maNd);
    }

    /**
     * Tra cứu danh sách chuyến giao (tất cả hoặc theo NV điều phối).
     */
    public List<ChuyenGiao> getChuyenGiaoByNvDieuPhoi(String maNvDieuPhoi) {
        if (maNvDieuPhoi != null && !maNvDieuPhoi.isBlank()) {
            return chuyenGiaoRepository.findByMaNvDieuPhoi(maNvDieuPhoi);
        }
        return chuyenGiaoRepository.findAll();
    }

    /**
     * Lấy chi tiết chuyến giao (danh sách kiện trong chuyến) đầy đủ thông tin:
     * mã kiện, mã đơn, khối lượng, người nhận, địa chỉ, COD, trạng thái...
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

    public List<ChiTietChuyenGiao> getChiTietChuyen(String maChuyen) {
        return chiTietChuyenGiaoRepository.findByMaChuyen(maChuyen);
    }

    /**
     * Lấy danh sách chuyến giao kèm chi tiết đầy đủ (tên kho, tên tài xế, biển số, tải trọng, số kiện, tổng kg).
     */
    public List<ChuyenGiaoChiTietDTO> getChuyenGiaoChiTietList() {
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
            ORDER BY c.ngay_xuat_phat DESC
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
        });
    }

    /**
     * Thống kê Dashboard Điều phối
     */
    public DieuPhoiDashboardDTO getDashboardStats() {
        Long tongSoChuyen = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE trang_thai <> 'Đã hủy'", Long.class);

        Long chuyenChuaDi = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE trang_thai = 'Chưa đi'", Long.class);

        Long chuyenDangDi = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE trang_thai = 'Đang đi'", Long.class);

        Long chuyenHoanThanh = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_giao WHERE trang_thai = 'Hoàn thành'", Long.class);

        Long donCanDieuPhoi = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kien_hang WHERE ma_kho_hien_tai IS NOT NULL " +
                "AND ma_kien NOT IN (SELECT c.ma_kien FROM chi_tiet_chuyen_giao c " +
                "JOIN chuyen_giao cg ON cg.ma_chuyen = c.ma_chuyen WHERE cg.trang_thai IN ('Chưa đi', 'Đang đi'))",
                Long.class);

        Long taiXeKhaDung = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tai_xe WHERE ma_tx NOT IN (" +
                "SELECT ma_tx FROM chuyen_giao WHERE trang_thai = 'Đang đi')", Long.class);

        Long phuongTienKhaDung = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM phuong_tien WHERE trang_thai = 'Sẵn sàng' AND ma_pt NOT IN (" +
                "SELECT ma_pt FROM chuyen_giao WHERE trang_thai = 'Đang đi')", Long.class);

        List<ChuyenGiaoChiTietDTO> recentTrips = getChuyenGiaoChiTietList();
        if (recentTrips.size() > 5) {
            recentTrips = recentTrips.subList(0, 5);
        }

        return DieuPhoiDashboardDTO.builder()
                .tongSoChuyen(tongSoChuyen != null ? tongSoChuyen : 0)
                .chuyenChuaDi(chuyenChuaDi != null ? chuyenChuaDi : 0)
                .chuyenDangDi(chuyenDangDi != null ? chuyenDangDi : 0)
                .chuyenHoanThanh(chuyenHoanThanh != null ? chuyenHoanThanh : 0)
                .donCanDieuPhoi(donCanDieuPhoi != null ? donCanDieuPhoi : 0)
                .chuyenDangHoatDong(chuyenDangDi != null ? chuyenDangDi : 0)
                .taiXeKhaDung(taiXeKhaDung != null ? taiXeKhaDung : 0)
                .phuongTienKhaDung(phuongTienKhaDung != null ? phuongTienKhaDung : 0)
                .chuyenGiaoGanDay(recentTrips)
                .build();
    }

    /**
     * STT 1: Danh sách đơn / kiện đang chờ điều phối, chờ xuất bến
     */
    public List<DonCanDieuPhoiDTO> getDonCanDieuPhoiList() {
        String sql = """
            SELECT 
                k.ma_kien,
                k.ma_dh,
                k.loai_hang,
                k.khoi_luong,
                k.ma_kho_hien_tai,
                kh.ten_kho AS ten_kho_hien_tai,
                dh.dia_chi_giao,
                dh.ma_kho_nhan,
                kn.ten_kho AS ten_kho_nhan,
                dh.ten_nguoi_nhan,
                dh.sdt_nhan,
                dh.cod,
                dh.ma_tt,
                tt.ten_trang_thai AS trang_thai_don
            FROM kien_hang k
            JOIN don_hang dh ON k.ma_dh = dh.ma_dh
            LEFT JOIN kho kh ON k.ma_kho_hien_tai = kh.ma_kho
            LEFT JOIN kho kn ON dh.ma_kho_nhan = kn.ma_kho
            LEFT JOIN trang_thai_dh tt ON dh.ma_tt = tt.ma_trang_thai
            WHERE k.ma_kho_hien_tai IS NOT NULL
              AND k.ma_kien NOT IN (
                  SELECT c.ma_kien FROM chi_tiet_chuyen_giao c
                  JOIN chuyen_giao cg ON cg.ma_chuyen = c.ma_chuyen
                  WHERE cg.trang_thai IN ('Chưa đi', 'Đang đi')
              )
            ORDER BY k.ma_kien ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> DonCanDieuPhoiDTO.builder()
                .maKien(rs.getString("ma_kien"))
                .maDh(rs.getString("ma_dh"))
                .loaiHang(rs.getString("loai_hang"))
                .khoiLuong(rs.getBigDecimal("khoi_luong"))
                .maKhoHienTai(rs.getString("ma_kho_hien_tai"))
                .tenKhoHienTai(rs.getString("ten_kho_hien_tai"))
                .diaChiGiao(rs.getString("dia_chi_giao"))
                .maKhoNhan(rs.getString("ma_kho_nhan"))
                .tenKhoNhan(rs.getString("ten_kho_nhan"))
                .tenNguoiNhan(rs.getString("ten_nguoi_nhan"))
                .sdtNhan(rs.getString("sdt_nhan"))
                .cod(rs.getBigDecimal("cod"))
                .maTt(rs.getString("ma_tt"))
                .trangThaiDon(rs.getString("trang_thai_don"))
                .build());
    }

    /**
     * Lấy danh sách kiện hàng khả dụng cho điều phối có đầy đủ metadata và lọc theo ràng buộc nghiệp vụ:
     * - loaiChuyen = 'LIEN_KHO':
     *     + Kiện hàng đang ở kho đi của tuyến (k.ma_kho_hien_tai = tuyen.ma_kho_di)
     *     + Trạng thái đơn: TT02 hoặc TT04
     *     + Kho nhận đơn khác kho đi (chưa đến đích cuối)
     *     + Nếu có maTuyen: ưu tiên hoặc chỉ lấy kiện hợp lệ với tuyến đó
     * - loaiChuyen = 'GIAO_CUOI':
     *     + Kiện hàng đang ở kho giao (k.ma_kho_hien_tai = maKhoGiao)
     *     + Kho nhận của đơn hàng PHẢI là kho giao (dh.ma_kho_nhan = maKhoGiao) - RÀNG BUỘC CƠ SỞ DỮ LIỆU
     *     + Trạng thái đơn: TT02, TT04, TT08
     */
    public List<DonCanDieuPhoiDTO> getAvailableKienHangDTOList(String maKho, String loaiChuyen, String maTuyen) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                k.ma_kien,
                k.ma_dh,
                k.loai_hang,
                k.khoi_luong,
                k.ma_kho_hien_tai,
                kh.ten_kho AS ten_kho_hien_tai,
                dh.dia_chi_giao,
                dh.ma_kho_nhan,
                kn.ten_kho AS ten_kho_nhan,
                dh.ten_nguoi_nhan,
                dh.sdt_nhan,
                dh.cod,
                dh.ma_tt,
                tt.ten_trang_thai AS trang_thai_don
            FROM kien_hang k
            JOIN don_hang dh ON k.ma_dh = dh.ma_dh
            LEFT JOIN kho kh ON k.ma_kho_hien_tai = kh.ma_kho
            LEFT JOIN kho kn ON dh.ma_kho_nhan = kn.ma_kho
            LEFT JOIN trang_thai_dh tt ON dh.ma_tt = tt.ma_trang_thai
            WHERE k.ma_kho_hien_tai IS NOT NULL
              AND k.ma_kien NOT IN (
                  SELECT c.ma_kien FROM chi_tiet_chuyen_giao c
                  JOIN chuyen_giao cg ON cg.ma_chuyen = c.ma_chuyen
                  WHERE cg.trang_thai IN ('Chưa đi', 'Đang đi')
              )
        """);

        List<Object> params = new java.util.ArrayList<>();

        if (loaiChuyen != null && !loaiChuyen.isBlank()) {
            if ("LIEN_KHO".equalsIgnoreCase(loaiChuyen)) {
                sql.append(" AND dh.ma_tt IN ('TT02', 'TT04') ");
                if (maTuyen != null && !maTuyen.isBlank()) {
                    TuyenVanChuyen tuyen = tuyenVanChuyenRepository.findById(maTuyen).orElse(null);
                    if (tuyen != null) {
                        sql.append(" AND k.ma_kho_hien_tai = ? ");
                        params.add(tuyen.getMaKhoDi());
                        sql.append(" AND dh.ma_kho_nhan <> ? ");
                        params.add(tuyen.getMaKhoDi());
                    }
                } else if (maKho != null && !maKho.isBlank()) {
                    sql.append(" AND k.ma_kho_hien_tai = ? ");
                    params.add(maKho);
                    sql.append(" AND dh.ma_kho_nhan <> ? ");
                    params.add(maKho);
                }
            } else if ("GIAO_CUOI".equalsIgnoreCase(loaiChuyen)) {
                sql.append(" AND dh.ma_tt IN ('TT02', 'TT04', 'TT08') ");
                if (maKho != null && !maKho.isBlank()) {
                    sql.append(" AND k.ma_kho_hien_tai = ? ");
                    params.add(maKho);
                    sql.append(" AND dh.ma_kho_nhan = ? ");
                    params.add(maKho);
                }
            }
        } else if (maKho != null && !maKho.isBlank()) {
            sql.append(" AND k.ma_kho_hien_tai = ? ");
            params.add(maKho);
        }

        sql.append(" ORDER BY k.ma_kien ASC ");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> DonCanDieuPhoiDTO.builder()
                .maKien(rs.getString("ma_kien"))
                .maDh(rs.getString("ma_dh"))
                .loaiHang(rs.getString("loai_hang"))
                .khoiLuong(rs.getBigDecimal("khoi_luong"))
                .maKhoHienTai(rs.getString("ma_kho_hien_tai"))
                .tenKhoHienTai(rs.getString("ten_kho_hien_tai"))
                .diaChiGiao(rs.getString("dia_chi_giao"))
                .maKhoNhan(rs.getString("ma_kho_nhan"))
                .tenKhoNhan(rs.getString("ten_kho_nhan"))
                .tenNguoiNhan(rs.getString("ten_nguoi_nhan"))
                .sdtNhan(rs.getString("sdt_nhan"))
                .cod(rs.getBigDecimal("cod"))
                .maTt(rs.getString("ma_tt"))
                .trangThaiDon(rs.getString("trang_thai_don"))
                .build(), params.toArray());
    }

    /**
     * STT 2: Danh sách các chuyến đang chạy (trang_thai = 'Đang đi')
     */
    public List<ChuyenGiaoChiTietDTO> getChuyenDangChayList() {
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
            WHERE c.trang_thai = 'Đang đi'
            ORDER BY c.ngay_xuat_phat ASC
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
        });
    }

    /**
     * STT 3: Danh sách tài xế khả dụng (không bị trùng lịch chuyến đang chạy)
     */
    public List<TaiXeKhaDungDTO> getTaiXeKhaDungList() {
        String sql = """
            SELECT 
                tx.ma_tx,
                tx.ho_ten,
                tx.sdt,
                tx.bang_lai,
                COALESCE(nd.trang_thai, 'Hoạt động') AS trang_thai_tk,
                cg.ma_chuyen,
                cg.trang_thai AS trang_thai_chuyen
            FROM tai_xe tx
            LEFT JOIN nguoi_dung nd ON tx.ma_nd = nd.ma_nd
            LEFT JOIN LATERAL (
                SELECT c.ma_chuyen, c.trang_thai
                FROM chuyen_giao c
                WHERE c.ma_tx = tx.ma_tx AND c.trang_thai IN ('Chưa đi', 'Đang đi')
                ORDER BY CASE WHEN c.trang_thai = 'Đang đi' THEN 1 ELSE 2 END
                LIMIT 1
            ) cg ON true
            WHERE tx.ma_tx NOT IN (
                SELECT c2.ma_tx FROM chuyen_giao c2 WHERE c2.trang_thai = 'Đang đi'
            )
            ORDER BY tx.ma_tx ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String maChuyen = rs.getString("ma_chuyen");
            String ttChuyen = rs.getString("trang_thai_chuyen");
            String thongTin;
            if (maChuyen != null && !maChuyen.isBlank()) {
                thongTin = "Đã xếp chuyến " + maChuyen + " (" + ttChuyen + ")";
            } else {
                thongTin = "Sẵn sàng nhận chuyến (Chưa gán)";
            }

            return TaiXeKhaDungDTO.builder()
                    .maTx(rs.getString("ma_tx"))
                    .hoTen(rs.getString("ho_ten"))
                    .sdt(rs.getString("sdt"))
                    .bangLai(rs.getString("bang_lai"))
                    .trangThaiTaiKhoan(rs.getString("trang_thai_tk"))
                    .chuyenHienTai(maChuyen)
                    .thongTinPhanCong(thongTin)
                    .khaDung(true)
                    .build();
        });
    }

    /**
     * STT 4: Danh sách phương tiện khả dụng (sẵn sàng, không bảo trì, không trùng chuyến đang chạy)
     */
    public List<PhuongTienKhaDungDTO> getPhuongTienKhaDungList() {
        String sql = """
            SELECT 
                pt.ma_pt,
                pt.bien_so,
                pt.loai_xe,
                pt.tai_trong,
                pt.trang_thai,
                cg.ma_chuyen AS chuyen_hien_tai
            FROM phuong_tien pt
            LEFT JOIN LATERAL (
                SELECT c.ma_chuyen
                FROM chuyen_giao c
                WHERE c.ma_pt = pt.ma_pt AND c.trang_thai IN ('Chưa đi', 'Đang đi')
                ORDER BY CASE WHEN c.trang_thai = 'Đang đi' THEN 1 ELSE 2 END
                LIMIT 1
            ) cg ON true
            WHERE pt.trang_thai = 'Sẵn sàng'
              AND pt.ma_pt NOT IN (
                  SELECT c2.ma_pt FROM chuyen_giao c2 WHERE c2.trang_thai = 'Đang đi'
              )
            ORDER BY pt.ma_pt ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> PhuongTienKhaDungDTO.builder()
                .maPt(rs.getString("ma_pt"))
                .bienSo(rs.getString("bien_so"))
                .loaiXe(rs.getString("loai_xe"))
                .taiTrong(rs.getBigDecimal("tai_trong"))
                .trangThai(rs.getString("trang_thai"))
                .chuyenHienTai(rs.getString("chuyen_hien_tai"))
                .khaDung(true)
                .build());
    }

    /**
     * Cập nhật thông tin chuyến giao (khi chuyến ở trạng thái 'Chưa đi').
     * Cho phép đổi tài xế, đổi phương tiện, đổi lộ trình, thời gian.
     */
    @Transactional
    public ChuyenGiao capNhatChuyenGiao(String maChuyen, CapNhatChuyenGiaoRequest req) {
        ChuyenGiao cg = chuyenGiaoRepository.findById(maChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao: " + maChuyen));

        if (!"Chưa đi".equals(cg.getTrangThai())) {
            throw new RuntimeException("Chuyến giao đang ở trạng thái [" + cg.getTrangThai() + "], không được phép chỉnh sửa.");
        }

        if (req.getLoai() != null && !req.getLoai().isBlank()) {
            cg.setLoaiChuyen(req.getLoai());
        }
        if ("LIEN_KHO".equals(cg.getLoaiChuyen())) {
            if (req.getMaTuyen() != null && !req.getMaTuyen().isBlank()) {
                cg.setMaTuyen(req.getMaTuyen());
            }
            cg.setMaKhoGiao(null);
        } else if ("GIAO_CUOI".equals(cg.getLoaiChuyen())) {
            if (req.getMaKhoGiao() != null && !req.getMaKhoGiao().isBlank()) {
                cg.setMaKhoGiao(req.getMaKhoGiao());
            }
            cg.setMaTuyen(null);
        }

        if (req.getMaTx() != null && !req.getMaTx().isBlank()) cg.setMaTx(req.getMaTx());
        if (req.getMaPt() != null && !req.getMaPt().isBlank()) cg.setMaPt(req.getMaPt());
        if (req.getNgayXuatPhat() != null) cg.setNgayXuatPhat(req.getNgayXuatPhat());
        if (req.getNgayDenDuKien() != null) cg.setNgayDenDuKien(req.getNgayDenDuKien());

        if (req.getMaNd() != null && !req.getMaNd().isBlank()) {
            try {
                jdbcTemplate.queryForList("SELECT fn_dat_phien(?, ?)", req.getMaNd(), "Cập nhật phân công chuyến " + maChuyen);
            } catch (Exception ignored) {
            }
        }

        try {
            return chuyenGiaoRepository.save(cg);
        } catch (org.springframework.dao.DataAccessException ex) {
            Throwable root = ex.getMostSpecificCause();
            String msg = (root != null && root.getMessage() != null) ? root.getMessage() : ex.getMessage();
            int nl = msg.indexOf('\n');
            if (nl > 0) msg = msg.substring(0, nl);
            if (msg.startsWith("ERROR: ")) msg = msg.substring(7);
            throw new RuntimeException(msg.trim());
        }
    }

    /**
     * Hủy chuyến giao (gọi Stored Procedure sp_huy_chuyen, tự động giải phóng kiện hàng).
     */
    @Transactional
    public void huyChuyenGiao(String maChuyen, String maNd) {
        ChuyenGiao cg = chuyenGiaoRepository.findById(maChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao: " + maChuyen));

        if (!"Chưa đi".equals(cg.getTrangThai())) {
            throw new RuntimeException("Thao tác xóa/hủy không thể thực hiện! Chuyến giao đang ở trạng thái [" + cg.getTrangThai() + "]. Chỉ được xóa/hủy chuyến khi chưa xuất phát.");
        }

        try {
            jdbcTemplate.execute((java.sql.Connection con) -> {
                try (java.sql.CallableStatement cs = con.prepareCall("CALL sp_huy_chuyen(?, ?)")) {
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

    /**
     * Xóa hẳn chuyến giao khỏi cơ sở dữ liệu (hard delete).
     * Áp dụng cho các chuyến ở trạng thái 'Chưa đi' hoặc 'Đã hủy'.
     * - 'Chưa đi': gọi sp_huy_chuyen để giải phóng kiện hàng về kho, sau đó xóa hẳn bản ghi khỏi database.
     * - 'Đã hủy': xóa hẳn bản ghi khỏi database.
     * - 'Đang đi', 'Hoàn thành': không cho phép xóa.
     */
    @Transactional
    public void xoaChuyenGiao(String maChuyen, String maNd) {
        ChuyenGiao cg = chuyenGiaoRepository.findById(maChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao: " + maChuyen));

        if ("Đang đi".equals(cg.getTrangThai())) {
            throw new RuntimeException("Không thể xóa chuyến đang di chuyển trên đường!");
        }
        if ("Hoàn thành".equals(cg.getTrangThai())) {
            throw new RuntimeException("Không thể xóa chuyến đã hoàn thành!");
        }

        // Nếu chuyến 'Chưa đi', hủy trước bằng sp_huy_chuyen để giải phóng kiện hàng về kho
        if ("Chưa đi".equals(cg.getTrangThai())) {
            try {
                jdbcTemplate.execute((java.sql.Connection con) -> {
                    try (java.sql.CallableStatement cs = con.prepareCall("CALL sp_huy_chuyen(?, ?)")) {
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

        // Xóa hoàn toàn bản ghi khỏi database
        jdbcTemplate.update("DELETE FROM chi_tiet_chuyen_giao WHERE ma_chuyen = ?", maChuyen);
        jdbcTemplate.update("DELETE FROM chuyen_giao WHERE ma_chuyen = ?", maChuyen);
    }

    /**
     * Xác nhận hoàn thành chuyến giao (sp_hoan_thanh_chuyen).
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
}
