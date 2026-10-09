package com.example.backend.controller.view;

import com.example.backend.dto.payment.CodReconciliationResponse;
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
public class ViewController {

    private final DonHangRepository donHangRepository;
    private final KhoRepository khoRepository;
    private final TrangThaiDHRepository trangThaiDHRepository;
    private final KienHangRepository kienHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PostOfficeService postOfficeService;
    private final PaymentService paymentService;
    private final AdminReportService adminReportService;
    private final DonHangService donHangService;

    @GetMapping("/")
    public String index() {
        return "redirect:/customer/create-order";
    }

    // =========================================================================
    // 1. PHÂN HỆ KHÁCH HÀNG: TẠO ĐƠN HÀNG (UC04)
    // =========================================================================
    @GetMapping("/customer/create-order")
    public String customerCreateOrder(Model model,
                                      @RequestParam(value = "success", required = false) Boolean success,
                                      @RequestParam(value = "maDh", required = false) String createdMaDh) {
        log.info("Hiển thị trang Tạo đơn hàng của khách hàng kết nối cơ sở dữ liệu");
        
        List<Kho> danhSachKho = khoRepository.findAll();
        KhachHang khachHang = khachHangRepository.findById("KH001").orElse(null);
        List<DonHang> danhSachDonHang = donHangRepository.findByMaKhGuiOrderByNgayTaoDesc("KH001");

        model.addAttribute("danhSachKho", danhSachKho);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("success", success);
        model.addAttribute("createdMaDh", createdMaDh);

        return "customer/create-order";
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
            req .setTenNguoiNhan(tenNguoiNhan);
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

            TaoDonHangResponse res = donHangService.taoDonHang("0901000001", req);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("maDh", res.getMaDh());
            return "redirect:/customer/create-order";
        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn hàng từ form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/create-order";
        }
    }

    // =========================================================================
    // 2. PHÂN HỆ BƯU CỤC: TIẾP NHẬN ĐƠN (UC08)
    // =========================================================================
    @GetMapping("/post-office/dashboard")
    public String postOfficeDashboard(
            @RequestParam(value = "maKho", defaultValue = "KHO01") String maKho,
            @RequestParam(value = "receivedMaDh", required = false) String receivedMaDh,
            Model model) {
        log.info("Tải Dashboard Bưu cục từ cơ sở dữ liệu cho kho {}", maKho);

        List<Kho> danhSachKho = khoRepository.findAll();
        Kho currentKho = khoRepository.findById(maKho).orElse(danhSachKho.isEmpty() ? null : danhSachKho.get(0));

        List<PendingOrderResponse> pendingOrders = postOfficeService.getPendingOrders(maKho);
        List<DonHang> allOrders = donHangRepository.findAllByOrderByNgayTaoDesc();

        long countPending = donHangRepository.countByTrangThai_MaTrangThai("TT01");
        long countReceived = donHangRepository.countByTrangThai_MaTrangThai("TT02");
        long countInTransit = donHangRepository.countByTrangThai_MaTrangThaiIn(List.of("TT03", "TT04", "TT05"));
        long countCompleted = donHangRepository.countByTrangThai_MaTrangThai("TT06");

        model.addAttribute("danhSachKho", danhSachKho);
        model.addAttribute("currentKho", currentKho);
        model.addAttribute("selectedMaKho", maKho);
        model.addAttribute("pendingOrders", pendingOrders);
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
            @RequestParam("maKho") String maKho,
            @RequestParam(value = "khoiLuong", defaultValue = "1.5") BigDecimal khoiLuong,
            @RequestParam(value = "dai", defaultValue = "20.0") BigDecimal dai,
            @RequestParam(value = "rong", defaultValue = "15.0") BigDecimal rong,
            @RequestParam(value = "cao", defaultValue = "10.0") BigDecimal cao,
            @RequestParam(value = "loaiHang", defaultValue = "Hàng tiêu chuẩn") String loaiHang,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {

        try {
            ReceiveOrderRequest req = ReceiveOrderRequest.builder()
                    .maKho(maKho)
                    .maNV("NV001")
                    .khoiLuong(khoiLuong)
                    .dai(dai)
                    .rong(rong)
                    .cao(cao)
                    .loaiHang(loaiHang)
                    .ghiChu(ghiChu)
                    .build();

            postOfficeService.receiveOrder(maDh, req);
            redirectAttributes.addAttribute("maKho", maKho);
            redirectAttributes.addAttribute("receivedMaDh", maDh);
            return "redirect:/post-office/dashboard";
        } catch (Exception e) {
            log.error("Lỗi khi tiếp nhận đơn hàng {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addAttribute("maKho", maKho);
            return "redirect:/post-office/dashboard";
        }
    }

    // =========================================================================
    // 3. PHÂN HỆ BƯU CỤC: QUẢN LÝ KIỆN HÀNG & CHUYỂN KHO (UC09)
    // =========================================================================
    @GetMapping("/post-office/packages")
    public String postOfficePackages(
            @RequestParam(value = "transferredMaKien", required = false) String transferredMaKien,
            Model model) {
        log.info("Tải danh sách Kiện hàng từ cơ sở dữ liệu");

        List<KienHang> danhSachKien = kienHangRepository.findAllWithDonHangAndKho();
        List<Kho> danhSachKho = khoRepository.findAll();

        model.addAttribute("danhSachKien", danhSachKien);
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
            TransferWarehouseRequest req = TransferWarehouseRequest.builder()
                    .maKhoMoi(maKhoMoi)
                    .maND("ND002")
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
    @GetMapping("/post-office/update-status")
    public String postOfficeUpdateStatus(
            @RequestParam(value = "updatedMaDh", required = false) String updatedMaDh,
            Model model) {
        log.info("Tải trang Cập nhật trạng thái từ cơ sở dữ liệu");

        List<DonHang> danhSachDonHang = donHangRepository.findAllByOrderByNgayTaoDesc();
        List<TrangThaiDH> danhSachTrangThai = trangThaiDHRepository.findAll();

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
            UpdateOrderStatusRequest req = UpdateOrderStatusRequest.builder()
                    .maTrangThaiMoi(maTrangThaiMoi)
                    .maND("ND002")
                    .ghiChu(ghiChu)
                    .build();

            postOfficeService.updateOrderStatus(maDh, req);
            redirectAttributes.addAttribute("updatedMaDh", maDh);
            return "redirect:/post-office/update-status";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái đơn {}: {}", maDh, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post-office/update-status";
        }
    }

    // =========================================================================
    // 5. PHÂN HỆ ADMIN: QUẢN LÝ THANH TOÁN & ĐỐI SOÁT COD (UC06, UC07, 4.2.22)
    // =========================================================================
    @GetMapping("/admin/payments")
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

        return "admin/payments";
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
    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        log.info("Tải Báo cáo thống kê quản trị từ cơ sở dữ liệu");

        AdminOverviewReportResponse overview = adminReportService.getOverviewReport();
        SlaPerformanceReportResponse slaReport = adminReportService.getSlaPerformanceReport();
        long tongKienLuuKho = kienHangRepository.countTongKienLuuKho();

        model.addAttribute("overview", overview);
        model.addAttribute("slaReport", slaReport);
        model.addAttribute("tongKienLuuKho", tongKienLuuKho);

        return "admin/reports";
    }
}
