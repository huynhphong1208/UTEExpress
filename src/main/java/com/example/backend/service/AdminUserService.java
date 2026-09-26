package com.example.backend.service;

import com.example.backend.dto.request.TaoTaiKhoanNoiBo;
import com.example.backend.dto.response.NguoiDungResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản trị người dùng (UC15) - dành riêng cho ADMIN
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final NguoiDungRepository nguoiDungRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TaiXeRepository taiXeRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private static final String DEFAULT_PASSWORD = "123456@Ute";

    // =========================================================
    // Lấy danh sách tài khoản (phân trang, tìm kiếm, lọc role)
    // =========================================================
    public Page<NguoiDungResponse> getAllUsers(String keyword, String maVaiTro,
                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("maNd").ascending());
        Page<NguoiDung> ndPage = nguoiDungRepository.searchUsers(keyword, maVaiTro, pageable);

        return ndPage.map(this::toNguoiDungResponse);
    }

    // =========================================================
    // Tạo tài khoản nội bộ (NVBC / NVDP / TAI_XE)
    // =========================================================
    @Transactional
    public NguoiDungResponse createInternalAccount(TaoTaiKhoanNoiBo request) {
        // Kiểm tra SĐT đã dùng làm tên đăng nhập chưa
        if (nguoiDungRepository.existsByTenDangNhap(request.getSdt())) {
            throw new BusinessException("Số điện thoại này đã có tài khoản trong hệ thống");
        }

        validateInternalRequest(request);

        // Tạo NguoiDung
        VaiTro vaiTro = entityManager.getReference(VaiTro.class, request.getMaVaiTro());
        NguoiDung nd = NguoiDung.builder()
                .tenDangNhap(request.getSdt())
                .matKhau(passwordEncoder.encode(DEFAULT_PASSWORD))
                .trangThai("Hoạt động")
                .vaiTro(vaiTro)
                .build();
        nd = nguoiDungRepository.save(nd);

        // Tạo hồ sơ tương ứng
        switch (request.getMaVaiTro()) {
            case "NVBC" -> {
                NhanVien nv = NhanVien.builder()
                        .hoTen(request.getHoTen())
                        .sdt(request.getSdt())
                        .email(request.getEmail())
                        .chucVu("Nhân viên bưu cục")
                        .maKho(request.getMaKho())
                        .nguoiDung(nd)
                        .build();
                nhanVienRepository.save(nv);
            }
            case "NVDP" -> {
                NhanVien nv = NhanVien.builder()
                        .hoTen(request.getHoTen())
                        .sdt(request.getSdt())
                        .email(request.getEmail())
                        .chucVu("Nhân viên điều phối")
                        .maKho(request.getMaKho())
                        .nguoiDung(nd)
                        .build();
                nhanVienRepository.save(nv);
            }
            case "TAI_XE" -> {
                TaiXe tx = TaiXe.builder()
                        .hoTen(request.getHoTen())
                        .sdt(request.getSdt())
                        .bangLai(request.getBangLai())
                        .nguoiDung(nd)
                        .build();
                taiXeRepository.save(tx);
            }
            default -> throw new BusinessException("Vai trò không hợp lệ: " + request.getMaVaiTro());
        }

        log.info("Tạo tài khoản nội bộ thành công: {} - {}", request.getMaVaiTro(), nd.getMaNd());
        return toNguoiDungResponse(nd);
    }

    // =========================================================
    // Khóa / Mở khóa tài khoản (với kiểm tra công việc dở dang)
    // =========================================================
    @Transactional
    public NguoiDungResponse updateStatus(String maNd, String trangThaiMoi) {
        if (!"Hoạt động".equals(trangThaiMoi) && !"Bị khóa".equals(trangThaiMoi)) {
            throw new BusinessException("Trạng thái không hợp lệ. Chỉ chấp nhận: 'Hoạt động' hoặc 'Bị khóa'");
        }

        NguoiDung nd = nguoiDungRepository.findById(maNd)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản: " + maNd, 404));

        // Kiểm tra công việc dở dang trước khi khóa (gọi fn_so_viec_dang_xu_ly)
        if ("Bị khóa".equals(trangThaiMoi)) {
            Long soViec = (Long) entityManager
                    .createNativeQuery("SELECT fn_so_viec_dang_xu_ly(:maNd)")
                    .setParameter("maNd", maNd)
                    .getSingleResult();

            if (soViec != null && soViec > 0) {
                throw new BusinessException(
                        String.format("Không thể khóa tài khoản này: còn %d công việc/đơn hàng đang xử lý dở dang", soViec)
                );
            }
        }

        nd.setTrangThai(trangThaiMoi);
        nd = nguoiDungRepository.save(nd);
        log.info("Cập nhật trạng thái tài khoản {} → {}", maNd, trangThaiMoi);
        return toNguoiDungResponse(nd);
    }

    // =========================================================
    // Đặt lại mật khẩu mặc định (Admin reset cho nhân viên)
    // =========================================================
    @Transactional
    public void resetToDefaultPassword(String maNd) {
        NguoiDung nd = nguoiDungRepository.findById(maNd)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản: " + maNd, 404));

        // Chỉ reset mật khẩu tài khoản nội bộ, không reset tài khoản KHACH_HANG
        if ("KHACH_HANG".equals(nd.getVaiTro().getMaVaiTro())) {
            throw new BusinessException("Không thể đặt lại mật khẩu tài khoản khách hàng qua chức năng này");
        }

        nd.setMatKhau(passwordEncoder.encode(DEFAULT_PASSWORD));
        nguoiDungRepository.save(nd);
        log.info("Đặt lại mật khẩu mặc định cho tài khoản: {}", maNd);
    }

    // =========================================================
    // Helper: map NguoiDung → NguoiDungResponse
    // =========================================================
    private NguoiDungResponse toNguoiDungResponse(NguoiDung nd) {
        String hoTen = null, email = null, sdt = null;

        // Thử lấy thông tin hồ sơ theo vai trò
        switch (nd.getVaiTro().getMaVaiTro()) {
            case "KHACH_HANG" -> {
                var kh = khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
                if (kh != null) { hoTen = kh.getHoTen(); email = kh.getEmail(); sdt = kh.getSdt(); }
            }
            case "NVBC", "NVDP" -> {
                var nv = nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
                if (nv != null) { hoTen = nv.getHoTen(); email = nv.getEmail(); sdt = nv.getSdt(); }
            }
            case "TAI_XE" -> {
                var tx = taiXeRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
                if (tx != null) { hoTen = tx.getHoTen(); sdt = tx.getSdt(); }
            }
        }

        return NguoiDungResponse.builder()
                .maNd(nd.getMaNd())
                .tenDangNhap(nd.getTenDangNhap())
                .maVaiTro(nd.getVaiTro().getMaVaiTro())
                .tenVaiTro(nd.getVaiTro().getTenVaiTro())
                .trangThai(nd.getTrangThai())
                .hoTen(hoTen)
                .email(email)
                .sdt(sdt)
                .build();
    }

    private void validateInternalRequest(TaoTaiKhoanNoiBo request) {
        if ("NVBC".equals(request.getMaVaiTro()) || "NVDP".equals(request.getMaVaiTro())) {
            if (request.getMaKho() == null || request.getMaKho().isBlank()) {
                throw new BusinessException("Nhân viên bưu cục / điều phối bắt buộc phải có mã kho");
            }
        }
        if ("TAI_XE".equals(request.getMaVaiTro())) {
            if (nhanVienRepository.existsBySdt(request.getSdt())) {
                throw new BusinessException("SĐT này đã được dùng trong hồ sơ nhân viên khác");
            }
        }
    }
}
