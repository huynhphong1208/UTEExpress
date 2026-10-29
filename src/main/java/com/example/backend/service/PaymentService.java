package com.example.backend.service;

import com.example.backend.dto.payment.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final LichSuTrangThaiRepository lichSuTrangThaiRepository;

    private static final List<String> SUCCESS_STATUSES = List.of("Đã thanh toán", "DA_THANH_TOAN");

    /**
     * Chuẩn hóa phương thức thanh toán để thỏa mãn check constraint ck_tt_pt ('Tiền mặt', 'Chuyển khoản', 'COD')
     */
    private String normalizePhuongThuc(String pt) {
        if (pt == null || pt.isBlank()) {
            return "Tiền mặt";
        }
        String upper = pt.toUpperCase().trim();
        if (upper.contains("CHUYEN") || upper.contains("CHUYỂN") || upper.contains("BANK") || upper.contains("QR") || upper.equals("CHUYEN_KHOAN")) {
            return "Chuyển khoản";
        } else if (upper.equals("COD")) {
            return "COD";
        } else {
            return "Tiền mặt";
        }
    }

    private boolean isDaThanhToan(String trangThai) {
        return trangThai != null && (trangThai.equalsIgnoreCase("DA_THANH_TOAN") || trangThai.equalsIgnoreCase("Đã thanh toán"));
    }

    /**
     * UC06: Lấy bảng kê chi tiết cước vận chuyển, tiền thu hộ COD và tổng số tiền cần thu của đơn hàng
     */
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getOrderPaymentSummary(String maDH) {
        log.info("Lấy thông tin tổng hợp thanh toán của đơn hàng {}", maDH);

        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng: " + maDH));

        List<ThanhToan> payments = thanhToanRepository.findByDonHang_MaDhOrderByThoiGianDesc(maDH);

        boolean phiDaThanhToan = payments.stream()
                .anyMatch(p -> "PHI_VC".equalsIgnoreCase(p.getLoaiKhoan()) && isDaThanhToan(p.getTrangThai()));

        boolean codDaThu = payments.stream()
                .anyMatch(p -> "COD".equalsIgnoreCase(p.getLoaiKhoan()) && isDaThanhToan(p.getTrangThai()));

        BigDecimal tongDaThu = payments.stream()
                .filter(p -> isDaThanhToan(p.getTrangThai()))
                .map(ThanhToan::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal phiVC = donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO;
        BigDecimal cod = donHang.getCod() != null ? donHang.getCod() : BigDecimal.ZERO;
        BigDecimal tongCanThu = phiVC.add(cod);

        String tinhTrangThanhToan;
        if ("TT07".equals(donHang.getTrangThai().getMaTrangThai())) {
            tinhTrangThanhToan = "Không thu (đơn đã hủy/trả hàng)";
        } else if (phiDaThanhToan && (cod.compareTo(BigDecimal.ZERO) == 0 || codDaThu)) {
            tinhTrangThanhToan = "Đã thanh toán đủ";
        } else if (phiDaThanhToan || codDaThu) {
            tinhTrangThanhToan = "Thanh toán một phần";
        } else {
            tinhTrangThanhToan = "Chưa thanh toán";
        }

        List<PaymentDetailDto> paymentDetailDtos = payments.stream().map(p -> PaymentDetailDto.builder()
                .maTTToan(p.getMaTtToan())
                .loaiKhoan(p.getLoaiKhoan())
                .soTien(p.getSoTien())
                .phuongThuc(p.getPhuongThuc())
                .trangThai(p.getTrangThai())
                .thoiGian(p.getThoiGian())
                .nguoiThanhToan(p.getNguoiThanhToan())
                .build()
        ).toList();

        // Xác định phương thức thanh toán cước phí và phương thức hiển thị tổng hợp
        String phuongThucCuocRaw = payments.stream()
                .filter(p -> "PHI_VC".equalsIgnoreCase(p.getLoaiKhoan()))
                .map(ThanhToan::getPhuongThuc)
                .filter(pt -> pt != null && !pt.isBlank())
                .findFirst()
                .orElseGet(() -> payments.stream()
                        .map(ThanhToan::getPhuongThuc)
                        .filter(pt -> pt != null && !pt.isBlank())
                        .findFirst()
                        .orElse("Tiền mặt"));

        boolean isCk = phuongThucCuocRaw.toUpperCase().contains("CHUYEN")
                || phuongThucCuocRaw.toUpperCase().contains("CHUYỂN")
                || phuongThucCuocRaw.toUpperCase().contains("BANK")
                || phuongThucCuocRaw.toUpperCase().contains("QR");
        String phuongThucHienThi;
        if (isCk) {
            phuongThucHienThi = (cod.compareTo(BigDecimal.ZERO) > 0) ? "Chuyển khoản / Thu hộ COD" : "Chuyển khoản trực tuyến";
        } else {
            phuongThucHienThi = (cod.compareTo(BigDecimal.ZERO) > 0) ? "Tiền mặt / Thu hộ COD" : "Tiền mặt khi giao/lấy hàng";
        }

        return PaymentSummaryResponse.builder()
                .maDh(donHang.getMaDh())
                .phiVanChuyen(phiVC)
                .cod(cod)
                .tongSoTienCanThu(tongCanThu)
                .tongSoTienDaThu(tongDaThu)
                .phiDaThanhToan(phiDaThanhToan)
                .codDaThu(codDaThu)
                .tinhTrangThanhToan(tinhTrangThanhToan)
                .phuongThucCuoc(isCk ? "Chuyển khoản" : "Tiền mặt")
                .phuongThucThanhToan(phuongThucHienThi)
                .maTrangThai(donHang.getTrangThai().getMaTrangThai())
                .tenTrangThai(donHang.getTrangThai().getTenTrangThai())
                .lichSuThanhToan(paymentDetailDtos)
                .build();
    }

    /**
     * UC06, UC07: Xử lý giao dịch thanh toán hoặc xác nhận thu COD
     */
    @Transactional
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Xử lý thanh toán cho đơn hàng {}: số tiền {}", request.getMaDH(), request.getSoTien());

        DonHang donHang = donHangRepository.findById(request.getMaDH())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng: " + request.getMaDH()));

        if ("TT07".equals(donHang.getTrangThai().getMaTrangThai())) {
            throw new IllegalArgumentException("Đơn hàng đã ở trạng thái hủy/trả hàng, không thể thực hiện thanh toán");
        }

        String loaiKhoan = request.getLoaiKhoan();
        if (loaiKhoan == null || loaiKhoan.isBlank()) {
            if (donHang.getCod() != null && donHang.getCod().compareTo(BigDecimal.ZERO) > 0
                    && donHang.getCod().compareTo(request.getSoTien()) == 0) {
                loaiKhoan = "COD";
            } else {
                loaiKhoan = "PHI_VC";
            }
        }
        loaiKhoan = loaiKhoan.toUpperCase().trim();

        // Kiểm tra xem khoản này đã được thanh toán trước đó chưa
        boolean daThanhToan = thanhToanRepository.existsByDonHang_MaDhAndLoaiKhoanAndTrangThaiIn(
                donHang.getMaDh(), loaiKhoan, SUCCESS_STATUSES);

        if (daThanhToan) {
            throw new IllegalArgumentException("Khoản " + loaiKhoan + " của đơn hàng " + donHang.getMaDh() + " đã được thanh toán trước đó");
        }

        if ("COD".equals(loaiKhoan)) {
            if (donHang.getCod() == null || donHang.getCod().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Đơn hàng này không có tiền thu hộ COD");
            }
        }

        String phuongThucChuan = normalizePhuongThuc(request.getPhuongThuc());

        // Kiểm tra xem đã có bản ghi chưa thanh toán tương ứng chưa để cập nhật thay vì tạo trùng
        List<ThanhToan> existingPending = thanhToanRepository.findByDonHang_MaDhAndLoaiKhoan(donHang.getMaDh(), loaiKhoan)
                .stream()
                .filter(p -> !isDaThanhToan(p.getTrangThai()))
                .toList();

        ThanhToan thanhToan;
        if (!existingPending.isEmpty()) {
            thanhToan = existingPending.get(0);
            thanhToan.setPhuongThuc(phuongThucChuan);
            thanhToan.setTrangThai("Đã thanh toán");
            thanhToan.setSoTien(request.getSoTien());
            thanhToan.setThoiGian(LocalDateTime.now());
            if (request.getNguoiThanhToan() != null && !request.getNguoiThanhToan().isBlank()) {
                thanhToan.setNguoiThanhToan(request.getNguoiThanhToan());
            }
        } else {
            // Sinh mã giao dịch GDxxxxxx
            String maTTToan = "GD" + String.format("%06d", (int) (System.currentTimeMillis() % 1000000));
            thanhToan = ThanhToan.builder()
                    .maTtToan(maTTToan)
                    .donHang(donHang)
                    .loaiKhoan(loaiKhoan)
                    .soTien(request.getSoTien())
                    .phuongThuc(phuongThucChuan)
                    .trangThai("Đã thanh toán")
                    .thoiGian(LocalDateTime.now())
                    .nguoiThanhToan(request.getNguoiThanhToan() != null && !request.getNguoiThanhToan().isBlank()
                            ? request.getNguoiThanhToan()
                            : donHang.getTenNguoiNhan())
                    .build();
        }

        thanhToanRepository.save(thanhToan);

        // Ghi nhận vào lịch sử
        String maLs = "LS" + String.format("%08d", (int) (System.currentTimeMillis() % 100000000));
        LichSuTrangThai lichSu = LichSuTrangThai.builder()
                .maLs(maLs)
                .donHang(donHang)
                .trangThai(donHang.getTrangThai())
                .thoiGian(LocalDateTime.now())
                .maNd(null)
                .ghiChu("Thanh toán thành công khoản " + loaiKhoan + ": " + request.getSoTien() + " VNĐ qua " + thanhToan.getPhuongThuc())
                .build();
        lichSuTrangThaiRepository.save(lichSu);

        return ProcessPaymentResponse.builder()
                .maTTToan(thanhToan.getMaTtToan())
                .maDh(donHang.getMaDh())
                .loaiKhoan(loaiKhoan)
                .soTien(request.getSoTien())
                .phuongThuc(thanhToan.getPhuongThuc())
                .trangThai("Đã thanh toán")
                .thoiGian(thanhToan.getThoiGian())
                .nguoiThanhToan(thanhToan.getNguoiThanhToan())
                .thongBao("Xử lý giao dịch thanh toán thành công")
                .build();
    }

    /**
     * Màn hình đối soát COD Admin (Mục 4.2.22)
     */
    @Transactional(readOnly = true)
    public CodReconciliationResponse getCodReconciliation() {
        log.info("Lấy dữ liệu đối soát COD Admin");

        List<DonHang> allOrders = donHangRepository.findAll();
        List<CodTransactionDto> codTransactions = new ArrayList<>();

        BigDecimal tongCodChoDoiSoat = BigDecimal.ZERO;
        BigDecimal tongCodDaHoanTat = BigDecimal.ZERO;

        for (DonHang dh : allOrders) {
            BigDecimal codVal = dh.getCod() != null ? dh.getCod() : BigDecimal.ZERO;
            if (codVal.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Bỏ qua đơn không có COD
            }

            Optional<ThanhToan> codPaymentOpt = thanhToanRepository.findFirstByDonHang_MaDhAndLoaiKhoanAndTrangThaiIn(
                    dh.getMaDh(), "COD", SUCCESS_STATUSES);

            String tinhTrangCod;
            BigDecimal codDaThu = BigDecimal.ZERO;
            String nguoiThu = null;
            LocalDateTime thoiGianThu = null;
            String maGiaoDich = null;

            if (codPaymentOpt.isPresent()) {
                ThanhToan payment = codPaymentOpt.get();
                codDaThu = payment.getSoTien();
                nguoiThu = payment.getNguoiThanhToan();
                thoiGianThu = payment.getThoiGian();
                maGiaoDich = payment.getMaTtToan();

                if ("TT06".equals(dh.getTrangThai().getMaTrangThai())) {
                    tinhTrangCod = "Đã hoàn tất chi trả";
                    tongCodDaHoanTat = tongCodDaHoanTat.add(codDaThu);
                } else {
                    tinhTrangCod = "Đã thu";
                    tongCodChoDoiSoat = tongCodChoDoiSoat.add(codDaThu);
                }
            } else if ("TT07".equals(dh.getTrangThai().getMaTrangThai())) {
                tinhTrangCod = "Không thu (hủy/trả)";
            } else {
                tinhTrangCod = "Chờ đối soát";
                tongCodChoDoiSoat = tongCodChoDoiSoat.add(codVal);
            }

            codTransactions.add(CodTransactionDto.builder()
                    .maDh(dh.getMaDh())
                    .maKhGui(dh.getMaKhGui())
                    .tenNguoiNhan(dh.getTenNguoiNhan())
                    .sdtNhan(dh.getSdtNhan())
                    .tienCod(codVal)
                    .codDaThu(codDaThu)
                    .maTrangThai(dh.getTrangThai().getMaTrangThai())
                    .tenTrangThai(dh.getTrangThai().getTenTrangThai())
                    .tinhTrangCod(tinhTrangCod)
                    .thoiGianThu(thoiGianThu)
                    .nguoiThu(nguoiThu)
                    .maGiaoDich(maGiaoDich)
                    .build());
        }

        BigDecimal tongCuocPhiDaThu = thanhToanRepository.sumPhiVanChuyenDaThu();

        return CodReconciliationResponse.builder()
                .tongCuocPhiDaThu(tongCuocPhiDaThu != null ? tongCuocPhiDaThu : BigDecimal.ZERO)
                .tongCodChoDoiSoat(tongCodChoDoiSoat)
                .tongCodDaHoanTat(tongCodDaHoanTat)
                .tongSoGiaoDichCod(codTransactions.size())
                .danhSachGiaoDich(codTransactions)
                .build();
    }
}
