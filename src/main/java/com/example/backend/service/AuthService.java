package com.example.backend.service;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.entity.NguoiDung;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.*;
import com.example.backend.security.JwtUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý Xác thực - Đăng nhập, Đăng ký, OTP, Quên mật khẩu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiXeRepository taiXeRepository;
    private final NhanVienRepository nhanVienRepository;
    private final OtpCacheService otpCacheService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    // =========================================================
    // UC02: Đăng nhập
    // =========================================================
    public LoginResponse login(LoginRequest request) {
        String input = request.getTenDangNhap() != null ? request.getTenDangNhap().trim() : "";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input,
                        request.getMatKhau()
                )
        );

        String token = jwtUtils.generateToken(authentication);

        String principalUsername = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(principalUsername)
                .or(() -> nguoiDungRepository.findByTenDangNhap(input))
                .or(() -> nhanVienRepository.findBySdt(input).map(com.example.backend.entity.NhanVien::getNguoiDung))
                .or(() -> khachHangRepository.findBySdt(input).map(com.example.backend.entity.KhachHang::getNguoiDung))
                .or(() -> taiXeRepository.findBySdt(input).map(com.example.backend.entity.TaiXe::getNguoiDung))
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại", 404));

        String hoTen = nd.getTenDangNhap();
        String maDoiTuong = null;
        String vaiTro = nd.getVaiTro().getMaVaiTro();

        if ("TAI_XE".equals(vaiTro)) {
            var tx = taiXeRepository.findByNguoiDung_MaNd(nd.getMaNd())
                    .or(() -> taiXeRepository.findByNguoiDung_TenDangNhap(nd.getTenDangNhap()))
                    .orElse(null);
            if (tx != null) {
                hoTen = tx.getHoTen();
                maDoiTuong = tx.getMaTx();
            }
        } else if ("NVBC".equals(vaiTro) || "NVDP".equals(vaiTro)) {
            var nv = nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (nv != null) {
                hoTen = nv.getHoTen();
                maDoiTuong = nv.getMaNv();
            }
        } else if ("KHACH_HANG".equals(vaiTro)) {
            var kh = khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (kh != null) {
                hoTen = kh.getHoTen();
                maDoiTuong = kh.getMaKh();
            }
        } else if ("ADMIN".equals(vaiTro)) {
            hoTen = "Quản trị viên";
            maDoiTuong = nd.getMaNd();
        }

        return LoginResponse.builder()
                .accessToken(token)
                .maNd(nd.getMaNd())
                .tenDangNhap(nd.getTenDangNhap())
                .vaiTro(vaiTro)
                .tenVaiTro(nd.getVaiTro().getTenVaiTro())
                .hoTen(hoTen)
                .maDoiTuong(maDoiTuong)
                .build();
    }

    // =========================================================
    // UC01 - Step 1: Gửi OTP đăng ký
    // =========================================================
    public void sendRegisterOtp(SendOtpRequest request) {
        // Kiểm tra SĐT đã tồn tại chưa (SĐT = tên đăng nhập trong bảng nguoi_dung)
        if (nguoiDungRepository.existsByTenDangNhap(request.getSdt())) {
            throw new BusinessException("Số điện thoại này đã được đăng ký tài khoản");
        }

        // Kiểm tra email trùng với các tài khoản ĐÃ KÍCH HOẠT (đã có tài khoản người dùng nguoi_dung)
        if (request.getEmail() != null && khachHangRepository.existsByEmailAndNguoiDungIsNotNull(request.getEmail())) {
            throw new BusinessException("Email này đã được sử dụng bởi tài khoản khác");
        }

        // Sinh OTP và gửi email
        String otp = otpCacheService.generateAndStore(request.getSdt());

        if (request.getEmail() != null) {
            emailService.sendOtpEmail(request.getEmail(), otp, "đăng ký tài khoản");
        } else {
            // Nếu không có email, log OTP ra (development only)
            log.info("===> OTP cho {} (không có email): {}", request.getSdt(), otp);
            throw new BusinessException("Vui lòng cung cấp email để nhận mã OTP");
        }
    }

    // =========================================================
    // UC01 - Step 2: Xác thực OTP và hoàn tất đăng ký
    // =========================================================
    @Transactional
    public String registerVerify(RegisterVerifyRequest request) {
        // Xác thực OTP
        otpCacheService.verifyAndConsume(request.getSdt(), request.getOtp());

        // Kiểm tra lại một lần nữa sau OTP (race condition)
        if (nguoiDungRepository.existsByTenDangNhap(request.getSdt())) {
            throw new BusinessException("Số điện thoại này đã được đăng ký tài khoản");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getMatKhau());

        // Gọi Stored Procedure sp_dang_ky_khach_hang
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_dang_ky_khach_hang")
                .registerStoredProcedureParameter("p_ho_ten", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_sdt", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_email", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_mat_khau_hash", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_ma_kh", String.class, ParameterMode.INOUT)
                .setParameter("p_ho_ten", request.getHoTen())
                .setParameter("p_sdt", request.getSdt())
                .setParameter("p_email", request.getEmail())
                .setParameter("p_mat_khau_hash", hashedPassword)
                .setParameter("p_ma_kh", null);

        query.execute();
        String maKh = (String) query.getOutputParameterValue("p_ma_kh");
        log.info("Đăng ký thành công - Mã KH: {}", maKh);
        return maKh;
    }

    // =========================================================
    // Quên mật khẩu - Gửi OTP reset password
    // =========================================================
    public void sendForgotPasswordOtp(ForgotPasswordRequest request) {
        // Kiểm tra tài khoản có tồn tại không
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(request.getSdt())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản với số điện thoại này", 404));

        // Kiểm tra email có khớp với hồ sơ không
        khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd()).ifPresentOrElse(kh -> {
            if (kh.getEmail() == null || !kh.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new BusinessException("Email không khớp với tài khoản này");
            }
        }, () -> {
            throw new BusinessException("Không tìm thấy hồ sơ khách hàng");
        });

        String otp = otpCacheService.generateAndStore("reset_" + request.getSdt());
        emailService.sendOtpEmail(request.getEmail(), otp, "đặt lại mật khẩu");
    }

    // =========================================================
    // Xác thực OTP và đặt lại mật khẩu
    // =========================================================
    @Transactional
    public void resetPassword(String sdt, String otp, String matKhauMoi) {
        otpCacheService.verifyAndConsume("reset_" + sdt, otp);

        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(sdt)
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại", 404));

        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        nd.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(nd);
        log.info("Đặt lại mật khẩu thành công cho tài khoản: {}", sdt);
    }
}
