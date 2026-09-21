-- =========================================================
-- UTEEXPRESS - FUNCTION / VIEW / TRIGGER / PROCEDURE
-- Chạy SAU schema.sql.
-- =========================================================

-- =========================================================
-- PHẦN 0. HÀM PHIÊN (truyền "ai đang thao tác" cho trigger)
-- =========================================================
CREATE OR REPLACE FUNCTION fn_dat_phien(p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.ma_nd',   COALESCE(p_ma_nd, ''),   true);   -- true = chỉ trong transaction
    PERFORM set_config('app.ghi_chu', COALESCE(p_ghi_chu, ''), true);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_phien_ma_nd() RETURNS VARCHAR AS $$
    SELECT NULLIF(current_setting('app.ma_nd', true), '')::VARCHAR;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_phien_ghi_chu() RETURNS TEXT AS $$
    SELECT NULLIF(current_setting('app.ghi_chu', true), '');
$$ LANGUAGE sql STABLE;

-- =========================================================
-- PHẦN 1. FUNCTION
-- =========================================================
CREATE OR REPLACE FUNCTION fn_mask_sdt(p_sdt VARCHAR) RETURNS VARCHAR AS $$
    SELECT CASE WHEN p_sdt IS NULL THEN NULL
                WHEN length(p_sdt) < 7 THEN '***'
                ELSE left(p_sdt, 3) || repeat('*', length(p_sdt) - 6) || right(p_sdt, 3) END;
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE FUNCTION fn_cau_hinh(p_khoa VARCHAR, p_mac_dinh NUMERIC) RETURNS NUMERIC AS $$
    SELECT COALESCE((SELECT gia_tri FROM cau_hinh_he_thong WHERE khoa = p_khoa), p_mac_dinh);
$$ LANGUAGE sql STABLE;

-- Phí của MỘT kiện: cước cơ bản + phần vượt × đơn giá kg + km × đơn giá km
CREATE OR REPLACE FUNCTION fn_tinh_phi_van_chuyen(
    p_khoi_luong NUMERIC, p_dai NUMERIC, p_rong NUMERIC, p_cao NUMERIC, p_khoang_cach NUMERIC)
RETURNS NUMERIC AS $$
DECLARE v_kl_qd NUMERIC; v_phi NUMERIC;
BEGIN
    IF COALESCE(p_khoi_luong,0) <= 0 OR COALESCE(p_dai,0) <= 0 OR COALESCE(p_rong,0) <= 0 OR COALESCE(p_cao,0) <= 0 THEN
        RAISE EXCEPTION 'Không thể tính phí, vui lòng thử lại';
    END IF;
    v_kl_qd := GREATEST(p_khoi_luong, (p_dai * p_rong * p_cao) / fn_cau_hinh('HE_SO_QUY_DOI', 5000));
    v_phi := fn_cau_hinh('PHI_CO_BAN', 15000)
           + GREATEST(v_kl_qd - fn_cau_hinh('KG_MIEN_PHI', 0.5), 0) * fn_cau_hinh('PHI_MOI_KG', 5000)
           + COALESCE(p_khoang_cach, 0) * fn_cau_hinh('PHI_MOI_KM', 300);
    RETURN ROUND(v_phi, -2);
END;
$$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION fn_tim_tuyen(p_kho_di VARCHAR, p_kho_den VARCHAR) RETURNS VARCHAR AS $$
    SELECT ma_tuyen FROM tuyen_van_chuyen WHERE ma_kho_di = p_kho_di AND ma_kho_den = p_kho_den;
$$ LANGUAGE sql STABLE;

-- Cùng kho => 0 km; khác kho => khoảng cách tuyến (NULL nếu chưa có tuyến)
CREATE OR REPLACE FUNCTION fn_khoang_cach_don(p_kho_gui VARCHAR, p_kho_nhan VARCHAR) RETURNS NUMERIC AS $$
    SELECT CASE WHEN p_kho_gui = p_kho_nhan THEN 0::NUMERIC
                ELSE (SELECT khoang_cach FROM tuyen_van_chuyen WHERE ma_kho_di = p_kho_gui AND ma_kho_den = p_kho_nhan) END;
$$ LANGUAGE sql STABLE;

-- Phí cả đơn = tổng phí các kiện
CREATE OR REPLACE FUNCTION fn_tinh_phi_don(p_ma_dh VARCHAR, p_kho_gui VARCHAR, p_kho_nhan VARCHAR) RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(fn_tinh_phi_van_chuyen(k.khoi_luong, k.dai, k.rong, k.cao,
                        COALESCE(fn_khoang_cach_don(p_kho_gui, p_kho_nhan), 0))), 0)
    FROM kien_hang k WHERE k.ma_dh = p_ma_dh;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_cap_nhat_phi_don(p_ma_dh VARCHAR) RETURNS VOID AS $$
    UPDATE don_hang d SET phi_van_chuyen = fn_tinh_phi_don(d.ma_dh, d.ma_kho_gui, d.ma_kho_nhan)
    WHERE d.ma_dh = p_ma_dh;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION fn_tong_thu(p_ma_dh VARCHAR) RETURNS NUMERIC AS $$
    SELECT phi_van_chuyen + cod FROM don_hang WHERE ma_dh = p_ma_dh;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_tong_khoi_luong_chuyen(p_ma_chuyen VARCHAR) RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(k.khoi_luong), 0)
    FROM chi_tiet_chuyen_giao c JOIN kien_hang k ON k.ma_kien = c.ma_kien
    WHERE c.ma_chuyen = p_ma_chuyen;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_tai_trong_con_lai(p_ma_chuyen VARCHAR) RETURNS NUMERIC AS $$
    SELECT p.tai_trong - fn_tong_khoi_luong_chuyen(c.ma_chuyen)
    FROM chuyen_giao c JOIN phuong_tien p ON p.ma_pt = c.ma_pt WHERE c.ma_chuyen = p_ma_chuyen;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_chuyen_trang_thai_hop_le(p_tu VARCHAR, p_den VARCHAR) RETURNS BOOLEAN AS $$
    SELECT EXISTS (SELECT 1 FROM quy_trinh_trang_thai WHERE tt_tu = p_tu AND tt_den = p_den);
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_la_tai_xe_cua_chuyen(p_ma_chuyen VARCHAR, p_ma_nd VARCHAR) RETURNS BOOLEAN AS $$
    SELECT EXISTS (SELECT 1 FROM chuyen_giao c JOIN tai_xe t ON t.ma_tx = c.ma_tx
                   WHERE c.ma_chuyen = p_ma_chuyen AND t.ma_nd = p_ma_nd);
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_tai_xe_ranh(p_tu TIMESTAMP, p_den TIMESTAMP)
RETURNS TABLE (ma_tx VARCHAR, ho_ten VARCHAR, sdt VARCHAR) AS $$
    SELECT t.ma_tx, t.ho_ten, t.sdt
    FROM tai_xe t LEFT JOIN nguoi_dung n ON n.ma_nd = t.ma_nd
    WHERE COALESCE(n.trang_thai, 'Hoạt động') = 'Hoạt động'
      AND NOT EXISTS (SELECT 1 FROM chuyen_giao c
                      WHERE c.ma_tx = t.ma_tx AND c.trang_thai <> 'Đã hủy'
                        AND tsrange(c.ngay_xuat_phat, c.ngay_den_du_kien) && tsrange(p_tu, p_den));
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_xe_ranh(p_tu TIMESTAMP, p_den TIMESTAMP)
RETURNS TABLE (ma_pt VARCHAR, bien_so VARCHAR, loai_xe VARCHAR, tai_trong NUMERIC) AS $$
    SELECT p.ma_pt, p.bien_so, p.loai_xe, p.tai_trong
    FROM phuong_tien p
    WHERE p.trang_thai <> 'Bảo trì'
      AND NOT EXISTS (SELECT 1 FROM chuyen_giao c
                      WHERE c.ma_pt = p.ma_pt AND c.trang_thai <> 'Đã hủy'
                        AND tsrange(c.ngay_xuat_phat, c.ngay_den_du_kien) && tsrange(p_tu, p_den));
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_ty_le_giao_thanh_cong(p_tu DATE, p_den DATE) RETURNS NUMERIC AS $$
    SELECT ROUND(100.0 * COUNT(*) FILTER (WHERE ma_tt = 'TT06')
                 / NULLIF(COUNT(*) FILTER (WHERE ma_tt IN ('TT06','TT07')), 0), 2)
    FROM don_hang WHERE ngay_tao >= p_tu AND ngay_tao < p_den + 1;
$$ LANGUAGE sql STABLE;

-- Doanh thu theo kỳ: p_kieu = 'day' | 'month' | 'quarter' | 'year'
CREATE OR REPLACE FUNCTION fn_doanh_thu(p_tu DATE, p_den DATE, p_kieu TEXT DEFAULT 'month')
RETURNS TABLE (ky TEXT, phi_van_chuyen NUMERIC, tong_cod NUMERIC, so_giao_dich BIGINT) AS $$
BEGIN
    IF p_kieu NOT IN ('day','month','quarter','year') THEN
        RAISE EXCEPTION 'Kiểu thống kê không hợp lệ (day | month | quarter | year)';
    END IF;
    RETURN QUERY
    SELECT to_char(x.k, CASE p_kieu WHEN 'day' THEN 'YYYY-MM-DD' WHEN 'month' THEN 'YYYY-MM'
                                    WHEN 'quarter' THEN 'YYYY-"Q"Q' ELSE 'YYYY' END),
           x.phi, x.cod, x.so
    FROM (SELECT date_trunc(p_kieu, t.thoi_gian) AS k,
                 COALESCE(SUM(t.so_tien) FILTER (WHERE t.loai_khoan = 'PHI_VC'), 0) AS phi,
                 COALESCE(SUM(t.so_tien) FILTER (WHERE t.loai_khoan = 'COD'), 0)    AS cod,
                 COUNT(*) AS so
          FROM thanh_toan t
          WHERE t.trang_thai = 'Đã thanh toán' AND t.thoi_gian >= p_tu AND t.thoi_gian < p_den + 1
          GROUP BY 1) x
    ORDER BY x.k;
