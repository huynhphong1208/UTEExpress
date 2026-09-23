-- =========================================================
-- PHẦN 1. DỮ LIỆU MẪU (chạy sau schema.sql + logic.sql)
-- Mật khẩu bên dưới chỉ là CHUỖI GIỮ CHỖ - thay bằng hash BCrypt thật do Spring sinh ra.
-- =========================================================
INSERT INTO kho (ma_kho, ten_kho, dia_chi, sdt) VALUES
('KHO01','Bưu cục Thủ Đức','12 Võ Văn Ngân, Thủ Đức, TP.HCM','0281111111'),
('KHO02','Bưu cục Cầu Giấy','25 Trần Thái Tông, Cầu Giấy, Hà Nội','0242222222'),
('KHO03','Bưu cục Đà Nẵng','45 Nguyễn Văn Linh, Hải Châu, Đà Nẵng','0236333333'),
('KHO04','Bưu cục Cần Thơ','88 Nguyễn Trãi, Ninh Kiều, Cần Thơ','0292444444');

INSERT INTO tuyen_van_chuyen (ma_tuyen, ma_kho_di, ma_kho_den, khoang_cach, thoi_gian_du_kien_gio) VALUES
('TU01','KHO01','KHO03',960,18),
('TU02','KHO03','KHO01',960,18),
('TU03','KHO03','KHO02',770,14),
('TU04','KHO01','KHO04',170,4),
('TU05','KHO04','KHO01',170,4);

INSERT INTO nguoi_dung (ma_nd, ten_dang_nhap, mat_khau, ma_vai_tro) VALUES
('ND001','admin','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','ADMIN'),
('ND002','nvbc01','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','NVBC'),
('ND003','nvbc02','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','NVBC'),
('ND004','nvdp01','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','NVDP'),
('ND005','tx01','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','TAI_XE'),
('ND006','tx02','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','TAI_XE'),
('ND007','0901000001','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','KHACH_HANG'),
('ND008','0901000002','$2a$10$PLACEHOLDER_HASH_thay_bang_bcrypt_that_0000000000000','KHACH_HANG');

INSERT INTO khach_hang (ma_kh, ho_ten, sdt, email, dia_chi_mac_dinh, ma_nd) VALUES
('KH001','Nguyễn Văn An','0901000001','an@example.com','12 Võ Văn Ngân, Thủ Đức, TP.HCM','ND007'),
('KH002','Trần Thị Bích','0901000002','bich@example.com','34 Lê Lợi, Quận 1, TP.HCM','ND008'),
('KH003','Lê Văn Cường','0901000003',NULL,'45 Nguyễn Văn Linh, Đà Nẵng',NULL);   -- người nhận chưa có tài khoản

INSERT INTO nhan_vien (ma_nv, ho_ten, chuc_vu, sdt, email, ma_kho, ma_nd) VALUES
('NV001','Phạm Hoàng Nam','Nhân viên bưu cục','0902000001','nam@uteexpress.vn','KHO01','ND002'),
('NV002','Võ Thị Lan','Nhân viên bưu cục','0902000002','lan@uteexpress.vn','KHO03','ND003'),
('NV003','Đặng Quốc Huy','Nhân viên điều phối','0902000003','huy@uteexpress.vn','KHO01','ND004');

INSERT INTO tai_xe (ma_tx, ho_ten, sdt, bang_lai, ma_nd) VALUES
('TX001','Huỳnh Văn Tài','0903000001','C','ND005'),
('TX002','Ngô Minh Đức','0903000002','E','ND006');

INSERT INTO phuong_tien (ma_pt, bien_so, loai_xe, tai_trong) VALUES
('PT001','51C-123.45','Xe tải',1500),
('PT002','51D-678.90','Xe tải',5000),
('PT003','59H1-111.22','Xe máy',30);


-- =========================================================
-- PHẦN 2. SCRIPT TEST  (chạy nguyên cụm; kết quả xem ở tab Messages/NOTICE)
-- Nếu có lỗi chưa bắt được: chạy ROLLBACK; rồi sửa và chạy lại.
-- =========================================================
BEGIN;

