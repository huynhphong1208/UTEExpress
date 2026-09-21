-- =========================================================
-- UTEEXPRESS - SCHEMA v2 (PostgreSQL 12+) - chạy trên DB trống
-- =========================================================
CREATE EXTENSION IF NOT EXISTS btree_gist;   -- cho ràng buộc EXCLUDE chống trùng lịch

-- ---------- SEQUENCE sinh mã ----------
CREATE SEQUENCE seq_nd;     CREATE SEQUENCE seq_kh;    CREATE SEQUENCE seq_kho;
CREATE SEQUENCE seq_nv;     CREATE SEQUENCE seq_tx;    CREATE SEQUENCE seq_pt;
CREATE SEQUENCE seq_tuyen;  CREATE SEQUENCE seq_chuyen;CREATE SEQUENCE seq_dh;
CREATE SEQUENCE seq_kien;   CREATE SEQUENCE seq_ls;    CREATE SEQUENCE seq_ttoan;

-- 1. VAI TRÒ
CREATE TABLE vai_tro (
    ma_vai_tro  VARCHAR(20) PRIMARY KEY,
    ten_vai_tro VARCHAR(50) NOT NULL,
    mo_ta       TEXT
);

-- 2. NGƯỜI DÙNG
CREATE TABLE nguoi_dung (
    ma_nd         VARCHAR(20) PRIMARY KEY DEFAULT ('ND' || lpad(nextval('seq_nd')::text, 6, '0')),
    ten_dang_nhap VARCHAR(50)  NOT NULL UNIQUE,
    mat_khau      VARCHAR(255) NOT NULL,                       -- hash BCrypt
    trang_thai    VARCHAR(20)  NOT NULL DEFAULT 'Hoạt động'
                  CONSTRAINT ck_nd_trang_thai CHECK (trang_thai IN ('Hoạt động','Bị khóa')),
    ma_vai_tro    VARCHAR(20)  NOT NULL REFERENCES vai_tro(ma_vai_tro)
);