END;
$$ LANGUAGE plpgsql STABLE;

-- Số việc dở dang của 1 tài khoản (cảnh báo khi khóa - UC15)
CREATE OR REPLACE FUNCTION fn_so_viec_dang_xu_ly(p_ma_nd VARCHAR) RETURNS BIGINT AS $$
    SELECT
      (SELECT COUNT(DISTINCT d.ma_dh) FROM don_hang d
        WHERE d.ma_tt NOT IN ('TT06','TT07')
          AND (d.ma_kh_gui  IN (SELECT ma_kh FROM khach_hang WHERE ma_nd = p_ma_nd)
            OR d.ma_kh_nhan IN (SELECT ma_kh FROM khach_hang WHERE ma_nd = p_ma_nd)
            OR d.ma_nv      IN (SELECT ma_nv FROM nhan_vien  WHERE ma_nd = p_ma_nd)))
    + (SELECT COUNT(*) FROM chuyen_giao c
        WHERE c.trang_thai IN ('Chưa đi','Đang đi')
          AND (c.ma_tx IN (SELECT ma_tx FROM tai_xe WHERE ma_nd = p_ma_nd)
            OR c.ma_nv_dieu_phoi IN (SELECT ma_nv FROM nhan_vien WHERE ma_nd = p_ma_nd)));
$$ LANGUAGE sql STABLE;

-- =========================================================
-- PHẦN 2. VIEW
-- =========================================================
CREATE OR REPLACE VIEW v_don_hang_chi_tiet AS
SELECT d.ma_dh, d.ngay_tao,
       d.ma_kh_gui, kg.ho_ten AS ten_nguoi_gui, kg.sdt AS sdt_nguoi_gui,
       d.ma_kh_nhan, d.ten_nguoi_nhan, d.sdt_nhan, d.dia_chi_lay, d.dia_chi_giao,
       d.ma_kho_gui, kho_g.ten_kho AS ten_kho_gui, d.ma_kho_nhan, kho_n.ten_kho AS ten_kho_nhan,
       d.ma_tt, tt.ten_trang_thai, d.ma_nv, nv.ho_ten AS ten_nhan_vien,
       COALESCE(k.so_kien, 0) AS so_kien, COALESCE(k.tong_kg, 0) AS tong_kg,
       d.phi_van_chuyen, d.cod, d.phi_van_chuyen + d.cod AS tong_thu,
       COALESCE(p.phi_da_tt, FALSE) AS phi_da_thanh_toan,
       COALESCE(p.cod_da_tt, FALSE) AS cod_da_thu,
       CASE WHEN d.ma_tt = 'TT07' THEN 'Không thu (hủy/trả)'
            WHEN COALESCE(p.phi_da_tt, FALSE) AND (d.cod = 0 OR COALESCE(p.cod_da_tt, FALSE)) THEN 'Đã thanh toán đủ'
            WHEN COALESCE(p.phi_da_tt, FALSE) OR COALESCE(p.cod_da_tt, FALSE) THEN 'Thanh toán một phần'
            ELSE 'Chưa thanh toán' END AS tinh_trang_thanh_toan
FROM don_hang d
JOIN khach_hang kg      ON kg.ma_kh = d.ma_kh_gui
JOIN kho kho_g          ON kho_g.ma_kho = d.ma_kho_gui
JOIN kho kho_n          ON kho_n.ma_kho = d.ma_kho_nhan
JOIN trang_thai_dh tt   ON tt.ma_trang_thai = d.ma_tt
LEFT JOIN nhan_vien nv  ON nv.ma_nv = d.ma_nv
LEFT JOIN LATERAL (SELECT COUNT(*) AS so_kien, COALESCE(SUM(khoi_luong), 0) AS tong_kg
                   FROM kien_hang WHERE ma_dh = d.ma_dh) k ON TRUE
LEFT JOIN LATERAL (SELECT bool_or(loai_khoan = 'PHI_VC' AND trang_thai = 'Đã thanh toán') AS phi_da_tt,
                          bool_or(loai_khoan = 'COD'    AND trang_thai = 'Đã thanh toán') AS cod_da_tt
                   FROM thanh_toan WHERE ma_dh = d.ma_dh) p ON TRUE;

-- Guest tra cứu: chỉ trạng thái tổng quát, che SĐT, không có địa chỉ
CREATE OR REPLACE VIEW v_tra_cuu_cong_khai AS
SELECT d.ma_dh, d.ngay_tao, tt.ten_trang_thai,
       (SELECT MAX(thoi_gian) FROM lich_su_trang_thai l WHERE l.ma_dh = d.ma_dh) AS cap_nhat_luc,
       fn_mask_sdt(d.sdt_nhan) AS sdt_nhan_che,
       (SELECT COUNT(*) FROM kien_hang k WHERE k.ma_dh = d.ma_dh) AS so_kien
FROM don_hang d JOIN trang_thai_dh tt ON tt.ma_trang_thai = d.ma_tt;

CREATE OR REPLACE VIEW v_lich_su_van_chuyen AS
SELECT l.ma_ls, l.ma_dh, l.ma_tt, tt.ten_trang_thai, l.thoi_gian,
       l.ma_nd, nd.ten_dang_nhap AS nguoi_cap_nhat, l.ghi_chu
FROM lich_su_trang_thai l
JOIN trang_thai_dh tt ON tt.ma_trang_thai = l.ma_tt
LEFT JOIN nguoi_dung nd ON nd.ma_nd = l.ma_nd;

CREATE OR REPLACE VIEW v_ton_kho AS
SELECT k.ma_kho, k.ten_kho, COUNT(kh.ma_kien) AS so_kien, COALESCE(SUM(kh.khoi_luong), 0) AS tong_kg
FROM kho k LEFT JOIN kien_hang kh ON kh.ma_kho_hien_tai = k.ma_kho
GROUP BY k.ma_kho, k.ten_kho;

CREATE OR REPLACE VIEW v_chuyen_giao_chi_tiet AS
SELECT c.ma_chuyen, c.loai_chuyen, c.trang_thai, c.ma_tuyen,
       COALESCE(t.ma_kho_di, c.ma_kho_giao) AS ma_kho_xuat_phat, kdi.ten_kho AS ten_kho_xuat_phat,
       t.ma_kho_den, COALESCE(kden.ten_kho, 'Địa chỉ khách hàng') AS ten_diem_den,
       c.ma_tx, tx.ho_ten AS ten_tai_xe, c.ma_pt, pt.bien_so, pt.tai_trong,
       c.ma_nv_dieu_phoi, c.ngay_xuat_phat, c.ngay_den_du_kien, c.ngay_xuat_phat_thuc, c.ngay_den,
       COALESCE(ct.so_kien, 0) AS so_kien, COALESCE(ct.tong_kg, 0) AS tong_kg,
       ROUND(100.0 * COALESCE(ct.tong_kg, 0) / pt.tai_trong, 1) AS ty_le_tai
FROM chuyen_giao c
JOIN tai_xe tx ON tx.ma_tx = c.ma_tx
JOIN phuong_tien pt ON pt.ma_pt = c.ma_pt
LEFT JOIN tuyen_van_chuyen t ON t.ma_tuyen = c.ma_tuyen
LEFT JOIN kho kdi  ON kdi.ma_kho  = COALESCE(t.ma_kho_di, c.ma_kho_giao)
LEFT JOIN kho kden ON kden.ma_kho = t.ma_kho_den
LEFT JOIN LATERAL (SELECT COUNT(*) AS so_kien, SUM(k.khoi_luong) AS tong_kg
                   FROM chi_tiet_chuyen_giao x JOIN kien_hang k ON k.ma_kien = x.ma_kien
                   WHERE x.ma_chuyen = c.ma_chuyen) ct ON TRUE;

-- Tài xế xem kiện trong chuyến (lọc theo ma_chuyen / ma_tx ở tầng Spring)
CREATE OR REPLACE VIEW v_kien_trong_chuyen AS
SELECT x.ma_chuyen, c.ma_tx, x.ma_kien, k.ma_dh, d.ten_nguoi_nhan, d.sdt_nhan, d.dia_chi_giao,
       k.khoi_luong, k.loai_hang, x.trang_thai, x.thoi_gian_gan, x.thoi_gian_quet, x.ghi_chu
FROM chi_tiet_chuyen_giao x
JOIN chuyen_giao c ON c.ma_chuyen = x.ma_chuyen
JOIN kien_hang k   ON k.ma_kien = x.ma_kien
JOIN don_hang d    ON d.ma_dh = k.ma_dh;

CREATE OR REPLACE VIEW v_thong_ke_don_theo_trang_thai AS
SELECT t.ma_trang_thai, t.ten_trang_thai, COUNT(d.ma_dh) AS so_don
FROM trang_thai_dh t LEFT JOIN don_hang d ON d.ma_tt = t.ma_trang_thai
GROUP BY t.ma_trang_thai, t.ten_trang_thai
ORDER BY t.ma_trang_thai;

