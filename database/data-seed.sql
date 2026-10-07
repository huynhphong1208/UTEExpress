-- ====================================================================
-- SCRIPT DỮ LIỆU MẪU CHO PHÂN HỆ BƯU CỤC, TÀI CHÍNH & BÁO CÁO (UTEEXPRESS)
-- Dùng để nạp vào PostgreSQL và test trực tiếp toàn bộ Swagger API
-- ====================================================================

-- 0. GIẢI PHÓNG TRANSACTION CŨ NẾU PHIÊN TRƯỚC ĐÓ BỊ LỖI
ROLLBACK;

-- 1. TẮT TẠM THỜI CÁC TRIGGER RÀNG BUỘC ĐỂ NẠP DỮ LIỆU MẪU
SET session_replication_role = 'replica';

BEGIN;

-- 2. DANH MỤC VAI TRÒ
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('ADMIN',      'Quản trị viên',       'Quản trị toàn bộ hệ thống'),
('NVBC',       'Nhân viên bưu cục',   'Tiếp nhận và xử lý đơn tại bưu cục'),
('NVDP',       'Nhân viên điều phối', 'Điều phối chuyến xe liên kho'),
('TAI_XE',     'Tài xế giao nhận',    'Thực hiện vận chuyển và giao hàng'),
('KHACH_HANG', 'Khách hàng',          'Người gửi và người nhận')
ON CONFLICT (ma_vai_tro) DO NOTHING;

-- 3. TRẠNG THÁI ĐƠN HÀNG
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

-- 4. QUY TRÌNH CHUYỂN TRẠNG THÁI
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

-- 5. BƯU CỤC / KHO HÀNG (2 kho mẫu theo yêu cầu)
INSERT INTO kho (ma_kho, ten_kho, dia_chi, sdt) VALUES
('KHO01', 'Bưu cục Thủ Đức', '12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM', '0281111111'),
('KHO02', 'Bưu cục Cầu Giấy', '25 Trần Thái Tông, Cầu Giấy, Hà Nội', '0242222222')
ON CONFLICT (ma_kho) DO UPDATE SET ten_kho = EXCLUDED.ten_kho, dia_chi = EXCLUDED.dia_chi, sdt = EXCLUDED.sdt;

