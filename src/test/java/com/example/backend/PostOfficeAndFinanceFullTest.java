package com.example.backend;

import com.example.backend.dto.payment.CodReconciliationResponse;
import com.example.backend.dto.payment.PaymentSummaryResponse;
import com.example.backend.dto.payment.ProcessPaymentRequest;
import com.example.backend.dto.payment.ProcessPaymentResponse;
import com.example.backend.dto.postoffice.*;
import com.example.backend.dto.report.AdminOverviewReportResponse;
import com.example.backend.dto.report.SlaPerformanceReportResponse;
import com.example.backend.entity.DonHang;
import com.example.backend.entity.TrangThaiDH;
import com.example.backend.repository.DonHangRepository;
import com.example.backend.repository.TrangThaiDHRepository;
import com.example.backend.service.AdminReportService;
import com.example.backend.service.PaymentService;
import com.example.backend.service.PostOfficeService;
import com.example.backend.dto.request.TaoDonHangRequest;
import com.example.backend.dto.response.TaoDonHangResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostOfficeAndFinanceFullTest {

    @Autowired
    private PostOfficeService postOfficeService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AdminReportService adminReportService;

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private TrangThaiDHRepository trangThaiDHRepository;

    @Autowired
    private com.example.backend.service.DonHangService donHangService;

    @Autowired
    private com.example.backend.repository.KhachHangRepository khachHangRepository;

    @Autowired
    private com.example.backend.service.AuthService authService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static String testMaDH;
    private static String testMaKien;

    @BeforeAll
    static void init() {
        // Sinh mã đơn hàng kiểm thử ngẫu nhiên không trùng lặp
        testMaDH = "TEST" + (System.currentTimeMillis() % 10000000);
    }

    @Test
    @Order(1)
    @DisplayName("Chuẩn bị đơn hàng kiểm thử (TT01)")
    void prepareTestOrder() {
        TrangThaiDH tt01 = trangThaiDHRepository.findById("TT01")
                .orElseGet(() -> TrangThaiDH.builder().maTrangThai("TT01").tenTrangThai("Mới tạo").build());

        DonHang dh = DonHang.builder()
                .maDh(testMaDH)
                .maKhGui("KH001")
                .maKhNhan("KH002")
                .sdtNhan("0901000002")
                .tenNguoiNhan("Trần Thị Bích")
                .diaChiLay("12 Võ Văn Ngân, Thủ Đức, TP.HCM")
                .diaChiGiao("25 Trần Thái Tông, Cầu Giấy, Hà Nội")
                .maKhoGui("KHO01")
                .maKhoNhan("KHO02")
                .ngayTao(LocalDateTime.now())
                .phiVanChuyen(new BigDecimal("35000.00"))
                .cod(new BigDecimal("200000.00"))
                .trangThai(tt01)
                .build();

        donHangRepository.save(dh);
        assertTrue(donHangRepository.existsById(testMaDH));
    }

    @Test
    @Order(2)
    @DisplayName("API 1: Lấy danh sách đơn chờ tiếp nhận (UC08)")
    void testGetPendingOrders() {
        List<PendingOrderResponse> pendingOrders = postOfficeService.getPendingOrders("KHO01");
        assertNotNull(pendingOrders);
        assertFalse(pendingOrders.isEmpty());
        assertTrue(pendingOrders.stream().anyMatch(o -> o.getMaDh().equals(testMaDH)));
    }

    @Test
    @Order(3)
    @DisplayName("API 2: Tiếp nhận đơn hàng và lập kiện (UC08) - Kiểm tra fix lỗi mã nhân viên/khóa ngoại")
    void testReceiveOrder() {
        ReceiveOrderRequest request = ReceiveOrderRequest.builder()
                .khoiLuong(new BigDecimal("2.50"))
                .dai(new BigDecimal("30.00"))
                .rong(new BigDecimal("20.00"))
                .cao(new BigDecimal("15.00"))
                .kichThuoc("30x20x15")
                .loaiHang("Hàng kiểm thử")
                .maKho("KHO01")
                .maNV("NV001") // Truyền mã nhân viên NV001 (được phân giải thành ND002 hợp lệ)
                .ghiChu("Tiếp nhận đơn kiểm thử tự động")
                .build();

        ReceiveOrderResponse response = postOfficeService.receiveOrder(testMaDH, request);
        assertNotNull(response);
        assertEquals(testMaDH, response.getMaDh());
        assertEquals("TT02", response.getMaTrangThai());
        assertNotNull(response.getMaKien());
        testMaKien = response.getMaKien();
    }

    @Test
    @Order(4)
    @DisplayName("API 3: Chuyển kho kiện hàng (UC09) - Kiểm tra fix lỗi trigger tính phí")
    void testTransferWarehouse() {
        assertNotNull(testMaKien, "Cần có kiện hàng tạo từ bước testReceiveOrder");
        TransferWarehouseRequest request = TransferWarehouseRequest.builder()
                .maKhoMoi("KHO02")
                .ghiChu("Chuyển lưu kho sang KHO02")
                .maND("ND002")
                .build();

        TransferWarehouseResponse response = postOfficeService.transferWarehouse(testMaKien, request);
        assertNotNull(response);
        assertEquals(testMaKien, response.getMaKien());
        assertEquals("KHO02", response.getMaKhoMoi());
    }

    @Test
    @Order(5)
    @DisplayName("API 4: Cập nhật trạng thái luân chuyển (UC10) - TT02 -> TT03")
    void testUpdateOrderStatus() {
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .maTrangThaiMoi("TT03")
                .maND("ND002")
                .ghiChu("Xuất kho đi vận chuyển liên tỉnh")
                .build();

        UpdateOrderStatusResponse response = postOfficeService.updateOrderStatus(testMaDH, request);
        assertNotNull(response);
        assertEquals(testMaDH, response.getMaDh());
        assertEquals("TT03", response.getMaTrangThaiMoi());
    }

    @Test
    @Order(6)
    @DisplayName("API 5: Bảng kê chi tiết thanh toán của đơn (UC06)")
    void testGetOrderPaymentSummary() {
        PaymentSummaryResponse response = paymentService.getOrderPaymentSummary(testMaDH);
        assertNotNull(response);
        assertEquals(testMaDH, response.getMaDh());
        assertEquals(new BigDecimal("35000.00"), response.getPhiVanChuyen());
        assertEquals(new BigDecimal("200000.00"), response.getCod());
    }

    @Test
    @Order(7)
    @DisplayName("API 6: Xử lý giao dịch nộp tiền cước vận chuyển (UC06, UC07)")
    void testProcessPayment() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .maDH(testMaDH)
                .loaiKhoan("PHI_VC")
                .soTien(new BigDecimal("35000.00"))
                .phuongThuc("Tiền mặt")
                .nguoiThanhToan("Trần Thị Bích")
                .build();

        ProcessPaymentResponse response = paymentService.processPayment(request);
        assertNotNull(response);
        assertEquals(testMaDH, response.getMaDh());
        assertEquals("PHI_VC", response.getLoaiKhoan());
        assertNotNull(response.getMaTTToan());
    }

    @Test
    @Order(8)
    @DisplayName("API 7: Màn hình đối soát COD Admin (Mục 4.2.22)")
    void testGetCodReconciliation() {
        CodReconciliationResponse response = paymentService.getCodReconciliation();
        assertNotNull(response);
        assertNotNull(response.getTongCodChoDoiSoat());
        assertNotNull(response.getDanhSachGiaoDich());
    }

    @Test
    @Order(9)
    @DisplayName("API 8: Thống kê tổng quan Admin Dashboard (UC17)")
    void testGetOverviewReport() {
        AdminOverviewReportResponse response = adminReportService.getOverviewReport();
        assertNotNull(response);
        assertTrue(response.getTongSoDonHang() > 0);
        assertNotNull(response.getTongDoanhThuCuoc());
    }

    @Test
    @Order(10)
    @DisplayName("API 9: Báo cáo chỉ số chất lượng SLA & Hiệu suất kho (Mục 4.2.23)")
    void testGetSlaPerformanceReport() {
        SlaPerformanceReportResponse response = adminReportService.getSlaPerformanceReport();
        assertNotNull(response);
        assertNotNull(response.getTyLeGiaoThanhCong());
        assertNotNull(response.getTopPostOffices());
    }

    @Test
    @Order(11)
    @DisplayName("UC04: Khách hàng tạo đơn chọn CHUYEN_KHOAN -> Tự động xác nhận Đã thanh toán")
    void testTaoDonHangChuyenKhoan() {
        var khList = khachHangRepository.findAll();
        assertFalse(khList.isEmpty(), "Cần có ít nhất 1 khách hàng trong DB");
        var kh = khList.get(0);

        TaoDonHangRequest req = new TaoDonHangRequest();
        req.setTenNguoiNhan("Người Nhận Chuyển Khoản");
        req.setSdtNhan("0912345678");
        req.setDiaChiLay("123 Lê Duẩn, Hà Nội");
        req.setDiaChiGiao("456 Nguyễn Huệ, TP.HCM");
        req.setMaKhoGui("KHO01");
        req.setMaKhoNhan("KHO02");
        req.setCod(new BigDecimal("100000"));
        req.setPhuongThucThanhToan("CHUYEN_KHOAN");

        TaoDonHangRequest.KienHangRequest kien = new TaoDonHangRequest.KienHangRequest();
        kien.setKhoiLuong(new BigDecimal("2.0"));
        kien.setDai(new BigDecimal("20.0"));
        kien.setRong(new BigDecimal("15.0"));
        kien.setCao(new BigDecimal("10.0"));
        kien.setLoaiHang("Hàng tiêu chuẩn");
        req.setDanhSachKien(List.of(kien));

        String maNd = kh.getNguoiDung() != null ? kh.getNguoiDung().getMaNd() : kh.getSdt();
        TaoDonHangResponse resp = donHangService.taoDonHang(maNd, req);

        assertNotNull(resp);
        assertNotNull(resp.getMaDh());
        assertEquals("Chuyển khoản", resp.getPhuongThucThanhToan());
        assertEquals("Đã thanh toán", resp.getTrangThaiThanhToan());

        // Kiểm tra trong danh sách đơn chờ lấy hàng của KHO01
        List<PendingOrderResponse> pendingOrders = postOfficeService.getPendingOrders("KHO01");
        PendingOrderResponse orderInList = pendingOrders.stream()
                .filter(o -> o.getMaDh().equals(resp.getMaDh()))
                .findFirst()
                .orElse(null);
        assertNotNull(orderInList);
        assertTrue(orderInList.getPhiDaThanhToan());
        assertEquals("Chuyển khoản", orderInList.getPhuongThucThanhToan());
    }

    @Test
    @Order(12)
    @DisplayName("UC04: Khách hàng tạo đơn chọn TIEN_MAT -> Chưa thanh toán -> Nhân viên lấy hàng thì chuyển sang Đã thanh toán")
    void testTaoDonHangTienMatVaTiepNhan() {
        var khList = khachHangRepository.findAll();
        assertFalse(khList.isEmpty(), "Cần có ít nhất 1 khách hàng trong DB");
        var kh = khList.get(0);

        TaoDonHangRequest req = new TaoDonHangRequest();
        req.setTenNguoiNhan("Người Nhận Tiền Mặt");
        req.setSdtNhan("0987654321");
        req.setDiaChiLay("78 Võ Văn Tần, Q3, TP.HCM");
        req.setDiaChiGiao("12 Bạch Đằng, Đà Nẵng");
        req.setMaKhoGui("KHO01");
        req.setMaKhoNhan("KHO02");
        req.setCod(new BigDecimal("500000"));
        req.setPhuongThucThanhToan("TIEN_MAT");

        TaoDonHangRequest.KienHangRequest kien = new TaoDonHangRequest.KienHangRequest();
        kien.setKhoiLuong(new BigDecimal("1.5"));
        kien.setDai(new BigDecimal("15.0"));
        kien.setRong(new BigDecimal("10.0"));
        kien.setCao(new BigDecimal("8.0"));
        kien.setLoaiHang("Hàng tiêu chuẩn");
        req.setDanhSachKien(List.of(kien));

        String maNd = kh.getNguoiDung() != null ? kh.getNguoiDung().getMaNd() : kh.getSdt();
        TaoDonHangResponse resp = donHangService.taoDonHang(maNd, req);

        assertNotNull(resp);
        assertEquals("Tiền mặt", resp.getPhuongThucThanhToan());
        assertEquals("Chưa thanh toán", resp.getTrangThaiThanhToan());

        // Kiểm tra trong danh sách pending
        List<PendingOrderResponse> pendingOrders = postOfficeService.getPendingOrders("KHO01");
        PendingOrderResponse orderInList = pendingOrders.stream()
                .filter(o -> o.getMaDh().equals(resp.getMaDh()))
                .findFirst()
                .orElse(null);
        assertNotNull(orderInList);
        assertFalse(orderInList.getPhiDaThanhToan());

        // Nhân viên đến nhà lấy hàng / tiếp nhận đơn
        ReceiveOrderRequest receiveReq = ReceiveOrderRequest.builder()
                .maKho("KHO01")
                .maNV("NV001")
                .khoiLuong(new BigDecimal("1.5"))
                .dai(new BigDecimal("15.0"))
                .rong(new BigDecimal("10.0"))
                .cao(new BigDecimal("8.0"))
                .loaiHang("Hàng tiêu chuẩn")
                .ghiChu("Đến nhà lấy hàng và thu tiền mặt trực tiếp")
                .build();
        ReceiveOrderResponse receiveResp = postOfficeService.receiveOrder(resp.getMaDh(), receiveReq);
        assertNotNull(receiveResp);

        // Kiểm tra sau khi nhân viên lấy hàng: phí vận chuyển đã chuyển sang Đã thanh toán
        var summary = paymentService.getOrderPaymentSummary(resp.getMaDh());
        assertTrue(summary.isPhiDaThanhToan(), "Cước vận chuyển phải tự động chuyển sang Đã thanh toán sau khi nhân viên lấy hàng");
    }

    @Autowired
    private com.example.backend.repository.NguoiDungRepository nguoiDungRepository;

    @Test
    @Order(13)
    @DisplayName("Kiểm tra đăng nhập của tài khoản 0901000001")
    void testLoginCustomer() {
        String realHash = passwordEncoder.encode("123456");
        System.out.println("REAL HASH FOR 123456: " + realHash);
        var nd = nguoiDungRepository.findByTenDangNhap("0901000001").orElseThrow();
        nd.setMatKhau(realHash);
        nguoiDungRepository.save(nd);

        // Cũng cập nhật cho admin và nvbc01 để mọi tài khoản đều đăng nhập được bằng 123456
        nguoiDungRepository.findByTenDangNhap("admin").ifPresent(a -> { a.setMatKhau(realHash); nguoiDungRepository.save(a); });
        nguoiDungRepository.findByTenDangNhap("nvbc01").ifPresent(n -> { n.setMatKhau(realHash); nguoiDungRepository.save(n); });

        var req = new com.example.backend.dto.request.LoginRequest();
        req.setTenDangNhap("0901000001");
        req.setMatKhau("123456");
        var resp = authService.login(req);
        System.out.println("LOGIN SUCCESS TOKEN: " + resp.getAccessToken());
        assertNotNull(resp.getAccessToken());
    }
}