CREATE OR REPLACE VIEW v_doanh_thu_ngay AS
SELECT thoi_gian::date AS ngay,
       COALESCE(SUM(so_tien) FILTER (WHERE loai_khoan = 'PHI_VC'), 0) AS phi_van_chuyen,
       COALESCE(SUM(so_tien) FILTER (WHERE loai_khoan = 'COD'), 0)    AS tong_cod,
       COUNT(*) AS so_giao_dich
FROM thanh_toan WHERE trang_thai = 'Đã thanh toán'
GROUP BY thoi_gian::date;

CREATE OR REPLACE VIEW v_doanh_thu_thang AS
SELECT date_trunc('month', thoi_gian)::date AS thang,
       COALESCE(SUM(so_tien) FILTER (WHERE loai_khoan = 'PHI_VC'), 0) AS phi_van_chuyen,
       COALESCE(SUM(so_tien) FILTER (WHERE loai_khoan = 'COD'), 0)    AS tong_cod,
       COUNT(*) AS so_giao_dich
FROM thanh_toan WHERE trang_thai = 'Đã thanh toán'
GROUP BY date_trunc('month', thoi_gian);

CREATE OR REPLACE VIEW v_cod_doi_soat AS
SELECT d.ma_dh, d.ma_kh_gui, d.ten_nguoi_nhan, d.cod, tt.ten_trang_thai,
       COALESCE(p.so_tien, 0) AS cod_da_thu, p.nguoi_thanh_toan, p.thoi_gian AS thoi_gian_thu,
       CASE WHEN p.ma_tt_toan IS NOT NULL THEN 'Đã thu'
            WHEN d.ma_tt = 'TT07' THEN 'Không thu (hủy/trả)'
            ELSE 'Chưa thu' END AS tinh_trang_cod
FROM don_hang d
JOIN trang_thai_dh tt ON tt.ma_trang_thai = d.ma_tt
LEFT JOIN thanh_toan p ON p.ma_dh = d.ma_dh AND p.loai_khoan = 'COD' AND p.trang_thai = 'Đã thanh toán'
WHERE d.cod > 0;

CREATE OR REPLACE VIEW v_thong_ke_chuyen AS
SELECT ngay_xuat_phat::date AS ngay, date_trunc('month', ngay_xuat_phat)::date AS thang,
       loai_chuyen, ma_tuyen, ma_tx, ma_pt,
       COUNT(*) AS so_chuyen,
       COUNT(*) FILTER (WHERE trang_thai = 'Hoàn thành') AS so_hoan_thanh,
       COUNT(*) FILTER (WHERE trang_thai IN ('Chưa đi','Đang đi')) AS so_chua_hoan_thanh
FROM chuyen_giao GROUP BY 1, 2, 3, 4, 5, 6;

CREATE OR REPLACE VIEW v_hieu_suat_tai_xe AS
SELECT t.ma_tx, t.ho_ten,
       COUNT(DISTINCT c.ma_chuyen) AS so_chuyen,
       COUNT(DISTINCT c.ma_chuyen) FILTER (WHERE c.trang_thai = 'Hoàn thành') AS so_chuyen_hoan_thanh,
       COUNT(ct.ma_kien) FILTER (WHERE ct.trang_thai = 'Đã giao')   AS kien_giao_thanh_cong,
       COUNT(ct.ma_kien) FILTER (WHERE ct.trang_thai = 'Thất bại')  AS kien_giao_that_bai,
       ROUND(100.0 * COUNT(ct.ma_kien) FILTER (WHERE ct.trang_thai = 'Đã giao')
             / NULLIF(COUNT(ct.ma_kien) FILTER (WHERE ct.trang_thai IN ('Đã giao','Thất bại')), 0), 2) AS ty_le_thanh_cong
FROM tai_xe t
LEFT JOIN chuyen_giao c ON c.ma_tx = t.ma_tx AND c.trang_thai <> 'Đã hủy'
LEFT JOIN chi_tiet_chuyen_giao ct ON ct.ma_chuyen = c.ma_chuyen
GROUP BY t.ma_tx, t.ho_ten;

CREATE OR REPLACE VIEW v_hieu_suat_nhan_vien AS
SELECT n.ma_nv, n.ho_ten, n.chuc_vu, n.ma_kho,
       COALESCE(d.so_don, 0)  AS so_don_xu_ly,       COALESCE(d.so_kien, 0) AS so_kien_xu_ly,
       COALESCE(c.so_chuyen, 0) AS so_chuyen_dieu_phoi, COALESCE(c.so_kien, 0) AS so_kien_dieu_phoi
FROM nhan_vien n
LEFT JOIN LATERAL (SELECT COUNT(DISTINCT dh.ma_dh) AS so_don, COUNT(k.ma_kien) AS so_kien
                   FROM don_hang dh LEFT JOIN kien_hang k ON k.ma_dh = dh.ma_dh
                   WHERE dh.ma_nv = n.ma_nv) d ON TRUE
LEFT JOIN LATERAL (SELECT COUNT(DISTINCT cg.ma_chuyen) AS so_chuyen, COUNT(x.ma_kien) AS so_kien
                   FROM chuyen_giao cg LEFT JOIN chi_tiet_chuyen_giao x ON x.ma_chuyen = cg.ma_chuyen
                   WHERE cg.ma_nv_dieu_phoi = n.ma_nv) c ON TRUE;

-- =========================================================
-- PHẦN 3. TRIGGER
-- =========================================================

-- 3.1 Vai trò của tài khoản phải khớp bảng liên kết ---------------------------
-- 3.1 Vai trò của tài khoản phải khớp bảng liên kết
CREATE OR REPLACE FUNCTION fn_trg_kiem_tra_vai_tro() RETURNS trigger AS $$
DECLARE 
    v_vt VARCHAR;
BEGIN
    IF NEW.ma_nd IS NULL THEN 
        RETURN NEW; 
    END IF;

    SELECT ma_vai_tro INTO v_vt FROM nguoi_dung WHERE ma_nd = NEW.ma_nd;

    IF TG_TABLE_NAME = 'khach_hang' THEN
        IF v_vt <> 'KHACH_HANG' THEN 
            RAISE EXCEPTION 'Tài khoản % không có vai trò Khách hàng', NEW.ma_nd; 
        END IF;
    ELSIF TG_TABLE_NAME = 'tai_xe' THEN
        IF v_vt <> 'TAI_XE' THEN 
            RAISE EXCEPTION 'Tài khoản % không có vai trò Tài xế', NEW.ma_nd; 
        END IF;
    ELSE  -- nhan_vien
        IF v_vt <> (CASE WHEN NEW.chuc_vu = 'Nhân viên bưu cục' THEN 'NVBC' ELSE 'NVDP' END) THEN
            RAISE EXCEPTION 'Vai trò tài khoản % không khớp chức vụ "%"', NEW.ma_nd, NEW.chuc_vu;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo các Triggers tương ứng
CREATE TRIGGER trg_kh_vai_tro BEFORE INSERT OR UPDATE OF ma_nd ON khach_hang
    FOR EACH ROW EXECUTE FUNCTION fn_trg_kiem_tra_vai_tro();

CREATE TRIGGER trg_tx_vai_tro BEFORE INSERT OR UPDATE OF ma_nd ON tai_xe
    FOR EACH ROW EXECUTE FUNCTION fn_trg_kiem_tra_vai_tro();

CREATE TRIGGER trg_nv_vai_tro BEFORE INSERT OR UPDATE OF ma_nd, chuc_vu ON nhan_vien
    FOR EACH ROW EXECUTE FUNCTION fn_trg_kiem_tra_vai_tro();

-- 3.2 ĐƠN HÀNG: ghi lịch sử + kiểm tra chuyển trạng thái + khóa sửa ------------
CREATE OR REPLACE FUNCTION fn_trg_don_hang_ai() RETURNS trigger AS $$
BEGIN
    INSERT INTO lich_su_trang_thai (ma_dh, ma_tt, ma_nd, ghi_chu)
    VALUES (NEW.ma_dh, NEW.ma_tt, fn_phien_ma_nd(), COALESCE(fn_phien_ghi_chu(), 'Tạo đơn hàng'));
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_don_hang_ai AFTER INSERT ON don_hang
    FOR EACH ROW EXECUTE FUNCTION fn_trg_don_hang_ai();

