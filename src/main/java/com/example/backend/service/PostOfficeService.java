package com.example.backend.service;

import com.example.backend.dto.postoffice.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostOfficeService {

    private final DonHangRepository donHangRepository;
    private final KhoRepository khoRepository;
    private final TrangThaiDHRepository trangThaiDHRepository;
    private final KienHangRepository kienHangRepository;
    private final LichSuTrangThaiRepository lichSuTrangThaiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ThanhToanRepository thanhToanRepository;

    // Bộ quy tắc chuyển đổi trạng thái hợp lệ
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // TT01: Mới tạo -> TT02 (Đã tiếp nhận), TT07 (Hủy/Trả)
        VALID_TRANSITIONS.put("TT01", Set.of("TT02", "TT07"));
        // TT02: Đã tiếp nhận -> TT03 (Đang vận chuyển), TT05 (Đang giao hàng nội bưu
        // cục), TT07
        VALID_TRANSITIONS.put("TT02", Set.of("TT03", "TT05", "TT07"));
        // TT03: Đang vận chuyển -> TT04 (Đã đến kho), TT07
        VALID_TRANSITIONS.put("TT03", Set.of("TT04", "TT07"));
        // TT04: Đã đến kho -> TT03 (Chuyển liên kho tiếp), TT05 (Đang giao hàng), TT07
        VALID_TRANSITIONS.put("TT04", Set.of("TT03", "TT05", "TT07"));
        // TT05: Đang giao hàng -> TT06 (Giao thành công), TT08 (Giao thất bại), TT07
        VALID_TRANSITIONS.put("TT05", Set.of("TT06", "TT08", "TT07"));
        // TT08: Giao thất bại -> TT05 (Giao lại), TT07 (Hủy/trả hàng)
        VALID_TRANSITIONS.put("TT08", Set.of("TT05", "TT07"));
    }

    private PendingOrderResponse mapToPendingOrderResponse(DonHang d) {
        List<KienHang> kiens = kienHangRepository.findByDonHang_MaDh(d.getMaDh());
        KienHang firstKien = kiens.isEmpty() ? null : kiens.get(0);

        List<ThanhToan> payments = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(d.getMaDh());
        ThanhToan phiPayment = payments.stream()
                .filter(p -> "PHI_VC".equalsIgnoreCase(p.getLoaiKhoan()))
                .findFirst()
                .orElse(null);

        boolean phiDaThanhToan = phiPayment != null &&
                ("Đã thanh toán".equalsIgnoreCase(phiPayment.getTrangThai())
                        || "DA_THANH_TOAN".equalsIgnoreCase(phiPayment.getTrangThai()));
        String phuongThuc = phiPayment != null ? phiPayment.getPhuongThuc() : "Chuyển khoản";
        String tinhTrang = phiDaThanhToan ? "Đã thanh toán" : ("Tiền mặt".equalsIgnoreCase(phuongThuc) ? "Thu tiền mặt khi giao" : "Chưa thanh toán");

        return PendingOrderResponse.builder()
                .maDh(d.getMaDh())
                .maKhGui(d.getMaKhGui())
                .tenNguoiNhan(d.getTenNguoiNhan())
                .sdtNhan(d.getSdtNhan())
                .diaChiLay(d.getDiaChiLay())
                .diaChiGiao(d.getDiaChiGiao())
                .maKhoGui(d.getMaKhoGui())
                .maKhoNhan(d.getMaKhoNhan())
                .ngayTao(d.getNgayTao())
                .phiVanChuyen(d.getPhiVanChuyen())
                .cod(d.getCod())
                .maTrangThai(d.getTrangThai().getMaTrangThai())
                .tenTrangThai(d.getTrangThai().getTenTrangThai())
                .phiDaThanhToan(phiDaThanhToan)
                .tinhTrangThanhToan(tinhTrang)
                .phuongThucThanhToan(phuongThuc)
                .khoiLuong(firstKien != null ? firstKien.getKhoiLuong() : new BigDecimal("1.5"))
                .dai(firstKien != null && firstKien.getDai() != null ? firstKien.getDai() : new BigDecimal("20.0"))
                .rong(firstKien != null && firstKien.getRong() != null ? firstKien.getRong() : new BigDecimal("15.0"))
                .cao(firstKien != null && firstKien.getCao() != null ? firstKien.getCao() : new BigDecimal("10.0"))
                .loaiHang(firstKien != null && firstKien.getLoaiHang() != null ? firstKien.getLoaiHang() : "Hàng tiêu chuẩn")
                .build();
    }

    /**
     * UC08: Lấy danh sách đơn hàng chờ tiếp nhận tại bưu cục (trạng thái 'Mới tạo' TT01)
     */
    @Transactional(readOnly = true)
    public List<PendingOrderResponse> getPendingOrders(String maKho) {
        log.info("Lấy danh sách đơn hàng chờ tiếp nhận tại bưu cục {}", maKho);
        if (!khoRepository.existsById(maKho)) {
            throw new EntityNotFoundException("Không tìm thấy bưu cục với mã: " + maKho);
        }

        List<DonHang> orders = donHangRepository.findByMaKhoGuiAndTrangThai_MaTrangThai(maKho, "TT01");
        return orders.stream().map(this::mapToPendingOrderResponse).toList();
    }

    /**
     * UC08: Lấy danh sách đơn hàng chờ tiếp nhận có phân trang tại bưu cục
     */
    @Transactional(readOnly = true)
    public Page<PendingOrderResponse> getPendingOrdersPaged(String maKho, int page, int size) {
        log.info("Lấy danh sách đơn hàng chờ tiếp nhận phân trang tại bưu cục {} (page={}, size={})", maKho, page, size);
        if (!khoRepository.existsById(maKho)) {
            throw new EntityNotFoundException("Không tìm thấy bưu cục với mã: " + maKho);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<DonHang> ordersPage = donHangRepository.findByMaKhoGuiAndTrangThai_MaTrangThai(maKho, "TT01", pageable);
        return ordersPage.map(this::mapToPendingOrderResponse);
    }

    /**
     * UC08: Bưu cục tiếp nhận đơn và lập kiện hàng
     */
    @Transactional
    public ReceiveOrderResponse receiveOrder(String maDH, ReceiveOrderRequest request) {
        log.info("Bưu cục {} tiếp nhận đơn hàng {}", request.getMaKho(), maDH);

        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng: " + maDH));

        Kho kho = khoRepository.findById(request.getMaKho())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bưu cục: " + request.getMaKho()));

        if (!"TT01".equals(donHang.getTrangThai().getMaTrangThai())) {
            throw new IllegalArgumentException(
                    "Đơn hàng không ở trạng thái 'Mới tạo' (Hiện tại: " + donHang.getTrangThai().getTenTrangThai()
                            + ")");
        }

        if (donHang.getMaKhoGui() != null && !donHang.getMaKhoGui().equals(request.getMaKho())) {
            throw new IllegalArgumentException(
                    "Bưu cục tiếp nhận (" + request.getMaKho() + ") không trùng với bưu cục gửi của đơn ("
                            + donHang.getMaKhoGui() + ")");
        }

        // Kiểm tra phương thức thanh toán: nếu chuyển khoản mà chưa thanh toán thì KHÔNG THỂ tiếp nhận
        List<ThanhToan> existingPayments = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(donHang.getMaDh());
        ThanhToan phiPayment = existingPayments.stream()
                .filter(p -> "PHI_VC".equalsIgnoreCase(p.getLoaiKhoan()))
                .findFirst()
                .orElse(null);

        if (phiPayment != null) {
            String pt = phiPayment.getPhuongThuc() != null ? phiPayment.getPhuongThuc().toUpperCase() : "";
            boolean isChuyenKhoan = pt.contains("CHUYEN") || pt.contains("CHUYỂN") || pt.contains("BANK") || pt.contains("QR");
            boolean isDaThanhToan = "Đã thanh toán".equalsIgnoreCase(phiPayment.getTrangThai())
                    || "DA_THANH_TOAN".equalsIgnoreCase(phiPayment.getTrangThai());
            if (isChuyenKhoan && !isDaThanhToan) {
                throw new IllegalArgumentException(
                        "Đơn hàng " + maDH + " thanh toán bằng chuyển khoản nhưng chưa hoàn tất thanh toán cước phí. Không thể tiếp nhận đơn hàng khi chưa thanh toán!");
            }
        }

        // Lấy hoặc tạo trạng thái TT02 (Đã tiếp nhận)
        TrangThaiDH tt02 = trangThaiDHRepository.findById("TT02")
                .orElseGet(() -> TrangThaiDH.builder().maTrangThai("TT02").tenTrangThai("Đã tiếp nhận").build());

        // Cập nhật kiện hàng
        List<KienHang> existingPackages = kienHangRepository.findByDonHang_MaDh(maDH);
        String maKien;

        if (!existingPackages.isEmpty()) {
            // Cập nhật kiện hàng đã có
            KienHang kien = existingPackages.get(0);
            if (request.getKhoiLuong() != null) {
                kien.setKhoiLuong(request.getKhoiLuong());
            }
            if (request.getDai() != null) {
                kien.setDai(request.getDai());
            }
            if (request.getRong() != null) {
                kien.setRong(request.getRong());
            }
            if (request.getCao() != null) {
                kien.setCao(request.getCao());
            }
            if (request.getKichThuoc() != null) {
                kien.setKichThuoc(request.getKichThuoc());
            }
            if (request.getLoaiHang() != null) {
                kien.setLoaiHang(request.getLoaiHang());
            }
            kien.setKhoHienTai(kho);
            kienHangRepository.save(kien);
            maKien = kien.getMaKien();
        } else {
            // Lập kiện hàng mới
            maKien = "KIEN" + String.format("%06d", (int) (System.currentTimeMillis() % 1000000));
            KienHang newKien = KienHang.builder()
                    .maKien(maKien)
                    .donHang(donHang)
                    .khoiLuong(request.getKhoiLuong())
                    .dai(request.getDai() != null ? request.getDai() : new java.math.BigDecimal("20.00"))
                    .rong(request.getRong() != null ? request.getRong() : new java.math.BigDecimal("15.00"))
                    .cao(request.getCao() != null ? request.getCao() : new java.math.BigDecimal("10.00"))
                    .loaiHang(request.getLoaiHang() != null ? request.getLoaiHang() : "Hàng tiêu chuẩn")
                    .khoHienTai(kho)
                    .build();
            if (request.getKichThuoc() != null && !request.getKichThuoc().isBlank()) {
                newKien.setKichThuoc(request.getKichThuoc());
            }
            kienHangRepository.save(newKien);
        }

        // Cập nhật đơn hàng
        donHang.setTrangThai(tt02);
        if (request.getMaNV() != null && !request.getMaNV().isBlank()) {
            donHang.setMaNv(request.getMaNV());
        }
        donHangRepository.save(donHang);

        // Đơn hàng thanh toán tiền mặt khi giao sẽ KHÔNG chuyển sang đã thanh toán ở bước tiếp nhận,
        // mà sẽ do tài xế thu tiền mặt và chuyển sang Đã thanh toán khi giao hàng thành công.

        // Ghi nhận lịch sử trạng thái
        String maLs = "LS" + String.format("%08d", (int) (System.currentTimeMillis() % 100000000));
        LichSuTrangThai lichSu = LichSuTrangThai.builder()
                .maLs(maLs)
                .donHang(donHang)
                .trangThai(tt02)
                .thoiGian(LocalDateTime.now())
                .maNd(resolveValidMaNd(request.getMaNV()))
                .ghiChu(request.getGhiChu() != null && !request.getGhiChu().isBlank()
                        ? request.getGhiChu()
                        : "Bưu cục " + kho.getTenKho() + " tiếp nhận và xác thực kiện hàng")
                .build();
        lichSuTrangThaiRepository.save(lichSu);

        return ReceiveOrderResponse.builder()
                .maDh(donHang.getMaDh())
                .maKien(maKien)
                .maKhoHienTai(kho.getMaKho())
                .maNv(donHang.getMaNv())
                .maTrangThai(tt02.getMaTrangThai())
                .tenTrangThai(tt02.getTenTrangThai())
                .thoiGianTiepNhan(LocalDateTime.now())
                .thongBao("Bưu cục " + kho.getTenKho() + " đã tiếp nhận đơn hàng và lập kiện thành công")
                .build();
    }

    /**
     * UC09: Chuyển kho cho kiện hàng lưu kho
     */
    @Transactional
    public TransferWarehouseResponse transferWarehouse(String maKien, TransferWarehouseRequest request) {
        log.info("Chuyển kiện hàng {} sang kho mới {}", maKien, request.getMaKhoMoi());

        KienHang kien = kienHangRepository.findByMaKien(maKien)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kiện hàng: " + maKien));

        Kho khoMoi = khoRepository.findById(request.getMaKhoMoi())
                .orElseThrow(
                        () -> new EntityNotFoundException("Không tìm thấy bưu cục đích: " + request.getMaKhoMoi()));

        String maKhoCu = kien.getKhoHienTai() != null ? kien.getKhoHienTai().getMaKho() : "Chưa nhập kho";
        kienHangRepository.updateKhoHienTai(maKien, khoMoi);
        kien.setKhoHienTai(khoMoi);

        // Ghi log chuyển kho vào lịch sử đơn hàng
        if (kien.getDonHang() != null) {
            String maLs = "LS" + String.format("%08d", (int) (System.currentTimeMillis() % 100000000));
            LichSuTrangThai lichSu = LichSuTrangThai.builder()
                    .maLs(maLs)
                    .donHang(kien.getDonHang())
                    .trangThai(kien.getDonHang().getTrangThai())
                    .thoiGian(LocalDateTime.now())
                    .maNd(resolveValidMaNd(request.getMaND()))
                    .ghiChu("Chuyển kho kiện hàng " + maKien + " từ " + maKhoCu + " sang " + khoMoi.getTenKho()
                            + (request.getGhiChu() != null ? ". " + request.getGhiChu() : ""))
                    .build();
            lichSuTrangThaiRepository.save(lichSu);
        }

        return TransferWarehouseResponse.builder()
                .maKien(maKien)
                .maDh(kien.getDonHang() != null ? kien.getDonHang().getMaDh() : null)
                .maKhoCu(maKhoCu)
                .maKhoMoi(khoMoi.getMaKho())
                .tenKhoMoi(khoMoi.getTenKho())
                .thoiGian(LocalDateTime.now())
                .thongBao("Đã chuyển kiện hàng " + maKien + " đến " + khoMoi.getTenKho())
                .build();
    }

    /**
     * UC10: Cập nhật trạng thái luân chuyển đơn hàng (kiểm tra quy trình)
     */
    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(String maDH, UpdateOrderStatusRequest request) {
        log.info("Cập nhật trạng thái đơn hàng {} sang {}", maDH, request.getMaTrangThaiMoi());

        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng: " + maDH));

        TrangThaiDH trangThaiMoi = trangThaiDHRepository.findById(request.getMaTrangThaiMoi())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy trạng thái đích: " + request.getMaTrangThaiMoi()));

        String currentCode = donHang.getTrangThai().getMaTrangThai();
        String targetCode = trangThaiMoi.getMaTrangThai();

        if (currentCode.equalsIgnoreCase(targetCode)) {
            throw new IllegalArgumentException(
                    "Đơn hàng hiện tại đã ở trạng thái: " + donHang.getTrangThai().getTenTrangThai());
        }

        Set<String> allowedTransitions = VALID_TRANSITIONS.get(currentCode);
        if (allowedTransitions == null || !allowedTransitions.contains(targetCode)) {
            throw new IllegalArgumentException(String.format(
                    "Quy trình chuyển đổi trạng thái không hợp lệ: '%s' (%s) -> '%s' (%s)",
                    currentCode, donHang.getTrangThai().getTenTrangThai(),
                    targetCode, trangThaiMoi.getTenTrangThai()));
        }

        String maTrangThaiCu = donHang.getTrangThai().getMaTrangThai();
        String tenTrangThaiCu = donHang.getTrangThai().getTenTrangThai();
        // Khi giao hàng thành công (TT06): tài xế thu tiền mặt, tự động cập nhật cước tiền mặt và COD sang Đã thanh toán TRƯỚC KHI lưu đơn (vì Trigger DB kiểm tra)
        if ("TT06".equals(targetCode)) {
            List<ThanhToan> unpaidPayments = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(donHang.getMaDh())
                    .stream()
                    .filter(p -> !"DA_THANH_TOAN".equalsIgnoreCase(p.getTrangThai())
                            && !"Đã thanh toán".equalsIgnoreCase(p.getTrangThai()))
                    .toList();
            for (ThanhToan p : unpaidPayments) {
                p.setTrangThai("Đã thanh toán");
                p.setThoiGian(LocalDateTime.now());
                if (p.getNguoiThanhToan() == null || p.getNguoiThanhToan().isBlank()) {
                    p.setNguoiThanhToan(donHang.getTenNguoiNhan());
                }
                thanhToanRepository.save(p);
            }

            // Nếu đơn có COD > 0 nhưng chưa có bản ghi COD thì lập bản ghi COD đã thanh toán
            if (donHang.getCod() != null && donHang.getCod().compareTo(java.math.BigDecimal.ZERO) > 0) {
                boolean hasPaidCod = thanhToanRepository.findByDonHang_MaDhAndLoaiKhoan(donHang.getMaDh(), "COD")
                        .stream()
                        .anyMatch(p -> "Đã thanh toán".equalsIgnoreCase(p.getTrangThai()) || "DA_THANH_TOAN".equalsIgnoreCase(p.getTrangThai()));
                if (!hasPaidCod) {
                    String maTtCod = "GD" + String.format("%06d", (int) (System.currentTimeMillis() % 1000000));
                    ThanhToan codPayment = ThanhToan.builder()
                            .maTtToan(maTtCod)
                            .donHang(donHang)
                            .loaiKhoan("COD")
                            .soTien(donHang.getCod())
                            .phuongThuc("COD")
                            .trangThai("Đã thanh toán")
                            .thoiGian(LocalDateTime.now())
                            .nguoiThanhToan(donHang.getTenNguoiNhan())
                            .build();
                    thanhToanRepository.save(codPayment);
                }
            }
            thanhToanRepository.flush();
        }

        // Cập nhật trạng thái mới
        donHang.setTrangThai(trangThaiMoi);
        donHangRepository.save(donHang);

        // Ghi nhận lịch sử
        String maLs = "LS" + String.format("%08d", (int) (System.currentTimeMillis() % 100000000));
        LichSuTrangThai lichSu = LichSuTrangThai.builder()
                .maLs(maLs)
                .donHang(donHang)
                .trangThai(trangThaiMoi)
                .thoiGian(LocalDateTime.now())
                .maNd(resolveValidMaNd(request.getMaND()))
                .ghiChu(request.getGhiChu() != null && !request.getGhiChu().isBlank()
                        ? request.getGhiChu()
                        : "Cập nhật trạng thái: " + tenTrangThaiCu + " -> " + trangThaiMoi.getTenTrangThai())
                .build();
        lichSuTrangThaiRepository.save(lichSu);

        return UpdateOrderStatusResponse.builder()
                .maDh(maDH)
                .maTrangThaiCu(maTrangThaiCu)
                .tenTrangThaiCu(tenTrangThaiCu)
                .maTrangThaiMoi(targetCode)
                .tenTrangThaiMoi(trangThaiMoi.getTenTrangThai())
                .thoiGian(LocalDateTime.now())
                .thongBao("Cập nhật trạng thái đơn hàng thành công")
                .build();
    }

    /**
     * Lấy danh sách kiện hàng phân trang (UC09)
     */
    @Transactional(readOnly = true)
    public Page<PackageListItemResponse> getPackagesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KienHang> paged = kienHangRepository.findAllWithDonHangAndKho(pageable);
        return paged.map(k -> PackageListItemResponse.builder()
                .maKien(k.getMaKien())
                .maDh(k.getDonHang() != null ? k.getDonHang().getMaDh() : null)
                .maKhoHienTai(k.getKhoHienTai() != null ? k.getKhoHienTai().getMaKho() : null)
                .tenKhoHienTai(k.getKhoHienTai() != null ? k.getKhoHienTai().getTenKho() : "Chưa nhập kho")
                .khoiLuong(k.getKhoiLuong())
                .kichThuoc(k.getKichThuoc() != null ? k.getKichThuoc() : (k.getDai() + "x" + k.getRong() + "x" + k.getCao()))
                .dai(k.getDai())
                .rong(k.getRong())
                .cao(k.getCao())
                .loaiHang(k.getLoaiHang())
                .build());
    }

    /**
     * Phân giải ID truyền vào (mã người dùng hoặc mã nhân viên) thành ma_nd hợp lệ
     * có trong bảng nguoi_dung.
     * Trả về null nếu không tìm thấy để không vi phạm khóa ngoại FK.
     */
    private String resolveValidMaNd(String rawUserIdOrEmployeeId) {
        if (rawUserIdOrEmployeeId == null || rawUserIdOrEmployeeId.isBlank()) {
            return null;
        }
        // 1. Kiểm tra trực tiếp xem có trong bảng nguoi_dung không
        if (nguoiDungRepository.existsById(rawUserIdOrEmployeeId)) {
            return rawUserIdOrEmployeeId;
        }
        // 2. Kiểm tra xem có phải mã nhân viên (nhân viên gắn với mã người dùng)
        return nhanVienRepository.findById(rawUserIdOrEmployeeId)
                .map(nv -> nv.getNguoiDung() != null ? nv.getNguoiDung().getMaNd() : null)
                .orElse(null);
    }
}