-- ---- Hàm hỗ trợ test (nằm trong pg_temp, tự biến mất) ----
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
        format($q$CALL sp_tiep_nhan_don(%L,'NV002','ND003')$q$, v_dh1));
    CALL sp_tiep_nhan_don(v_dh1, 'NV001', 'ND002');
    CALL sp_tiep_nhan_don(v_dh2, 'NV001', 'ND002');
    CALL sp_tiep_nhan_don(v_dh3, 'NV001', 'ND002');
    PERFORM pg_temp.assert_true('Đơn 1 → TT02, kiện nằm ở KHO01, gán NV001',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT02'
        AND NOT EXISTS (SELECT 1 FROM kien_hang WHERE ma_dh = v_dh1 AND ma_kho_hien_tai IS DISTINCT FROM 'KHO01')
        AND (SELECT ma_nv FROM don_hang WHERE ma_dh = v_dh1) = 'NV001');

    ---------------- 4. Chuyến liên kho ----------------
    RAISE NOTICE '--- 4. Chuyến liên kho HCM → Đà Nẵng (UC12) ---';
    CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX001','PT001', LOCALTIMESTAMP + INTERVAL '1 hour','NV003','ND004', NULL, v_cg1);
    PERFORM pg_temp.assert_true('Ngày đến dự kiến = xuất phát + 18h (từ tuyến)',
        (SELECT ngay_den_du_kien - ngay_xuat_phat FROM chuyen_giao WHERE ma_chuyen = v_cg1) = INTERVAL '18 hours');
    PERFORM pg_temp.expect_error('Tài xế TX001 trùng lịch',
        $q$CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX001','PT002', LOCALTIMESTAMP + INTERVAL '2 hours','NV003','ND004',NULL,NULL)$q$);
    PERFORM pg_temp.expect_error('Phương tiện PT001 trùng lịch',
        $q$CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX002','PT001', LOCALTIMESTAMP + INTERVAL '2 hours','NV003','ND004',NULL,NULL)$q$);

    FOR r IN SELECT ma_kien FROM kien_hang WHERE ma_dh = v_dh1 ORDER BY ma_kien LOOP
        CALL sp_gan_kien_vao_chuyen(v_cg1, r.ma_kien, 'ND004');
    END LOOP;
    PERFORM pg_temp.assert_true('Đã gán 2 kiện, tải còn lại = 1500 - 3.5',
        fn_tai_trong_con_lai(v_cg1) = 1496.5);

    -- quá tải: xe máy 30 kg, kiện 50 kg
    CALL sp_tao_chuyen_giao('LIEN_KHO','TU01',NULL,'TX002','PT003', LOCALTIMESTAMP + INTERVAL '1 hour','NV003','ND004', NULL, v_cgx);
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
                            'NV003','ND004', LOCALTIMESTAMP + INTERVAL '24 hours', v_cg2);
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
                            'NV003','ND004', LOCALTIMESTAMP + INTERVAL '50 hours', v_cg3);
    CALL sp_gan_kien_vao_chuyen(v_cg3, v_k1[2], 'ND004');   -- đơn đang TT08, vẫn hợp lệ để gán lại
    CALL sp_quet_kien(v_cg3, v_k1[2], 'ND006');
    CALL sp_xuat_phat_chuyen(v_cg3, 'ND006');
    PERFORM pg_temp.assert_true('Xuất phát lại: đơn 1 → TT05 (từ TT08)',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT05');

    CALL sp_giao_thanh_cong(v_cg3, v_k1[2], 'ND006', 'Giao thành công lần 2');
    PERFORM pg_temp.assert_true('Giao đủ tất cả kiện: đơn 1 → TT06, tự tạo khoản thu COD 500000 đã thanh toán',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh1) = 'TT06'
        AND EXISTS (SELECT 1 FROM thanh_toan WHERE ma_dh = v_dh1 AND loai_khoan = 'COD'
                    AND so_tien = 500000 AND trang_thai = 'Đã thanh toán'));
    PERFORM pg_temp.expect_error('Không được thu COD hai lần cho cùng một đơn',
        format($q$CALL sp_thanh_toan(%L,'COD','Tiền mặt','X','ND006',NULL)$q$, v_dh1));

    ---------------- 7. Thanh toán phí vận chuyển ----------------
    RAISE NOTICE '--- 7. Thanh toán phí vận chuyển (UC06) ---';
    CALL sp_thanh_toan(v_dh1, 'PHI_VC', 'Chuyển khoản', 'Nguyễn Văn An', 'ND007', v_ma_ttoan);
    PERFORM pg_temp.assert_true('Sau khi thanh toán phí: v_don_hang_chi_tiet báo "Đã thanh toán đủ"',
        (SELECT tinh_trang_thanh_toan FROM v_don_hang_chi_tiet WHERE ma_dh = v_dh1) = 'Đã thanh toán đủ');

    ---------------- 8. Bắt buộc thu COD trước khi xác nhận giao thành công ----------------
    RAISE NOTICE '--- 8. Trigger chặn TT06 khi chưa thu COD (đơn nội bộ, không qua chuyến giao cuối) ---';
    CALL sp_tao_don_hang('KH001','Khách lẻ tại kho','0901000002','12 Võ Văn Ngân','34 Lê Lợi, Q1',
        'KHO01','KHO01', 200000, '[{"khoi_luong":1,"dai":10,"rong":10,"cao":10}]'::jsonb, 'ND007', v_dh4);
    CALL sp_tiep_nhan_don(v_dh4, 'NV001', 'ND002');
    CALL sp_cap_nhat_trang_thai(v_dh4, 'TT05', 'ND002');
    PERFORM pg_temp.expect_error('Không thể xác nhận giao thành công khi COD > 0 chưa được thu',
        format($q$CALL sp_cap_nhat_trang_thai(%L,'TT06','ND002')$q$, v_dh4));
    CALL sp_thanh_toan(v_dh4, 'COD', 'Tiền mặt', 'Khách lẻ tại kho', 'ND002', v_ma_ttoan);
    CALL sp_cap_nhat_trang_thai(v_dh4, 'TT06', 'ND002');
    PERFORM pg_temp.assert_true('Sau khi thu COD, chuyển TT06 thành công',
        (SELECT ma_tt FROM don_hang WHERE ma_dh = v_dh4) = 'TT06');

    ---------------- 9. Lịch sử trạng thái bất biến ----------------
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

ROLLBACK;  