CREATE OR REPLACE FUNCTION fn_trg_don_hang_bu() RETURNS trigger AS $$
BEGIN
    IF NEW.ma_tt IS DISTINCT FROM OLD.ma_tt THEN
        IF NOT fn_chuyen_trang_thai_hop_le(OLD.ma_tt, NEW.ma_tt) THEN
            RAISE EXCEPTION 'Chuyển trạng thái không hợp lệ: "%" → "%"',
                (SELECT ten_trang_thai FROM trang_thai_dh WHERE ma_trang_thai = OLD.ma_tt),
                (SELECT ten_trang_thai FROM trang_thai_dh WHERE ma_trang_thai = NEW.ma_tt);
        END IF;
        IF NEW.ma_tt = 'TT06' AND NEW.cod > 0 AND NOT EXISTS (
               SELECT 1 FROM thanh_toan WHERE ma_dh = NEW.ma_dh AND loai_khoan = 'COD' AND trang_thai = 'Đã thanh toán') THEN
            RAISE EXCEPTION 'Chưa thu tiền COD, không thể xác nhận giao thành công';
        END IF;
    ELSE
        -- từ "Đang vận chuyển" trở đi không được sửa thông tin/phí/COD/kho
        IF OLD.ma_tt NOT IN ('TT01','TT02') AND
           (NEW.dia_chi_lay, NEW.dia_chi_giao, NEW.cod, NEW.phi_van_chuyen, NEW.ma_kho_gui, NEW.ma_kho_nhan,
            NEW.ten_nguoi_nhan, NEW.sdt_nhan)
           IS DISTINCT FROM
           (OLD.dia_chi_lay, OLD.dia_chi_giao, OLD.cod, OLD.phi_van_chuyen, OLD.ma_kho_gui, OLD.ma_kho_nhan,
            OLD.ten_nguoi_nhan, OLD.sdt_nhan) THEN
            RAISE EXCEPTION 'Đơn hàng % đã vận chuyển, không được thay đổi thông tin/phí/COD', OLD.ma_dh;
        END IF;
        IF (NEW.ma_kho_gui, NEW.ma_kho_nhan) IS DISTINCT FROM (OLD.ma_kho_gui, OLD.ma_kho_nhan) THEN
            NEW.phi_van_chuyen := fn_tinh_phi_don(NEW.ma_dh, NEW.ma_kho_gui, NEW.ma_kho_nhan);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_don_hang_bu BEFORE UPDATE ON don_hang
    FOR EACH ROW EXECUTE FUNCTION fn_trg_don_hang_bu();

CREATE OR REPLACE FUNCTION fn_trg_don_hang_au() RETURNS trigger AS $$
BEGIN
    INSERT INTO lich_su_trang_thai (ma_dh, ma_tt, ma_nd, ghi_chu)
    VALUES (NEW.ma_dh, NEW.ma_tt, fn_phien_ma_nd(), COALESCE(fn_phien_ghi_chu(), 'Cập nhật trạng thái'));
    IF NEW.ma_tt = 'TT06' THEN            -- đã giao: kiện rời hệ thống kho
        UPDATE kien_hang SET ma_kho_hien_tai = NULL WHERE ma_dh = NEW.ma_dh;
    ELSIF NEW.ma_tt = 'TT07' THEN         -- hủy: gỡ kiện khỏi các chuyến chưa đi
        DELETE FROM chi_tiet_chuyen_giao
        WHERE ma_kien IN (SELECT ma_kien FROM kien_hang WHERE ma_dh = NEW.ma_dh)
          AND ma_chuyen IN (SELECT ma_chuyen FROM chuyen_giao WHERE trang_thai = 'Chưa đi');
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_don_hang_au AFTER UPDATE OF ma_tt ON don_hang
    FOR EACH ROW WHEN (OLD.ma_tt IS DISTINCT FROM NEW.ma_tt)
    EXECUTE FUNCTION fn_trg_don_hang_au();

-- 3.3 LỊCH SỬ TRẠNG THÁI: bất biến (yêu cầu truy vết) --------------------------
CREATE OR REPLACE FUNCTION fn_trg_lich_su_bat_bien() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Lịch sử trạng thái không được sửa hoặc xóa';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_lich_su_bat_bien BEFORE UPDATE OR DELETE ON lich_su_trang_thai
    FOR EACH ROW EXECUTE FUNCTION fn_trg_lich_su_bat_bien();

-- 3.4 KIỆN HÀNG: tự tính lại phí đơn ------------------------------------------
CREATE OR REPLACE FUNCTION fn_trg_kien_hang_tinh_phi() RETURNS trigger AS $$
BEGIN
    IF TG_OP IN ('INSERT','UPDATE') THEN PERFORM fn_cap_nhat_phi_don(NEW.ma_dh); END IF;
    IF TG_OP = 'DELETE' OR (TG_OP = 'UPDATE' AND OLD.ma_dh <> NEW.ma_dh) THEN
        PERFORM fn_cap_nhat_phi_don(OLD.ma_dh);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_kien_hang_tinh_phi
    AFTER INSERT OR DELETE OR UPDATE OF khoi_luong, dai, rong, cao, ma_dh ON kien_hang
    FOR EACH ROW EXECUTE FUNCTION fn_trg_kien_hang_tinh_phi();

-- 3.5 CHUYẾN GIAO ---------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_trg_chuyen_giao_bi() RETURNS trigger AS $$
DECLARE v_pt_tt VARCHAR; v_nd_tt VARCHAR;
BEGIN
    SELECT trang_thai INTO v_pt_tt FROM phuong_tien WHERE ma_pt = NEW.ma_pt;
    IF v_pt_tt = 'Bảo trì' THEN RAISE EXCEPTION 'Phương tiện % đang bảo trì', NEW.ma_pt; END IF;
    SELECT n.trang_thai INTO v_nd_tt FROM tai_xe t LEFT JOIN nguoi_dung n ON n.ma_nd = t.ma_nd WHERE t.ma_tx = NEW.ma_tx;
    IF v_nd_tt = 'Bị khóa' THEN RAISE EXCEPTION 'Tài khoản tài xế % đang bị khóa', NEW.ma_tx; END IF;
    IF NEW.trang_thai <> 'Chưa đi' THEN RAISE EXCEPTION 'Chuyến mới phải ở trạng thái "Chưa đi"'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chuyen_giao_bi BEFORE INSERT ON chuyen_giao
    FOR EACH ROW EXECUTE FUNCTION fn_trg_chuyen_giao_bi();

CREATE OR REPLACE FUNCTION fn_trg_chuyen_giao_bu() RETURNS trigger AS $$
DECLARE v_so INT; v_chua INT;
BEGIN
    IF NEW.trang_thai IS DISTINCT FROM OLD.trang_thai THEN
        IF NOT ((OLD.trang_thai = 'Chưa đi' AND NEW.trang_thai IN ('Đang đi','Đã hủy'))
             OR (OLD.trang_thai = 'Đang đi' AND NEW.trang_thai = 'Hoàn thành')) THEN
            RAISE EXCEPTION 'Không thể chuyển chuyến giao từ "%" sang "%"', OLD.trang_thai, NEW.trang_thai;
        END IF;
        IF NEW.trang_thai = 'Đang đi' THEN
            SELECT COUNT(*), COUNT(*) FILTER (WHERE trang_thai <> 'Đã quét') INTO v_so, v_chua
            FROM chi_tiet_chuyen_giao WHERE ma_chuyen = NEW.ma_chuyen;
            IF v_so = 0   THEN RAISE EXCEPTION 'Chuyến % chưa có kiện hàng nào', NEW.ma_chuyen; END IF;
            IF v_chua > 0 THEN RAISE EXCEPTION 'Còn % kiện chưa được quét lên xe', v_chua; END IF;
            NEW.ngay_xuat_phat_thuc := clock_timestamp()::timestamp;
        ELSIF NEW.trang_thai = 'Hoàn thành' THEN
            IF NEW.loai_chuyen = 'GIAO_CUOI' THEN
                SELECT COUNT(*) INTO v_chua FROM chi_tiet_chuyen_giao
                WHERE ma_chuyen = NEW.ma_chuyen AND trang_thai NOT IN ('Đã giao','Thất bại');
                IF v_chua > 0 THEN RAISE EXCEPTION 'Còn % kiện chưa có kết quả giao', v_chua; END IF;
            END IF;
            NEW.ngay_den := clock_timestamp()::timestamp;
        END IF;
    ELSE
        IF OLD.trang_thai <> 'Chưa đi' AND
           (NEW.loai_chuyen, NEW.ma_tuyen, NEW.ma_kho_giao, NEW.ma_tx, NEW.ma_pt)
           IS DISTINCT FROM (OLD.loai_chuyen, OLD.ma_tuyen, OLD.ma_kho_giao, OLD.ma_tx, OLD.ma_pt) THEN
            RAISE EXCEPTION 'Chuyến đã xuất phát hoặc kết thúc, không được đổi tuyến/tài xế/phương tiện';
        END IF;
        IF NEW.ma_pt IS DISTINCT FROM OLD.ma_pt AND
           fn_tong_khoi_luong_chuyen(NEW.ma_chuyen) > (SELECT tai_trong FROM phuong_tien WHERE ma_pt = NEW.ma_pt) THEN
            RAISE EXCEPTION 'Phương tiện mới không đủ tải trọng cho các kiện đã gán';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chuyen_giao_bu BEFORE UPDATE ON chuyen_giao
    FOR EACH ROW EXECUTE FUNCTION fn_trg_chuyen_giao_bu();

