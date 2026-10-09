-- =========================================================
-- DỮ LIỆU MẪU BỔ SUNG ĐỂ KIỂM THỬ ĐIỀU PHỐI (LIÊN KHO & GIAO CUỐI)
-- Chạy sau schema.sql, logic.sql (và seed_and_test.sql)
-- =========================================================

DO $$
DECLARE
    v_dh VARCHAR;
BEGIN
    -- 1. Đơn tại KHO01 đi KHO03 Đà Nẵng (2 kiện: 12kg, 8kg) -> Phù hợp tuyến TU01
    CALL sp_tao_don_hang('KH001', 'Nguyen Thi Hoa', '0901000001',
        '12 Vo Van Ngan, Thu Duc', '100 Hai Phong, Da Nang', 'KHO01', 'KHO03', 150000,
        '[{"khoi_luong":12.0,"dai":40,"rong":30,"cao":20,"loai_hang":"Thiet bi dien tu"},
          {"khoi_luong":8.0,"dai":30,"rong":25,"cao":15,"loai_hang":"Quan ao thoi trang"}]'::jsonb,
        'ND007', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV001', 'ND002');

    -- 2. Đơn tại KHO01 đi KHO04 Cần Thơ (1 kiện: 15kg) -> Phù hợp tuyến TU04
    CALL sp_tao_don_hang('KH002', 'Tran Van Nam', '0901000002',
        '34 Le Loi, Q1, TP.HCM', '20 Dai lo Hoa Binh, Can Tho', 'KHO01', 'KHO04', 0,
        '[{"khoi_luong":15.0,"dai":35,"rong":30,"cao":25,"loai_hang":"Do gia dung"}]'::jsonb,
        'ND008', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV001', 'ND002');

    -- 3. Đơn giao cuối tại KHO01 (1 kiện: 3.5kg) -> Phù hợp Giao cuối tại KHO01
    CALL sp_tao_don_hang('KH001', 'Le Huu Phuoc', '0901000003',
        '12 Vo Van Ngan, Thu Duc', '50 Dang Van Bi, Thu Duc', 'KHO01', 'KHO01', 80000,
        '[{"khoi_luong":3.5,"dai":25,"rong":20,"cao":15,"loai_hang":"Thuc pham hoa qua"}]'::jsonb,
        'ND007', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV001', 'ND002');

    -- 4. Đơn tại KHO03 đã đến kho (TT04) -> Phù hợp Giao cuối tại Đà Nẵng (1 kiện: 6kg)
    CALL sp_tao_don_hang('KH002', 'Do Thanh Tung', '0901000001',
        '12 Vo Van Ngan, TP.HCM', '75 Bach Dang, Da Nang', 'KHO01', 'KHO03', 250000,
        '[{"khoi_luong":6.0,"dai":30,"rong":20,"cao":15,"loai_hang":"Giay the thao"}]'::jsonb,
        'ND008', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV001', 'ND002');
    UPDATE don_hang SET ma_tt = 'TT03' WHERE ma_dh = v_dh;
    UPDATE don_hang SET ma_tt = 'TT04' WHERE ma_dh = v_dh;
    UPDATE kien_hang SET ma_kho_hien_tai = 'KHO03' WHERE ma_dh = v_dh;

    -- 5. Đơn tại KHO03 hẹn giao lại (TT08) -> Phù hợp Giao cuối lại tại Đà Nẵng (1 kiện: 2kg)
    CALL sp_tao_don_hang('KH001', 'Pham Quynh Anh', '0901000002',
        '34 Le Loi, TP.HCM', '12 Le Duan, Da Nang', 'KHO01', 'KHO03', 0,
        '[{"khoi_luong":2.0,"dai":20,"rong":15,"cao":10,"loai_hang":"Sach va tai lieu"}]'::jsonb,
        'ND007', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV001', 'ND002');
    UPDATE don_hang SET ma_tt = 'TT03' WHERE ma_dh = v_dh;
    UPDATE don_hang SET ma_tt = 'TT04' WHERE ma_dh = v_dh;
    UPDATE don_hang SET ma_tt = 'TT05' WHERE ma_dh = v_dh;
    UPDATE don_hang SET ma_tt = 'TT08' WHERE ma_dh = v_dh;
    UPDATE kien_hang SET ma_kho_hien_tai = 'KHO03' WHERE ma_dh = v_dh;

    -- 6. Đơn tại KHO03 gửi về KHO01 -> Phù hợp Liên kho ngược TU02 (1 kiện: 25kg)
    CALL sp_tao_don_hang('KH003', 'Nguyen Van An', '0901000001',
        '45 Nguyen Van Linh, Da Nang', '12 Vo Van Ngan, Thu Duc', 'KHO03', 'KHO01', 0,
        '[{"khoi_luong":25.0,"dai":50,"rong":40,"cao":30,"loai_hang":"Hai san kho Da Nang"}]'::jsonb,
        'ND007', v_dh);
    CALL sp_tiep_nhan_don(v_dh, 'NV002', 'ND003');

    -- 7. Đơn tại KHO04 Cần Thơ gửi về KHO01 -> Phù hợp Liên kho TU05 (1 kiện: 18kg)
    CALL sp_tao_don_hang('KH002', 'Tran Thi Mai', '0901000002',
        '88 Nguyen Trai, Can Tho', '34 Le Loi, Q1, TP.HCM', 'KHO04', 'KHO01', 300000,
        '[{"khoi_luong":18.0,"dai":45,"rong":35,"cao":25,"loai_hang":"Trai cay dac san"}]'::jsonb,
        'ND008', v_dh);
    UPDATE don_hang SET ma_tt = 'TT02' WHERE ma_dh = v_dh;
    UPDATE kien_hang SET ma_kho_hien_tai = 'KHO04' WHERE ma_dh = v_dh;

    RAISE NOTICE 'Da khoi tao bo du lieu mau seed_extra thanh cong!';
END $$;
