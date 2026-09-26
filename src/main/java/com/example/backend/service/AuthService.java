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
    private final OtpCacheService otpCacheService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    // =========================================================
    // UC02: Đăng nhập
    // =========================================================
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getTenDangNhap(),
                        request.getMatKhau()
                )
        );

        String token = jwtUtils.generateToken(authentication);

        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(request.getTenDangNhap())
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại", 404));

        return LoginResponse.builder()
                .accessToken(token)
                .maNd(nd.getMaNd())
                .tenDangNhap(nd.getTenDangNhap())
                .vaiTro(nd.getVaiTro().getMaVaiTro())
                .tenVaiTro(nd.getVaiTro().getTenVaiTro())
                .build();
    }

    // =========================================================
    // UC01 - Step 1: Gửi OTP đăng ký
    // =========================================================
    public void sendRegisterOtp(SendOtpRequest request) {
        // Kiểm tra SĐT đã tồn tại chưa (SĐT = tên đăng nhập)
        if (nguoiDungRepository.existsByTenDangNhap(request.getSdt())) {
            throw new BusinessException("Số điện thoại này đã được đăng ký tài khoản");
        }

        // Kiểm tra email trùng
        if (request.getEmail() != null && khachHangRepository.existsByEmail(request.getEmail())) {
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