CREATE OR REPLACE FUNCTION fn_trg_chuyen_giao_au() RETURNS trigger AS $$
DECLARE v_kho_den VARCHAR; v_tt_moi VARCHAR;
BEGIN
    IF NEW.loai_chuyen = 'LIEN_KHO' THEN
        SELECT ma_kho_den INTO v_kho_den FROM tuyen_van_chuyen WHERE ma_tuyen = NEW.ma_tuyen;
    END IF;
    CASE NEW.trang_thai
    WHEN 'Đang đi' THEN
        PERFORM set_config('app.ghi_chu', 'Chuyến ' || NEW.ma_chuyen || ' xuất phát', true);
        UPDATE phuong_tien SET trang_thai = 'Đang chạy' WHERE ma_pt = NEW.ma_pt;
        UPDATE kien_hang SET ma_kho_hien_tai = NULL
        WHERE ma_kien IN (SELECT ma_kien FROM chi_tiet_chuyen_giao WHERE ma_chuyen = NEW.ma_chuyen);
        v_tt_moi := CASE WHEN NEW.loai_chuyen = 'LIEN_KHO' THEN 'TT03' ELSE 'TT05' END;
        UPDATE don_hang SET ma_tt = v_tt_moi
        WHERE ma_dh IN (SELECT k.ma_dh FROM kien_hang k JOIN chi_tiet_chuyen_giao c ON c.ma_kien = k.ma_kien
                        WHERE c.ma_chuyen = NEW.ma_chuyen)
          AND fn_chuyen_trang_thai_hop_le(ma_tt, v_tt_moi);
    WHEN 'Hoàn thành' THEN
        PERFORM set_config('app.ghi_chu', 'Chuyến ' || NEW.ma_chuyen || ' hoàn thành', true);
        UPDATE phuong_tien SET trang_thai = 'Sẵn sàng' WHERE ma_pt = NEW.ma_pt;
        IF NEW.loai_chuyen = 'LIEN_KHO' THEN
            UPDATE kien_hang SET ma_kho_hien_tai = v_kho_den
            WHERE ma_kien IN (SELECT ma_kien FROM chi_tiet_chuyen_giao WHERE ma_chuyen = NEW.ma_chuyen);
            UPDATE don_hang SET ma_tt = 'TT04'
            WHERE ma_dh IN (SELECT k.ma_dh FROM kien_hang k JOIN chi_tiet_chuyen_giao c ON c.ma_kien = k.ma_kien
                            WHERE c.ma_chuyen = NEW.ma_chuyen)
              AND fn_chuyen_trang_thai_hop_le(ma_tt, 'TT04');
        ELSE   -- giao cuối: kiện giao thất bại quay về kho giao
            UPDATE kien_hang SET ma_kho_hien_tai = NEW.ma_kho_giao
            WHERE ma_kien IN (SELECT ma_kien FROM chi_tiet_chuyen_giao
                              WHERE ma_chuyen = NEW.ma_chuyen AND trang_thai = 'Thất bại');
        END IF;
    WHEN 'Đã hủy' THEN
        DELETE FROM chi_tiet_chuyen_giao WHERE ma_chuyen = NEW.ma_chuyen;
    ELSE NULL;
    END CASE;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chuyen_giao_au AFTER UPDATE OF trang_thai ON chuyen_giao
    FOR EACH ROW WHEN (OLD.trang_thai IS DISTINCT FROM NEW.trang_thai)
    EXECUTE FUNCTION fn_trg_chuyen_giao_au();

-- 3.6 CHI TIẾT CHUYẾN GIAO: kiểm tra khi gán kiện ------------------------------
CREATE OR REPLACE FUNCTION fn_trg_ctcg_bi() RETURNS trigger AS $$
DECLARE
    v_cg chuyen_giao%ROWTYPE; v_kien kien_hang%ROWTYPE; v_dh don_hang%ROWTYPE;
    v_tai_trong NUMERIC; v_kho_xuat VARCHAR;
BEGIN
    SELECT * INTO v_cg FROM chuyen_giao WHERE ma_chuyen = NEW.ma_chuyen FOR UPDATE;  -- khóa chuyến, tránh gán đồng thời
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy chuyến giao %', NEW.ma_chuyen; END IF;
    IF v_cg.trang_thai <> 'Chưa đi' THEN RAISE EXCEPTION 'Chỉ được gán kiện vào chuyến chưa xuất phát'; END IF;

    SELECT * INTO v_kien FROM kien_hang WHERE ma_kien = NEW.ma_kien;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy kiện hàng!'; END IF;
    SELECT * INTO v_dh FROM don_hang WHERE ma_dh = v_kien.ma_dh;

    IF v_cg.loai_chuyen = 'LIEN_KHO' THEN
        IF v_dh.ma_tt NOT IN ('TT02','TT04') THEN
            RAISE EXCEPTION 'Đơn % chưa sẵn sàng vận chuyển liên kho (trạng thái: %)', v_dh.ma_dh, v_dh.ma_tt;
        END IF;
        SELECT ma_kho_di INTO v_kho_xuat FROM tuyen_van_chuyen WHERE ma_tuyen = v_cg.ma_tuyen;
    ELSE
        IF v_dh.ma_tt NOT IN ('TT02','TT04','TT08') THEN
            RAISE EXCEPTION 'Đơn % chưa sẵn sàng giao hàng (trạng thái: %)', v_dh.ma_dh, v_dh.ma_tt;
        END IF;
        IF v_dh.ma_kho_nhan <> v_cg.ma_kho_giao THEN
            RAISE EXCEPTION 'Kho giao của chuyến (%) không phải kho nhận của đơn % (%)',
                v_cg.ma_kho_giao, v_dh.ma_dh, v_dh.ma_kho_nhan;
        END IF;
        v_kho_xuat := v_cg.ma_kho_giao;
    END IF;

    IF v_kien.ma_kho_hien_tai IS DISTINCT FROM v_kho_xuat THEN
        RAISE EXCEPTION 'Kiện % đang ở kho %, không phải kho xuất phát % của chuyến',
            v_kien.ma_kien, COALESCE(v_kien.ma_kho_hien_tai, '(không ở kho)'), v_kho_xuat;
    END IF;

    IF EXISTS (SELECT 1 FROM chi_tiet_chuyen_giao c JOIN chuyen_giao g ON g.ma_chuyen = c.ma_chuyen
               WHERE c.ma_kien = NEW.ma_kien AND c.ma_chuyen <> NEW.ma_chuyen
                 AND g.trang_thai IN ('Chưa đi','Đang đi')) THEN
        RAISE EXCEPTION 'Kiện % đã thuộc một chuyến giao khác chưa hoàn thành', NEW.ma_kien;
    END IF;

    SELECT tai_trong INTO v_tai_trong FROM phuong_tien WHERE ma_pt = v_cg.ma_pt;
    IF fn_tong_khoi_luong_chuyen(NEW.ma_chuyen) + v_kien.khoi_luong > v_tai_trong THEN
        RAISE EXCEPTION 'Vượt tải trọng phương tiện %: đã xếp % kg + kiện % kg > % kg',
            v_cg.ma_pt, fn_tong_khoi_luong_chuyen(NEW.ma_chuyen), v_kien.khoi_luong, v_tai_trong;
    END IF;

    NEW.trang_thai := 'Đã gán';
    NEW.thoi_gian_quet := NULL;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ctcg_bi BEFORE INSERT ON chi_tiet_chuyen_giao
    FOR EACH ROW EXECUTE FUNCTION fn_trg_ctcg_bi();

-- 3.7 THANH TOÁN -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_trg_thanh_toan_bi() RETURNS trigger AS $$
DECLARE v_dh don_hang%ROWTYPE;
BEGIN
    SELECT * INTO v_dh FROM don_hang WHERE ma_dh = NEW.ma_dh;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy đơn hàng %', NEW.ma_dh; END IF;
    IF v_dh.ma_tt = 'TT07' THEN RAISE EXCEPTION 'Đơn hàng đã hủy/trả, không thể thanh toán'; END IF;

    IF NEW.loai_khoan = 'COD' THEN
        IF v_dh.cod <= 0 THEN RAISE EXCEPTION 'Đơn hàng % không có tiền COD', NEW.ma_dh; END IF;
        IF NEW.so_tien <> v_dh.cod THEN RAISE EXCEPTION 'Số tiền COD phải bằng % (đơn %)', v_dh.cod, NEW.ma_dh; END IF;
        IF TG_OP = 'INSERT' AND NEW.trang_thai = 'Đã thanh toán' AND v_dh.ma_tt <> 'TT05' THEN
            RAISE EXCEPTION 'COD chỉ được thu khi đơn đang ở trạng thái "Đang giao hàng"';
        END IF;
    ELSE
        IF NEW.so_tien <> v_dh.phi_van_chuyen THEN
            RAISE EXCEPTION 'Số tiền phí vận chuyển phải bằng % (đơn %)', v_dh.phi_van_chuyen, NEW.ma_dh;
        END IF;
    END IF;

    IF NEW.trang_thai = 'Đã thanh toán' AND EXISTS (
           SELECT 1 FROM thanh_toan WHERE ma_dh = NEW.ma_dh AND loai_khoan = NEW.loai_khoan
             AND trang_thai = 'Đã thanh toán' AND ma_tt_toan <> NEW.ma_tt_toan) THEN
        RAISE EXCEPTION 'Khoản % của đơn % đã được thanh toán', NEW.loai_khoan, NEW.ma_dh;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_thanh_toan_bi BEFORE INSERT OR UPDATE ON thanh_toan
    FOR EACH ROW EXECUTE FUNCTION fn_trg_thanh_toan_bi();

-- 3.8 NHẬT KÝ HỆ THỐNG (không lưu mật khẩu) -----------------------------------
CREATE OR REPLACE FUNCTION fn_trg_nhat_ky() RETURNS trigger AS $$
DECLARE v_old JSONB; v_new JSONB;
BEGIN
    IF TG_OP IN ('UPDATE','DELETE') THEN v_old := to_jsonb(OLD) - 'mat_khau'; END IF;
    IF TG_OP IN ('INSERT','UPDATE') THEN v_new := to_jsonb(NEW) - 'mat_khau'; END IF;
    INSERT INTO nhat_ky_he_thong (ten_bang, hanh_dong, khoa, du_lieu_cu, du_lieu_moi, ma_nd)
    VALUES (TG_TABLE_NAME, TG_OP, COALESCE(v_new, v_old) ->> TG_ARGV[0], v_old, v_new, fn_phien_ma_nd());
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_nhat_ky_nguoi_dung AFTER INSERT OR UPDATE OR DELETE ON nguoi_dung
    FOR EACH ROW EXECUTE FUNCTION fn_trg_nhat_ky('ma_nd');
