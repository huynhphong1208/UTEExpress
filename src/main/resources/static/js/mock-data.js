/**
 * UTE EXPRESS LOGISTICS - MOCK DATA
 * Dữ liệu giả lập chuẩn theo schema.sql phục vụ prototype & demo UI/UX
 */

const UTE_DATA = {
    // 1. Kho / Bưu cục
    warehouses: [
        { ma_kho: 'KHO0001', ten_kho: 'Kho Trung tâm Thủ Đức (Hub)', dia_chi: '01 Võ Văn Ngân, TP. Thủ Đức, TP.HCM', sdt: '02838968641' },
        { ma_kho: 'KHO0002', ten_kho: 'Bưu cục Quận 1', dia_chi: '125 Hai Bà Trưng, P. Bến Nghé, Quận 1, TP.HCM', sdt: '02838221234' },
        { ma_kho: 'KHO0003', ten_kho: 'Bưu cục Tân Bình', dia_chi: '45 Cộng Hòa, P. 4, Q. Tân Bình, TP.HCM', sdt: '02838445678' },
        { ma_kho: 'KHO0004', ten_kho: 'Bưu cục Biên Hòa', dia_chi: '12 Đồng Khởi, TP. Biên Hòa, Đồng Nai', sdt: '02513829999' },
        { ma_kho: 'KHO0005', ten_kho: 'Bưu cục Dĩ An', dia_chi: '88 Lý Thường Kiệt, TP. Dĩ An, Bình Dương', sdt: '02743778899' }
    ],

    // 2. Tuyến vận chuyển
    routes: [
        { ma_tuyen: 'TU0001', ma_kho_di: 'KHO0001', ten_kho_di: 'Kho TT Thủ Đức', ma_kho_den: 'KHO0002', ten_kho_den: 'Bưu cục Quận 1', khoang_cach: 15.5, thoi_gian_du_kien_gio: 0.8 },
        { ma_tuyen: 'TU0002', ma_kho_di: 'KHO0001', ten_kho_di: 'Kho TT Thủ Đức', ma_kho_den: 'KHO0003', ten_kho_den: 'Bưu cục Tân Bình', khoang_cach: 18.2, thoi_gian_du_kien_gio: 1.0 },
        { ma_tuyen: 'TU0003', ma_kho_di: 'KHO0001', ten_kho_di: 'Kho TT Thủ Đức', ma_kho_den: 'KHO0004', ten_kho_den: 'Bưu cục Biên Hòa', khoang_cach: 22.0, thoi_gian_du_kien_gio: 1.2 },
        { ma_tuyen: 'TU0004', ma_kho_di: 'KHO0001', ten_kho_di: 'Kho TT Thủ Đức', ma_kho_den: 'KHO0005', ten_kho_den: 'Bưu cục Dĩ An', khoang_cach: 12.0, thoi_gian_du_kien_gio: 0.6 }
    ],

    // 3. Phương tiện
    vehicles: [
        { ma_pt: 'PT00001', bien_so: '51C-987.65', loai_xe: 'Xe tải 1.5 tấn', tai_trong: 1500, trang_thai: 'Sẵn sàng' },
        { ma_pt: 'PT00002', bien_so: '51D-123.45', loai_xe: 'Xe tải 2.5 tấn', tai_trong: 2500, trang_thai: 'Sẵn sàng' },
        { ma_pt: 'PT00003', bien_so: '50A-555.88', loai_xe: 'Xe tải 5.0 tấn', tai_trong: 5000, trang_thai: 'Đang chạy' },
        { ma_pt: 'PT00004', bien_so: '59B-444.22', loai_xe: 'Xe van 850kg', tai_trong: 850, trang_thai: 'Bảo trì' }
    ],

    // 4. Tài xế
    drivers: [
        { ma_tx: 'TX00001', ho_ten: 'Trần Văn Dũng', sdt: '0903112233', bang_lai: 'Hạng C', trang_thai: 'Sẵn sàng' },
        { ma_tx: 'TX00002', ho_ten: 'Lê Hoàng Nam', sdt: '0918445566', bang_lai: 'Hạng D', trang_thai: 'Đang chạy' },
        { ma_tx: 'TX00003', ho_ten: 'Phạm Minh Tuấn', sdt: '0977889900', bang_lai: 'Hạng C', trang_thai: 'Sẵn sàng' }
    ],

    // 5. Đơn hàng mẫu
    orders: [
        {
            ma_dh: 'DH000101',
            nguoi_gui: 'Nguyễn Văn An',
            sdt_gui: '0912345678',
            dia_chi_gui: 'Số 1 Đỗ Mười, P. Linh Trung, Thủ Đức, TP.HCM',
            nguoi_nhan: 'Trần Thị Bích',
            sdt_nhan: '0987654321',
            dia_chi_nhan: '48 Lê Lợi, P. Bến Nghé, Quận 1, TP.HCM',
            ngay_tao: '2026-09-22 08:30',
            khoi_luong: 2.5,
            loai_hang: 'Điện tử / Thiết bị',
            phi_vc: 35000,
            tien_cod: 550000,
            tong_tien: 585000,
            trang_thai: 'Đang vận chuyển',
            trang_thai_tt: 'Đã thanh toán',
            phuong_thuc_tt: 'Chuyển khoản'
        },
        {
            ma_dh: 'DH000102',
            nguoi_gui: 'Cty TNHH Thiết bị Sao Mai',
            sdt_gui: '0903998877',
            dia_chi_gui: 'Khu CNC Thủ Đức, TP.HCM',
            nguoi_nhan: 'Hoàng Minh Quân',
            sdt_nhan: '0934123456',
            dia_chi_nhan: '12 Hoàng Hoa Thám, Q. Tân Bình, TP.HCM',
            ngay_tao: '2026-09-22 09:15',
            khoi_luong: 5.0,
            loai_hang: 'Gia dụng',
            phi_vc: 45000,
            tien_cod: 1200000,
            tong_tien: 1245000,
            trang_thai: 'Mới tạo',
            trang_thai_tt: 'Chưa thanh toán',
            phuong_thuc_tt: 'COD'
        },
        {
            ma_dh: 'DH000103',
            nguoi_gui: 'Shop Thời trang H&M Express',
            sdt_gui: '0988112233',
            dia_chi_gui: 'Bưu cục Quận 1, TP.HCM',
            nguoi_nhan: 'Lê Thu Thảo',
            sdt_nhan: '0966554433',
            dia_chi_nhan: '77 Nguyễn Ái Quốc, TP. Biên Hòa',
            ngay_tao: '2026-09-21 14:20',
            khoi_luong: 1.2,
            loai_hang: 'Thời trang / May mặc',
            phi_vc: 28000,
            tien_cod: 350000,
            tong_tien: 378000,
            trang_thai: 'Giao thành công',
            trang_thai_tt: 'Đã thanh toán',
            phuong_thuc_tt: 'Ví điện tử'
        },
        {
            ma_dh: 'DH000104',
            nguoi_gui: 'Phạm Đức Long',
            sdt_gui: '0944778899',
            dia_chi_gui: '22 Kha Vạn Cân, Thủ Đức',
            nguoi_nhan: 'Vũ Quốc Cường',
            sdt_nhan: '0911223344',
            dia_chi_nhan: '102 Nguyễn Trãi, P. Dĩ An, Bình Dương',
            ngay_tao: '2026-09-22 10:00',
            khoi_luong: 0.8,
            loai_hang: 'Tài liệu / Chứng từ',
            phi_vc: 22000,
            tien_cod: 0,
            tong_tien: 22000,
            trang_thai: 'Đang giao',
            trang_thai_tt: 'Đã thanh toán',
            phuong_thuc_tt: 'Chuyển khoản'
        }
    ]
};
