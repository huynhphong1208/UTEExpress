-- ====================================================================
-- UTEEXPRESS - TOÀN BỘ DỮ LIỆU MẪU & TEST TỔNG HỢP (SEED_AND_TEST.SQL)
-- Bao gồm:
--   PHẦN 1: Dữ liệu danh mục hệ thống (10 Bưu cục, 20 Tuyến, 20 Tài khoản,
--            12 Nhân viên chuẩn hóa 1-1, 5 Tài xế, 7 Phương tiện).
--   PHẦN 2: Bộ Test Suite 38 ca kiểm thử PL/pgSQL (Ràng buộc, Nghiệp vụ, View).
--   PHẦN 3: Dữ liệu vận hành mẫu chuyên sâu (Đơn hàng, Kiện hàng, Chuyến xe,
--            Thanh toán, Lịch sử), đặc biệt mỗi NVBC 1, 2, 3 có 7 đơn mới tạo.
-- Chạy SAU: schema.sql -> logic.sql
-- ====================================================================

-- =========================================================
-- PHẦN 1. DANH MỤC CƠ SỞ & MASTER DATA
-- =========================================================

-- 1. VAI TRÒ HỆ THỐNG
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('ADMIN',      'Quản trị viên',       'Quản trị toàn bộ hệ thống'),
('NVBC',       'Nhân viên bưu cục',   'Tiếp nhận và xử lý đơn tại bưu cục'),
('NVDP',       'Nhân viên điều phối', 'Điều phối chuyến xe liên kho và giao cuối'),
('TAI_XE',     'Tài xế giao nhận',    'Thực hiện vận chuyển liên kho và giao hàng'),
('KHACH_HANG', 'Khách hàng',          'Người gửi và người nhận')
ON CONFLICT (ma_vai_tro) DO NOTHING;

-- 2. TRẠNG THÁI ĐƠN HÀNG
INSERT INTO trang_thai_dh (ma_trang_thai, ten_trang_thai) VALUES
('TT01', 'Mới tạo'),
('TT02', 'Đã tiếp nhận'),
('TT03', 'Đang vận chuyển'),
('TT04', 'Đã đến kho'),
('TT05', 'Đang giao hàng'),
('TT06', 'Giao thành công'),
('TT07', 'Hủy / Trả hàng'),
('TT08', 'Giao thất bại')
ON CONFLICT (ma_trang_thai) DO NOTHING;

-- 3. QUY TRÌNH CHUYỂN TRẠNG THÁI
INSERT INTO quy_trinh_trang_thai (tt_tu, tt_den) VALUES
('TT01','TT02'),
('TT02','TT03'),
('TT03','TT04'),
('TT04','TT03'),
('TT04','TT05'),
('TT05','TT06'),
('TT05','TT08'),
('TT08','TT05'),
('TT02','TT05'),
('TT01','TT07'),
('TT02','TT07'),
('TT03','TT07'),
('TT04','TT07'),
('TT05','TT07'),
('TT08','TT07')
ON CONFLICT (tt_tu, tt_den) DO NOTHING;

-- 4. DANH MỤC KHO & BƯU CỤC (10 Bưu cục toàn quốc)
INSERT INTO kho (ma_kho, ten_kho, dia_chi, sdt) VALUES
('KHO01', 'Bưu cục Thủ Đức',          '12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM',       '0281111111'),
('KHO02', 'Bưu cục Cầu Giấy',         '25 Trần Thái Tông, Cầu Giấy, Hà Nội',       '0242222222'),
('KHO03', 'Bưu cục Đà Nẵng',          '45 Nguyễn Văn Linh, Hải Châu, Đà Nẵng',     '0236333333'),
('KHO04', 'Bưu cục Cần Thơ',          '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ',        '0292444444'),
('KHO05', 'Bưu cục Quận 1',           '125 Hai Bà Trưng, Quận 1, TP.HCM',          '0283555555'),
('KHO06', 'Bưu cục Đống Đa',          '182 Xã Đàn, Đống Đa, Hà Nội',               '0243666666'),
('KHO07', 'Bưu cục Hải Phòng',        '68 Lạch Tray, Ngô Quyền, Hải Phòng',        '0225777777'),
('KHO08', 'Bưu cục Biên Hòa',         '54 Nguyễn Ái Quốc, Biên Hòa, Đồng Nai',     '0251888888'),
('KHO09', 'Bưu cục Nha Trang',        '72 Trần Phú, Nha Trang, Khánh Hòa',         '0258999999'),
('KHO10', 'Bưu cục Buôn Ma Thuột',    '15 Lê Duẩn, Buôn Ma Thuột, Đắk Lắk',        '0262101010')
ON CONFLICT (ma_kho) DO UPDATE 
SET ten_kho = EXCLUDED.ten_kho, dia_chi = EXCLUDED.dia_chi, sdt = EXCLUDED.sdt;

