package com.example.backend.controller.view;

import com.example.backend.dto.payment.CodReconciliationResponse;
import com.example.backend.dto.payment.PaymentSummaryResponse;
import com.example.backend.dto.payment.ProcessPaymentRequest;
import com.example.backend.dto.postoffice.*;
import com.example.backend.dto.report.AdminOverviewReportResponse;
import com.example.backend.dto.report.SlaPerformanceReportResponse;
import com.example.backend.dto.request.TaoDonHangRequest;
import com.example.backend.dto.response.TaoDonHangResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.AdminReportService;
import com.example.backend.service.DonHangService;
import com.example.backend.service.PaymentService;
import com.example.backend.service.PostOfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller điều hướng giao diện Thymeleaf kết nối trực tiếp với cơ sở dữ liệu PostgreSQL.
 * Hỗ trợ đầy đủ dữ liệu thời gian thực cho:
 * 1. Khách hàng: Tạo đơn hàng và xem lịch sử đơn (UC04)
 * 2. Bưu cục: Tiếp nhận đơn (UC08), Quản lý kiện & Chuyển kho (UC09), Cập nhật trạng thái (UC10)
 * 3. Quản trị & Tài chính: Đối soát COD & Thanh toán (UC06, UC07), Báo cáo thống kê & SLA (UC17)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ViewController implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        String uploadPath = "file:" + System.getProperty("user.dir").replace("\\", "/") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    private final DonHangRepository donHangRepository;
    private final KhoRepository khoRepository;
    private final TrangThaiDHRepository trangThaiDHRepository;
    private final KienHangRepository kienHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final LichSuTrangThaiRepository lichSuTrangThaiRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PostOfficeService postOfficeService;
    private final PaymentService paymentService;
    private final AdminReportService adminReportService;
    private final DonHangService donHangService;
    private final NhanVienRepository nhanVienRepository;
    private final PhuongTienRepository phuongTienRepository;
    private final TaiXeRepository taiXeRepository;

    private KhachHang getCurrentKhachHang() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            return khachHangRepository.findBySdt(username)
                    .or(() -> khachHangRepository.findByNguoiDung_MaNd(username))
                    .or(() -> nguoiDungRepository.findByTenDangNhap(username)
                            .flatMap(nd -> khachHangRepository.findByNguoiDung_MaNd(nd.getMaNd())
                                    .or(() -> khachHangRepository.findBySdt(nd.getTenDangNhap()))))
                    .orElse(null);
        }
        return null;
    }

    @GetMapping({"/", "/index", "/index.html"})
    public String index(Model model) {
        List<Kho> danhSachKho = khoRepository.findAll();
        model.addAttribute("danhSachKho", danhSachKho);
        return "guest/03-index";
    }

    @GetMapping("/login")
    public String login() {
        return "guest/02-login";
    }

    @GetMapping("/register")
    public String register() {
        return "guest/01-register";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/login?logout=true";
    }

    @GetMapping("/tracking")
    public String tracking(@RequestParam(required = false) String maDh, Model model) {
        if (maDh != null && !maDh.isBlank()) {
            model.addAttribute("maDh", maDh);
        }
        KhachHang kh = getCurrentKhachHang();
        model.addAttribute("khachHang", kh);
        return "guest/04-tracking";
    }

    @GetMapping({"/customer/dashboard", "/customer/05-dashboard.html"})
    public String customerDashboard(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        KhachHang kh = getCurrentKhachHang();
        if (kh == null) {
            kh = khachHangRepository.findById("KH001").orElse(null);
        }
        model.addAttribute("khachHang", kh);
        if (kh != null) {
            Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
            Page<DonHang> pagedDonHang = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc(kh.getMaKh(), pageable);
            model.addAttribute("danhSachDonHang", pagedDonHang.getContent());
            model.addAttribute("currentPage", pagedDonHang.getNumber());
            model.addAttribute("pageSize", pagedDonHang.getSize());
            model.addAttribute("totalPages", pagedDonHang.getTotalPages());
            model.addAttribute("totalElements", pagedDonHang.getTotalElements());

            List<DonHang> allOrders = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc(kh.getMaKh());
            long total = allOrders.size();
            long shipping = allOrders.stream().filter(d -> d.getTrangThai() != null &&
                    ("TT03".equals(d.getTrangThai().getMaTrangThai()) || "TT04".equals(d.getTrangThai().getMaTrangThai()) ||
                     "TT05".equals(d.getTrangThai().getMaTrangThai()) || "TT06".equals(d.getTrangThai().getMaTrangThai()))).count();
            long delivered = allOrders.stream().filter(d -> d.getTrangThai() != null && "TT07".equals(d.getTrangThai().getMaTrangThai())).count();
            long pending = allOrders.stream().filter(d -> d.getTrangThai() != null &&
                    ("TT01".equals(d.getTrangThai().getMaTrangThai()) || "TT02".equals(d.getTrangThai().getMaTrangThai()))).count();
            model.addAttribute("totalOrders", total);
            model.addAttribute("countShipping", shipping);
            model.addAttribute("countDelivered", delivered);
            model.addAttribute("countPending", pending);
        }
        return "customer/05-dashboard";
    }

    @GetMapping({"/admin/dashboard", "/admin/18-dashboard.html"})
    public String adminDashboard(Model model) {
        log.info("Tải dữ liệu Admin Dashboard từ cơ sở dữ liệu");
        AdminOverviewReportResponse overview = adminReportService.getOverviewReport();
        SlaPerformanceReportResponse slaReport = adminReportService.getSlaPerformanceReport();
        long tongNguoiDung = nguoiDungRepository.count();
        long tongKhachHang = khachHangRepository.count();
        long tongNhanVien = nhanVienRepository.count();
        long tongTaiXe = taiXeRepository.count();
        long tongKho = khoRepository.count();
        long tongPhuongTien = phuongTienRepository.count();
        List<DonHang> donHangMoiNhat = donHangRepository.findAllByOrderByNgayTaoDesc(PageRequest.of(0, 6)).getContent();

        model.addAttribute("overview", overview);
        model.addAttribute("slaReport", slaReport);
        model.addAttribute("tongNguoiDung", tongNguoiDung);
        model.addAttribute("tongKhachHang", tongKhachHang);
        model.addAttribute("tongNhanVien", tongNhanVien);
        model.addAttribute("tongTaiXe", tongTaiXe);
        model.addAttribute("tongKho", tongKho);
        model.addAttribute("tongPhuongTien", tongPhuongTien);
        model.addAttribute("donHangMoiNhat", donHangMoiNhat);

        return "admin/18-dashboard";
    }

    @GetMapping({"/admin/orders", "/admin/27-orders.html"})
    public String adminOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String maTrangThai,
            @RequestParam(required = false) String maKho,
            Model model) {
        log.info("Tải danh sách đơn hàng Admin, keyword={}, maTrangThai={}, maKho={}", keyword, maTrangThai, maKho);

        List<DonHang> allOrders = donHangRepository.findAllByOrderByNgayTaoDesc();

        List<DonHang> filteredOrders = allOrders.stream().filter(dh -> {
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.trim().toLowerCase();
                boolean matchMa = dh.getMaDh() != null && dh.getMaDh().toLowerCase().contains(kw);
                boolean matchTenNhan = dh.getTenNguoiNhan() != null && dh.getTenNguoiNhan().toLowerCase().contains(kw);
                boolean matchSdtNhan = dh.getSdtNhan() != null && dh.getSdtNhan().contains(kw);
                boolean matchMaKh = dh.getMaKhGui() != null && dh.getMaKhGui().toLowerCase().contains(kw);
                if (!matchMa && !matchTenNhan && !matchSdtNhan && !matchMaKh) return false;
            }
            if (maTrangThai != null && !maTrangThai.isBlank()) {
                if (dh.getTrangThai() == null || !maTrangThai.equalsIgnoreCase(dh.getTrangThai().getMaTrangThai())) return false;
            }
            if (maKho != null && !maKho.isBlank()) {
                if (!maKho.equalsIgnoreCase(dh.getMaKhoGui()) && !maKho.equalsIgnoreCase(dh.getMaKhoNhan())) return false;
            }
            return true;
        }).toList();

        model.addAttribute("danhSachDonHang", filteredOrders);
        model.addAttribute("danhSachTrangThai", trangThaiDHRepository.findAll());
        model.addAttribute("danhSachKho", khoRepository.findAll());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedTrangThai", maTrangThai);
        model.addAttribute("selectedKho", maKho);
        model.addAttribute("totalFiltered", filteredOrders.size());
        model.addAttribute("totalAll", allOrders.size());

        return "admin/27-orders";
    }

    @GetMapping("/api/admin/orders/{maDh}")
    @ResponseBody
    public ResponseEntity<?> getAdminOrderDetailModal(@PathVariable String maDh) {
        log.info("Admin lấy chi tiết đơn hàng cho modal: {}", maDh);
        DonHang dh = donHangRepository.findById(maDh).orElse(null);
        if (dh == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng " + maDh));
        }

        List<KienHang> kiens = kienHangRepository.findByDonHang_MaDh(maDh);
        List<LichSuTrangThai> lichSus = lichSuTrangThaiRepository.findByDonHang_MaDhOrderByThoiGianAsc(maDh);
        List<ThanhToan> thanhToans = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(maDh);

        Kho khoGui = khoRepository.findById(dh.getMaKhoGui()).orElse(null);
        Kho khoNhan = khoRepository.findById(dh.getMaKhoNhan()).orElse(null);
        KhachHang khGui = khachHangRepository.findById(dh.getMaKhGui()).orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("maDh", dh.getMaDh());
        data.put("ngayTao", dh.getNgayTao() != null ? dh.getNgayTao().toString() : "");
        data.put("maKhGui", dh.getMaKhGui());
        data.put("tenKhGui", khGui != null ? khGui.getHoTen() : dh.getMaKhGui());
        data.put("sdtKhGui", khGui != null ? khGui.getSdt() : "");
        data.put("tenNguoiNhan", dh.getTenNguoiNhan());
        data.put("sdtNhan", dh.getSdtNhan());
        data.put("diaChiLay", dh.getDiaChiLay());
        data.put("diaChiGiao", dh.getDiaChiGiao());
        data.put("maKhoGui", dh.getMaKhoGui());
        data.put("tenKhoGui", khoGui != null ? khoGui.getTenKho() : dh.getMaKhoGui());
        data.put("maKhoNhan", dh.getMaKhoNhan());
        data.put("tenKhoNhan", khoNhan != null ? khoNhan.getTenKho() : dh.getMaKhoNhan());
        data.put("maTrangThai", dh.getTrangThai() != null ? dh.getTrangThai().getMaTrangThai() : "TT01");
        data.put("tenTrangThai", dh.getTrangThai() != null ? dh.getTrangThai().getTenTrangThai() : "Mới tạo");
        data.put("phiVanChuyen", dh.getPhiVanChuyen() != null ? dh.getPhiVanChuyen() : BigDecimal.ZERO);
        data.put("cod", dh.getCod() != null ? dh.getCod() : BigDecimal.ZERO);
        data.put("trangThaiLay", dh.getTrangThaiLay());

        List<Map<String, Object>> kienList = kiens.stream().map(k -> {
            Map<String, Object> km = new HashMap<>();
            km.put("maKien", k.getMaKien());
            km.put("khoiLuong", k.getKhoiLuong());
            km.put("dai", k.getDai());
            km.put("rong", k.getRong());
            km.put("cao", k.getCao());
            km.put("loaiHang", k.getLoaiHang());
            km.put("tenKhoHienTai", k.getKhoHienTai() != null ? k.getKhoHienTai().getTenKho() : "Đang luân chuyển");
            return km;
        }).toList();
        data.put("kienHangs", kienList);

        List<Map<String, Object>> lsList = lichSus.stream().map(ls -> {
            Map<String, Object> lm = new HashMap<>();
            lm.put("maTrangThai", ls.getTrangThai() != null ? ls.getTrangThai().getMaTrangThai() : "");
            lm.put("tenTrangThai", ls.getTrangThai() != null ? ls.getTrangThai().getTenTrangThai() : "");
            lm.put("thoiGian", ls.getThoiGian() != null ? ls.getThoiGian().toString() : "");
            lm.put("ghiChu", ls.getGhiChu());
            lm.put("maNd", ls.getMaNd());
            return lm;
        }).toList();
        data.put("lichSu", lsList);

        List<Map<String, Object>> payList = thanhToans.stream().map(tt -> {
            Map<String, Object> pm = new HashMap<>();
            pm.put("maTtToan", tt.getMaTtToan());
            pm.put("loaiKhoan", tt.getLoaiKhoan());
            pm.put("soTien", tt.getSoTien());
            pm.put("phuongThuc", tt.getPhuongThuc());
            pm.put("trangThai", tt.getTrangThai());
            pm.put("thoiGian", tt.getThoiGian() != null ? tt.getThoiGian().toString() : "");
            return pm;
        }).toList();
        data.put("thanhToans", payList);

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping({"/admin/users", "/admin/19-users.html"})
    public String adminUsers(Model model) {
        model.addAttribute("danhSachKho", khoRepository.findAll());
        return "admin/19-users";
    }

    @GetMapping({"/admin/warehouses", "/admin/24-warehouses.html"})
    public String adminWarehouses(Model model) {
        log.info("Tải danh sách Kho/Bưu cục Admin từ cơ sở dữ liệu");
        List<Kho> danhSachKho = khoRepository.findAll();
        model.addAttribute("danhSachKho", danhSachKho);
        return "admin/24-warehouses";
    }

    @PostMapping("/admin/warehouses/save")
    public String saveWarehouse(
            @RequestParam(required = false) String maKho,
            @RequestParam String tenKho,
            @RequestParam String diaChi,
            @RequestParam(required = false) String sdt,
            RedirectAttributes redirectAttributes) {
        try {
            Kho kho;
            if (maKho != null && !maKho.isBlank() && khoRepository.existsById(maKho.trim())) {
                kho = khoRepository.findById(maKho.trim()).get();
                kho.setTenKho(tenKho.trim());
                kho.setDiaChi(diaChi.trim());
                kho.setSdt(sdt != null ? sdt.trim() : null);
                khoRepository.save(kho);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bưu cục " + maKho + " thành công!");
            } else {
                String nextCode = (maKho != null && !maKho.isBlank()) ? maKho.trim() : null;
                if (nextCode == null) {
                    long count = khoRepository.count() + 1;
                    nextCode = String.format("KHO%02d", count);
                    while (khoRepository.existsById(nextCode)) {
                        count++;
                        nextCode = String.format("KHO%02d", count);
                    }
                }
                kho = Kho.builder()
                        .maKho(nextCode)
                        .tenKho(tenKho.trim())
                        .diaChi(diaChi.trim())
                        .sdt(sdt != null ? sdt.trim() : null)
                        .build();
                khoRepository.save(kho);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm mới bưu cục " + nextCode + " thành công!");
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu kho/bưu cục: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu kho/bưu cục: " + e.getMessage());
        }
        return "redirect:/admin/warehouses";
    }

    @GetMapping({"/admin/vehicles", "/admin/25-vehicles.html"})
    public String adminVehicles(Model model) {
        log.info("Tải danh sách Phương tiện vận chuyển Admin từ cơ sở dữ liệu");
        List<PhuongTien> danhSachPt = phuongTienRepository.findAll();
        model.addAttribute("danhSachPhuongTien", danhSachPt);
        return "admin/25-vehicles";
    }

    @PostMapping("/admin/vehicles/save")
    public String saveVehicle(
            @RequestParam(required = false) String maPt,
            @RequestParam String bienSo,
            @RequestParam String loaiXe,
            @RequestParam BigDecimal taiTrong,
            @RequestParam String trangThai,
            RedirectAttributes redirectAttributes) {
        try {
            PhuongTien pt;
            if (maPt != null && !maPt.isBlank() && phuongTienRepository.existsById(maPt.trim())) {
                pt = phuongTienRepository.findById(maPt.trim()).get();
                pt.setBienSo(bienSo.trim());
                pt.setLoaiXe(loaiXe.trim());
                pt.setTaiTrong(taiTrong);
                pt.setTrangThai(trangThai.trim());
                phuongTienRepository.save(pt);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phương tiện " + maPt + " thành công!");
            } else {
                String nextCode = (maPt != null && !maPt.isBlank()) ? maPt.trim() : null;
                if (nextCode == null) {
                    long count = phuongTienRepository.count() + 1;
                    nextCode = String.format("PT%03d", count);
                    while (phuongTienRepository.existsById(nextCode)) {
                        count++;
                        nextCode = String.format("PT%03d", count);
                    }
                }
                pt = new PhuongTien(nextCode, bienSo.trim(), loaiXe.trim(), taiTrong, trangThai.trim());
                phuongTienRepository.save(pt);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm mới phương tiện " + nextCode + " thành công!");
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu phương tiện: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu phương tiện: " + e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @GetMapping({"/admin/settings", "/admin/30-settings.html"})
    public String adminSettings(Model model) {
        model.addAttribute("tongKho", khoRepository.count());
        model.addAttribute("tongDonHang", donHangRepository.count());
        model.addAttribute("tongNguoiDung", nguoiDungRepository.count());
        return "admin/30-settings";
    }

    // =========================================================================
    // PHÂN HỆ KHÁCH HÀNG: CHI TIẾT ĐƠN HÀNG (UC07) & THANH TOÁN (UC08)
    // =========================================================================
    @GetMapping({"/customer/order-detail", "/customer/07-order-detail.html"})
    public String customerOrderDetail(@RequestParam(required = false) String maDh, Model model) {
        KhachHang khachHang = getCurrentKhachHang();
        if (khachHang == null) {
            khachHang = khachHangRepository.findById("KH001").orElse(null);
        }
        model.addAttribute("khachHang", khachHang);

        List<DonHang> danhSachDonHang = List.of();
        if (khachHang != null) {
            danhSachDonHang = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc(khachHang.getMaKh());
        }
        model.addAttribute("danhSachDonHang", danhSachDonHang);

        DonHang selectedOrder = null;
        if (maDh != null && !maDh.isBlank()) {
            selectedOrder = donHangRepository.findById(maDh).orElse(null);
        }
        if (selectedOrder == null && !danhSachDonHang.isEmpty()) {
            selectedOrder = danhSachDonHang.get(0);
        }

        PaymentSummaryResponse paymentSummary = null;
        if (selectedOrder != null) {
            model.addAttribute("donHang", selectedOrder);
            model.addAttribute("maDh", selectedOrder.getMaDh());

            List<KienHang> kienHangs = kienHangRepository.findByDonHang_MaDh(selectedOrder.getMaDh());
            model.addAttribute("kienHangs", kienHangs);

            List<LichSuTrangThai> lichSu = lichSuTrangThaiRepository.findByDonHang_MaDhOrderByThoiGianAsc(selectedOrder.getMaDh());
            model.addAttribute("lichSu", lichSu);

            List<ThanhToan> thanhToans = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(selectedOrder.getMaDh());
            model.addAttribute("thanhToans", thanhToans);

            try {
                paymentSummary = paymentService.getOrderPaymentSummary(selectedOrder.getMaDh());
            } catch (Exception e) {
                log.warn("Không thể lấy payment summary cho đơn hàng {}: {}", selectedOrder.getMaDh(), e.getMessage());
            }
        }
        model.addAttribute("paymentSummary", paymentSummary);

        return "customer/07-order-detail";
    }

    @GetMapping({"/customer/payment", "/customer/08-payment.html"})
    public String customerPayment(@RequestParam(required = false) String maDh,
                                  @RequestParam(value = "paid", required = false) Boolean paid,
                                  Model model) {
        KhachHang khachHang = getCurrentKhachHang();
        if (khachHang == null) {
            khachHang = khachHangRepository.findById("KH001").orElse(null);
        }
        model.addAttribute("khachHang", khachHang);

        List<DonHang> danhSachDonHang = List.of();
        if (khachHang != null) {
            danhSachDonHang = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc(khachHang.getMaKh());
        }
        model.addAttribute("danhSachDonHang", danhSachDonHang);

        DonHang selectedOrder = null;
        if (maDh != null && !maDh.isBlank()) {
            selectedOrder = donHangRepository.findById(maDh).orElse(null);
        }
        if (selectedOrder == null && !danhSachDonHang.isEmpty()) {
            selectedOrder = danhSachDonHang.get(0);
        }

        PaymentSummaryResponse paymentSummary = null;
        if (selectedOrder != null) {
            model.addAttribute("donHang", selectedOrder);
            model.addAttribute("maDh", selectedOrder.getMaDh());
            List<ThanhToan> thanhToans = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(selectedOrder.getMaDh());
            model.addAttribute("thanhToans", thanhToans);

            try {
                paymentSummary = paymentService.getOrderPaymentSummary(selectedOrder.getMaDh());
            } catch (Exception e) {
                log.warn("Không thể lấy payment summary cho đơn hàng {}: {}", selectedOrder.getMaDh(), e.getMessage());
            }
        }
        model.addAttribute("paymentSummary", paymentSummary);
        model.addAttribute("paid", paid);

        return "customer/08-payment";
    }

    @PostMapping("/customer/process-payment")
    public String handleCustomerProcessPayment(
            @RequestParam("maDh") String maDh,
            @RequestParam(value = "phuongThuc", defaultValue = "Chuyển khoản") String phuongThuc,
            @RequestParam(value = "soTien", required = false) BigDecimal soTien,
            RedirectAttributes redirectAttributes) {
        try {
            DonHang donHang = donHangRepository.findById(maDh)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy đơn hàng: " + maDh));

            BigDecimal amount = (donHang.getPhiVanChuyen() != null && donHang.getPhiVanChuyen().compareTo(BigDecimal.ZERO) > 0)
                    ? donHang.getPhiVanChuyen()
                    : ((soTien != null && soTien.compareTo(BigDecimal.ZERO) > 0) ? soTien : BigDecimal.ZERO);

            ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                    .maDH(maDh)
                    .loaiKhoan("PHI_VC")
                    .soTien(amount)
                    .phuongThuc(phuongThuc)
                    .nguoiThanhToan(donHang.getTenNguoiNhan())
                    .build();

            paymentService.processPayment(req);
            redirectAttributes.addAttribute("maDh", maDh);
            redirectAttributes.addAttribute("paid", true);
            return "redirect:/customer/payment";
        } catch (Exception e) {
            log.error("Lỗi khi khách hàng thanh toán đơn hàng {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addAttribute("maDh", maDh);
            return "redirect:/customer/payment";
        }
    }

    // =========================================================================
    // 1. PHÂN HỆ KHÁCH HÀNG: TẠO ĐƠN HÀNG (UC04)
    // =========================================================================
    @GetMapping({"/customer/create-order", "/customer/06-create-order.html"})
    public String customerCreateOrder(Model model,
                                      @RequestParam(value = "success", required = false) Boolean success,
                                      @RequestParam(value = "maDh", required = false) String createdMaDh) {
        log.info("Hiển thị trang Tạo đơn hàng của khách hàng kết nối cơ sở dữ liệu");
        
        List<Kho> danhSachKho = khoRepository.findAll();
        KhachHang khachHang = getCurrentKhachHang();
        if (khachHang == null) {
            khachHang = khachHangRepository.findById("KH001").orElse(null);
        }
        String maKh = khachHang != null ? khachHang.getMaKh() : "KH001";
        List<DonHang> danhSachDonHang = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc(maKh);

        model.addAttribute("danhSachKho", danhSachKho);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("success", success);
        model.addAttribute("createdMaDh", createdMaDh);

        return "customer/06-create-order";
    }

    @PostMapping("/customer/create-order")
    public String handleCustomerCreateOrder(
            @RequestParam("tenNguoiNhan") String tenNguoiNhan,
            @RequestParam("sdtNhan") String sdtNhan,
            @RequestParam("diaChiLay") String diaChiLay,
            @RequestParam("diaChiGiao") String diaChiGiao,
            @RequestParam("maKhoGui") String maKhoGui,
            @RequestParam("maKhoNhan") String maKhoNhan,
            @RequestParam(value = "khoiLuong", defaultValue = "1.0") BigDecimal khoiLuong,
            @RequestParam(value = "loaiHang", defaultValue = "Hàng tiêu chuẩn") String loaiHang,
            @RequestParam(value = "cod", defaultValue = "0") BigDecimal cod,
            @RequestParam(value = "phuongThucThanhToan", defaultValue = "CHUYEN_KHOAN") String phuongThucThanhToan,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {

        log.info("Xử lý tạo đơn hàng mới từ biểu mẫu web: gửi đến {}", tenNguoiNhan);
        try {
            TaoDonHangRequest req = new TaoDonHangRequest();
            req.setTenNguoiNhan(tenNguoiNhan);
            req.setSdtNhan(sdtNhan);
            req.setDiaChiLay(diaChiLay);
            req.setDiaChiGiao(diaChiGiao);
            req.setMaKhoGui(maKhoGui);
            req.setMaKhoNhan(maKhoNhan);
            req.setCod(cod);
            req.setPhuongThucThanhToan(phuongThucThanhToan);

            TaoDonHangRequest.KienHangRequest kien = new TaoDonHangRequest.KienHangRequest();
            kien.setKhoiLuong(khoiLuong);
            kien.setLoaiHang(loaiHang);
            kien.setDai(new BigDecimal("20.00"));
            kien.setRong(new BigDecimal("15.00"));
            kien.setCao(new BigDecimal("10.00"));
            req.setDanhSachKien(List.of(kien));

            KhachHang khachHang = getCurrentKhachHang();
            String sdtOrUsername = (khachHang != null && khachHang.getSdt() != null) ? khachHang.getSdt() : "0901000001";

            TaoDonHangResponse res = donHangService.taoDonHang(sdtOrUsername, req);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("maDh", res.getMaDh());
            return "redirect:/customer/create-order";
        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn hàng từ form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/create-order";
        }
    }

    private NhanVien getCurrentNhanVien() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            var nvOpt = nhanVienRepository.findByNguoiDung_MaNd(username)
                    .or(() -> nguoiDungRepository.findByTenDangNhap(username)
                            .flatMap(nd -> nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd())))
                    .or(() -> nhanVienRepository.findBySdt(username))
                    .or(() -> nhanVienRepository.findById(username));
            if (nvOpt.isPresent()) {
                return nvOpt.get();
            }
        }
        return nhanVienRepository.findById("NV001").orElse(null);
    }

    // =========================================================================
    // 2. PHÂN HỆ BƯU CỤC: BẢNG ĐIỀU KHIỂN & TIẾP NHẬN ĐƠN (UC08)
    // =========================================================================
    @GetMapping({"/post-office/dashboard", "/post-office/09-dashboard.html"})
    public String postOfficeDashboard(
            @RequestParam(value = "maKho", required = false) String maKho,
            @RequestParam(value = "receivedMaDh", required = false) String receivedMaDh,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        NhanVien currentNv = getCurrentNhanVien();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        String resolvedMaKho;
        if (!isAdmin && currentNv != null && currentNv.getMaKho() != null) {
            resolvedMaKho = currentNv.getMaKho(); // Nhân viên bưu cục chỉ được quản lý và tiếp nhận đơn đúng kho của mình
        } else if (maKho != null && !maKho.isBlank()) {
            resolvedMaKho = maKho;
        } else if (currentNv != null && currentNv.getMaKho() != null) {
            resolvedMaKho = currentNv.getMaKho();
        } else {
            resolvedMaKho = "KHO01";
        }

        log.info("Tải Dashboard Bưu cục từ cơ sở dữ liệu cho nhân viên {} tại kho {}", currentNv != null ? currentNv.getMaNv() : "N/A", resolvedMaKho);

        List<Kho> danhSachKho = khoRepository.findAll();
        Kho currentKho = khoRepository.findById(resolvedMaKho).orElse(danhSachKho.isEmpty() ? null : danhSachKho.get(0));

        Page<PendingOrderResponse> pagedPending = postOfficeService.getPendingOrdersPaged(resolvedMaKho, Math.max(0, page), Math.max(1, size));
        List<DonHang> allOrders = isAdmin 
                ? donHangRepository.findAllByOrderByNgayTaoDesc() 
                : donHangRepository.findByMaKhoGuiOrderByNgayTaoDesc(resolvedMaKho);

        long countPending = donHangRepository.countByMaKhoGuiAndTrangThai_MaTrangThai(resolvedMaKho, "TT01");
        long countReceived = donHangRepository.countByMaKhoGuiAndTrangThai_MaTrangThai(resolvedMaKho, "TT02");
        long countInTransit = donHangRepository.countByTrangThai_MaTrangThaiIn(List.of("TT03", "TT04", "TT05"));
        long countCompleted = donHangRepository.countByMaKhoGuiAndTrangThai_MaTrangThai(resolvedMaKho, "TT06");

        model.addAttribute("nhanVien", currentNv);
        model.addAttribute("danhSachKho", danhSachKho);
        model.addAttribute("currentKho", currentKho);
        model.addAttribute("selectedMaKho", resolvedMaKho);
        model.addAttribute("pendingOrders", pagedPending.getContent());
        model.addAttribute("currentPage", pagedPending.getNumber());
        model.addAttribute("pageSize", pagedPending.getSize());
        model.addAttribute("totalPages", pagedPending.getTotalPages());
        model.addAttribute("totalElements", pagedPending.getTotalElements());
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("countPending", countPending);
        model.addAttribute("countReceived", countReceived);
        model.addAttribute("countInTransit", countInTransit);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("receivedMaDh", receivedMaDh);

        return "post-office/dashboard";
    }

    @PostMapping("/post-office/receive-order")
    public String handleReceiveOrder(
            @RequestParam("maDh") String maDh,
            @RequestParam(value = "maKho", required = false) String maKho,
            @RequestParam(value = "khoiLuong", defaultValue = "1.5") BigDecimal khoiLuong,
            @RequestParam(value = "dai", defaultValue = "20.0") BigDecimal dai,
            @RequestParam(value = "rong", defaultValue = "15.0") BigDecimal rong,
            @RequestParam(value = "cao", defaultValue = "10.0") BigDecimal cao,
            @RequestParam(value = "loaiHang", defaultValue = "Hàng tiêu chuẩn") String loaiHang,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        NhanVien currentNv = getCurrentNhanVien();
        String currentMaNv = currentNv != null ? currentNv.getMaNv() : "NV001";
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        String finalMaKho = (!isAdmin && currentNv != null && currentNv.getMaKho() != null)
                ? currentNv.getMaKho()
                : ((maKho != null && !maKho.isBlank()) ? maKho : (currentNv != null && currentNv.getMaKho() != null ? currentNv.getMaKho() : "KHO01"));

        try {
            DonHang dh = donHangRepository.findById(maDh).orElse(null);
            if (dh == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng " + maDh);
                redirectAttributes.addAttribute("maKho", finalMaKho);
                return "redirect:/post-office/dashboard";
            }

            if (!isAdmin && currentNv != null && currentNv.getMaKho() != null && !currentNv.getMaKho().equals(dh.getMaKhoGui())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Từ chối tiếp nhận: Bạn là nhân viên thuộc bưu cục " + currentNv.getMaKho() +
                        ", chỉ có nhân viên tại bưu cục gửi " + dh.getMaKhoGui() + " mới được tiếp nhận đơn này!");
                redirectAttributes.addAttribute("maKho", finalMaKho);
                return "redirect:/post-office/dashboard";
            }

            ReceiveOrderRequest req = ReceiveOrderRequest.builder()
                    .maKho(finalMaKho)
                    .maNV(currentMaNv)
                    .khoiLuong(khoiLuong)
                    .dai(dai)
                    .rong(rong)
                    .cao(cao)
                    .loaiHang(loaiHang)
                    .ghiChu(ghiChu)
                    .build();

            postOfficeService.receiveOrder(maDh, req);
            redirectAttributes.addAttribute("maKho", finalMaKho);
            redirectAttributes.addAttribute("receivedMaDh", maDh);
            return "redirect:/post-office/dashboard";
        } catch (Exception e) {
            log.error("Lỗi khi tiếp nhận đơn hàng {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addAttribute("maKho", finalMaKho);
            return "redirect:/post-office/dashboard";
        }
    }

    // =========================================================================
    // 3. PHÂN HỆ BƯU CỤC: QUẢN LÝ KIỆN HÀNG & CHUYỂN KHO (UC09)
    // =========================================================================
    @GetMapping({"/post-office/packages", "/post-office/10-packages.html"})
    public String postOfficePackages(
            @RequestParam(value = "maKho", required = false) String maKhoParam,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "transferredMaKien", required = false) String transferredMaKien,
            Model model) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        NhanVien currentNv = getCurrentNhanVien();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ADMIN".equals(a.getAuthority()));

        String resolvedMaKho;
        if (!isAdmin && currentNv != null && currentNv.getMaKho() != null) {
            resolvedMaKho = currentNv.getMaKho();
        } else if (maKhoParam != null && !maKhoParam.isBlank()) {
            resolvedMaKho = maKhoParam;
        } else if (currentNv != null && currentNv.getMaKho() != null) {
            resolvedMaKho = currentNv.getMaKho();
        } else {
            resolvedMaKho = "KHO01";
        }

        log.info("Tải danh sách Kiện hàng từ cơ sở dữ liệu cho kho {}", resolvedMaKho);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<KienHang> pagedKien;
        if (isAdmin && (maKhoParam == null || maKhoParam.isBlank())) {
            pagedKien = kienHangRepository.findAllWithDonHangAndKho(pageable);
        } else {
            pagedKien = kienHangRepository.findByKhoHienTaiWithDonHangAndKho(resolvedMaKho, pageable);
        }

        List<Kho> danhSachKho = khoRepository.findAll();
        Kho currentKho = khoRepository.findById(resolvedMaKho).orElse(danhSachKho.isEmpty() ? null : danhSachKho.get(0));

        model.addAttribute("nhanVien", currentNv);
        model.addAttribute("currentKho", currentKho);
        model.addAttribute("selectedMaKho", resolvedMaKho);
        model.addAttribute("danhSachKien", pagedKien.getContent());
        model.addAttribute("currentPage", pagedKien.getNumber());
        model.addAttribute("pageSize", pagedKien.getSize());
        model.addAttribute("totalPages", pagedKien.getTotalPages());
        model.addAttribute("totalElements", pagedKien.getTotalElements());
        model.addAttribute("danhSachKho", danhSachKho);
        model.addAttribute("transferredMaKien", transferredMaKien);

        return "post-office/packages";
    }

    @PostMapping("/post-office/transfer-package")
    public String handleTransferPackage(
            @RequestParam("maKien") String maKien,
            @RequestParam("maKhoMoi") String maKhoMoi,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {

        try {
            NhanVien currentNv = getCurrentNhanVien();
            String maNd = (currentNv != null && currentNv.getNguoiDung() != null) ? currentNv.getNguoiDung().getMaNd() : "ND002";
            TransferWarehouseRequest req = TransferWarehouseRequest.builder()
                    .maKhoMoi(maKhoMoi)
                    .maND(maNd)
                    .ghiChu(ghiChu)
                    .build();

            postOfficeService.transferWarehouse(maKien, req);
            redirectAttributes.addAttribute("transferredMaKien", maKien);
            return "redirect:/post-office/packages";
        } catch (Exception e) {
            log.error("Lỗi khi chuyển kho kiện {}: {}", maKien, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post-office/packages";
        }
    }

    // =========================================================================
    // 4. PHÂN HỆ BƯU CỤC: CẬP NHẬT TRẠNG THÁI (UC10)
    // =========================================================================
    @GetMapping({"/post-office/update-status", "/post-office/11-update-status.html"})
    public String postOfficeUpdateStatus(
            @RequestParam(value = "updatedMaDh", required = false) String updatedMaDh,
            Model model) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        NhanVien currentNv = getCurrentNhanVien();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        String resolvedMaKho = (!isAdmin && currentNv != null && currentNv.getMaKho() != null) ? currentNv.getMaKho() : "KHO01";

        log.info("Tải trang Cập nhật trạng thái cho kho {}", resolvedMaKho);

        List<DonHang> danhSachDonHang;
        if (isAdmin) {
            danhSachDonHang = donHangRepository.findAllByOrderByNgayTaoDesc();
        } else {
            danhSachDonHang = donHangRepository.findByMaKhoGuiOrderByNgayTaoDesc(resolvedMaKho);
        }

        List<TrangThaiDH> danhSachTrangThai = trangThaiDHRepository.findAll();
        List<Kho> danhSachKho = khoRepository.findAll();
        Kho currentKho = khoRepository.findById(resolvedMaKho).orElse(danhSachKho.isEmpty() ? null : danhSachKho.get(0));

        model.addAttribute("nhanVien", currentNv);
        model.addAttribute("currentKho", currentKho);
        model.addAttribute("selectedMaKho", resolvedMaKho);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("danhSachTrangThai", danhSachTrangThai);
        model.addAttribute("updatedMaDh", updatedMaDh);

        return "post-office/update-status";
    }

    @PostMapping("/post-office/update-status")
    public String handleUpdateStatus(
            @RequestParam("maDh") String maDh,
            @RequestParam("maTrangThaiMoi") String maTrangThaiMoi,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {

        try {
            NhanVien currentNv = getCurrentNhanVien();
            String maNd = (currentNv != null && currentNv.getNguoiDung() != null) ? currentNv.getNguoiDung().getMaNd() : "ND002";
            UpdateOrderStatusRequest req = UpdateOrderStatusRequest.builder()
                    .maTrangThaiMoi(maTrangThaiMoi)
                    .maND(maNd)
                    .ghiChu(ghiChu)
                    .build();

            postOfficeService.updateOrderStatus(maDh, req);
            redirectAttributes.addAttribute("updatedMaDh", maDh);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái đơn {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/post-office/update-status";
    }

    // =========================================================================
    // 5. PHÂN HỆ ADMIN: QUẢN LÝ THANH TOÁN & ĐỐI SOÁT COD (UC06, UC07, 4.2.22)
    // =========================================================================
    @GetMapping({"/admin/payments", "/admin/28-payments.html"})
    public String adminPayments(
            @RequestParam(value = "processedMaDh", required = false) String processedMaDh,
            Model model) {
        log.info("Tải thông tin Tài chính và Đối soát COD từ cơ sở dữ liệu");

        CodReconciliationResponse codReconciliation = paymentService.getCodReconciliation();
        List<ThanhToan> danhSachThanhToan = thanhToanRepository.findAllWithDonHang();
        List<DonHang> danhSachDonHang = donHangRepository.findAllByOrderByNgayTaoDesc();

        model.addAttribute("codReconciliation", codReconciliation);
        model.addAttribute("danhSachThanhToan", danhSachThanhToan);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("processedMaDh", processedMaDh);

        return "admin/28-payments";
    }

    @PostMapping("/admin/process-payment")
    public String handleProcessPayment(
            @RequestParam("maDh") String maDh,
            @RequestParam("soTien") BigDecimal soTien,
            @RequestParam("loaiKhoan") String loaiKhoan,
            @RequestParam("phuongThuc") String phuongThuc,
            @RequestParam(value = "nguoiThanhToan", required = false) String nguoiThanhToan,
            RedirectAttributes redirectAttributes) {

        try {
            ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                    .maDH(maDh)
                    .soTien(soTien)
                    .loaiKhoan(loaiKhoan)
                    .phuongThuc(phuongThuc)
                    .nguoiThanhToan(nguoiThanhToan)
                    .build();

            paymentService.processPayment(req);
            redirectAttributes.addAttribute("processedMaDh", maDh);
            return "redirect:/admin/payments";
        } catch (Exception e) {
            log.error("Lỗi khi xử lý thanh toán đơn {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/payments";
        }
    }

    // =========================================================================
    // 6. PHÂN HỆ ADMIN: BÁO CÁO THỐNG KÊ & HIỆU SUẤT SLA (UC17, 4.2.23)
    // =========================================================================
    @GetMapping({"/admin/reports", "/admin/29-reports.html"})
    public String adminReports(Model model) {
        log.info("Tải Báo cáo thống kê quản trị từ cơ sở dữ liệu");

        AdminOverviewReportResponse overview = adminReportService.getOverviewReport();
        SlaPerformanceReportResponse slaReport = adminReportService.getSlaPerformanceReport();
        long tongKienLuuKho = kienHangRepository.countTongKienLuuKho();

        model.addAttribute("overview", overview);
        model.addAttribute("slaReport", slaReport);
        model.addAttribute("tongKienLuuKho", tongKienLuuKho);

        return "admin/29-reports";
    }

    // =========================================================================
    // 7. PHÂN HỆ ĐIỀU PHỐI (DISPATCHER)
    // =========================================================================
    @GetMapping({"/dispatcher/dashboard", "/dispatcher/12-dashboard.html"})
    public String dispatcherDashboard() {
        return "dispatcher/12-dashboard";
    }

    @GetMapping({"/dispatcher/routes", "/dispatcher/13-routes.html"})
    public String dispatcherRoutes() {
        return "dispatcher/13-routes";
    }

    @GetMapping({"/dispatcher/trips", "/dispatcher/14-trips.html"})
    public String dispatcherTrips() {
        return "dispatcher/14-trips";
    }

    // =========================================================================
    // 8. PHÂN HỆ TÀI XẾ (DRIVER)
    // =========================================================================
    @GetMapping({"/driver/dashboard", "/driver/15-dashboard.html"})
    public String driverDashboard() {
        return "driver/15-dashboard";
    }

    @GetMapping({"/driver/trip-detail", "/driver/16-trip-detail.html"})
    public String driverTripDetail() {
        return "driver/16-trip-detail";
    }

    @GetMapping({"/driver/delivery-update", "/driver/17-delivery-update.html"})
    public String driverDeliveryUpdate() {
        return "driver/17-delivery-update";
    }
}
