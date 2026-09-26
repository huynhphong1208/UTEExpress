package com.example.backend.service;

import com.example.backend.dto.request.TaoDonHangRequest;
import com.example.backend.dto.response.DonHangChiTietResponse;
import com.example.backend.dto.response.TraCuuDonHangResponse;
import com.example.backend.entity.KhachHang;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.KhachHangRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý nghiệp vụ Đơn hàng:
 *  - Tạo đơn (UC04) - gọi SP
 *  - Tra cứu công khai (UC03) - query View
 *  - Đơn hàng của tôi (UC05) - query View có phân trang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DonHangService {

    private final KhachHangRepository khachHangRepository;
    private final EntityManager entityManager;

    // =========================================================
    // UC04: Tạo đơn hàng (gọi sp_tao_don_hang)
    // =========================================================
    @Transactional
    public String taoDonHang(String maNd, TaoDonHangRequest request) {
        // Lấy mã khách hàng từ mã người dùng
        KhachHang kh = khachHangRepository.findByNguoiDung_MaNd(maNd)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ khách hàng cho tài khoản này", 404));

        // Chuyển danh sách kiện sang JSONB string
        StringBuilder jsonBuilder = new StringBuilder("[");
        List<TaoDonHangRequest.KienHangRequest> kienList = request.getDanhSachKien();
        for (int i = 0; i < kienList.size(); i++) {
            TaoDonHangRequest.KienHangRequest k = kienList.get(i);
            jsonBuilder.append(String.format(
                    "{\"khoi_luong\":%s,\"dai\":%s,\"rong\":%s,\"cao\":%s,\"loai_hang\":%s}",
                    k.getKhoiLuong(), k.getDai(), k.getRong(), k.getCao(),
                    k.getLoaiHang() != null ? "\"" + k.getLoaiHang() + "\"" : "null"
            ));
            if (i < kienList.size() - 1) jsonBuilder.append(",");
        }
        jsonBuilder.append("]");

        // Đặt phiên người dùng trong PostgreSQL session
        entityManager.createNativeQuery(
                "SELECT fn_dat_phien(:maNd, 'Tạo đơn hàng')")
                .setParameter("maNd", maNd)
                .getSingleResult();

        // Gọi SP sp_tao_don_hang
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_tao_don_hang")
                .registerStoredProcedureParameter("p_ma_kh_gui", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ten_nguoi_nhan", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_sdt_nhan", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_dia_chi_lay", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_dia_chi_giao", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ma_kho_gui", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ma_kho_nhan", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_cod", BigDecimal.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_kien", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ma_nd", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ma_dh", String.class, ParameterMode.INOUT)
                .setParameter("p_ma_kh_gui", kh.getMaKh())
                .setParameter("p_ten_nguoi_nhan", request.getTenNguoiNhan())
                .setParameter("p_sdt_nhan", request.getSdtNhan())
                .setParameter("p_dia_chi_lay", request.getDiaChiLay())
                .setParameter("p_dia_chi_giao", request.getDiaChiGiao())
                .setParameter("p_ma_kho_gui", request.getMaKhoGui())
                .setParameter("p_ma_kho_nhan", request.getMaKhoNhan())
                .setParameter("p_cod", request.getCod() != null ? request.getCod() : BigDecimal.ZERO)
                .setParameter("p_kien", jsonBuilder.toString())
                .setParameter("p_ma_nd", maNd)
                .setParameter("p_ma_dh", null);

        query.execute();
        String maDh = (String) query.getOutputParameterValue("p_ma_dh");
        log.info("Tạo đơn hàng thành công - maDH: {}", maDh);
        return maDh;
    }

    // =========================================================
    // UC03: Tra cứu công khai (query View v_tra_cuu_cong_khai)
    // =========================================================
    @SuppressWarnings("unchecked")
    public TraCuuDonHangResponse traCuuCongKhai(String maDh) {
        // Query view tra cứu
        List<Object[]> rows = entityManager.createNativeQuery(
                """
                SELECT ma_dh, ngay_tao, ten_trang_thai, cap_nhat_luc, sdt_nhan_che, so_kien
                FROM v_tra_cuu_cong_khai
                WHERE ma_dh = :maDh
                """)
                .setParameter("maDh", maDh)
                .getResultList();

        if (rows.isEmpty()) {
            throw new BusinessException("Không tìm thấy đơn hàng với mã: " + maDh, 404);
        }

        Object[] row = rows.get(0);

        // Query lịch sử vận chuyển
        List<Object[]> lichSuRows = entityManager.createNativeQuery(
                """
                SELECT ma_tt, ten_trang_thai, thoi_gian, ghi_chu
                FROM v_lich_su_van_chuyen
                WHERE ma_dh = :maDh
                ORDER BY thoi_gian DESC
                """)
                .setParameter("maDh", maDh)
                .getResultList();

        List<TraCuuDonHangResponse.LichSuTrangThai> lichSu = lichSuRows.stream()
                .map(ls -> TraCuuDonHangResponse.LichSuTrangThai.builder()
                        .maTt((String) ls[0])
                        .tenTrangThai((String) ls[1])
                        .thoiGian(toLocalDateTime(ls[2]))
                        .ghiChu((String) ls[3])
                        .build())
                .toList();

        return TraCuuDonHangResponse.builder()
                .maDh((String) row[0])
                .ngayTao(toLocalDateTime(row[1]))
                .trangThai((String) row[2])
                .capNhatLuc(toLocalDateTime(row[3]))
                .sdtNhanChe((String) row[4])
                .soKien(toLong(row[5]))
                .lichSu(lichSu)
                .build();
    }

    // =========================================================
    // UC05: Đơn hàng của tôi (Customer - phân trang, lọc trạng thái)
    // =========================================================
    @SuppressWarnings("unchecked")
    public Page<DonHangChiTietResponse> layDonHangCuaToi(String maNd, String maTt,
                                                          int page, int size) {
        KhachHang kh = khachHangRepository.findByNguoiDung_MaNd(maNd)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ khách hàng", 404));

        String baseQuery = """
                SELECT ma_dh, ngay_tao, ten_nguoi_nhan, sdt_nhan, dia_chi_lay, dia_chi_giao,
                       ten_kho_gui, ten_kho_nhan, ma_tt, ten_trang_thai,
                       so_kien, tong_kg, phi_van_chuyen, cod, tong_thu
                FROM v_don_hang_chi_tiet
                WHERE ma_kh_gui = :maKh
                """;

        String filterQuery = maTt != null && !maTt.isBlank()
                ? baseQuery + " AND ma_tt = :maTt ORDER BY ngay_tao DESC"
                : baseQuery + " ORDER BY ngay_tao DESC";

        String countQuery = "SELECT COUNT(*) FROM v_don_hang_chi_tiet WHERE ma_kh_gui = :maKh"
                + (maTt != null && !maTt.isBlank() ? " AND ma_tt = :maTt" : "");

        var dataQuery = entityManager.createNativeQuery(filterQuery)
                .setParameter("maKh", kh.getMaKh())
                .setFirstResult(page * size)
                .setMaxResults(size);

        var cntQuery = entityManager.createNativeQuery(countQuery)
                .setParameter("maKh", kh.getMaKh());

        if (maTt != null && !maTt.isBlank()) {
            dataQuery.setParameter("maTt", maTt);
            cntQuery.setParameter("maTt", maTt);
        }

        List<Object[]> rows = dataQuery.getResultList();
        long total = toLong(cntQuery.getSingleResult());

        List<DonHangChiTietResponse> content = rows.stream()
                .map(r -> DonHangChiTietResponse.builder()
                        .maDh((String) r[0])
                        .ngayTao(toLocalDateTime(r[1]))
                        .tenNguoiNhan((String) r[2])
                        .sdtNhan((String) r[3])
                        .diaChiLay((String) r[4])
                        .diaChiGiao((String) r[5])
                        .tenKhoGui((String) r[6])
                        .tenKhoNhan((String) r[7])
                        .maTt((String) r[8])
                        .tenTrangThai((String) r[9])
                        .soKien(toLong(r[10]))
                        .tongKg(toBigDecimal(r[11]))
                        .phiVanChuyen(toBigDecimal(r[12]))
                        .cod(toBigDecimal(r[13]))
                        .tongThu(toBigDecimal(r[14]))
                        .build())
                .toList();

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    // --- Helpers ---
    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime ldt) return ldt;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }

    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long l) return l;
        if (obj instanceof Number n) return n.longValue();
        return 0L;
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number n) return new BigDecimal(n.toString());
        return BigDecimal.ZERO;
    }
}