-- 5. TUYẾN VẬN CHUYỂN LIÊN KHO CHÍNH & VỆ TINH
INSERT INTO tuyen_van_chuyen (ma_tuyen, ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES
('TU01', 'KHO01', 'KHO03', 960, 18),
('TU02', 'KHO03', 'KHO01', 960, 18),
('TU03', 'KHO03', 'KHO02', 770, 14),
('TU04', 'KHO01', 'KHO04', 170, 4),
('TU05', 'KHO04', 'KHO01', 170, 4),
('TU06', 'KHO01', 'KHO02', 1700, 36),
('TU07', 'KHO02', 'KHO01', 1700, 36),
('TU08', 'KHO02', 'KHO03', 770, 14),
('TU09', 'KHO01', 'KHO05', 15, 1),
('TU10', 'KHO05', 'KHO01', 15, 1),
('TU11', 'KHO01', 'KHO08', 25, 1.5),
('TU12', 'KHO08', 'KHO01', 25, 1.5),
('TU13', 'KHO02', 'KHO06', 8, 0.5),
('TU14', 'KHO06', 'KHO02', 8, 0.5),
('TU15', 'KHO02', 'KHO07', 120, 2.5),
('TU16', 'KHO07', 'KHO02', 120, 2.5),
('TU17', 'KHO01', 'KHO09', 430, 9),
('TU18', 'KHO09', 'KHO01', 430, 9),
('TU19', 'KHO01', 'KHO10', 350, 8),
('TU20', 'KHO10', 'KHO01', 350, 8)
ON CONFLICT (ma_tuyen) DO UPDATE 
SET khoang_cach = EXCLUDED.khoang_cach, thoi_gian_du_kien_gio = EXCLUDED.thoi_gian_du_kien_gio;

-- 6. TÀI KHOẢN NGƯỜI DÙNG (Mật khẩu mã hóa BCrypt cho '123456@Ute')
INSERT INTO nguoi_dung (ma_nd, ten_dang_nhap, mat_khau, ma_vai_tro, trang_thai) VALUES
-- Quản trị viên
('ND001', 'admin',      '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'ADMIN',      'Hoạt động'),
-- 10 Nhân viên bưu cục (Khớp chuẩn 1-1 với KHO01 -> KHO10)
('ND002', 'nvbc01',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND013', 'nvbc02',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND003', 'nvbc03',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND014', 'nvbc04',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND015', 'nvbc05',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND016', 'nvbc06',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND017', 'nvbc07',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND018', 'nvbc08',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND019', 'nvbc09',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
('ND020', 'nvbc10',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVBC',       'Hoạt động'),
-- Điều phối viên (Dispatcher)
('ND004', 'nvdp01',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVDP',       'Hoạt động'),
('ND009', 'nvdp02',     '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'NVDP',       'Hoạt động'),
-- Đội ngũ Tài xế (Driver)
('ND005', 'tx01',       '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'TAI_XE',     'Hoạt động'),
('ND006', 'tx02',       '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'TAI_XE',     'Hoạt động'),
('ND010', 'tx03',       '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'TAI_XE',     'Hoạt động'),
('ND011', 'tx04',       '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'TAI_XE',     'Hoạt động'),
('ND012', 'tx05',       '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'TAI_XE',     'Hoạt động'),
-- Khách hàng
('ND007', '0901000001', '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'KHACH_HANG', 'Hoạt động'),
('ND008', '0901000002', '$2a$10$BaxCyj/IY6CxGzc7aG2WauO0rOSnphOXftgCMNQDjMt2R24eUOgAi', 'KHACH_HANG', 'Hoạt động')
ON CONFLICT (ma_nd) DO UPDATE 
SET mat_khau = EXCLUDED.mat_khau, trang_thai = EXCLUDED.trang_thai;

-- 7. KHÁCH HÀNG MẪU
INSERT INTO khach_hang (ma_kh, ho_ten, sdt, email, dia_chi_mac_dinh, ma_nd) VALUES
('KH001', 'Nguyễn Văn An', '0901000001', 'an@example.com',     '12 Võ Văn Ngân, Thủ Đức, TP.HCM', 'ND007'),
('KH002', 'Trần Thị Bích', '0901000002', 'bich@example.com',   '34 Lê Lợi, Quận 1, TP.HCM',       'ND008'),
('KH003', 'Lê Văn Cường',  '0901000003', 'cuong@example.com',  '45 Nguyễn Văn Linh, Đà Nẵng',     NULL)
ON CONFLICT (ma_kh) DO UPDATE 
SET ho_ten = EXCLUDED.ho_ten, email = EXCLUDED.email;

-- 8. NHÂN VIÊN (Chuẩn hóa 1-1 chính xác: NV001..NV010 cho KHO01..KHO10)
INSERT INTO nhan_vien (ma_nv, ho_ten, chuc_vu, sdt, email, ma_kho, ma_nd) VALUES
('NV001', 'Phạm Hoàng Nam',   'Nhân viên bưu cục',   '0902000001', 'nam@uteexpress.vn',   'KHO01', 'ND002'),
('NV002', 'Trần Quang Minh',  'Nhân viên bưu cục',   '0902000002', 'minh@uteexpress.vn',  'KHO02', 'ND013'),
('NV003', 'Võ Thị Lan',       'Nhân viên bưu cục',   '0902000003', 'lan@uteexpress.vn',   'KHO03', 'ND003'),
('NV004', 'Lê Thị Mai',       'Nhân viên bưu cục',   '0902000004', 'mai@uteexpress.vn',   'KHO04', 'ND014'),
('NV005', 'Nguyễn Văn Hùng',  'Nhân viên bưu cục',   '0902000005', 'hung@uteexpress.vn',  'KHO05', 'ND015'),
('NV006', 'Hoàng Thị Yến',    'Nhân viên bưu cục',   '0902000006', 'yen@uteexpress.vn',   'KHO06', 'ND016'),
('NV007', 'Đặng Hữu Phúc',    'Nhân viên bưu cục',   '0902000007', 'phuc@uteexpress.vn',  'KHO07', 'ND017'),
('NV008', 'Bùi Thị Thu',      'Nhân viên bưu cục',   '0902000008', 'thu@uteexpress.vn',   'KHO08', 'ND018'),
('NV009', 'Vũ Anh Tuấn',      'Nhân viên bưu cục',   '0902000009', 'tuan@uteexpress.vn',  'KHO09', 'ND019'),
('NV010', 'Phan Thị Nga',     'Nhân viên bưu cục',   '0902000010', 'nga@uteexpress.vn',   'KHO10', 'ND020'),
('NV011', 'Đặng Quốc Huy',    'Nhân viên điều phối', '0902000011', 'huy@uteexpress.vn',   'KHO01', 'ND004'),
('NV012', 'Nguyễn Thanh Tùng', 'Nhân viên điều phối', '0902000012', 'tung@uteexpress.vn',  'KHO03', 'ND009')
ON CONFLICT (ma_nv) DO UPDATE 
SET ho_ten = EXCLUDED.ho_ten, sdt = EXCLUDED.sdt, email = EXCLUDED.email, ma_kho = EXCLUDED.ma_kho, ma_nd = EXCLUDED.ma_nd;

-- 9. TÀI XẾ (5 tài xế với đủ các hạng bằng lái B2, C, E)
INSERT INTO tai_xe (ma_tx, ho_ten, sdt, bang_lai, ma_nd) VALUES
('TX001', 'Huỳnh Văn Tài',  '0903000001', 'C',  'ND005'),
('TX002', 'Ngô Minh Đức',   '0903000002', 'E',  'ND006'),
('TX003', 'Lê Hoàng Long',  '0903000003', 'C',  'ND010'),
('TX004', 'Trần Quốc Toản', '0903000004', 'B2', 'ND011'),
('TX005', 'Nguyễn Văn Dũng','0903000005', 'E',  'ND012')
ON CONFLICT (ma_tx) DO UPDATE 
SET ho_ten = EXCLUDED.ho_ten, sdt = EXCLUDED.sdt, bang_lai = EXCLUDED.bang_lai;

-- 10. PHƯƠNG TIỆN VẬN TẢI (Xe tải & Xe máy)
INSERT INTO phuong_tien (ma_pt, bien_so, loai_xe, tai_trong, trang_thai) VALUES
('PT001', '51C-123.45',  'Xe tải', 1500, 'Đang chạy'),
('PT002', '51D-678.90',  'Xe tải', 5000, 'Sẵn sàng'),
('PT003', '59H1-111.22', 'Xe máy',   30, 'Sẵn sàng'),
('PT004', '51E-999.88',  'Xe tải', 8000, 'Sẵn sàng'),
('PT005', '51C-456.78',  'Xe tải', 2500, 'Đang chạy'),
('PT006', '59H1-888.99', 'Xe máy',   30, 'Sẵn sàng'),
('PT007', '43C-333.44',  'Xe tải', 3500, 'Sẵn sàng')
ON CONFLICT (ma_pt) DO UPDATE 
SET bien_so = EXCLUDED.bien_so, tai_trong = EXCLUDED.tai_trong, trang_thai = EXCLUDED.trang_thai;


-- =========================================================
-- PHẦN 2. SCRIPT KIỂM THỬ PL/PGSQL (38 CA TEST TỔNG HỢP)
-- Khối transaction kiểm thử tự động ROLLBACK để không gây nhiễu dữ liệu vận hành.
-- =========================================================
BEGIN;

-- Hàm hỗ trợ test (nằm trong pg_temp, tự động biến mất)
CREATE FUNCTION pg_temp.assert_true(p_ten TEXT, p_dk BOOLEAN) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    IF COALESCE(p_dk, FALSE) THEN RAISE NOTICE '[PASS] %', p_ten;
    ELSE RAISE NOTICE '[FAIL] %', p_ten; END IF;
END $$;

CREATE FUNCTION pg_temp.expect_error(p_ten TEXT, p_sql TEXT) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    BEGIN
        EXECUTE p_sql;
        RAISE NOTICE '[FAIL] % -- lẽ ra phải báo lỗi', p_ten;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '[PASS] % -> %', p_ten, SQLERRM;
    END;
END $$;

-- ---------------------------------------------------------
-- T1. RÀNG BUỘC DỮ LIỆU
-- ---------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE '=== T1. RÀNG BUỘC DỮ LIỆU ===';
    PERFORM pg_temp.expect_error('SĐT khách hàng sai định dạng',
        $q$INSERT INTO khach_hang(ho_ten, sdt) VALUES ('X', '12345')$q$);
    PERFORM pg_temp.expect_error('Trùng SĐT khách hàng',
        $q$INSERT INTO khach_hang(ho_ten, sdt) VALUES ('X', '0901000001')$q$);
    PERFORM pg_temp.expect_error('Tuyến cùng kho đi/đến',
        $q$INSERT INTO tuyen_van_chuyen(ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES ('KHO01','KHO01',10,1)$q$);
    PERFORM pg_temp.expect_error('Tuyến đã tồn tại',
        $q$INSERT INTO tuyen_van_chuyen(ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES ('KHO01','KHO03',960,18)$q$);
    PERFORM pg_temp.expect_error('Khoảng cách tuyến âm',
        $q$INSERT INTO tuyen_van_chuyen(ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES ('KHO02','KHO04',-5,1)$q$);
    PERFORM pg_temp.expect_error('Trạng thái tài khoản sai',
        $q$UPDATE nguoi_dung SET trang_thai = 'Không rõ' WHERE ma_nd = 'ND001'$q$);
    PERFORM pg_temp.expect_error('Tải trọng phương tiện = 0',
        $q$INSERT INTO phuong_tien(bien_so, loai_xe, tai_trong) VALUES ('99X-000.00','Xe tải',0)$q$);
    PERFORM pg_temp.expect_error('Gắn tài khoản TÀI XẾ cho nhân viên bưu cục',
        $q$INSERT INTO nhan_vien(ho_ten, chuc_vu, ma_nd) VALUES ('X','Nhân viên bưu cục','ND005')$q$);
    PERFORM pg_temp.expect_error('COD âm',
        $q$INSERT INTO don_hang(ma_kh_gui, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao, ma_kho_gui, ma_kho_nhan, cod)
           VALUES ('KH001','0901000002','A','a','b','KHO01','KHO03',-1)$q$);
    PERFORM pg_temp.expect_error('Ngày đến dự kiến <= ngày xuất phát',
        $q$INSERT INTO chuyen_giao(loai_chuyen, ma_tuyen, ma_tx, ma_pt, ngay_xuat_phat, ngay_den_du_kien)
           VALUES ('LIEN_KHO','TU01','TX001','PT001', LOCALTIMESTAMP, LOCALTIMESTAMP - INTERVAL '1 hour')$q$);
    PERFORM pg_temp.expect_error('Chuyến liên kho thiếu tuyến',
        $q$INSERT INTO chuyen_giao(loai_chuyen, ma_tx, ma_pt, ngay_xuat_phat, ngay_den_du_kien)
           VALUES ('LIEN_KHO','TX001','PT001', LOCALTIMESTAMP, LOCALTIMESTAMP + INTERVAL '1 hour')$q$);
END $$;

-- ---------------------------------------------------------
-- T2. KỊCH BẢN NGHIỆP VỤ ĐẦU–CUỐI
-- ---------------------------------------------------------
DO $$
DECLARE
    v_kh VARCHAR; v_dh1 VARCHAR; v_dh2 VARCHAR; v_dh3 VARCHAR; v_dh4 VARCHAR;
    v_cg1 VARCHAR; v_cg2 VARCHAR; v_cg3 VARCHAR; v_cg4 VARCHAR; v_cgx VARCHAR;
    v_k1 VARCHAR[]; v_k3 VARCHAR[]; v_k2 VARCHAR; v_tt VARCHAR; v_ma_ttoan VARCHAR; r RECORD;
BEGIN
    ---------------- 1. Đăng ký ----------------
    RAISE NOTICE '--- 1. Đăng ký tài khoản (UC01) ---';
    CALL sp_dang_ky_khach_hang('Phạm Minh Tuấn','0912345678','tuan@example.com','$2a$10$PLACEHOLDER', v_kh);
    PERFORM pg_temp.assert_true('Đăng ký tạo khách hàng + tài khoản vai trò KHACH_HANG',
        EXISTS (SELECT 1 FROM khach_hang k JOIN nguoi_dung n ON n.ma_nd = k.ma_nd
                WHERE k.ma_kh = v_kh AND n.ma_vai_tro = 'KHACH_HANG'));
    PERFORM pg_temp.expect_error('Đăng ký trùng SĐT',
        $q$CALL sp_dang_ky_khach_hang('A','0912345678','a@x.com','h',NULL)$q$);
    CALL sp_dang_ky_khach_hang('Lê Văn Cường','0901000003','cuong@example.com','$2a$10$PLACEHOLDER', v_kh);
    PERFORM pg_temp.assert_true('Đăng ký liên kết vào hồ sơ KH003 (người nhận cũ chưa có tài khoản)',
        v_kh = 'KH003' AND (SELECT ma_nd FROM khach_hang WHERE ma_kh = 'KH003') IS NOT NULL);

    ---------------- 2. Tạo đơn ----------------
    RAISE NOTICE '--- 2. Tạo đơn hàng (UC04) ---';
    CALL sp_tao_don_hang('KH001','Lê Văn Cường','0901000003',
        '12 Võ Văn Ngân, Thủ Đức, TP.HCM','45 Nguyễn Văn Linh, Đà Nẵng','KHO01','KHO03', 500000,
        '[{"khoi_luong":2.5,"dai":30,"rong":20,"cao":15,"loai_hang":"Điện tử"},
          {"khoi_luong":1,"dai":20,"rong":20,"cao":10,"loai_hang":"Quần áo"}]'::jsonb, 'ND007', v_dh1);
    PERFORM pg_temp.assert_true('Đơn 1: phí = 313000 + 305500 = 618500',
        (SELECT phi_van_chuyen FROM don_hang WHERE ma_dh = v_dh1) = 618500);
    PERFORM pg_temp.assert_true('Đơn 1: trạng thái TT01, có 1 dòng lịch sử, ma_kh_nhan = KH003',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT01'
        AND (SELECT COUNT(*) FROM lich_su_trang_thai WHERE ma_dh = v_dh1) = 1
        AND (SELECT ma_kh_nhan FROM don_hang WHERE ma_dh = v_dh1) = 'KH003');

    CALL sp_tao_don_hang('KH002','Nguyễn Thị Mai','0901000001','34 Lê Lợi, Q1, TP.HCM','9 Hùng Vương, Đà Nẵng',
        'KHO01','KHO03', 0, '[{"khoi_luong":50,"dai":60,"rong":40,"cao":40,"loai_hang":"Máy móc"}]'::jsonb, 'ND008', v_dh2);
    CALL sp_tao_don_hang('KH001','Trần Thị Bích','0901000002','12 Võ Văn Ngân, Thủ Đức','34 Lê Lợi, Q1, TP.HCM',
        'KHO01','KHO01', 0, '[{"khoi_luong":1,"dai":20,"rong":20,"cao":10,"loai_hang":"Sách"}]'::jsonb, 'ND007', v_dh3);
    PERFORM pg_temp.assert_true('Đơn 3 (nội bưu cục, 0 km): phí = 17500',
        (SELECT phi_van_chuyen FROM don_hang WHERE ma_dh = v_dh3) = 17500);
    PERFORM pg_temp.expect_error('Tuyến chưa hỗ trợ (KHO04 → KHO03)',
        $q$CALL sp_tao_don_hang('KH001','X','0901000002','a','b','KHO04','KHO03',0,
           '[{"khoi_luong":1,"dai":10,"rong":10,"cao":10}]'::jsonb,'ND007',NULL)$q$);
    PERFORM pg_temp.expect_error('Đơn không có kiện',
        $q$CALL sp_tao_don_hang('KH001','X','0901000002','a','b','KHO01','KHO03',0,'[]'::jsonb,'ND007',NULL)$q$);

    ---------------- 3. Tiếp nhận ----------------
    RAISE NOTICE '--- 3. Tiếp nhận & cập nhật trạng thái (UC08, UC10) ---';
    PERFORM pg_temp.expect_error('Nhảy trạng thái TT01 → TT06',
        format($q$CALL sp_cap_nhat_trang_thai(%L,'TT06','ND002')$q$, v_dh1));
    PERFORM pg_temp.expect_error('NV bưu cục KHO03 tiếp nhận đơn của KHO01',
        format($q$CALL sp_tiep_nhan_don(%L,'NV003','ND003')$q$, v_dh1));
    CALL sp_tiep_nhan_don(v_dh1, 'NV001', 'ND002');
    CALL sp_tiep_nhan_don(v_dh2, 'NV001', 'ND002');
    CALL sp_tiep_nhan_don(v_dh3, 'NV001', 'ND002');
    PERFORM pg_temp.assert_true('Đơn 1 → TT02, kiện nằm ở KHO01, gán NV001',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT02'
        AND NOT EXISTS (SELECT 1 FROM kien_hang WHERE ma_dh = v_dh1 AND ma_kho_hien_tai IS DISTINCT FROM 'KHO01')
        AND (SELECT ma_nv FROM don_hang WHERE ma_dh = v_dh1) = 'NV001');

    ---------------- 4. Chuyến liên kho ----------------
    RAISE NOTICE '--- 4. Chuyến liên kho HCM → Đà Nẵng (UC12) ---';
    CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX001','PT001', LOCALTIMESTAMP + INTERVAL '1 hour','NV011','ND004', NULL, v_cg1);
    PERFORM pg_temp.assert_true('Ngày đến dự kiến = xuất phát + 18h (từ tuyến)',
        (SELECT ngay_den_du_kien - ngay_xuat_phat FROM chuyen_giao WHERE ma_chuyen = v_cg1) = INTERVAL '18 hours');
    PERFORM pg_temp.expect_error('Tài xế TX001 trùng lịch',
        $q$CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX001','PT002', LOCALTIMESTAMP + INTERVAL '2 hours','NV011','ND004',NULL,NULL)$q$);
    PERFORM pg_temp.expect_error('Phương tiện PT001 trùng lịch',
        $q$CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX002','PT001', LOCALTIMESTAMP + INTERVAL '2 hours','NV011','ND004',NULL,NULL)$q$);

    FOR r IN SELECT ma_kien FROM kien_hang WHERE ma_dh = v_dh1 ORDER BY ma_kien LOOP
        CALL sp_gan_kien_vao_chuyen(v_cg1, r.ma_kien, 'ND004');
    END LOOP;
    PERFORM pg_temp.assert_true('Đã gán 2 kiện, tải còn lại = 1500 - 3.5',
        fn_tai_trong_con_lai(v_cg1) = 1496.5);

    -- quá tải: xe máy 30 kg, kiện 50 kg
    CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX002','PT003', LOCALTIMESTAMP + INTERVAL '1 hour','NV011','ND004', NULL, v_cgx);
    SELECT ma_kien INTO v_k2 FROM kien_hang WHERE ma_dh = v_dh2;
    PERFORM pg_temp.expect_error('Kiện 50kg vượt tải xe máy 30kg',
        format($q$CALL sp_gan_kien_vao_chuyen(%L,%L,'ND004')$q$, v_cgx, v_k2));
    CALL sp_huy_chuyen(v_cgx, 'ND004');
    PERFORM pg_temp.assert_true('Hủy chuyến → trạng thái "Đã hủy"',
        (SELECT trang_thai FROM chuyen_giao WHERE ma_chuyen = v_cgx) = 'Đã hủy');

    -- xuất phát
    PERFORM pg_temp.expect_error('Xuất phát khi chưa quét kiện',
        format($q$CALL sp_xuat_phat_chuyen(%L,'ND005')$q$, v_cg1));
    SELECT array_agg(ma_kien ORDER BY ma_kien) INTO v_k1 FROM kien_hang WHERE ma_dh = v_dh1;
    PERFORM pg_temp.expect_error('Tài xế khác (TX002) quét kiện của chuyến TX001',
        format($q$CALL sp_quet_kien(%L,%L,'ND006')$q$, v_cg1, v_k1[1]));
    CALL sp_quet_kien(v_cg1, v_k1[1], 'ND005', 'Nhận kiện điện tử');
    CALL sp_quet_kien(v_cg1, v_k1[2], 'ND005');
    CALL sp_xuat_phat_chuyen(v_cg1, 'ND005');
    PERFORM pg_temp.assert_true('Xuất phát: chuyến "Đang đi", đơn TT03, kiện rời kho, xe "Đang chạy"',
        (SELECT trang_thai FROM chuyen_giao WHERE ma_chuyen = v_cg1) = 'Đang đi'
        AND (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT03'
        AND NOT EXISTS (SELECT 1 FROM kien_hang WHERE ma_dh = v_dh1 AND ma_kho_hien_tai IS NOT NULL)
        AND (SELECT trang_thai FROM phuong_tien WHERE ma_pt = 'PT001') = 'Đang chạy');
    PERFORM pg_temp.expect_error('Sửa địa chỉ giao khi đơn đã vận chuyển',
        format($q$UPDATE don_hang SET dia_chi_giao = 'Địa chỉ khác' WHERE ma_dh = %L$q$, v_dh1));

    CALL sp_hoan_thanh_chuyen(v_cg1, 'ND005');
    PERFORM pg_temp.assert_true('Hoàn thành: đơn TT04, kiện ở KHO03, xe "Sẵn sàng", có ngày đến thực tế',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT04'
        AND NOT EXISTS (SELECT 1 FROM kien_hang WHERE ma_dh = v_dh1 AND ma_kho_hien_tai IS DISTINCT FROM 'KHO03')
        AND (SELECT trang_thai FROM phuong_tien WHERE ma_pt = 'PT001') = 'Sẵn sàng'
        AND (SELECT ngay_den FROM chuyen_giao WHERE ma_chuyen = v_cg1) IS NOT NULL);

    ---------------- 5. Giao cuối đơn 1 (có COD) ----------------
    RAISE NOTICE '--- 5. Giao cuối tại Đà Nẵng + COD (UC06, UC07, UC14) ---';
    CALL sp_tao_chuyen_giao('GIAO_CUOI',NULL,'KHO03','TX002','PT002', LOCALTIMESTAMP + INTERVAL '20 hours',
                            'NV011','ND004', LOCALTIMESTAMP + INTERVAL '24 hours', v_cg2);
    SELECT array_agg(ma_kien) INTO v_k3 FROM kien_hang WHERE ma_dh = v_dh3;
    PERFORM pg_temp.expect_error('Gán kiện của đơn khác kho nhận vào chuyến giao KHO03',
        format($q$CALL sp_gan_kien_vao_chuyen(%L,%L,'ND004')$q$, v_cg2, v_k3[1]));
    CALL sp_gan_kien_vao_chuyen(v_cg2, v_k1[1], 'ND004');
    CALL sp_gan_kien_vao_chuyen(v_cg2, v_k1[2], 'ND004');
    CALL sp_quet_kien(v_cg2, v_k1[1], 'ND006');
    CALL sp_quet_kien(v_cg2, v_k1[2], 'ND006');
    CALL sp_xuat_phat_chuyen(v_cg2, 'ND006');
    PERFORM pg_temp.assert_true('Xuất phát chuyến giao cuối: đơn 1 → TT05 (Đang giao hàng)',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT05');

    -- Giao kiện 1 thành công trước, kiện 2 giao sau -> đơn CHƯA hoàn tất khi còn kiện chưa xử lý
    CALL sp_giao_thanh_cong(v_cg2, v_k1[1], 'ND006', 'Giao thành công lần 1');
    PERFORM pg_temp.assert_true('Kiện 1 đã giao nhưng đơn vẫn TT05 (còn kiện 2 chưa xử lý), chưa thu COD',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT05'
        AND NOT EXISTS (SELECT 1 FROM thanh_toan WHERE ma_dh = v_dh1 AND loai_khoan = 'COD'));

    PERFORM pg_temp.expect_error('Bắt buộc nhập lý do khi giao thất bại',
        format($q$CALL sp_giao_that_bai(%L,%L,'ND006','')$q$, v_cg2, v_k1[2]));
    CALL sp_giao_that_bai(v_cg2, v_k1[2], 'ND006', 'Khách vắng mặt, hẹn giao lại');
    PERFORM pg_temp.assert_true('Kiện 2 giao thất bại → đơn 1 chuyển TT08',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT08');

    CALL sp_hoan_thanh_chuyen(v_cg2, 'ND006');
    PERFORM pg_temp.assert_true('Hoàn thành chuyến giao cuối: kiện thất bại quay về kho giao KHO03, xe rảnh lại',
        (SELECT ma_kho_hien_tai FROM kien_hang WHERE ma_kien = v_k1[2]) = 'KHO03'
        AND (SELECT trang_thai FROM phuong_tien WHERE ma_pt = 'PT002') = 'Sẵn sàng');

    ---------------- 6. Giao lại kiện thất bại ----------------
    RAISE NOTICE '--- 6. Giao lại kiện thất bại + tự thu COD khi giao đủ (UC14, UC06) ---';
    CALL sp_tao_chuyen_giao('GIAO_CUOI',NULL,'KHO03','TX002','PT002', LOCALTIMESTAMP + INTERVAL '48 hours',
                            'NV011','ND004', LOCALTIMESTAMP + INTERVAL '50 hours', v_cg3);
    CALL sp_gan_kien_vao_chuyen(v_cg3, v_k1[2], 'ND004');   -- đơn đang TT08, vẫn hợp lệ để gán lại
    CALL sp_quet_kien(v_cg3, v_k1[2], 'ND006');
    CALL sp_xuat_phat_chuyen(v_cg3, 'ND006');
    PERFORM pg_temp.assert_true('Xuất phát lại: đơn 1 → TT05 (từ TT08)',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT05');

    CALL sp_giao_thanh_cong(v_cg3, v_k1[2], 'ND006', 'Giao lần 2 thành công');
    PERFORM pg_temp.assert_true('Giao đủ tất cả kiện: đơn 1 → TT06, tự tạo khoản thu COD 500000 đã thanh toán',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT06'
        AND (SELECT so_tien FROM thanh_toan WHERE ma_dh = v_dh1 AND loai_khoan = 'COD' AND trang_thai = 'Đã thanh toán') = 500000);
    PERFORM pg_temp.expect_error('Không được thu COD hai lần cho cùng một đơn',
        format($q$INSERT INTO thanh_toan(ma_dh, so_tien, loai_khoan, phuong_thuc, trang_thai)
                  VALUES (%L, 500000, 'COD', 'TIEN_MAT', 'Đã thanh toán')$q$, v_dh1));

    ---------------- 7. Thanh toán phí vận chuyển ----------------
    RAISE NOTICE '--- 7. Thanh toán phí vận chuyển (UC06) ---';
    CALL sp_thanh_toan(v_dh1, 'PHI_VC', 'Chuyển khoản', 'Người gửi', 'ND007', v_ma_ttoan);
    PERFORM pg_temp.assert_true('Sau khi thanh toán phí: v_don_hang_chi_tiet báo "Đã thanh toán đủ"',
        (SELECT tinh_trang_thanh_toan FROM v_don_hang_chi_tiet WHERE ma_dh = v_dh1) = 'Đã thanh toán đủ');

    ---------------- 8. Trigger chặn TT06 khi chưa thu COD ----------------
    RAISE NOTICE '--- 8. Trigger chặn TT06 khi chưa thu COD (đơn nội bộ, không qua chuyến giao cuối) ---';
    CALL sp_tao_don_hang('KH001','X','0901000002','a','b','KHO01','KHO01', 100000,
        '[{"khoi_luong":1,"dai":10,"rong":10,"cao":10}]'::jsonb, 'ND007', v_dh4);
    CALL sp_tiep_nhan_don(v_dh4, 'NV001', 'ND002');
    CALL sp_cap_nhat_trang_thai(v_dh4, 'TT05', 'ND002');
    PERFORM pg_temp.expect_error('Không thể xác nhận giao thành công khi COD > 0 chưa được thu',
        format($q$CALL sp_cap_nhat_trang_thai(%L,'TT06','ND002')$q$, v_dh4));
    CALL sp_thanh_toan(v_dh4, 'COD', 'Tiền mặt', 'Người nhận', 'ND002', v_ma_ttoan);
    CALL sp_cap_nhat_trang_thai(v_dh4, 'TT06', 'ND002');
    PERFORM pg_temp.assert_true('Sau khi thu COD, chuyển TT06 thành công',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh4) = 'TT06');

    ---------------- 9. Bất biến lịch sử trạng thái ----------------
    RAISE NOTICE '--- 9. Lịch sử trạng thái không được sửa/xóa (yêu cầu truy vết) ---';
    SELECT ma_ls INTO v_tt FROM lich_su_trang_thai WHERE ma_dh = v_dh1 LIMIT 1;
    PERFORM pg_temp.expect_error('Sửa bản ghi lịch sử trạng thái',
        format($q$UPDATE lich_su_trang_thai SET ghi_chu = 'sửa' WHERE ma_ls = %L$q$, v_tt));
    PERFORM pg_temp.expect_error('Xóa bản ghi lịch sử trạng thái',
        format($q$DELETE FROM lich_su_trang_thai WHERE ma_ls = %L$q$, v_tt));
    PERFORM pg_temp.assert_true('Đơn 1 có đủ các bước lịch sử',
        (SELECT COUNT(*) FROM lich_su_trang_thai WHERE ma_dh = v_dh1) = 8);

END $$;

-- ---------------------------------------------------------
-- T3. VIEW & THỐNG KÊ
-- ---------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE '=== T3. VIEW & THỐNG KÊ ===';
    PERFORM pg_temp.assert_true('Tổng v_thong_ke_don_theo_trang_thai = tổng số đơn hàng',
        (SELECT SUM(so_don) FROM v_thong_ke_don_theo_trang_thai) = (SELECT COUNT(*) FROM don_hang));
    PERFORM pg_temp.assert_true('v_hieu_suat_tai_xe: TX002 có kiện giao thành công và giao thất bại',
        (SELECT kien_giao_thanh_cong FROM v_hieu_suat_tai_xe WHERE ma_tx = 'TX002') >= 2
        AND (SELECT kien_giao_that_bai FROM v_hieu_suat_tai_xe WHERE ma_tx = 'TX002') >= 1);
    PERFORM pg_temp.assert_true('v_cod_doi_soat: COD đơn 1 đã thu đủ, đúng người thanh toán',
        (SELECT tinh_trang_cod FROM v_cod_doi_soat WHERE ma_dh = (SELECT ma_dh FROM don_hang
            WHERE ma_kh_gui = 'KH001' AND cod = 500000 LIMIT 1)) = 'Đã thu');
    PERFORM pg_temp.assert_true('v_tra_cuu_cong_khai che số điện thoại người nhận',
        (SELECT sdt_nhan_che FROM v_tra_cuu_cong_khai LIMIT 1) LIKE '%*%');
    PERFORM pg_temp.assert_true('v_ton_kho: KHO01 còn ít nhất 1 kiện (đơn 2, đơn 3 chưa vận chuyển)',
        (SELECT so_kien FROM v_ton_kho WHERE ma_kho = 'KHO01') >= 1);
    PERFORM pg_temp.assert_true('fn_ty_le_giao_thanh_cong trả về giá trị 0-100',
        fn_ty_le_giao_thanh_cong(CURRENT_DATE - 7, CURRENT_DATE) BETWEEN 0 AND 100);
    RAISE NOTICE '=== HOÀN TẤT TOÀN BỘ TEST ===';
END $$;

ROLLBACK;  -- Khối Test Suite rollback an toàn, nhường chỗ cho Dữ liệu vận hành bên dưới.


-- ====================================================================
-- PHẦN 3. DỮ LIỆU VẬN HÀNH MẪU CHUYÊN SÂU (OPERATIONAL SEED DATA)
-- Nạp đầy đủ đơn hàng, kiện hàng, chuyến giao, thanh toán & lịch sử.
-- ĐẶC BIỆT: Nhân viên bưu cục 1, 2, 3 mỗi người có 7 đơn MỚI TẠO (TT01) chờ tiếp nhận!
-- ====================================================================

SET session_replication_role = 'replica';

BEGIN;

-- 1. DANH MỤC ĐƠN HÀNG MẪU (39 KỊCH BẢN ĐƠN HÀNG TOÀN DIỆN)
INSERT INTO don_hang (ma_dh, ma_kh_gui, ma_kh_nhan, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao,
                      ma_kho_gui, ma_kho_nhan, ngay_tao, phi_van_chuyen, cod, ma_tt, ma_nv) VALUES

-- [A. BƯU CỤC THỦ ĐỨC (KHO01, NVBC 1: Phạm Hoàng Nam) - 7 ĐƠN CHỜ TIẾP NHẬN (TT01)]
('DH000001', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '3 hours', 35000, 150000, 'TT01', NULL),

('DH000022', 'KH001', 'KH003', '0901000003', 'Lê Văn Cường',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '45 Nguyễn Văn Linh, Đà Nẵng',
 'KHO01', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '2 hours 40 minutes', 45000, 250000, 'TT01', NULL),

('DH000023', 'KH002', 'KH001', '0901000001', 'Nguyễn Văn An',
 '34 Lê Lợi, Quận 1, TP.HCM', '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ',
 'KHO01', 'KHO04', CURRENT_TIMESTAMP - INTERVAL '2 hours 15 minutes', 30000, 0, 'TT01', NULL),

('DH000024', 'KH001', 'KH002', '0901000002', 'Phạm Minh Tuấn',
 '50 Đặng Văn Bi, Thủ Đức, TP.HCM', '125 Hai Bà Trưng, Quận 1, TP.HCM',
 'KHO01', 'KHO05', CURRENT_TIMESTAMP - INTERVAL '1 hour 45 minutes', 22000, 500000, 'TT01', NULL),

('DH000025', 'KH002', 'KH003', '0901000003', 'Lê Hữu Phước',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '54 Nguyễn Ái Quốc, Biên Hòa, Đồng Nai',
 'KHO01', 'KHO08', CURRENT_TIMESTAMP - INTERVAL '1 hour 10 minutes', 25000, 120000, 'TT01', NULL),

('DH000026', 'KH001', 'KH002', '0901000002', 'Nguyễn Thị Hoa',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '72 Trần Phú, Nha Trang, Khánh Hòa',
 'KHO01', 'KHO09', CURRENT_TIMESTAMP - INTERVAL '45 minutes', 55000, 350000, 'TT01', NULL),

('DH000027', 'KH002', 'KH001', '0901000001', 'Trần Văn Nam',
 '34 Lê Lợi, Quận 1, TP.HCM', '15 Lê Duẩn, Buôn Ma Thuột, Đắk Lắk',
 'KHO01', 'KHO10', CURRENT_TIMESTAMP - INTERVAL '20 minutes', 60000, 0, 'TT01', NULL),

-- [B. BƯU CỤC CẦU GIẤY (KHO02, NVBC 2: Trần Quang Minh) - 7 ĐƠN CHỜ TIẾP NHẬN (TT01)]
('DH000018', 'KH002', 'KH001', '0901000001', 'Nguyễn Văn An',
 '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO02', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes', 65000, 200000, 'TT01', NULL),

('DH000028', 'KH002', 'KH003', '0901000003', 'Đặng Quốc Bảo',
 '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '45 Nguyễn Văn Linh, Đà Nẵng',
 'KHO02', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '2 hours 50 minutes', 48000, 300000, 'TT01', NULL),

('DH000029', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
 '10 Duy Tân, Cầu Giấy, Hà Nội', '182 Xã Đàn, Đống Đa, Hà Nội',
 'KHO02', 'KHO06', CURRENT_TIMESTAMP - INTERVAL '2 hours 10 minutes', 18000, 150000, 'TT01', NULL),

('DH000030', 'KH002', 'KH001', '0901000001', 'Phan Văn Hậu',
 '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '68 Lạch Tray, Ngô Quyền, Hải Phòng',
 'KHO02', 'KHO07', CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes', 32000, 420000, 'TT01', NULL),

('DH000031', 'KH001', 'KH003', '0901000003', 'Lê Bích Ngọc',
 '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO02', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '1 hour', 70000, 0, 'TT01', NULL),

('DH000032', 'KH002', 'KH001', '0901000001', 'Vũ Thị Hạnh',
 '15 Phạm Hùng, Cầu Giấy, Hà Nội', '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ',
 'KHO02', 'KHO04', CURRENT_TIMESTAMP - INTERVAL '40 minutes', 80000, 650000, 'TT01', NULL),

('DH000033', 'KH001', 'KH002', '0901000002', 'Hoàng Minh Tuấn',
 '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '100 Hải Phòng, Hải Châu, Đà Nẵng',
 'KHO02', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '15 minutes', 42000, 180000, 'TT01', NULL),

-- [C. BƯU CỤC ĐÀ NẴNG (KHO03, NVBC 3: Võ Thị Lan) - 7 ĐƠN CHỜ TIẾP NHẬN (TT01)]
('DH000019', 'KH003', 'KH002', '0901000002', 'Trần Thị Bích',
 '45 Nguyễn Văn Linh, Đà Nẵng', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO03', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '3 hours 15 minutes', 55000, 0, 'TT01', NULL),

('DH000034', 'KH003', 'KH001', '0901000001', 'Nguyễn Văn An',
 '45 Nguyễn Văn Linh, Đà Nẵng', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO03', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes', 50000, 280000, 'TT01', NULL),

('DH000035', 'KH001', 'KH003', '0901000003', 'Đỗ Thanh Tùng',
 '100 Hải Phòng, Hải Châu, Đà Nẵng', '200 Nguyễn Hữu Thọ, Cẩm Lệ, Đà Nẵng',
 'KHO03', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '2 hours', 18000, 100000, 'TT01', NULL),

('DH000036', 'KH002', 'KH003', '0901000003', 'Phạm Quỳnh Anh',
 '45 Nguyễn Văn Linh, Đà Nẵng', '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ',
 'KHO03', 'KHO04', CURRENT_TIMESTAMP - INTERVAL '1 hour 25 minutes', 62000, 320000, 'TT01', NULL),

('DH000037', 'KH003', 'KH002', '0901000002', 'Lý Gia Hân',
 '75 Bạch Đằng, Hải Châu, Đà Nẵng', '72 Trần Phú, Nha Trang, Khánh Hòa',
 'KHO03', 'KHO09', CURRENT_TIMESTAMP - INTERVAL '50 minutes', 40000, 0, 'TT01', NULL),

('DH000038', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
 '45 Nguyễn Văn Linh, Đà Nẵng', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO03', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 58000, 750000, 'TT01', NULL),

('DH000039', 'KH002', 'KH001', '0901000001', 'Nguyễn Văn An',
 '12 Lê Duẩn, Hải Châu, Đà Nẵng', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO03', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '10 minutes', 52000, 200000, 'TT01', NULL),

-- [D. ĐƠN CHỜ TIẾP NHẬN TẠI CẦN THƠ VÀ QUẬN 1]
('DH000020', 'KH001', 'KH003', '0901000003', 'Phạm Minh Trí',
 '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ', '100 Hải Phòng, Hải Châu, Đà Nẵng',
 'KHO04', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '15 minutes', 70000, 350000, 'TT01', NULL),

('DH000021', 'KH002', 'KH001', '0901000001', 'Vũ Hoàng Yến',
 '34 Lê Lợi, Quận 1, TP.HCM', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO05', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '10 minutes', 25000, 180000, 'TT01', NULL),

-- [E. ĐƠN ĐÃ TIẾP NHẬN & CÁC TRẠNG THÁI KHÁC PHỤC VỤ ĐIỀU PHỐI VÀ TÀI XẾ]
('DH000002', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '1 day', 45000, 300000, 'TT02', 'NV001'),

('DH000003', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '2 days', 50000, 500000, 'TT06', 'NV001'),

('DH000004', 'KH001', 'KH003', '0901000001', 'Nguyễn Thị Hoa',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '100 Hải Phòng, Hải Châu, Đà Nẵng',
 'KHO01', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '4 hours', 701000, 150000, 'TT03', 'NV001'),

('DH000005', 'KH002', 'KH001', '0901000002', 'Trần Văn Nam',
 '34 Lê Lợi, Quận 1, TP.HCM', '20 Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ',
 'KHO01', 'KHO04', CURRENT_TIMESTAMP - INTERVAL '6 hours', 138500, 0, 'TT03', 'NV001'),

('DH000006', 'KH001', 'KH002', '0901000003', 'Lê Hữu Phước',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '50 Đặng Văn Bi, Thủ Đức, TP.HCM',
 'KHO01', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '2 hours', 30000, 80000, 'TT02', 'NV001'),

('DH000007', 'KH002', 'KH003', '0901000001', 'Đỗ Thanh Tùng',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '75 Bạch Đằng, Hải Châu, Đà Nẵng',
 'KHO01', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '1 day', 330500, 250000, 'TT05', 'NV001'),

('DH000008', 'KH001', 'KH003', '0901000002', 'Phạm Quỳnh Anh',
 '34 Lê Lợi, Quận 1, TP.HCM', '12 Lê Duẩn, Hải Châu, Đà Nẵng',
 'KHO01', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '1 day', 310500, 0, 'TT05', 'NV001'),

('DH000009', 'KH003', 'KH001', '0901000001', 'Nguyễn Văn An',
 '45 Nguyễn Văn Linh, Đà Nẵng', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO03', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '5 hours', 425500, 0, 'TT02', 'NV003'),

('DH000010', 'KH002', 'KH001', '0901000002', 'Trần Thị Mai',
 '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ', '34 Lê Lợi, Quận 1, TP.HCM',
 'KHO04', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '5 hours', 153500, 300000, 'TT02', 'NV004'),

('DH000011', 'KH001', 'KH003', '0901000001', 'Hoàng Minh Tuấn',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '150 Hùng Vương, Hải Châu, Đà Nẵng',
 'KHO01', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '1 hour', 120000, 200000, 'TT02', 'NV001'),

('DH000012', 'KH002', 'KH001', '0901000002', 'Vũ Thị Hạnh',
 '34 Lê Lợi, Quận 1, TP.HCM', '45 Mậu Thân, Ninh Kiều, Cần Thơ',
 'KHO01', 'KHO04', CURRENT_TIMESTAMP - INTERVAL '2 hours', 95000, 0, 'TT02', 'NV001'),

('DH000013', 'KH003', 'KH001', '0901000001', 'Lê Bích Ngọc',
 '45 Nguyễn Văn Linh, Đà Nẵng', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO03', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '3 hours', 180000, 150000, 'TT02', 'NV003'),

('DH000014', 'KH003', 'KH002', '0901000002', 'Nguyễn Tiến Dũng',
 '45 Nguyễn Văn Linh, Đà Nẵng', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
 'KHO03', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '4 hours', 210000, 0, 'TT02', 'NV003'),

('DH000015', 'KH002', 'KH001', '0901000002', 'Phan Văn Hậu',
 '88 Nguyễn Trãi, Ninh Kiều, Cần Thơ', '12 Võ Văn Ngân, Thủ Đức, TP.HCM',
 'KHO04', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '2 hours', 85000, 100000, 'TT02', 'NV004'),

('DH000016', 'KH001', 'KH003', '0901000003', 'Đặng Quốc Bảo',
 '100 Hải Phòng, Đà Nẵng', '200 Nguyễn Hữu Thọ, Cẩm Lệ, Đà Nẵng',
 'KHO03', 'KHO03', CURRENT_TIMESTAMP - INTERVAL '1 hour', 25000, 400000, 'TT02', 'NV003'),

('DH000017', 'KH002', 'KH001', '0901000002', 'Lý Gia Hân',
 '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '88 Hoàng Diệu 2, Linh Chiểu, Thủ Đức',
 'KHO01', 'KHO01', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 22000, 120000, 'TT02', 'NV001')
ON CONFLICT (ma_dh) DO UPDATE 
SET ma_tt = EXCLUDED.ma_tt, cod = EXCLUDED.cod, phi_van_chuyen = EXCLUDED.phi_van_chuyen, ma_nv = EXCLUDED.ma_nv;

-- 2. DANH MỤC KIỆN HÀNG MẪU TƯƠNG ỨNG
INSERT INTO kien_hang (ma_kien, ma_dh, khoi_luong, dai, rong, cao, loai_hang, ma_kho_hien_tai) VALUES
('KI0000001', 'DH000001',  2.5, 30, 20, 15, 'Đồ dùng học tập',     'KHO01'),
('KI0000002', 'DH000002',  2.0, 30, 20, 15, 'Đồ gia dụng',          'KHO01'),
('KI0000003', 'DH000003',  3.5, 40, 25, 20, 'Thiết bị điện tử',     'KHO02'),
('KI0000004', 'DH000004', 12.0, 40, 30, 20, 'Thiết bị điện tử',     NULL),
('KI0000005', 'DH000004',  8.0, 30, 25, 15, 'Quần áo thời trang',   NULL),
('KI0000006', 'DH000005', 15.0, 35, 30, 25, 'Đồ gia dụng',          NULL),
('KI0000007', 'DH000006',  3.5, 25, 20, 15, 'Thực phẩm hoa quả',    'KHO01'),
('KI0000008', 'DH000007',  6.0, 30, 20, 15, 'Giày thể thao',        'KHO03'),
('KI0000009', 'DH000008',  2.0, 20, 15, 10, 'Sách và tài liệu',     'KHO03'),
('KI0000010', 'DH000009', 25.0, 50, 40, 30, 'Hải sản khô Đà Nẵng', 'KHO03'),
('KI0000011', 'DH000010', 18.0, 45, 35, 25, 'Trái cây đặc sản',     'KHO04'),
('KI0000012', 'DH000011',  5.0, 25, 20, 15, 'Linh kiện máy tính',   'KHO01'),
('KI0000013', 'DH000011',  3.0, 20, 15, 10, 'Phụ kiện điện tử',     'KHO01'),
('KI0000014', 'DH000012',  4.0, 30, 20, 15, 'Mỹ phẩm dưỡng da',     'KHO01'),
('KI0000015', 'DH000013',  8.0, 35, 25, 20, 'Trà sâm cao cấp',      'KHO03'),
('KI0000016', 'DH000014', 10.0, 40, 30, 25, 'Thủ công mỹ nghệ',     'KHO03'),
('KI0000017', 'DH000015',  6.0, 30, 20, 15, 'Bánh pía Sóc Trăng',    'KHO04'),
('KI0000018', 'DH000016',  4.5, 25, 20, 15, 'Hàng tiêu dùng',       'KHO03'),
('KI0000019', 'DH000017',  1.8, 20, 15, 10, 'Tài liệu hợp đồng',     'KHO01'),
('KI0000020', 'DH000018',  3.0, 30, 20, 15, 'Tài liệu giáo trình',   'KHO02'),
('KI0000021', 'DH000019',  2.5, 25, 20, 15, 'Đặc sản Đà Nẵng',       'KHO03'),
('KI0000022', 'DH000020',  4.0, 35, 25, 20, 'Gạo ST25 Cần Thơ',      'KHO04'),
('KI0000023', 'DH000021',  1.5, 20, 15, 10, 'Phụ kiện máy ảnh',      'KHO05'),

-- Kiện hàng cho 6 đơn mới tạo tại Bưu cục Thủ Đức (KHO01)
('KI0000024', 'DH000022',  3.0, 35, 25, 15, 'Quần áo thời trang',   'KHO01'),
('KI0000025', 'DH000023',  1.5, 25, 20, 10, 'Sách giáo trình',       'KHO01'),
('KI0000026', 'DH000024',  4.2, 30, 30, 25, 'Gốm sứ Bát Tràng',      'KHO01'),
('KI0000027', 'DH000025',  2.0, 20, 15, 10, 'Linh kiện điện tử',     'KHO01'),
('KI0000028', 'DH000026',  5.0, 35, 25, 20, 'Mỹ phẩm chăm sóc da',   'KHO01'),
('KI0000029', 'DH000027',  6.5, 45, 30, 25, 'Dụng cụ thể thao',      'KHO01'),

-- Kiện hàng cho 6 đơn mới tạo tại Bưu cục Cầu Giấy (KHO02)
('KI0000030', 'DH000028',  2.8, 25, 20, 15, 'Đặc sản ô mai Hà Nội',  'KHO02'),
('KI0000031', 'DH000029',  1.2, 30, 22,  5, 'Hồ sơ tài liệu',        'KHO02'),
('KI0000032', 'DH000030',  4.0, 40, 30, 15, 'Trang phục veston nam', 'KHO02'),
('KI0000033', 'DH000031',  5.5, 50, 35, 30, 'Mô hình kiến trúc',     'KHO02'),
('KI0000034', 'DH000032',  7.0, 45, 30, 25, 'Thiết bị livestream',   'KHO02'),
('KI0000035', 'DH000033',  2.2, 25, 20, 15, 'Cà phê hạt Robusta',    'KHO02'),

-- Kiện hàng cho 6 đơn mới tạo tại Bưu cục Đà Nẵng (KHO03)
('KI0000036', 'DH000034',  3.5, 30, 20, 15, 'Chả bò Đà Nẵng',        'KHO03'),
('KI0000037', 'DH000035',  1.0, 20, 15, 10, 'Bánh khô mè Cẩm Lệ',    'KHO03'),
('KI0000038', 'DH000036',  4.8, 35, 25, 20, 'Mực một nắng khô',      'KHO03'),
('KI0000039', 'DH000037',  2.0, 25, 20, 15, 'Trầm hương tinh dầu',   'KHO03'),
('KI0000040', 'DH000038',  6.0, 40, 30, 25, 'Đá mỹ nghệ điêu khắc',  'KHO03'),
('KI0000041', 'DH000039',  3.2, 30, 20, 15, 'Rong biển sấy giòn',    'KHO03')
ON CONFLICT (ma_kien) DO UPDATE 
SET ma_kho_hien_tai = EXCLUDED.ma_kho_hien_tai, khoi_luong = EXCLUDED.khoi_luong;

-- 3. CHUYẾN GIAO MẪU (8 CHUYẾN ĐA DẠNG: ĐANG ĐI, CHƯA ĐI, HOÀN THÀNH)
INSERT INTO chuyen_giao (ma_chuyen, loai_chuyen, ma_tuyen, ma_kho_giao, ma_tx, ma_pt, ma_nv_dieu_phoi,
                         ngay_xuat_phat, ngay_den_du_kien, ngay_xuat_phat_thuc, ngay_den, trang_thai) VALUES
-- Chuyến 1: Chuyến liên kho KHO01 -> KHO03 do TX001 (Huỳnh Văn Tài) lái xe PT001 (Đang đi)
('CG000001', 'LIEN_KHO', 'TU01', NULL, 'TX001', 'PT001', 'NV011',
 CURRENT_TIMESTAMP - INTERVAL '2 hours',
 CURRENT_TIMESTAMP + INTERVAL '16 hours',
 CURRENT_TIMESTAMP - INTERVAL '2 hours',
 NULL,
 'Đang đi'),

-- Chuyến 2: Chuyến giao cuối tại Thủ Đức KHO01 do TX002 (Ngô Minh Đức) lái xe máy PT003 (Chưa đi)
('CG000002', 'GIAO_CUOI', NULL, 'KHO01', 'TX002', 'PT003', 'NV011',
 CURRENT_TIMESTAMP + INTERVAL '30 minutes',
 CURRENT_TIMESTAMP + INTERVAL '4 hours',
 NULL,
 NULL,
 'Chưa đi'),

-- Chuyến 3: Chuyến liên kho ĐÃ HOÀN THÀNH của TX001
('CG000003', 'LIEN_KHO', 'TU01', NULL, 'TX001', 'PT002', 'NV011',
 CURRENT_TIMESTAMP - INTERVAL '3 days',
 CURRENT_TIMESTAMP - INTERVAL '2 days',
 CURRENT_TIMESTAMP - INTERVAL '3 days',
 CURRENT_TIMESTAMP - INTERVAL '2 days',
 'Hoàn thành'),

-- Chuyến 4: Chuyến giao cuối ĐÃ HOÀN THÀNH của TX002
('CG000004', 'GIAO_CUOI', NULL, 'KHO02', 'TX002', 'PT006', 'NV011',
 CURRENT_TIMESTAMP - INTERVAL '2 days',
 CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '3 hours',
 CURRENT_TIMESTAMP - INTERVAL '2 days',
 CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '3 hours',
 'Hoàn thành'),

-- Chuyến 5: Chuyến liên kho KHO01 -> KHO04 do TX003 (Lê Hoàng Long) lái xe PT005 (Đang đi)
('CG000005', 'LIEN_KHO', 'TU04', NULL, 'TX003', 'PT005', 'NV011',
 CURRENT_TIMESTAMP - INTERVAL '1 hour',
 CURRENT_TIMESTAMP + INTERVAL '3 hours',
 CURRENT_TIMESTAMP - INTERVAL '1 hour',
 NULL,
 'Đang đi'),

-- Chuyến 6: Chuyến giao cuối tại KHO03 Đà Nẵng do TX004 (Trần Quốc Toản) lái xe PT007 (Chưa đi)
('CG000006', 'GIAO_CUOI', NULL, 'KHO03', 'TX004', 'PT007', 'NV012',
 CURRENT_TIMESTAMP + INTERVAL '1 hour',
 CURRENT_TIMESTAMP + INTERVAL '5 hours',
 NULL,
 NULL,
 'Chưa đi'),

-- Chuyến 7: Chuyến liên kho KHO03 -> KHO01 do TX005 (Nguyễn Văn Dũng) lái xe PT004 (Chưa đi)
('CG000007', 'LIEN_KHO', 'TU02', NULL, 'TX005', 'PT004', 'NV012',
 CURRENT_TIMESTAMP + INTERVAL '2 hours',
 CURRENT_TIMESTAMP + INTERVAL '20 hours',
 NULL,
 NULL,
 'Chưa đi'),

-- Chuyến 8: Chuyến liên kho kế tiếp cho TX001 (KHO04 -> KHO01)
('CG000008', 'LIEN_KHO', 'TU05', NULL, 'TX001', 'PT001', 'NV011',
 CURRENT_TIMESTAMP + INTERVAL '24 hours',
 CURRENT_TIMESTAMP + INTERVAL '28 hours',
 NULL,
 NULL,
 'Chưa đi')
ON CONFLICT (ma_chuyen) DO UPDATE 
SET trang_thai = EXCLUDED.trang_thai, ngay_den_du_kien = EXCLUDED.ngay_den_du_kien;

-- 4. CHI TIẾT GÁN KIỆN VÀO CHUYẾN GIAO
INSERT INTO chi_tiet_chuyen_giao (ma_chuyen, ma_kien, thoi_gian_gan, thoi_gian_quet, trang_thai, ghi_chu) VALUES
-- Chuyến 1: CG000001 (Liên kho KHO01 -> KHO03, TX001 - xe tải PT001 1.500 kg, Đang đi)
('CG000001', 'KI0000004', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Đã quét', 'Đã xếp lên xe tải 51C-123.45'),
('CG000001', 'KI0000005', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Đã quét', 'Đã xếp lên xe tải 51C-123.45'),

-- Chuyến 2: CG000002 (Giao cuối tại Thủ Đức KHO01, TX002 - xe máy PT003 30 kg, Chưa đi)
('CG000002', 'KI0000007', CURRENT_TIMESTAMP - INTERVAL '30 minutes', NULL, 'Đã gán', 'Chờ tài xế quét lên xe máy giao Thủ Đức'),
('CG000002', 'KI0000019', CURRENT_TIMESTAMP - INTERVAL '30 minutes', NULL, 'Đã gán', 'Chờ tài xế quét lên xe máy giao Thủ Đức'),

-- Chuyến 3: CG000003 (Liên kho KHO01 -> KHO02, TX001 - xe tải PT002, Hoàn thành)
('CG000003', 'KI0000002', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Đã giao', 'Chuyến hoàn tất giao kho KHO02'),
('CG000003', 'KI0000003', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Đã giao', 'Chuyến hoàn tất giao kho KHO02'),

-- Chuyến 4: CG000004 (Giao cuối tại KHO02 Cầu Giấy, TX002, Hoàn thành)
('CG000004', 'KI0000003', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Đã giao', 'Đã giao thành công cho khách tại Cầu Giấy'),

-- Chuyến 5: CG000005 (Liên kho KHO01 -> KHO04 đi Cần Thơ, TX003 - xe tải PT005 2.500 kg, Đang đi)
('CG000005', 'KI0000006', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', 'Đã quét', 'Đang trên xe tải PT005 đi Cần Thơ'),

-- Chuyến 6: CG000006 (Giao cuối tại KHO03 Đà Nẵng, TX004 - xe PT007 3.500 kg, Chưa đi)
('CG000006', 'KI0000008', CURRENT_TIMESTAMP - INTERVAL '10 minutes', NULL, 'Đã gán', 'Chờ tài xế TX004 nhận giao cuối'),
('CG000006', 'KI0000009', CURRENT_TIMESTAMP - INTERVAL '10 minutes', NULL, 'Đã gán', 'Chờ tài xế TX004 nhận giao cuối'),
('CG000006', 'KI0000018', CURRENT_TIMESTAMP - INTERVAL '10 minutes', NULL, 'Đã gán', 'Chờ tài xế TX004 nhận giao cuối'),

-- Chuyến 7: CG000007 (Liên kho KHO03 -> KHO01 về Thủ Đức, TX005 - xe tải PT004 8.000 kg, Chưa đi)
('CG000007', 'KI0000010', CURRENT_TIMESTAMP - INTERVAL '15 minutes', NULL, 'Đã gán', 'Chờ gom hàng chuyến ngược về Thủ Đức'),
('CG000007', 'KI0000015', CURRENT_TIMESTAMP - INTERVAL '15 minutes', NULL, 'Đã gán', 'Chờ gom hàng chuyến ngược về Thủ Đức'),

-- Chuyến 8: CG000008 (Liên kho KHO04 -> KHO01 Cần Thơ về Thủ Đức, TX001 - xe tải PT001 1.500 kg, Chưa đi)
('CG000008', 'KI0000011', CURRENT_TIMESTAMP - INTERVAL '20 minutes', NULL, 'Đã gán', 'Kiện nông sản dự kiến đi ngày mai'),
('CG000008', 'KI0000017', CURRENT_TIMESTAMP - INTERVAL '20 minutes', NULL, 'Đã gán', 'Đặc sản Sóc Trăng đi cùng chuyến')
ON CONFLICT (ma_chuyen, ma_kien) DO NOTHING;

-- 5. LỊCH SỬ THANH TOÁN MẪU
INSERT INTO thanh_toan (ma_tt_toan, ma_dh, so_tien, loai_khoan, phuong_thuc, nguoi_thanh_toan, thoi_gian, trang_thai) VALUES
-- Các giao dịch cho đơn luân chuyển & hoàn thành
('TT000001', 'DH000002',  45000, 'PHI_VC', 'Tiền mặt',      'Người gửi', CURRENT_TIMESTAMP - INTERVAL '1 day',  'Đã thanh toán'),
('TT000002', 'DH000003',  50000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Đã thanh toán'),
('TT000003', 'DH000003', 500000, 'COD',    'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Đã thanh toán'),
('TT000004', 'DH000004', 701000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '4 hours', 'Đã thanh toán'),
('TT000005', 'DH000005', 138500, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '6 hours', 'Đã thanh toán'),

-- Thanh toán cho 7 đơn mới tại Bưu cục Thủ Đức (KHO01, NVBC 1): Chuyển khoản (Đã thanh toán) / Tiền mặt (Chưa thanh toán)
('TT000006', 'DH000001',  35000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '2 hours 55 minutes', 'Đã thanh toán'),
('TT000007', 'DH000022',  45000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '2 hours 40 minutes', 'Chưa thanh toán'),
('TT000008', 'DH000023',  30000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '2 hours 10 minutes', 'Đã thanh toán'),
('TT000009', 'DH000024',  22000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '1 hour 40 minutes',   'Đã thanh toán'),
('TT000010', 'DH000025',  25000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '1 hour 10 minutes',   'Chưa thanh toán'),
('TT000011', 'DH000026',  55000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '40 minutes',          'Đã thanh toán'),
('TT000012', 'DH000027',  60000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '20 minutes',          'Chưa thanh toán'),

-- Thanh toán cho 7 đơn mới tại Bưu cục Cầu Giấy (KHO02, NVBC 2): Chuyển khoản (Đã thanh toán) / Tiền mặt (Chưa thanh toán)
('TT000013', 'DH000018',  65000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '3 hours 25 minutes', 'Đã thanh toán'),
('TT000014', 'DH000028',  48000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '2 hours 45 minutes', 'Đã thanh toán'),
('TT000015', 'DH000029',  18000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '2 hours 10 minutes', 'Chưa thanh toán'),
('TT000016', 'DH000030',  32000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '1 hour 25 minutes',   'Đã thanh toán'),
('TT000017', 'DH000031',  70000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '55 minutes',          'Đã thanh toán'),
('TT000018', 'DH000032',  80000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '40 minutes',          'Chưa thanh toán'),
('TT000019', 'DH000033',  42000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '15 minutes',          'Chưa thanh toán'),

-- Thanh toán cho 7 đơn mới tại Bưu cục Đà Nẵng (KHO03, NVBC 3): Chuyển khoản (Đã thanh toán) / Tiền mặt (Chưa thanh toán)
('TT000020', 'DH000019',  55000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '3 hours 10 minutes', 'Đã thanh toán'),
('TT000021', 'DH000034',  50000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '2 hours 25 minutes', 'Đã thanh toán'),
('TT000022', 'DH000035',  18000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '2 hours',             'Chưa thanh toán'),
('TT000023', 'DH000036',  62000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '1 hour 20 minutes',   'Đã thanh toán'),
('TT000024', 'DH000037',  40000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '50 minutes',          'Chưa thanh toán'),
('TT000025', 'DH000038',  58000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '25 minutes',          'Đã thanh toán'),
('TT000026', 'DH000039',  52000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '10 minutes',          'Chưa thanh toán'),

-- Đơn chờ tiếp nhận tại Cần Thơ & Quận 1
('TT000027', 'DH000020',  70000, 'PHI_VC', 'Chuyển khoản', 'Người gửi', CURRENT_TIMESTAMP - INTERVAL '15 minutes',          'Đã thanh toán'),
('TT000028', 'DH000021',  25000, 'PHI_VC', 'Tiền mặt',      'Người nhận', CURRENT_TIMESTAMP - INTERVAL '10 minutes',          'Chưa thanh toán')
ON CONFLICT (ma_tt_toan) DO NOTHING;

-- 6. LỊCH SỬ TRẠNG THÁI MẪU CHO CÁC ĐƠN HÀNG
INSERT INTO lich_su_trang_thai (ma_ls, ma_dh, ma_tt, thoi_gian, ma_nd, ghi_chu) VALUES
('LS00000001', 'DH000001', 'TT01', CURRENT_TIMESTAMP - INTERVAL '3 hours', 'ND007', 'Khách hàng tạo đơn qua ứng dụng'),
('LS00000002', 'DH000002', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 day',   'ND007', 'Khách hàng tạo đơn qua ứng dụng'),
('LS00000003', 'DH000002', 'TT02', CURRENT_TIMESTAMP - INTERVAL '23 hours','ND002', 'Bưu cục Thủ Đức đã tiếp nhận và cân đo kiện'),
('LS00000004', 'DH000003', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 days',  'ND007', 'Khách hàng tạo đơn qua ứng dụng'),
('LS00000005', 'DH000003', 'TT02', CURRENT_TIMESTAMP - INTERVAL '47 hours','ND002', 'Bưu cục Thủ Đức tiếp nhận đơn'),
('LS00000006', 'DH000003', 'TT03', CURRENT_TIMESTAMP - INTERVAL '45 hours','ND004', 'Chuyến liên kho CG000003 xuất phát'),
('LS00000007', 'DH000003', 'TT04', CURRENT_TIMESTAMP - INTERVAL '30 hours','ND004', 'Kiện hàng đã đến Bưu cục Cầu Giấy'),
('LS00000008', 'DH000003', 'TT05', CURRENT_TIMESTAMP - INTERVAL '26 hours','ND006', 'Tài xế TX002 đang đi giao chặng cuối'),
('LS00000009', 'DH000003', 'TT06', CURRENT_TIMESTAMP - INTERVAL '24 hours','ND006', 'Giao thành công và thu tiền COD'),
('LS00000010', 'DH000004', 'TT01', CURRENT_TIMESTAMP - INTERVAL '4 hours', 'ND007', 'Khách hàng tạo đơn qua ứng dụng'),
('LS00000011', 'DH000004', 'TT02', CURRENT_TIMESTAMP - INTERVAL '3 hours', 'ND002', 'Bưu cục Thủ Đức tiếp nhận 2 kiện'),
('LS00000012', 'DH000004', 'TT03', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'ND004', 'Đã xếp lên chuyến xe liên kho CG000001'),
('LS00000013', 'DH000005', 'TT01', CURRENT_TIMESTAMP - INTERVAL '6 hours', 'ND008', 'Khách hàng tạo đơn qua ứng dụng'),
('LS00000014', 'DH000005', 'TT02', CURRENT_TIMESTAMP - INTERVAL '5 hours', 'ND002', 'Bưu cục Thủ Đức tiếp nhận đơn'),
('LS00000015', 'DH000005', 'TT03', CURRENT_TIMESTAMP - INTERVAL '1 hour',  'ND004', 'Đã xếp lên chuyến xe liên kho CG000005'),
('LS00000016', 'DH000018', 'TT01', CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes', 'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy'),
('LS00000017', 'DH000019', 'TT01', CURRENT_TIMESTAMP - INTERVAL '3 hours 15 minutes', 'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng'),
('LS00000018', 'DH000020', 'TT01', CURRENT_TIMESTAMP - INTERVAL '15 minutes', 'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Cần Thơ'),
('LS00000019', 'DH000021', 'TT01', CURRENT_TIMESTAMP - INTERVAL '10 minutes', 'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Quận 1'),

-- Lịch sử cho 6 đơn mới tạo tại Bưu cục Thủ Đức (KHO01)
('LS00000020', 'DH000022', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours 40 minutes', 'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Đà Nẵng'),
('LS00000021', 'DH000023', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours 15 minutes', 'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Cần Thơ'),
('LS00000022', 'DH000024', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 hour 45 minutes',   'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Quận 1'),
('LS00000023', 'DH000025', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 hour 10 minutes',   'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Biên Hòa'),
('LS00000024', 'DH000026', 'TT01', CURRENT_TIMESTAMP - INTERVAL '45 minutes',          'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Nha Trang'),
('LS00000025', 'DH000027', 'TT01', CURRENT_TIMESTAMP - INTERVAL '20 minutes',          'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Thủ Đức đi Buôn Ma Thuột'),

-- Lịch sử cho 6 đơn mới tạo tại Bưu cục Cầu Giấy (KHO02)
('LS00000026', 'DH000028', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours 50 minutes', 'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Đà Nẵng'),
('LS00000027', 'DH000029', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours 10 minutes', 'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Đống Đa'),
('LS00000028', 'DH000030', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes',   'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Hải Phòng'),
('LS00000029', 'DH000031', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 hour',              'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Thủ Đức'),
('LS00000030', 'DH000032', 'TT01', CURRENT_TIMESTAMP - INTERVAL '40 minutes',          'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Cần Thơ'),
('LS00000031', 'DH000033', 'TT01', CURRENT_TIMESTAMP - INTERVAL '15 minutes',          'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Cầu Giấy đi Đà Nẵng'),

-- Lịch sử cho 6 đơn mới tạo tại Bưu cục Đà Nẵng (KHO03)
('LS00000032', 'DH000034', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes', 'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng đi Thủ Đức'),
('LS00000033', 'DH000035', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours',             'ND008', 'Khách hàng tạo đơn gửi nội thành Đà Nẵng'),
('LS00000034', 'DH000036', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 hour 25 minutes',   'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng đi Cần Thơ'),
('LS00000035', 'DH000037', 'TT01', CURRENT_TIMESTAMP - INTERVAL '50 minutes',          'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng đi Nha Trang'),
('LS00000036', 'DH000038', 'TT01', CURRENT_TIMESTAMP - INTERVAL '30 minutes',          'ND007', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng đi Cầu Giấy'),
('LS00000037', 'DH000039', 'TT01', CURRENT_TIMESTAMP - INTERVAL '10 minutes',          'ND008', 'Khách hàng tạo đơn gửi từ Bưu cục Đà Nẵng đi Thủ Đức')
ON CONFLICT (ma_ls) DO NOTHING;

-- 7. ĐỒNG BỘ CÁC SEQUENCES TRÁNH XUNG ĐỘT KHI TẠO TIẾP
DO $$
BEGIN
    PERFORM setval('seq_dh',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_dh FROM 3)::BIGINT), 1) FROM don_hang WHERE ma_dh ~ '^DH[0-9]{1,6}$'), 50));
    PERFORM setval('seq_kien',   GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_kien FROM 3)::BIGINT), 1) FROM kien_hang WHERE ma_kien ~ '^KI[0-9]{1,7}$'), 60));
    PERFORM setval('seq_chuyen', GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_chuyen FROM 3)::BIGINT), 1) FROM chuyen_giao WHERE ma_chuyen ~ '^CG[0-9]{1,6}$'), 20));
    PERFORM setval('seq_ttoan',  GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_tt_toan FROM 3)::BIGINT), 1) FROM thanh_toan WHERE ma_tt_toan ~ '^(GD|TT)[0-9]{1,6}$'), 50));
    PERFORM setval('seq_ls',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_ls FROM 3)::BIGINT), 1) FROM lich_su_trang_thai WHERE ma_ls ~ '^LS[0-9]{1,8}$'), 50));
    PERFORM setval('seq_tx',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_tx FROM 3)::BIGINT), 1) FROM tai_xe WHERE ma_tx ~ '^TX[0-9]{1,4}$'), 10));
    PERFORM setval('seq_pt',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_pt FROM 3)::BIGINT), 1) FROM phuong_tien WHERE ma_pt ~ '^PT[0-9]{1,4}$'), 10));
    PERFORM setval('seq_nv',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_nv FROM 3)::BIGINT), 1) FROM nhan_vien WHERE ma_nv ~ '^NV[0-9]{1,4}$'), 20));
    PERFORM setval('seq_nd',     GREATEST((SELECT COALESCE(MAX(SUBSTRING(ma_nd FROM 3)::BIGINT), 1) FROM nguoi_dung WHERE ma_nd ~ '^ND[0-9]{1,4}$'), 30));
END $$;

COMMIT;

-- 8. BẬT LẠI TRIGGER BÌNH THƯỜNG
SET session_replication_role = 'origin';

-- THÔNG BÁO HOÀN TẤT
DO $$
BEGIN
    RAISE NOTICE '====================================================================';
    RAISE NOTICE 'NẠP TOÀN BỘ CƠ SỞ DỮ LIỆU SEED_AND_TEST VÀ DỮ LIỆU MẪU MỞ RỘNG THÀNH CÔNG!';
    RAISE NOTICE 'Bao gồm:';
    RAISE NOTICE '- 10 Bưu cục toàn quốc & 20 Tuyến vận chuyển kết nối';
    RAISE NOTICE '- 12 Nhân viên (NV001..NV010 là NVBC tại 10 bưu cục, NV011..NV012 là NVDP)';
    RAISE NOTICE '- 5 Tài xế (tx01 -> tx05) và 7 Phương tiện (xe tải & xe máy)';
    RAISE NOTICE '- 39 Đơn hàng mẫu & 41 Kiện hàng';
    RAISE NOTICE '  + Bưu cục Thủ Đức (KHO01): 7 đơn Mới tạo (TT01) chờ NVBC 1 tiếp nhận';
    RAISE NOTICE '  + Bưu cục Cầu Giấy (KHO02): 7 đơn Mới tạo (TT01) chờ NVBC 2 tiếp nhận';
    RAISE NOTICE '  + Bưu cục Đà Nẵng  (KHO03): 7 đơn Mới tạo (TT01) chờ NVBC 3 tiếp nhận';
    RAISE NOTICE '- 8 Chuyến giao (Đang đi, Chưa đi, Hoàn thành)';
    RAISE NOTICE '- 28 Giao dịch thanh toán & 37 Bản ghi lịch sử trạng thái truy vết';
    RAISE NOTICE '====================================================================';
END $$;