CREATE TRIGGER trg_nhat_ky_don_hang AFTER INSERT OR UPDATE OR DELETE ON don_hang
    FOR EACH ROW EXECUTE FUNCTION fn_trg_nhat_ky('ma_dh');
CREATE TRIGGER trg_nhat_ky_thanh_toan AFTER INSERT OR UPDATE OR DELETE ON thanh_toan
    FOR EACH ROW EXECUTE FUNCTION fn_trg_nhat_ky('ma_tt_toan');

-- =========================================================
-- PHẦN 4. STORED PROCEDURE
-- Tham số INOUT ở cuối = mã vừa tạo. p_ma_nd = tài khoản đang thao tác.
-- =========================================================

-- UC01: Đăng ký khách hàng (tên đăng nhập = SĐT; mật khẩu đã BCrypt ở Spring)
CREATE OR REPLACE PROCEDURE sp_dang_ky_khach_hang(
    p_ho_ten VARCHAR, p_sdt VARCHAR, p_email VARCHAR, p_mat_khau_hash VARCHAR,
    INOUT p_ma_kh VARCHAR DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_ma_nd VARCHAR; v_kh khach_hang%ROWTYPE;
BEGIN
    IF COALESCE(trim(p_ho_ten), '') = '' OR p_sdt IS NULL OR COALESCE(p_mat_khau_hash, '') = '' THEN
        RAISE EXCEPTION 'Thiếu thông tin bắt buộc (họ tên, số điện thoại, mật khẩu)';
    END IF;
    IF p_sdt !~ '^[0-9]{10}$' THEN RAISE EXCEPTION 'Số điện thoại phải gồm 10 chữ số'; END IF;
    IF EXISTS (SELECT 1 FROM nguoi_dung WHERE ten_dang_nhap = p_sdt) THEN
        RAISE EXCEPTION 'Tài khoản đã tồn tại!';
    END IF;

    SELECT * INTO v_kh FROM khach_hang WHERE sdt = p_sdt;
    IF v_kh.ma_kh IS NOT NULL AND v_kh.ma_nd IS NOT NULL THEN RAISE EXCEPTION 'Tài khoản đã tồn tại!'; END IF;
    IF p_email IS NOT NULL AND EXISTS (SELECT 1 FROM khach_hang WHERE lower(email) = lower(p_email)
                                        AND ma_kh IS DISTINCT FROM v_kh.ma_kh) THEN
        RAISE EXCEPTION 'Email đã được sử dụng';
    END IF;

    INSERT INTO nguoi_dung (ten_dang_nhap, mat_khau, ma_vai_tro)
    VALUES (p_sdt, p_mat_khau_hash, 'KHACH_HANG') RETURNING ma_nd INTO v_ma_nd;

    IF v_kh.ma_kh IS NULL THEN
        INSERT INTO khach_hang (ho_ten, sdt, email, ma_nd)
        VALUES (trim(p_ho_ten), p_sdt, p_email, v_ma_nd) RETURNING ma_kh INTO p_ma_kh;
    ELSE   -- từng là người nhận chưa có tài khoản → liên kết vào hồ sơ cũ
        UPDATE khach_hang SET ho_ten = trim(p_ho_ten), email = COALESCE(p_email, email), ma_nd = v_ma_nd
        WHERE ma_kh = v_kh.ma_kh;
        p_ma_kh := v_kh.ma_kh;
    END IF;
END;
$$;

-- UC04: Tạo đơn hàng. p_kien = JSON: [{"khoi_luong":2.5,"dai":30,"rong":20,"cao":15,"loai_hang":"Điện tử"}, ...]
CREATE OR REPLACE PROCEDURE sp_tao_don_hang(
    p_ma_kh_gui VARCHAR, p_ten_nguoi_nhan VARCHAR, p_sdt_nhan VARCHAR,
    p_dia_chi_lay TEXT, p_dia_chi_giao TEXT, p_ma_kho_gui VARCHAR, p_ma_kho_nhan VARCHAR,
    p_cod NUMERIC, p_kien JSONB, p_ma_nd VARCHAR,
    INOUT p_ma_dh VARCHAR DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_ma_kh_nhan VARCHAR;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_kh = p_ma_kh_gui) THEN
        RAISE EXCEPTION 'Không tìm thấy khách hàng gửi'; END IF;
    IF COALESCE(trim(p_ten_nguoi_nhan),'') = '' OR COALESCE(trim(p_dia_chi_lay),'') = ''
       OR COALESCE(trim(p_dia_chi_giao),'') = '' THEN
        RAISE EXCEPTION 'Thiếu thông tin bắt buộc (người nhận, địa chỉ lấy/giao)'; END IF;
    IF p_sdt_nhan IS NULL OR p_sdt_nhan !~ '^[0-9]{10}$' THEN
        RAISE EXCEPTION 'Số điện thoại người nhận phải gồm 10 chữ số'; END IF;
    IF COALESCE(p_cod, 0) < 0 THEN RAISE EXCEPTION 'Tiền COD không được âm'; END IF;
    IF NOT EXISTS (SELECT 1 FROM kho WHERE ma_kho = p_ma_kho_gui)
       OR NOT EXISTS (SELECT 1 FROM kho WHERE ma_kho = p_ma_kho_nhan) THEN
        RAISE EXCEPTION 'Kho gửi/kho nhận không tồn tại'; END IF;
    IF p_ma_kho_gui <> p_ma_kho_nhan AND fn_tim_tuyen(p_ma_kho_gui, p_ma_kho_nhan) IS NULL THEN
        RAISE EXCEPTION 'Chưa hỗ trợ tuyến này'; END IF;
    IF p_kien IS NULL OR jsonb_typeof(p_kien) <> 'array' OR jsonb_array_length(p_kien) = 0 THEN
        RAISE EXCEPTION 'Đơn hàng phải có ít nhất một kiện hàng'; END IF;
    IF EXISTS (SELECT 1 FROM jsonb_array_elements(p_kien) e
               WHERE (e->>'khoi_luong') IS NULL OR (e->>'dai') IS NULL
                  OR (e->>'rong') IS NULL OR (e->>'cao') IS NULL) THEN
        RAISE EXCEPTION 'Mỗi kiện cần đủ khối lượng, dài, rộng, cao'; END IF;

    PERFORM fn_dat_phien(p_ma_nd, 'Tạo đơn hàng');
    SELECT ma_kh INTO v_ma_kh_nhan FROM khach_hang WHERE sdt = p_sdt_nhan;

    INSERT INTO don_hang (ma_kh_gui, ma_kh_nhan, sdt_nhan, ten_nguoi_nhan, dia_chi_lay, dia_chi_giao,
                          ma_kho_gui, ma_kho_nhan, cod)
    VALUES (p_ma_kh_gui, v_ma_kh_nhan, p_sdt_nhan, trim(p_ten_nguoi_nhan), p_dia_chi_lay, p_dia_chi_giao,
            p_ma_kho_gui, p_ma_kho_nhan, COALESCE(p_cod, 0))
    RETURNING ma_dh INTO p_ma_dh;                     -- trigger ghi lịch sử TT01

    INSERT INTO kien_hang (ma_dh, khoi_luong, dai, rong, cao, loai_hang)
    SELECT p_ma_dh, (e->>'khoi_luong')::NUMERIC, (e->>'dai')::NUMERIC, (e->>'rong')::NUMERIC,
           (e->>'cao')::NUMERIC, e->>'loai_hang'
    FROM jsonb_array_elements(p_kien) e;              -- trigger tự tính phí
END;
$$;

-- UC08: NV bưu cục tiếp nhận đơn (TT01 → TT02, kiện vào kho của NV)
CREATE OR REPLACE PROCEDURE sp_tiep_nhan_don(
    p_ma_dh VARCHAR, p_ma_nv VARCHAR, p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_dh don_hang%ROWTYPE; v_nv nhan_vien%ROWTYPE;
BEGIN
    SELECT * INTO v_dh FROM don_hang WHERE ma_dh = p_ma_dh FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy đơn hàng!'; END IF;
    SELECT * INTO v_nv FROM nhan_vien WHERE ma_nv = p_ma_nv;
    IF NOT FOUND OR v_nv.chuc_vu <> 'Nhân viên bưu cục' THEN
        RAISE EXCEPTION 'Chỉ nhân viên bưu cục mới được tiếp nhận đơn'; END IF;
    IF v_nv.ma_kho IS DISTINCT FROM v_dh.ma_kho_gui THEN
        RAISE EXCEPTION 'Nhân viên không thuộc bưu cục gửi (%) của đơn hàng', v_dh.ma_kho_gui; END IF;
    IF v_dh.ma_tt <> 'TT01' THEN RAISE EXCEPTION 'Đơn hàng không ở trạng thái "Mới tạo"'; END IF;

    PERFORM fn_dat_phien(p_ma_nd, COALESCE(p_ghi_chu, 'Tiếp nhận đơn tại bưu cục'));
    UPDATE kien_hang SET ma_kho_hien_tai = v_nv.ma_kho WHERE ma_dh = p_ma_dh;
    UPDATE don_hang  SET ma_tt = 'TT02', ma_nv = p_ma_nv WHERE ma_dh = p_ma_dh;
END;
$$;

-- UC10: cập nhật trạng thái thủ công (trigger kiểm tra quy trình + ghi lịch sử)
CREATE OR REPLACE PROCEDURE sp_cap_nhat_trang_thai(
    p_ma_dh VARCHAR, p_ma_tt_moi VARCHAR, p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_tt VARCHAR;
BEGIN
    SELECT ma_tt INTO v_tt FROM don_hang WHERE ma_dh = p_ma_dh FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy đơn hàng!'; END IF;
    IF NOT EXISTS (SELECT 1 FROM trang_thai_dh WHERE ma_trang_thai = p_ma_tt_moi) THEN
        RAISE EXCEPTION 'Trạng thái không tồn tại'; END IF;
    IF v_tt = p_ma_tt_moi THEN RAISE EXCEPTION 'Đơn hàng đã ở trạng thái này'; END IF;
    PERFORM fn_dat_phien(p_ma_nd, p_ghi_chu);
    UPDATE don_hang SET ma_tt = p_ma_tt_moi WHERE ma_dh = p_ma_dh;
END;
$$;

-- UC09: nhập kho / xuất kho thủ công cho kiện
CREATE OR REPLACE PROCEDURE sp_nhap_kho_kien(
    p_ma_kien VARCHAR, p_ma_kho VARCHAR, p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_kien kien_hang%ROWTYPE;
BEGIN
    SELECT * INTO v_kien FROM kien_hang WHERE ma_kien = p_ma_kien FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy kiện hàng!'; END IF;
    IF NOT EXISTS (SELECT 1 FROM kho WHERE ma_kho = p_ma_kho) THEN RAISE EXCEPTION 'Kho không tồn tại'; END IF;
    IF v_kien.ma_kho_hien_tai IS NOT NULL THEN
        RAISE EXCEPTION 'Kiện đang ở kho %, hãy xuất kho trước', v_kien.ma_kho_hien_tai; END IF;
    PERFORM fn_dat_phien(p_ma_nd, COALESCE(p_ghi_chu, 'Nhập kho ' || p_ma_kho));
    UPDATE kien_hang SET ma_kho_hien_tai = p_ma_kho WHERE ma_kien = p_ma_kien;
    UPDATE don_hang SET ma_tt = 'TT04' WHERE ma_dh = v_kien.ma_dh AND ma_tt = 'TT03';
END;
$$;

CREATE OR REPLACE PROCEDURE sp_xuat_kho_kien(
    p_ma_kien VARCHAR, p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_kien kien_hang%ROWTYPE;
BEGIN
    SELECT * INTO v_kien FROM kien_hang WHERE ma_kien = p_ma_kien FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy kiện hàng!'; END IF;
    IF v_kien.ma_kho_hien_tai IS NULL THEN RAISE EXCEPTION 'Kiện hiện không nằm trong kho nào'; END IF;
    PERFORM fn_dat_phien(p_ma_nd, COALESCE(p_ghi_chu, 'Xuất kho ' || v_kien.ma_kho_hien_tai));
    UPDATE kien_hang SET ma_kho_hien_tai = NULL WHERE ma_kien = p_ma_kien;
    UPDATE don_hang SET ma_tt = 'TT03'
    WHERE ma_dh = v_kien.ma_dh AND fn_chuyen_trang_thai_hop_le(ma_tt, 'TT03');
END;
$$;

-- UC12: lập chuyến giao (LIEN_KHO cần p_ma_tuyen; GIAO_CUOI cần p_ma_kho_giao + p_ngay_den_du_kien)
CREATE OR REPLACE PROCEDURE sp_tao_chuyen_giao(
    p_loai VARCHAR, p_ma_tuyen VARCHAR, p_ma_kho_giao VARCHAR, p_ma_tx VARCHAR, p_ma_pt VARCHAR,
    p_ngay_xuat_phat TIMESTAMP, p_ma_nv_dp VARCHAR, p_ma_nd VARCHAR,
    p_ngay_den_du_kien TIMESTAMP DEFAULT NULL,
    INOUT p_ma_chuyen VARCHAR DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_gio NUMERIC; v_den TIMESTAMP := p_ngay_den_du_kien; v_c TEXT;
BEGIN
    IF p_loai NOT IN ('LIEN_KHO','GIAO_CUOI') THEN RAISE EXCEPTION 'Loại chuyến không hợp lệ'; END IF;
    IF p_ngay_xuat_phat IS NULL THEN RAISE EXCEPTION 'Thiếu ngày xuất phát'; END IF;

    IF p_loai = 'LIEN_KHO' THEN
        IF p_ma_tuyen IS NULL THEN RAISE EXCEPTION 'Chuyến liên kho phải chọn tuyến vận chuyển'; END IF;
        SELECT thoi_gian_du_kien_gio INTO v_gio FROM tuyen_van_chuyen WHERE ma_tuyen = p_ma_tuyen;
        IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy tuyến vận chuyển'; END IF;
        v_den := COALESCE(v_den, p_ngay_xuat_phat + v_gio * INTERVAL '1 hour');
    ELSE
        IF p_ma_kho_giao IS NULL THEN RAISE EXCEPTION 'Chuyến giao cuối phải chọn kho xuất phát'; END IF;
        IF v_den IS NULL THEN RAISE EXCEPTION 'Chuyến giao cuối phải có ngày đến dự kiến'; END IF;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM nhan_vien WHERE ma_nv = p_ma_nv_dp AND chuc_vu = 'Nhân viên điều phối') THEN
        RAISE EXCEPTION 'Chỉ nhân viên điều phối mới được lập chuyến'; END IF;
    IF NOT EXISTS (SELECT 1 FROM tai_xe WHERE ma_tx = p_ma_tx) THEN RAISE EXCEPTION 'Không tìm thấy tài xế'; END IF;
    IF NOT EXISTS (SELECT 1 FROM phuong_tien WHERE ma_pt = p_ma_pt) THEN RAISE EXCEPTION 'Không tìm thấy phương tiện'; END IF;

    PERFORM fn_dat_phien(p_ma_nd, 'Lập chuyến giao');
    BEGIN
        INSERT INTO chuyen_giao (loai_chuyen, ma_tuyen, ma_kho_giao, ma_tx, ma_pt, ma_nv_dieu_phoi,
                                 ngay_xuat_phat, ngay_den_du_kien)
        VALUES (p_loai,
                CASE WHEN p_loai = 'LIEN_KHO'  THEN p_ma_tuyen END,
                CASE WHEN p_loai = 'GIAO_CUOI' THEN p_ma_kho_giao END,
                p_ma_tx, p_ma_pt, p_ma_nv_dp, p_ngay_xuat_phat, v_den)
        RETURNING ma_chuyen INTO p_ma_chuyen;
    EXCEPTION WHEN exclusion_violation THEN
        GET STACKED DIAGNOSTICS v_c = CONSTRAINT_NAME;
        IF v_c = 'ex_cg_tx_trung_lich' THEN
            RAISE EXCEPTION 'Tài xế đã được phân công chuyến khác trùng thời gian';
        ELSE
            RAISE EXCEPTION 'Phương tiện đã được phân công chuyến khác trùng thời gian';
        END IF;
    END;
END;
$$;

-- Gán kiện vào chuyến (kiểm tra tải trọng, kho, trạng thái nằm trong trigger)
CREATE OR REPLACE PROCEDURE sp_gan_kien_vao_chuyen(p_ma_chuyen VARCHAR, p_ma_kien VARCHAR, p_ma_nd VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM fn_dat_phien(p_ma_nd, 'Gán kiện vào chuyến ' || p_ma_chuyen);
    INSERT INTO chi_tiet_chuyen_giao (ma_chuyen, ma_kien) VALUES (p_ma_chuyen, p_ma_kien);
EXCEPTION WHEN unique_violation THEN
    RAISE EXCEPTION 'Kiện % đã có trong chuyến %', p_ma_kien, p_ma_chuyen;
END;
$$;

-- UC14: tài xế quét kiện lên xe
CREATE OR REPLACE PROCEDURE sp_quet_kien(
    p_ma_chuyen VARCHAR, p_ma_kien VARCHAR, p_ma_nd VARCHAR, p_ghi_chu TEXT DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_tt VARCHAR;
BEGIN
    IF NOT fn_la_tai_xe_cua_chuyen(p_ma_chuyen, p_ma_nd) THEN
        RAISE EXCEPTION 'Bạn không được phân công chuyến này'; END IF;
    SELECT trang_thai INTO v_tt FROM chuyen_giao WHERE ma_chuyen = p_ma_chuyen;
    IF v_tt <> 'Chưa đi' THEN RAISE EXCEPTION 'Chỉ quét kiện lên xe khi chuyến chưa xuất phát'; END IF;
    UPDATE chi_tiet_chuyen_giao
    SET trang_thai = 'Đã quét', thoi_gian_quet = clock_timestamp()::timestamp, ghi_chu = COALESCE(p_ghi_chu, ghi_chu)
    WHERE ma_chuyen = p_ma_chuyen AND ma_kien = p_ma_kien;
    IF NOT FOUND THEN RAISE EXCEPTION 'Kiện không thuộc chuyến này'; END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_xuat_phat_chuyen(p_ma_chuyen VARCHAR, p_ma_nd VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM fn_dat_phien(p_ma_nd);
    UPDATE chuyen_giao SET trang_thai = 'Đang đi' WHERE ma_chuyen = p_ma_chuyen;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy chuyến giao'; END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_hoan_thanh_chuyen(p_ma_chuyen VARCHAR, p_ma_nd VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM fn_dat_phien(p_ma_nd);
    UPDATE chuyen_giao SET trang_thai = 'Hoàn thành' WHERE ma_chuyen = p_ma_chuyen;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy chuyến giao'; END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_huy_chuyen(p_ma_chuyen VARCHAR, p_ma_nd VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM fn_dat_phien(p_ma_nd);
    UPDATE chuyen_giao SET trang_thai = 'Đã hủy' WHERE ma_chuyen = p_ma_chuyen;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy chuyến giao'; END IF;
END;
$$;

-- UC14: giao kiện thành công (đủ kiện của đơn → tự thu COD nếu chưa thu, đơn → TT06)
CREATE OR REPLACE PROCEDURE sp_giao_thanh_cong(
    p_ma_chuyen VARCHAR, p_ma_kien VARCHAR, p_ma_nd VARCHAR,
    p_ghi_chu TEXT DEFAULT NULL, p_nguoi_thu VARCHAR DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_cg chuyen_giao%ROWTYPE; v_dh don_hang%ROWTYPE;
BEGIN
    IF NOT fn_la_tai_xe_cua_chuyen(p_ma_chuyen, p_ma_nd) THEN
        RAISE EXCEPTION 'Bạn không được phân công chuyến này'; END IF;
    SELECT * INTO v_cg FROM chuyen_giao WHERE ma_chuyen = p_ma_chuyen;
    IF v_cg.loai_chuyen <> 'GIAO_CUOI' OR v_cg.trang_thai <> 'Đang đi' THEN
        RAISE EXCEPTION 'Chỉ giao hàng khi chuyến giao cuối đang chạy'; END IF;

    SELECT d.* INTO v_dh FROM don_hang d JOIN kien_hang k ON k.ma_dh = d.ma_dh
    WHERE k.ma_kien = p_ma_kien FOR UPDATE OF d;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy kiện hàng!'; END IF;

    PERFORM fn_dat_phien(p_ma_nd, COALESCE(p_ghi_chu, 'Giao hàng thành công'));
    UPDATE chi_tiet_chuyen_giao
    SET trang_thai = 'Đã giao', ghi_chu = COALESCE(p_ghi_chu, ghi_chu)
    WHERE ma_chuyen = p_ma_chuyen AND ma_kien = p_ma_kien;
    IF NOT FOUND THEN RAISE EXCEPTION 'Kiện không thuộc chuyến này'; END IF;

    IF NOT EXISTS (SELECT 1 FROM kien_hang k WHERE k.ma_dh = v_dh.ma_dh
                   AND NOT EXISTS (SELECT 1 FROM chi_tiet_chuyen_giao c
                                   WHERE c.ma_kien = k.ma_kien AND c.trang_thai = 'Đã giao')) THEN
        IF v_dh.cod > 0 AND NOT EXISTS (SELECT 1 FROM thanh_toan WHERE ma_dh = v_dh.ma_dh
                                        AND loai_khoan = 'COD' AND trang_thai = 'Đã thanh toán') THEN
            INSERT INTO thanh_toan (ma_dh, loai_khoan, so_tien, phuong_thuc, trang_thai, nguoi_thanh_toan)
            VALUES (v_dh.ma_dh, 'COD', v_dh.cod, 'COD', 'Đã thanh toán', COALESCE(p_nguoi_thu, v_dh.ten_nguoi_nhan));
        END IF;
        UPDATE don_hang SET ma_tt = 'TT06' WHERE ma_dh = v_dh.ma_dh;
    END IF;
END;
$$;

-- UC14: giao không thành công (bắt buộc ghi lý do) → đơn TT08
CREATE OR REPLACE PROCEDURE sp_giao_that_bai(
    p_ma_chuyen VARCHAR, p_ma_kien VARCHAR, p_ma_nd VARCHAR, p_ly_do TEXT)
LANGUAGE plpgsql AS $$
DECLARE v_cg chuyen_giao%ROWTYPE; v_ma_dh VARCHAR;
BEGIN
    IF NOT fn_la_tai_xe_cua_chuyen(p_ma_chuyen, p_ma_nd) THEN
        RAISE EXCEPTION 'Bạn không được phân công chuyến này'; END IF;
    IF COALESCE(trim(p_ly_do), '') = '' THEN RAISE EXCEPTION 'Phải nhập lý do giao không thành công'; END IF;
    SELECT * INTO v_cg FROM chuyen_giao WHERE ma_chuyen = p_ma_chuyen;
    IF v_cg.loai_chuyen <> 'GIAO_CUOI' OR v_cg.trang_thai <> 'Đang đi' THEN
        RAISE EXCEPTION 'Chỉ cập nhật kết quả giao khi chuyến giao cuối đang chạy'; END IF;

    SELECT ma_dh INTO v_ma_dh FROM kien_hang WHERE ma_kien = p_ma_kien;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy kiện hàng!'; END IF;

    PERFORM fn_dat_phien(p_ma_nd, 'Giao không thành công: ' || p_ly_do);
    UPDATE chi_tiet_chuyen_giao SET trang_thai = 'Thất bại', ghi_chu = p_ly_do
    WHERE ma_chuyen = p_ma_chuyen AND ma_kien = p_ma_kien;
    IF NOT FOUND THEN RAISE EXCEPTION 'Kiện không thuộc chuyến này'; END IF;
    UPDATE don_hang SET ma_tt = 'TT08' WHERE ma_dh = v_ma_dh AND ma_tt = 'TT05';
END;
$$;

-- UC07: người nhận xác nhận đã nhận hàng (COD phải được thu trước - trigger kiểm tra)
CREATE OR REPLACE PROCEDURE sp_xac_nhan_nhan_hang(p_ma_dh VARCHAR, p_ma_nd VARCHAR)
LANGUAGE plpgsql AS $$
DECLARE v_dh don_hang%ROWTYPE;
BEGIN
    SELECT d.* INTO v_dh FROM don_hang d JOIN khach_hang k ON k.ma_kh = d.ma_kh_nhan
    WHERE d.ma_dh = p_ma_dh AND k.ma_nd = p_ma_nd FOR UPDATE OF d;
    IF NOT FOUND THEN RAISE EXCEPTION 'Bạn không phải người nhận của đơn hàng này'; END IF;
    IF v_dh.ma_tt <> 'TT05' THEN RAISE EXCEPTION 'Đơn hàng chưa ở trạng thái "Đang giao hàng"'; END IF;
    PERFORM fn_dat_phien(p_ma_nd, 'Người nhận xác nhận đã nhận hàng');
    UPDATE don_hang SET ma_tt = 'TT06' WHERE ma_dh = p_ma_dh;
END;
$$;

-- UC06: thanh toán (số tiền lấy từ đơn hàng, không tin số tiền client gửi)
CREATE OR REPLACE PROCEDURE sp_thanh_toan(
    p_ma_dh VARCHAR, p_loai_khoan VARCHAR, p_phuong_thuc VARCHAR, p_nguoi_thanh_toan VARCHAR,
    p_ma_nd VARCHAR, INOUT p_ma_ttoan VARCHAR DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE v_dh don_hang%ROWTYPE; v_so_tien NUMERIC;
BEGIN
    SELECT * INTO v_dh FROM don_hang WHERE ma_dh = p_ma_dh FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Không tìm thấy đơn hàng!'; END IF;
    IF p_loai_khoan = 'PHI_VC' THEN v_so_tien := v_dh.phi_van_chuyen;
    ELSIF p_loai_khoan = 'COD' THEN v_so_tien := v_dh.cod;
    ELSE RAISE EXCEPTION 'Loại khoản không hợp lệ (PHI_VC | COD)'; END IF;
    IF v_so_tien <= 0 THEN RAISE EXCEPTION 'Không có khoản % cần thanh toán', p_loai_khoan; END IF;

    PERFORM fn_dat_phien(p_ma_nd, 'Thanh toán ' || p_loai_khoan);
    INSERT INTO thanh_toan (ma_dh, loai_khoan, so_tien, phuong_thuc, trang_thai, nguoi_thanh_toan)
    VALUES (p_ma_dh, p_loai_khoan, v_so_tien, p_phuong_thuc, 'Đã thanh toán', p_nguoi_thanh_toan)
    RETURNING ma_tt_toan INTO p_ma_ttoan;
END;
$$;

-- UC15: khóa / mở khóa tài khoản (cảnh báo nếu còn việc dở dang)
CREATE OR REPLACE PROCEDURE sp_khoa_tai_khoan(
    p_ma_nd VARCHAR, p_khoa BOOLEAN, p_xac_nhan BOOLEAN DEFAULT FALSE)
LANGUAGE plpgsql AS $$
DECLARE v_so BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE ma_nd = p_ma_nd) THEN
        RAISE EXCEPTION 'Không tìm thấy tài khoản'; END IF;
    IF p_khoa THEN
        v_so := fn_so_viec_dang_xu_ly(p_ma_nd);
        IF v_so > 0 AND NOT p_xac_nhan THEN
            RAISE EXCEPTION 'Cảnh báo: tài khoản còn % công việc dở dang. Gọi lại với p_xac_nhan = TRUE để xác nhận khóa.', v_so;
        END IF;
    END IF;
    UPDATE nguoi_dung SET trang_thai = CASE WHEN p_khoa THEN 'Bị khóa' ELSE 'Hoạt động' END
    WHERE ma_nd = p_ma_nd;
END;

$$;