-- 3. KHÁCH HÀNG (ma_nd NULL = khách chưa đăng ký tài khoản, vd người nhận)
CREATE TABLE khach_hang (
    ma_kh   VARCHAR(20) PRIMARY KEY DEFAULT ('KH' || lpad(nextval('seq_kh')::text, 6, '0')),
    ho_ten  VARCHAR(100) NOT NULL,
    sdt     VARCHAR(15)  NOT NULL
            CONSTRAINT uq_kh_sdt UNIQUE
            CONSTRAINT ck_kh_sdt CHECK (sdt ~ '^[0-9]{10}$'),
    email   VARCHAR(100)
            CONSTRAINT uq_kh_email UNIQUE
            CONSTRAINT ck_kh_email CHECK (email ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$'),
    dia_chi_mac_dinh TEXT,
    ma_nd   VARCHAR(20) CONSTRAINT uq_kh_nd UNIQUE REFERENCES nguoi_dung(ma_nd)
);

-- 4. KHO / BƯU CỤC
CREATE TABLE kho (
    ma_kho  VARCHAR(20) PRIMARY KEY DEFAULT ('KHO' || lpad(nextval('seq_kho')::text, 4, '0')),
    ten_kho VARCHAR(100) NOT NULL,
    dia_chi TEXT NOT NULL,
    sdt     VARCHAR(15) CONSTRAINT ck_kho_sdt CHECK (sdt ~ '^[0-9]{10}$')
);

-- 5. NHÂN VIÊN
CREATE TABLE nhan_vien (
    ma_nv   VARCHAR(20) PRIMARY KEY DEFAULT ('NV' || lpad(nextval('seq_nv')::text, 5, '0')),
    ho_ten  VARCHAR(100) NOT NULL,
    chuc_vu VARCHAR(50)  NOT NULL
            CONSTRAINT ck_nv_chuc_vu CHECK (chuc_vu IN ('Nhân viên bưu cục','Nhân viên điều phối')),
    sdt     VARCHAR(15) CONSTRAINT ck_nv_sdt CHECK (sdt ~ '^[0-9]{10}$'),
    email   VARCHAR(100),
    ma_kho  VARCHAR(20) REFERENCES kho(ma_kho),
    ma_nd   VARCHAR(20) CONSTRAINT uq_nv_nd UNIQUE REFERENCES nguoi_dung(ma_nd)
);

-- 6. TÀI XẾ
CREATE TABLE tai_xe (
    ma_tx    VARCHAR(20) PRIMARY KEY DEFAULT ('TX' || lpad(nextval('seq_tx')::text, 5, '0')),
    ho_ten   VARCHAR(100) NOT NULL,
    sdt      VARCHAR(15)  NOT NULL CONSTRAINT ck_tx_sdt CHECK (sdt ~ '^[0-9]{10}$'),
    bang_lai VARCHAR(20),
    ma_nd    VARCHAR(20) CONSTRAINT uq_tx_nd UNIQUE REFERENCES nguoi_dung(ma_nd)
);

-- 7. PHƯƠNG TIỆN
CREATE TABLE phuong_tien (
    ma_pt      VARCHAR(20) PRIMARY KEY DEFAULT ('PT' || lpad(nextval('seq_pt')::text, 5, '0')),
    bien_so    VARCHAR(20) NOT NULL UNIQUE,
    loai_xe    VARCHAR(50),
    tai_trong  NUMERIC(10,2) NOT NULL CHECK (tai_trong > 0),            -- kg
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'Sẵn sàng'
               CONSTRAINT ck_pt_trang_thai CHECK (trang_thai IN ('Sẵn sàng','Đang chạy','Bảo trì'))
);

-- 8. TUYẾN VẬN CHUYỂN (kho → kho, có hướng)
CREATE TABLE tuyen_van_chuyen (
    ma_tuyen   VARCHAR(20) PRIMARY KEY DEFAULT ('TU' || lpad(nextval('seq_tuyen')::text, 4, '0')),
    ma_kho_di  VARCHAR(20) NOT NULL REFERENCES kho(ma_kho),
    ma_kho_den VARCHAR(20) NOT NULL REFERENCES kho(ma_kho),
    khoang_cach           NUMERIC(10,2) NOT NULL CHECK (khoang_cach > 0),          -- km
    thoi_gian_du_kien_gio NUMERIC(6,2)  NOT NULL CHECK (thoi_gian_du_kien_gio > 0),
    CONSTRAINT ck_tuyen_khac_kho CHECK (ma_kho_di <> ma_kho_den),
    CONSTRAINT uq_tuyen_kho UNIQUE (ma_kho_di, ma_kho_den)
);

-- 9. CHUYẾN GIAO
--   LIEN_KHO : kho → kho theo tuyến (ma_tuyen bắt buộc)
--   GIAO_CUOI: từ kho ma_kho_giao đi giao tới địa chỉ khách (ma_tuyen NULL)
--   ngay_xuat_phat/ngay_den_du_kien = kế hoạch; ngay_xuat_phat_thuc/ngay_den = thực tế
CREATE TABLE chuyen_giao (
    ma_chuyen   VARCHAR(20) PRIMARY KEY DEFAULT ('CG' || lpad(nextval('seq_chuyen')::text, 6, '0')),
    loai_chuyen VARCHAR(20) NOT NULL DEFAULT 'LIEN_KHO'
                CONSTRAINT ck_cg_loai_val CHECK (loai_chuyen IN ('LIEN_KHO','GIAO_CUOI')),
    ma_tuyen    VARCHAR(20) REFERENCES tuyen_van_chuyen(ma_tuyen),
    ma_kho_giao VARCHAR(20) REFERENCES kho(ma_kho),
    ma_tx       VARCHAR(20) NOT NULL REFERENCES tai_xe(ma_tx),
    ma_pt       VARCHAR(20) NOT NULL REFERENCES phuong_tien(ma_pt),
    ma_nv_dieu_phoi VARCHAR(20) REFERENCES nhan_vien(ma_nv),
    ngay_xuat_phat      TIMESTAMP NOT NULL,
    ngay_den_du_kien    TIMESTAMP NOT NULL,
    ngay_xuat_phat_thuc TIMESTAMP,
    ngay_den            TIMESTAMP,
    trang_thai  VARCHAR(50) NOT NULL DEFAULT 'Chưa đi'
                CONSTRAINT ck_cg_trang_thai CHECK (trang_thai IN ('Chưa đi','Đang đi','Hoàn thành','Đã hủy')),
    CONSTRAINT ck_cg_loai CHECK (
        (loai_chuyen = 'LIEN_KHO'  AND ma_tuyen IS NOT NULL AND ma_kho_giao IS NULL) OR
        (loai_chuyen = 'GIAO_CUOI' AND ma_tuyen IS NULL     AND ma_kho_giao IS NOT NULL)),
    CONSTRAINT ck_cg_thoi_gian CHECK (ngay_den_du_kien > ngay_xuat_phat),
    CONSTRAINT ck_cg_thuc CHECK (ngay_den IS NULL OR ngay_xuat_phat_thuc IS NULL OR ngay_den >= ngay_xuat_phat_thuc),
    -- Một tài xế / một xe không được có 2 chuyến chồng thời gian
    CONSTRAINT ex_cg_tx_trung_lich EXCLUDE USING gist
        (ma_tx WITH =, tsrange(ngay_xuat_phat, ngay_den_du_kien) WITH &&) WHERE (trang_thai <> 'Đã hủy'),
    CONSTRAINT ex_cg_pt_trung_lich EXCLUDE USING gist
        (ma_pt WITH =, tsrange(ngay_xuat_phat, ngay_den_du_kien) WITH &&) WHERE (trang_thai <> 'Đã hủy')
);

-- 10. TRẠNG THÁI ĐƠN HÀNG
CREATE TABLE trang_thai_dh (
    ma_trang_thai  VARCHAR(20) PRIMARY KEY,
    ten_trang_thai VARCHAR(50) NOT NULL
);

-- 10b. QUY TRÌNH CHUYỂN TRẠNG THÁI HỢP LỆ (state machine theo dữ liệu)
CREATE TABLE quy_trinh_trang_thai (
    tt_tu  VARCHAR(20) NOT NULL REFERENCES trang_thai_dh(ma_trang_thai),
    tt_den VARCHAR(20) NOT NULL REFERENCES trang_thai_dh(ma_trang_thai),
    PRIMARY KEY (tt_tu, tt_den)
);

-- 11. ĐƠN HÀNG
CREATE TABLE don_hang (
    ma_dh          VARCHAR(20) PRIMARY KEY DEFAULT ('DH' || lpad(nextval('seq_dh')::text, 6, '0')),
    ma_kh_gui      VARCHAR(20) NOT NULL REFERENCES khach_hang(ma_kh),
    ma_kh_nhan     VARCHAR(20) REFERENCES khach_hang(ma_kh),
    sdt_nhan       VARCHAR(15) NOT NULL CONSTRAINT ck_dh_sdt CHECK (sdt_nhan ~ '^[0-9]{10}$'),
    ten_nguoi_nhan VARCHAR(100) NOT NULL,
    dia_chi_lay    TEXT NOT NULL,
    dia_chi_giao   TEXT NOT NULL,
    ma_kho_gui     VARCHAR(20) NOT NULL REFERENCES kho(ma_kho),   -- bưu cục tiếp nhận
    ma_kho_nhan    VARCHAR(20) NOT NULL REFERENCES kho(ma_kho),   -- bưu cục phụ trách giao
    ngay_tao       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    phi_van_chuyen NUMERIC(12,2) NOT NULL DEFAULT 0 CONSTRAINT ck_dh_phi CHECK (phi_van_chuyen >= 0),
    cod            NUMERIC(12,2) NOT NULL DEFAULT 0 CONSTRAINT ck_dh_cod CHECK (cod >= 0),
    ma_tt          VARCHAR(20) NOT NULL DEFAULT 'TT01' REFERENCES trang_thai_dh(ma_trang_thai),
    ma_nv          VARCHAR(20) REFERENCES nhan_vien(ma_nv)
);

-- 12. KIỆN HÀNG (ma_kho_hien_tai NULL = chưa tiếp nhận / đang trên đường)
CREATE TABLE kien_hang (
    ma_kien  VARCHAR(20) PRIMARY KEY DEFAULT ('KI' || lpad(nextval('seq_kien')::text, 7, '0')),
    ma_dh    VARCHAR(20) NOT NULL REFERENCES don_hang(ma_dh),
    khoi_luong NUMERIC(10,2) NOT NULL CHECK (khoi_luong > 0),   -- kg
    dai  NUMERIC(8,2) NOT NULL CHECK (dai  > 0),                -- cm
    rong NUMERIC(8,2) NOT NULL CHECK (rong > 0),
    cao  NUMERIC(8,2) NOT NULL CHECK (cao  > 0),
    loai_hang VARCHAR(50),
    ma_kho_hien_tai VARCHAR(20) REFERENCES kho(ma_kho)
);

-- 13. CHI TIẾT CHUYẾN GIAO
CREATE TABLE chi_tiet_chuyen_giao (
    ma_chuyen VARCHAR(20) NOT NULL REFERENCES chuyen_giao(ma_chuyen),
    ma_kien   VARCHAR(20) NOT NULL REFERENCES kien_hang(ma_kien),
    thoi_gian_gan  TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    thoi_gian_quet TIMESTAMP,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'Đã gán'
               CONSTRAINT ck_ctcg_trang_thai CHECK (trang_thai IN ('Đã gán','Đã quét','Đã giao','Thất bại')),
    ghi_chu TEXT,
    PRIMARY KEY (ma_chuyen, ma_kien)
);

-- 14. LỊCH SỬ TRẠNG THÁI
CREATE TABLE lich_su_trang_thai (
    ma_ls    VARCHAR(20) PRIMARY KEY DEFAULT ('LS' || lpad(nextval('seq_ls')::text, 8, '0')),
    ma_dh    VARCHAR(20) NOT NULL REFERENCES don_hang(ma_dh),
    ma_tt    VARCHAR(20) NOT NULL REFERENCES trang_thai_dh(ma_trang_thai),
    thoi_gian TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    ma_nd    VARCHAR(20) REFERENCES nguoi_dung(ma_nd),
    ghi_chu  TEXT
);

-- 15. THANH TOÁN
CREATE TABLE thanh_toan (
    ma_tt_toan VARCHAR(20) PRIMARY KEY DEFAULT ('TTO' || lpad(nextval('seq_ttoan')::text, 7, '0')),
    ma_dh      VARCHAR(20) NOT NULL REFERENCES don_hang(ma_dh),
    loai_khoan VARCHAR(10) NOT NULL CONSTRAINT ck_tt_loai CHECK (loai_khoan IN ('PHI_VC','COD')),
    so_tien    NUMERIC(12,2) NOT NULL CHECK (so_tien > 0),
    phuong_thuc VARCHAR(50) CONSTRAINT ck_tt_pt CHECK (phuong_thuc IN ('Tiền mặt','Chuyển khoản','COD')),
    trang_thai VARCHAR(50) NOT NULL DEFAULT 'Chưa thanh toán'
               CONSTRAINT ck_tt_trang_thai CHECK (trang_thai IN ('Chưa thanh toán','Đã thanh toán')),
    thoi_gian  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nguoi_thanh_toan VARCHAR(100)
);
-- mỗi loại khoản chỉ được thanh toán thành công 1 lần / đơn
CREATE UNIQUE INDEX uq_thanh_toan_da_tt ON thanh_toan(ma_dh, loai_khoan) WHERE trang_thai = 'Đã thanh toán';

-- 16. CẤU HÌNH HỆ THỐNG (đơn giá tính phí...)
CREATE TABLE cau_hinh_he_thong (
    khoa    VARCHAR(50) PRIMARY KEY,
    gia_tri NUMERIC(14,2) NOT NULL,
    mo_ta   TEXT
);

-- 17. NHẬT KÝ HỆ THỐNG
CREATE TABLE nhat_ky_he_thong (
    ma_nk BIGSERIAL PRIMARY KEY,
    ten_bang VARCHAR(50) NOT NULL,
    hanh_dong VARCHAR(10) NOT NULL,
    khoa VARCHAR(50),
    du_lieu_cu JSONB,
    du_lieu_moi JSONB,
    ma_nd VARCHAR(20),
    thoi_gian TIMESTAMP NOT NULL DEFAULT clock_timestamp()
);

-- ---------- INDEX (PostgreSQL không tự tạo index cho FK) ----------
CREATE INDEX ix_dh_kh_gui   ON don_hang(ma_kh_gui);
CREATE INDEX ix_dh_kh_nhan  ON don_hang(ma_kh_nhan);
CREATE INDEX ix_dh_tt       ON don_hang(ma_tt);
CREATE INDEX ix_dh_ngay_tao ON don_hang(ngay_tao);
CREATE INDEX ix_dh_nv       ON don_hang(ma_nv);
CREATE INDEX ix_dh_kho      ON don_hang(ma_kho_gui, ma_kho_nhan);
CREATE INDEX ix_kien_dh     ON kien_hang(ma_dh);
CREATE INDEX ix_kien_kho    ON kien_hang(ma_kho_hien_tai);
CREATE INDEX ix_cg_tx       ON chuyen_giao(ma_tx);
CREATE INDEX ix_cg_pt       ON chuyen_giao(ma_pt);
CREATE INDEX ix_cg_tuyen    ON chuyen_giao(ma_tuyen);
CREATE INDEX ix_cg_tt       ON chuyen_giao(trang_thai);
CREATE INDEX ix_ctcg_kien   ON chi_tiet_chuyen_giao(ma_kien);
CREATE INDEX ix_ls_dh       ON lich_su_trang_thai(ma_dh, thoi_gian);
CREATE INDEX ix_tt_dh       ON thanh_toan(ma_dh);
CREATE INDEX ix_tt_thoi_gian ON thanh_toan(thoi_gian);
CREATE INDEX ix_nk_bang     ON nhat_ky_he_thong(ten_bang, khoa);

-- ---------- DỮ LIỆU DANH MỤC MẶC ĐỊNH ----------
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('ADMIN',      'Quản trị viên',       'Quản lý toàn bộ hệ thống'),
('NVBC',       'Nhân viên bưu cục',   'Tiếp nhận và xử lý đơn tại kho'),
('NVDP',       'Nhân viên điều phối', 'Quản lý tuyến và chuyến giao'),
('TAI_XE',     'Tài xế',              'Thực hiện vận chuyển và giao hàng'),
('KHACH_HANG', 'Khách hàng',          'Người gửi và người nhận hàng');

INSERT INTO trang_thai_dh (ma_trang_thai, ten_trang_thai) VALUES
('TT01','Mới tạo'),('TT02','Đã tiếp nhận'),('TT03','Đang vận chuyển'),('TT04','Đã đến kho'),
('TT05','Đang giao hàng'),('TT06','Giao thành công'),('TT07','Hủy / Trả hàng'),('TT08','Giao thất bại');

INSERT INTO quy_trinh_trang_thai (tt_tu, tt_den) VALUES
('TT01','TT02'),('TT02','TT03'),('TT03','TT04'),('TT04','TT03'),   -- TT04→TT03: chuyển tiếp kho khác
('TT04','TT05'),('TT05','TT06'),('TT05','TT08'),('TT08','TT05'),   -- TT08→TT05: giao lại
('TT02','TT05'),                                                    -- đơn nội bưu cục giao thẳng, không qua liên kho
('TT01','TT07'),('TT02','TT07'),('TT03','TT07'),('TT04','TT07'),('TT05','TT07'),('TT08','TT07');

INSERT INTO cau_hinh_he_thong (khoa, gia_tri, mo_ta) VALUES
('PHI_CO_BAN',     15000, 'Cước cơ bản mỗi kiện (VNĐ)'),
('KG_MIEN_PHI',    0.5,   'Khối lượng (kg) đã gồm trong cước cơ bản'),
('PHI_MOI_KG',     5000,  'Đơn giá mỗi kg vượt (VNĐ/kg)'),
('PHI_MOI_KM',     300,   'Đơn giá mỗi km (VNĐ/km)'),
('HE_SO_QUY_DOI',  5000,  'Hệ số quy đổi thể tích: (D×R×C cm³) / hệ số = kg');