-- Tuyến vận chuyển giữa 2 kho
INSERT INTO tuyen_van_chuyen (ma_tuyen, ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES
('TU01', 'KHO01', 'KHO02', 1700, 36),
('TU02', 'KHO02', 'KHO01', 1700, 36)
ON CONFLICT (ma_tuyen) DO NOTHING;

-- 6. TÀI KHOẢN NGƯỜI DÙNG PHỤC VỤ TEST
INSERT INTO nguoi_dung (ma_nd, ten_dang_nhap, mat_khau, ma_vai_tro, trang_thai) VALUES
('ND001', 'admin',  '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi0qM5B.pU/4GZ3Y41N7F/2kQc5Xl6K', 'ADMIN',      'Hoạt động'),
('ND002', 'nvbc01', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi0qM5B.pU/4GZ3Y41N7F/2kQc5Xl6K', 'NVBC',       'Hoạt động'),
('ND007', '0901000001', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi0qM5B.pU/4GZ3Y41N7F/2kQc5Xl6K', 'KHACH_HANG', 'Hoạt động')
ON CONFLICT (ma_nd) DO NOTHING;

-- 7. NHÂN VIÊN BƯU CỤC
INSERT INTO nhan_vien (ma_nv, ho_ten, chuc_vu, sdt, email, ma_kho, ma_nd) VALUES
('NV001', 'Phạm Hoàng Nam', 'Nhân viên bưu cục', '0902000001', 'nam@uteexpress.vn', 'KHO01', 'ND002')
ON CONFLICT (ma_nv) DO NOTHING;

-- 8. KHÁCH HÀNG GỬI / NHẬN
INSERT INTO khach_hang (ma_kh, ho_ten, sdt, email, dia_chi_mac_dinh, ma_nd) VALUES
('KH001', 'Nguyễn Văn An', '0901000001', 'an@example.com', '12 Võ Văn Ngân, Thủ Đức, TP.HCM', 'ND007'),
('KH002', 'Trần Thị Bích', '0901000002', 'bich@example.com', '34 Lê Lợi, Cầu Giấy, Hà Nội', NULL)
ON CONFLICT (ma_kh) DO NOTHING;

-- 9. BA ĐƠN HÀNG MẪU (3 ĐƠN TƯỢNG TRƯNG CHO CÁC KỊCH BẢN TEST)
-- Đơn 1: DH000001 - Mới tạo (TT01) tại KHO01 -> Dùng để test API tiếp nhận đơn (UC08)
INSERT INTO don_hang (ma_dh, ma_kh_gui, ma_kh_nhan, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao,
                      ma_kho_gui, ma_kho_nhan, ngay_tao, phi_van_chuyen, cod, ma_tt, ma_nv)
VALUES ('DH000001', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
        '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
        'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '2 hours', 35000, 150000, 'TT01', NULL)
ON CONFLICT (ma_dh) DO UPDATE SET ma_tt = EXCLUDED.ma_tt, cod = EXCLUDED.cod, phi_van_chuyen = EXCLUDED.phi_van_chuyen;

-- Đơn 2: DH000002 - Đã tiếp nhận (TT02) -> Dùng để test API chuyển kho (UC09) và đổi trạng thái (UC10)
INSERT INTO don_hang (ma_dh, ma_kh_gui, ma_kh_nhan, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao,
                      ma_kho_gui, ma_kho_nhan, ngay_tao, phi_van_chuyen, cod, ma_tt, ma_nv)
VALUES ('DH000002', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
        '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
        'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '1 day', 45000, 300000, 'TT02', 'NV001')
ON CONFLICT (ma_dh) DO UPDATE SET ma_tt = EXCLUDED.ma_tt, cod = EXCLUDED.cod, phi_van_chuyen = EXCLUDED.phi_van_chuyen;

-- Đơn 3: DH000003 - Giao thành công (TT06) -> Dùng để test API thanh toán, đối soát COD và Báo cáo (UC06, UC07, UC17)
INSERT INTO don_hang (ma_dh, ma_kh_gui, ma_kh_nhan, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao,
                      ma_kho_gui, ma_kho_nhan, ngay_tao, phi_van_chuyen, cod, ma_tt, ma_nv)
VALUES ('DH000003', 'KH001', 'KH002', '0901000002', 'Trần Thị Bích',
        '12 Võ Văn Ngân, Thủ Đức, TP.HCM', '25 Trần Thái Tông, Cầu Giấy, Hà Nội',
        'KHO01', 'KHO02', CURRENT_TIMESTAMP - INTERVAL '2 days', 50000, 500000, 'TT06', 'NV001')
ON CONFLICT (ma_dh) DO UPDATE SET ma_tt = EXCLUDED.ma_tt, cod = EXCLUDED.cod, phi_van_chuyen = EXCLUDED.phi_van_chuyen;

-- 10. KIỆN HÀNG MẪU CHO ĐƠN 2 VÀ ĐƠN 3
INSERT INTO kien_hang (ma_kien, ma_dh, khoi_luong, dai, rong, cao, loai_hang, ma_kho_hien_tai) VALUES
('KIEN000002', 'DH000002', 2.0, 30.0, 20.0, 15.0, 'Đồ gia dụng', 'KHO01'),
('KIEN000003', 'DH000003', 3.5, 40.0, 25.0, 20.0, 'Thiết bị điện tử', 'KHO02')
ON CONFLICT (ma_kien) DO UPDATE SET ma_kho_hien_tai = EXCLUDED.ma_kho_hien_tai;

-- 11. GIAO DỊCH THANH TOÁN MẪU (khớp chính xác ck_tt_pt: 'Tiền mặt'|'Chuyển khoản'|'COD' và ck_tt_trang_thai: 'Đã thanh toán')
INSERT INTO thanh_toan (ma_tt_toan, ma_dh, loai_khoan, so_tien, phuong_thuc, trang_thai, thoi_gian, nguoi_thanh_toan) VALUES
('GD000001', 'DH000003', 'PHI_VC', 50000,  'Chuyển khoản', 'Đã thanh toán', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Nguyễn Văn An'),
('GD000002', 'DH000003', 'COD',    500000, 'Tiền mặt',     'Đã thanh toán', CURRENT_TIMESTAMP - INTERVAL '3 hours','Trần Thị Bích')
ON CONFLICT (ma_tt_toan) DO NOTHING;

-- 12. LỊCH SỬ TRẠNG THÁI MẪU
INSERT INTO lich_su_trang_thai (ma_ls, ma_dh, ma_tt, thoi_gian, ma_nd, ghi_chu) VALUES
('LS00000001', 'DH000001', 'TT01', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'ND007', 'Khách hàng tạo đơn hàng qua ứng dụng'),
('LS00000002', 'DH000002', 'TT01', CURRENT_TIMESTAMP - INTERVAL '1 day',   'ND007', 'Khách hàng tạo đơn hàng'),
('LS00000003', 'DH000002', 'TT02', CURRENT_TIMESTAMP - INTERVAL '20 hours','ND002', 'Bưu cục Thủ Đức tiếp nhận và lập kiện KIEN000002'),
('LS00000004', 'DH000003', 'TT06', CURRENT_TIMESTAMP - INTERVAL '3 hours', 'ND002', 'Giao hàng và thu COD thành công')
ON CONFLICT (ma_ls) DO NOTHING;

COMMIT;

-- 13. BẬT LẠI HOẠT ĐỘNG TRIGGER BÌNH THƯỜNG
SET session_replication_role = 'origin';
