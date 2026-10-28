package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự động đồng bộ và bổ sung danh mục Bưu cục (Kho hàng), Tuyến vận chuyển,
 * và Nhân viên bưu cục (mỗi kho tối thiểu 1 nhân viên bưu cục trực thuộc)
 * khi ứng dụng khởi động.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class DatabaseWarehouseAndStaffInitializer implements ApplicationRunner {

    private final KhoRepository khoRepository;
    private final TuyenVanChuyenRepository tuyenVanChuyenRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final DonHangRepository donHangRepository;
    private final KienHangRepository kienHangRepository;
    private final TrangThaiDHRepository trangThaiDHRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("===> Khởi chạy DatabaseWarehouseAndStaffInitializer: Đồng bộ dữ liệu kho & nhân viên bưu cục...");
        try {
            initWarehouses();
            initRoutes();
            initStaffAndAccounts();
            initSamplePendingOrders();
            log.info("===> Hoàn tất khởi tạo dữ liệu kho và nhân viên bưu cục thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi tự động khởi tạo dữ liệu: {}", e.getMessage(), e);
        }
    }

    private void initWarehouses() {
        record WarehouseData(String ma, String ten, String diaChi, String sdt) {}
        List<WarehouseData> warehouses = List.of(
                new WarehouseData("KHO01", "Bưu cục Thủ Đức (TP.HCM)", "12 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức, TP.HCM", "0281111111"),
                new WarehouseData("KHO02", "Bưu cục Cầu Giấy (Hà Nội)", "25 Trần Thái Tông, P. Dịch Vọng Hậu, Q. Cầu Giấy, Hà Nội", "0242222222"),
                new WarehouseData("KHO03", "Bưu cục Đà Nẵng", "45 Nguyễn Văn Linh, P. Nam Dương, Q. Hải Châu, Đà Nẵng", "0236333333"),
                new WarehouseData("KHO04", "Bưu cục Cần Thơ", "88 Nguyễn Trãi, P. An Hội, Q. Ninh Kiều, Cần Thơ", "0292444444"),
                new WarehouseData("KHO05", "Bưu cục Quận 1 (TP.HCM)", "125 Hai Bà Trưng, P. Bến Nghé, Quận 1, TP.HCM", "0283555555"),
                new WarehouseData("KHO06", "Bưu cục Đống Đa (Hà Nội)", "182 Xã Đàn, P. Phương Liên, Q. Đống Đa, Hà Nội", "0243666666"),
                new WarehouseData("KHO07", "Bưu cục Hải Phòng", "68 Lạch Tray, P. Lạch Tray, Q. Ngô Quyền, Hải Phòng", "0225777777"),
                new WarehouseData("KHO08", "Bưu cục Biên Hòa (Đồng Nai)", "54 Nguyễn Ái Quốc, P. Tân Tiến, TP. Biên Hòa, Đồng Nai", "0251888888"),
                new WarehouseData("KHO09", "Bưu cục Nha Trang (Khánh Hòa)", "72 Trần Phú, P. Lộc Thọ, TP. Nha Trang, Khánh Hòa", "0258999999"),
                new WarehouseData("KHO10", "Bưu cục Buôn Ma Thuột (Đắk Lắk)", "15 Lê Duẩn, P. Tự An, TP. Buôn Ma Thuột, Đắk Lắk", "0262101010")
        );

        for (var w : warehouses) {
            if (!khoRepository.existsById(w.ma())) {
                Kho kho = Kho.builder()
                        .maKho(w.ma())
                        .tenKho(w.ten())
                        .diaChi(w.diaChi())
                        .sdt(w.sdt())
                        .build();
                khoRepository.save(kho);
                log.info("Đã tạo mới kho/bưu cục: {} - {}", w.ma(), w.ten());
            } else {
                Kho kho = khoRepository.findById(w.ma()).orElse(null);
                if (kho != null && (kho.getTenKho() == null || !kho.getTenKho().equals(w.ten()))) {
                    kho.setTenKho(w.ten());
                    kho.setDiaChi(w.diaChi());
                    kho.setSdt(w.sdt());
                    khoRepository.save(kho);
                }
            }
        }
    }

    private void initRoutes() {
        record RouteData(String maTuyen, String maKhoDi, String maKhoDen, double km, double gio) {}
        List<RouteData> routes = List.of(
                // Bắc - Nam chính
                new RouteData("TU01", "KHO01", "KHO03", 960, 18),
                new RouteData("TU02", "KHO03", "KHO01", 960, 18),
                new RouteData("TU03", "KHO03", "KHO02", 770, 14),
                new RouteData("TU04", "KHO02", "KHO03", 770, 14),
                new RouteData("TU05", "KHO01", "KHO02", 1700, 36),
                new RouteData("TU06", "KHO02", "KHO01", 1700, 36),
                // Miền Nam
                new RouteData("TU07", "KHO01", "KHO04", 170, 4),
                new RouteData("TU08", "KHO04", "KHO01", 170, 4),
                new RouteData("TU09", "KHO01", "KHO05", 15, 1),
                new RouteData("TU10", "KHO05", "KHO01", 15, 1),
                new RouteData("TU11", "KHO01", "KHO08", 25, 1.5),
                new RouteData("TU12", "KHO08", "KHO01", 25, 1.5),
                new RouteData("TU13", "KHO05", "KHO04", 165, 4),
                new RouteData("TU14", "KHO04", "KHO05", 165, 4),
                new RouteData("TU15", "KHO05", "KHO08", 32, 1.5),
                new RouteData("TU16", "KHO08", "KHO05", 32, 1.5),
                // Miền Bắc
                new RouteData("TU17", "KHO02", "KHO06", 8, 0.5),
                new RouteData("TU18", "KHO06", "KHO02", 8, 0.5),
                new RouteData("TU19", "KHO02", "KHO07", 120, 2.5),
                new RouteData("TU20", "KHO07", "KHO02", 120, 2.5),
                new RouteData("TU21", "KHO06", "KHO07", 125, 2.5),
                new RouteData("TU22", "KHO07", "KHO06", 125, 2.5),
                // Miền Trung & Tây Nguyên
                new RouteData("TU23", "KHO01", "KHO09", 430, 9),
                new RouteData("TU24", "KHO09", "KHO01", 430, 9),
                new RouteData("TU25", "KHO01", "KHO10", 350, 8),
                new RouteData("TU26", "KHO10", "KHO01", 350, 8),
                new RouteData("TU27", "KHO03", "KHO09", 530, 11),
                new RouteData("TU28", "KHO09", "KHO03", 530, 11),
                new RouteData("TU29", "KHO09", "KHO10", 185, 4.5),
                new RouteData("TU30", "KHO10", "KHO09", 185, 4.5)
        );

        for (var r : routes) {
            boolean exists = tuyenVanChuyenRepository.findByMaKhoDiAndMaKhoDen(r.maKhoDi(), r.maKhoDen()).isPresent();
            if (!exists) {
                TuyenVanChuyen tvc = TuyenVanChuyen.builder()
                        .maTuyen(r.maTuyen())
                        .maKhoDi(r.maKhoDi())
                        .maKhoDen(r.maKhoDen())
                        .khoangCach(BigDecimal.valueOf(r.km()))
                        .thoiGianDuKienGio(BigDecimal.valueOf(r.gio()))
                        .build();
                tuyenVanChuyenRepository.save(tvc);
            }
        }
    }

    private void initStaffAndAccounts() {
        VaiTro vtNvbc = vaiTroRepository.findById("NVBC").orElseGet(() ->
                vaiTroRepository.save(new VaiTro("NVBC", "Nhân viên bưu cục", "Tiếp nhận và xử lý đơn tại bưu cục"))
        );

        String defaultEncodedPass = passwordEncoder.encode("123456@Ute");

        record StaffData(String maNd, String username, String maNv, String hoTen, String sdt, String email, String maKho) {}
        List<StaffData> staffList = List.of(
                new StaffData("ND002", "nvbc01", "NV001", "Phạm Hoàng Nam",  "0902000001", "nam@uteexpress.vn",   "KHO01"),
                new StaffData("ND013", "nvbc02", "NV002", "Trần Quang Minh", "0902000002", "minh@uteexpress.vn",  "KHO02"),
                new StaffData("ND003", "nvbc03", "NV003", "Võ Thị Lan",      "0902000003", "lan@uteexpress.vn",   "KHO03"),
                new StaffData("ND014", "nvbc04", "NV004", "Lê Thị Mai",      "0902000004", "mai@uteexpress.vn",   "KHO04"),
                new StaffData("ND015", "nvbc05", "NV005", "Nguyễn Văn Hùng", "0902000005", "hung@uteexpress.vn",  "KHO05"),
                new StaffData("ND016", "nvbc06", "NV006", "Hoàng Thị Yến",   "0902000006", "yen@uteexpress.vn",   "KHO06"),
                new StaffData("ND017", "nvbc07", "NV007", "Đặng Hữu Phúc",   "0902000007", "phuc@uteexpress.vn",  "KHO07"),
                new StaffData("ND018", "nvbc08", "NV008", "Bùi Thị Thu",     "0902000008", "thu@uteexpress.vn",   "KHO08"),
                new StaffData("ND019", "nvbc09", "NV009", "Vũ Anh Tuấn",     "0902000009", "tuan@uteexpress.vn",  "KHO09"),
                new StaffData("ND020", "nvbc10", "NV010", "Phan Thị Nga",    "0902000010", "nga@uteexpress.vn",   "KHO10")
        );

        for (var s : staffList) {
            // 1. Tạo hoặc lấy NguoiDung
            NguoiDung nd = nguoiDungRepository.findByTenDangNhap(s.username())
                    .or(() -> nguoiDungRepository.findById(s.maNd()))
                    .orElseGet(() -> {
                        NguoiDung newNd = NguoiDung.builder()
                                .maNd(s.maNd())
                                .tenDangNhap(s.username())
                                .matKhau(defaultEncodedPass)
                                .trangThai("Hoạt động")
                                .vaiTro(vtNvbc)
                                .build();
                        return nguoiDungRepository.save(newNd);
                    });

            // 2. Tạo hoặc gán NhanVien cho bưu cục tương ứng
            NhanVien nv = nhanVienRepository.findById(s.maNv())
                    .or(() -> nhanVienRepository.findByNguoiDung_MaNd(nd.getMaNd()))
                    .orElseGet(() -> {
                        NhanVien newNv = NhanVien.builder()
                                .maNv(s.maNv())
                                .hoTen(s.hoTen())
                                .chucVu("Nhân viên bưu cục")
                                .sdt(s.sdt())
                                .email(s.email())
                                .maKho(s.maKho())
                                .nguoiDung(nd)
                                .build();
                        return nhanVienRepository.save(newNv);
                    });

            // Đồng bộ lại mã kho, họ tên, SĐT, email và liên kết tài khoản
            boolean needUpdate = false;
            if (!s.maKho().equals(nv.getMaKho())) {
                nv.setMaKho(s.maKho());
                needUpdate = true;
            }
            if (!s.hoTen().equals(nv.getHoTen())) {
                nv.setHoTen(s.hoTen());
                needUpdate = true;
            }
            if (!s.sdt().equals(nv.getSdt())) {
                nv.setSdt(s.sdt());
                needUpdate = true;
            }
            if (!s.email().equals(nv.getEmail())) {
                nv.setEmail(s.email());
                needUpdate = true;
            }
            if (nv.getNguoiDung() == null || !nv.getNguoiDung().getMaNd().equals(nd.getMaNd())) {
                nv.setNguoiDung(nd);
                needUpdate = true;
            }
            if (needUpdate) {
                nhanVienRepository.save(nv);
            }
        }
    }

    private void initSamplePendingOrders() {
        TrangThaiDH tt01 = trangThaiDHRepository.findById("TT01")
                .orElseGet(() -> trangThaiDHRepository.save(new TrangThaiDH("TT01", "Mới tạo")));

        KhachHang kh1 = khachHangRepository.findById("KH001").orElse(null);
        KhachHang kh2 = khachHangRepository.findById("KH002").orElse(null);
        if (kh1 == null || kh2 == null) return;

        record OrderSeed(String maDh, String maKhoGui, String maKhoNhan, String nguoiNhan, String sdt, String diaChiLay, String diaChiGiao, BigDecimal phi, BigDecimal cod) {}
        List<OrderSeed> list = List.of(
                new OrderSeed("DH_TD01", "KHO01", "KHO02", "Nguyễn Thu Thủy", "0912345671", "12 Võ Văn Ngân, Thủ Đức, TP.HCM", "25 Trần Thái Tông, Cầu Giấy, Hà Nội", new BigDecimal("35000"), new BigDecimal("250000")),
                new OrderSeed("DH_TD02", "KHO01", "KHO03", "Trần Văn Toàn", "0912345672", "45 Kha Vạn Cân, Thủ Đức, TP.HCM", "45 Nguyễn Văn Linh, Hải Châu, Đà Nẵng", new BigDecimal("30000"), BigDecimal.ZERO),
                new OrderSeed("DH_CG01", "KHO02", "KHO01", "Phạm Văn Bình", "0912345673", "25 Trần Thái Tông, Cầu Giấy, Hà Nội", "12 Võ Văn Ngân, Thủ Đức, TP.HCM", new BigDecimal("35000"), new BigDecimal("500000")),
                new OrderSeed("DH_DN01", "KHO03", "KHO01", "Lê Thị Bích", "0912345674", "45 Nguyễn Văn Linh, Đà Nẵng", "12 Võ Văn Ngân, Thủ Đức, TP.HCM", new BigDecimal("30000"), new BigDecimal("120000")),
                new OrderSeed("DH_CT01", "KHO04", "KHO01", "Hoàng Văn Nam", "0912345675", "88 Nguyễn Trãi, Ninh Kiều, Cần Thơ", "125 Hai Bà Trưng, Quận 1, TP.HCM", new BigDecimal("25000"), BigDecimal.ZERO),
                new OrderSeed("DH_Q1_01", "KHO05", "KHO02", "Đặng Thị Hoa", "0912345676", "125 Hai Bà Trưng, Quận 1, TP.HCM", "182 Xã Đàn, Đống Đa, Hà Nội", new BigDecimal("35000"), new BigDecimal("300000"))
        );

        for (var o : list) {
            if (!donHangRepository.existsById(o.maDh())) {
                DonHang dh = DonHang.builder()
                        .maDh(o.maDh())
                        .maKhGui(kh1.getMaKh())
                        .maKhNhan(kh2.getMaKh())
                        .sdtNhan(o.sdt())
                        .tenNguoiNhan(o.nguoiNhan())
                        .diaChiLay(o.diaChiLay())
                        .diaChiGiao(o.diaChiGiao())
                        .maKhoGui(o.maKhoGui())
                        .maKhoNhan(o.maKhoNhan())
                        .ngayTao(LocalDateTime.now().minusHours(2))
                        .phiVanChuyen(o.phi())
                        .cod(o.cod())
                        .trangThai(tt01)
                        .build();
                dh = donHangRepository.save(dh);

                String maKien = "KI_" + o.maDh();
                if (!kienHangRepository.existsById(maKien)) {
                    KienHang kien = KienHang.builder()
                            .maKien(maKien)
                            .donHang(dh)
                            .khoiLuong(new BigDecimal("1.50"))
                            .dai(new BigDecimal("20.00"))
                            .rong(new BigDecimal("15.00"))
                            .cao(new BigDecimal("10.00"))
                            .loaiHang("Hàng tiêu dùng")
                            .build();
                    kienHangRepository.save(kien);
                }
                log.info("Tạo đơn hàng chờ tiếp nhận mẫu: {} tại bưu cục {}", o.maDh(), o.maKhoGui());
            }
        }
    }
}
