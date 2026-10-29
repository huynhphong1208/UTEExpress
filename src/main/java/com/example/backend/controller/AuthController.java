package com.example.backend.controller;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.NguoiDung;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.TaiXe;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NguoiDungRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.TaiXeRepository;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xác thực: Đăng nhập, Đăng ký (OTP), Quên mật khẩu, Đổi mật khẩu
 */
@Tag(name = "Auth", description = "API Xác thực - Đăng nhập, Đăng ký, Quên mật khẩu, Đổi mật khẩu")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TaiXeRepository taiXeRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.backend.service.UserAvatarStorageService userAvatarStorageService;

    public AuthController(AuthService authService,
                          KhachHangRepository khachHangRepository,
                          NhanVienRepository nhanVienRepository,
                          TaiXeRepository taiXeRepository,
                          NguoiDungRepository nguoiDungRepository,
                          PasswordEncoder passwordEncoder,
                          com.example.backend.service.UserAvatarStorageService userAvatarStorageService) {
        this.authService = authService;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.taiXeRepository = taiXeRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAvatarStorageService = userAvatarStorageService;
    }

    // ---- UC02: Đăng nhập ----
    @Operation(summary = "Đăng nhập", description = "Nhận username + password, trả về JWT Token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        LoginResponse resp = authService.login(request);

        // Đảm bảo hoTen luôn được điền chính xác theo role của tài khoản
        if (resp.getHoTen() == null || resp.getHoTen().isBlank() || resp.getHoTen().equals(resp.getTenDangNhap())) {
            String role = resp.getVaiTro();
            String name = null;
            if ("KHACH_HANG".equals(role)) {
                name = khachHangRepository.findBySdt(resp.getTenDangNhap())
                        .or(() -> khachHangRepository.findByNguoiDung_MaNd(resp.getMaNd()))
                        .map(KhachHang::getHoTen)
                        .orElse(null);
            } else if ("TAI_XE".equals(role)) {
                name = taiXeRepository.findByNguoiDung_MaNd(resp.getMaNd())
                        .map(TaiXe::getHoTen)
                        .orElse(null);
            } else {
                name = nhanVienRepository.findByNguoiDung_MaNd(resp.getMaNd())
                        .map(NhanVien::getHoTen)
                        .orElse(null);
            }
            if (name != null && !name.isBlank()) {
                resp.setHoTen(name);
            }
        }

        resp.setAvatarUrl(userAvatarStorageService.getAvatar(resp.getMaNd()));

        ResponseCookie cookie = ResponseCookie.from("accessToken", resp.getAccessToken())
                .path("/")
                .maxAge(86400)
                .httpOnly(false)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", resp));
    }

    @Operation(summary = "Đăng xuất", description = "Xóa cookie xác thực accessToken")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(false)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    // ---- UC01 Step 1: Gửi OTP đăng ký ----
    @Operation(summary = "Gửi OTP đăng ký",
               description = "Kiểm tra SĐT / Email chưa trùng → Sinh OTP → Gửi về Email khách hàng")
    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        authService.sendRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đã được gửi đến email " + request.getEmail() + ". Vui lòng kiểm tra hộp thư."));
    }

    // ---- UC01 Step 2: Xác thực OTP và hoàn tất đăng ký ----
    @Operation(summary = "Xác thực OTP & hoàn tất đăng ký",
               description = "Nhận form đăng ký + mã OTP → Tạo tài khoản KHACH_HANG")
    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<String>> registerVerify(
            @Valid @RequestBody RegisterVerifyRequest request) {
        String maKh = authService.registerVerify(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", maKh));
    }

    // ---- Quên mật khẩu: Gửi OTP ----
    @Operation(summary = "Quên mật khẩu - Gửi OTP",
               description = "Gửi OTP về email để đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn"));
    }

    // ---- Quên mật khẩu: Xác thực OTP và đặt lại mật khẩu ----
    @Operation(summary = "Đặt lại mật khẩu",
               description = "Xác thực OTP và cập nhật mật khẩu mới")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam @NotBlank String sdt,
            @RequestParam @NotBlank String otp,
            @RequestParam @NotBlank @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự") String matKhauMoi) {
        authService.resetPassword(sdt, otp, matKhauMoi);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."));
    }

    // ---- Đổi mật khẩu tài khoản đang đăng nhập ----
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng hiện tại")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }

        String matKhauCu = body.get("matKhauCu");
        String matKhauMoi = body.get("matKhauMoi");
        String xacNhanMatKhau = body.get("xacNhanMatKhau");

        if (matKhauCu == null || matKhauCu.isBlank()) {
            throw new BusinessException("Vui lòng nhập mật khẩu hiện tại");
        }
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (xacNhanMatKhau == null || !matKhauMoi.equals(xacNhanMatKhau)) {
            throw new BusinessException("Xác nhận mật khẩu mới không khớp");
        }

        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản", 404));

        if (!passwordEncoder.matches(matKhauCu, nd.getMatKhau())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }

        nd.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(nd);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công!"));
    }

    // ---- Cập nhật thông tin tài khoản (Họ tên, SĐT, Email, Địa chỉ) cho mọi Role ----
    @Operation(summary = "Cập nhật hồ sơ tài khoản", description = "Cho phép mọi role cập nhật họ tên, SĐT, Email")
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }
        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        String role = nd.getVaiTro() != null ? nd.getVaiTro().getMaVaiTro() : "";
        String hoTen = request.getHoTen().trim();
        String sdt = request.getSdt().trim();
        String email = request.getEmail() != null ? request.getEmail().trim() : null;

        if ("KHACH_HANG".equals(role)) {
            KhachHang kh = khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd())
                    .or(() -> khachHangRepository.findBySdt(nd.getTenDangNhap()))
                    .orElse(null);
            if (kh != null) {
                if (!sdt.equals(kh.getSdt()) && khachHangRepository.existsBySdt(sdt)) {
                    throw new BusinessException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
                }
                kh.setHoTen(hoTen);
                kh.setSdt(sdt);
                if (email != null && !email.isBlank()) {
                    kh.setEmail(email);
                }
                if (request.getDiaChi() != null && !request.getDiaChi().isBlank()) {
                    kh.setDiaChiMacDinh(request.getDiaChi().trim());
                }
                khachHangRepository.save(kh);
            }
        } else if ("TAI_XE".equals(role)) {
            TaiXe tx = taiXeRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (tx != null) {
                if (!sdt.equals(tx.getSdt()) && taiXeRepository.existsBySdt(sdt)) {
                    throw new BusinessException("Số điện thoại này đã được sử dụng bởi tài xế khác");
                }
                tx.setHoTen(hoTen);
                tx.setSdt(sdt);
                taiXeRepository.save(tx);
            }
        } else { // NVBC, NVDP, ADMIN
            NhanVien nv = nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (nv != null) {
                if (!sdt.equals(nv.getSdt()) && nhanVienRepository.existsBySdt(sdt)) {
                    throw new BusinessException("Số điện thoại này đã được sử dụng bởi nhân viên khác");
                }
                nv.setHoTen(hoTen);
                nv.setSdt(sdt);
                if (email != null && !email.isBlank()) {
                    nv.setEmail(email);
                }
                nhanVienRepository.save(nv);
            } else if ("ADMIN".equals(role)) {
                String maNv = "NV000";
                NhanVien adminNv = nhanVienRepository.findById(maNv).orElse(null);
                if (adminNv == null) {
                    adminNv = NhanVien.builder()
                            .maNv(maNv)
                            .hoTen(hoTen)
                            .chucVu("Nhân viên điều phối")
                            .sdt(sdt)
                            .email(email)
                            .nguoiDung(nd)
                            .build();
                } else {
                    adminNv.setHoTen(hoTen);
                    adminNv.setSdt(sdt);
                    adminNv.setEmail(email);
                }
                nhanVienRepository.save(adminNv);
            }
        }

        return getCurrentUser(authentication);
    }

    // ---- Lấy thông tin tài khoản hiện tại (Xem chi tiết hồ sơ tài khoản) ----
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }
        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        String role = nd.getVaiTro() != null ? nd.getVaiTro().getMaVaiTro() : "";
        Map<String, Object> data = new HashMap<>();
        data.put("maNd", nd.getMaNd());
        data.put("tenDangNhap", nd.getTenDangNhap());
        data.put("vaiTro", role);
        data.put("tenVaiTro", nd.getVaiTro() != null ? nd.getVaiTro().getTenVaiTro() : "");
        data.put("trangThai", nd.getTrangThai());
        data.put("avatarUrl", userAvatarStorageService.getAvatar(nd.getMaNd()));

        if ("KHACH_HANG".equals(role)) {
            KhachHang kh = khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd())
                    .or(() -> khachHangRepository.findBySdt(nd.getTenDangNhap()))
                    .orElse(null);
            if (kh != null) {
                data.put("hoTen", kh.getHoTen());
                data.put("sdt", kh.getSdt());
                data.put("email", kh.getEmail() != null ? kh.getEmail() : "Chưa cập nhật");
                data.put("diaChi", kh.getDiaChiMacDinh() != null ? kh.getDiaChiMacDinh() : "Chưa cập nhật");
                data.put("maDinhDanh", kh.getMaKh());
            } else {
                data.put("hoTen", nd.getTenDangNhap());
                data.put("sdt", nd.getTenDangNhap());
            }
        } else if ("TAI_XE".equals(role)) {
            TaiXe tx = taiXeRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (tx != null) {
                data.put("hoTen", tx.getHoTen());
                data.put("sdt", tx.getSdt());
                data.put("bangLai", tx.getBangLai() != null ? tx.getBangLai() : "Chưa cập nhật");
                data.put("maDinhDanh", tx.getMaTx());
            } else {
                data.put("hoTen", nd.getTenDangNhap());
                data.put("sdt", nd.getTenDangNhap());
            }
        } else {
            NhanVien nv = nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd()).orElse(null);
            if (nv != null) {
                data.put("hoTen", nv.getHoTen());
                data.put("sdt", nv.getSdt());
                data.put("email", nv.getEmail() != null ? nv.getEmail() : "Chưa cập nhật");
                data.put("chucVu", nv.getChucVu());
                data.put("khoLamViec", nv.getKho() != null ? nv.getKho().getTenKho() : (nv.getMaKho() != null ? nv.getMaKho() : "Tổng công ty"));
                data.put("maDinhDanh", nv.getMaNv());
            } else {
                data.put("hoTen", nd.getTenDangNhap());
                data.put("sdt", nd.getTenDangNhap());
            }
        }
        if (!data.containsKey("hoTen") || data.get("hoTen") == null) {
            data.put("hoTen", nd.getTenDangNhap());
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", data));
    }

    // ---- Tải lên ảnh đại diện (Lưu trên File System, không cần cột DB) ----
    @Operation(summary = "Tải lên ảnh đại diện")
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file ảnh để tải lên");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("File tải lên phải là định dạng hình ảnh (PNG, JPG, WEBP,...)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("Dung lượng ảnh không được vượt quá 5MB");
        }

        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản", 404));

        try {
            String avatarUrl = userAvatarStorageService.saveFile(nd.getMaNd(), file);
            return ResponseEntity.ok(ApiResponse.success("Tải lên ảnh đại diện thành công", Map.of("avatarUrl", avatarUrl)));
        } catch (Exception e) {
            throw new BusinessException("Lỗi lưu trữ ảnh: " + e.getMessage());
        }
    }

    // ---- Cập nhật link ảnh đại diện (Lưu ánh xạ trên File System, không cần cột DB) ----
    @Operation(summary = "Cập nhật link ảnh đại diện")
    @PostMapping("/avatar/link")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateAvatarLink(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }
        String avatarUrl = body.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new BusinessException("Vui lòng cung cấp link ảnh hợp lệ");
        }
        String trimmed = avatarUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("/uploads/")) {
            throw new BusinessException("Link ảnh phải bắt đầu bằng http:// hoặc https://");
        }

        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản", 404));

        String savedUrl = userAvatarStorageService.saveLink(nd.getMaNd(), trimmed);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật link ảnh đại diện thành công", Map.of("avatarUrl", savedUrl)));
    }

    // ---- Xóa ảnh đại diện (Trở về mặc định) ----
    @Operation(summary = "Xóa ảnh đại diện")
    @PostMapping("/avatar/remove")
    public ResponseEntity<ApiResponse<Void>> removeAvatar(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("Bạn chưa đăng nhập", 401);
        }
        String username = authentication.getName();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản", 404));

        userAvatarStorageService.removeAvatar(nd.getMaNd());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa ảnh đại diện", null));
    }
}

