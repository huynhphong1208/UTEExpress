# 📦 TÀI LIỆU BÀN GIAO & TÍCH HỢP PHÂN HỆ LOGISTICS (ĐIỀU PHỐI & TÀI XẾ)
> **Branch**: `feature/logistics`  
> **Dự án**: UTE Express — Hệ thống Quản lý Vận chuyển & Giao nhận thông minh  
> **Phiên bản tài liệu**: 1.0.0  
> **Mục đích**: Cung cấp toàn bộ thông tin kiến trúc, cơ sở dữ liệu, danh mục API, giao diện người dùng và hướng dẫn tích hợp để chuẩn bị gộp (merge) nhánh `feature/logistics` vào nhánh chính (`main`/`develop`) cùng các nhánh chức năng khác (Auth, Order, Warehouse, Admin).

---

## 📑 MỤC LỤC
1. [Tổng quan phân hệ](#1-tổng-quan-phân-hệ)
2. [Cấu trúc mã nguồn trên nhánh](#2-cấu-trúc-mã-nguồn-trên-nhánh)
3. [Cơ sở dữ liệu & Thứ tự chạy Scripts](#3-cơ-sở-dữ-liệu--thứ-tự-chạy-scripts)
4. [Kiến trúc Backend & Danh mục REST API](#4-kiến-trúc-backend--danh-mục-rest-api)
5. [Quy tắc nghiệp vụ cốt lõi (Business Rules)](#5-quy-tắc-nghiệp-vụ-cốt-lõi-business-rules)
6. [Giao diện Người dùng (UI Frontend)](#6-giao-diện-người-dùng-ui-frontend)
7. [Hướng dẫn tích hợp khi Merge với các nhánh khác](#7-hướng-dẫn-tích-hợp-khi-merge-với-các-nhánh-khác)
8. [Checklist kiểm thử sau khi Merge (Verification Checklist)](#8-checklist-kiểm-thử-sau-khi-merge-verification-checklist)

---

## 1. TỔNG QUAN PHÂN HỆ

Phân hệ Logistics phụ trách toàn bộ chuỗi mắt xích vận chuyển hàng hóa từ khi kiện hàng nằm tại kho cho đến khi được điều phối lên các chuyến xe và tài xế hoàn tất phát hàng đến tay người nhận.

Phân hệ gồm 2 nhóm đối tượng người dùng chính:

### 1.1. Nhân viên Điều phối (Dispatcher / NVDP)
- **UC12 — Giám sát & Quản lý vận hành (Dashboard)**: Theo dõi tổng quan mạng lưới vận chuyển theo thời gian thực (tổng số chuyến, chuyến đang chạy, chuyến hoàn thành, chuyến trễ hạn, tỷ lệ lấp đầy tải trọng xe, biểu đồ phân bổ trạng thái).
- **UC13 — Quản lý Tuyến vận chuyển (Routes)**: Quản lý danh mục các tuyến kết nối giữa các kho (CRUD tuyến, tính toán khoảng cách km, thời gian dự kiến di chuyển).
- **UC14 — Quản lý & Lập lịch Chuyến giao (Trips & Capacity)**: 
  - Khởi tạo chuyến giao: Chọn loại chuyến (**Liên kho** - `LIEN_KHO` hoặc **Giao cuối** - `GIAO_CUOI`).
  - Phân công phương tiện và tài xế phù hợp với tải trọng và bằng lái.
  - Gán kiện hàng vào chuyến có kiểm tra tự động giới hạn tải trọng xe và loại hình chuyến.
  - Theo dõi lộ trình, lệnh xuất phát chuyến hoặc hủy chuyến khi có sự cố.

### 1.2. Tài xế (Driver / TX)
- **Dashboard Tài xế**: Xem ca làm việc, tổng hợp chỉ số cá nhân (số chuyến cần giao hôm nay, số kiện đã phát thành công, tiền COD cần nộp, danh sách chuyến đang hoạt động).
- **Chi tiết chuyến giao của tôi**: Xem danh sách kiện hàng của chuyến được phân công, xác nhận nhận chuyến, chuyển trạng thái xe sang xuất phát hành trình, xác nhận đến kho đích đối với chuyến liên kho.
- **Quét kiện & Cập nhật kết quả phát hàng**: Quét mã vạch kiện hàng hoặc chọn từ danh sách; cập nhật trạng thái **Giao thành công** (ghi nhận ảnh chụp, chữ ký, thu tiền COD) hoặc **Giao thất bại** (ghi nhận lý do: khách hẹn lại, không liên lạc được, sai địa chỉ,... gọi Stored Procedure cập nhật số lần giao và lịch sử trạng thái).

---

## 2. CẤU TRÚC MÃ NGUỒN TRÊN NHÁNH

```
UTEExpress/
├── database/                                 # Kịch bản cơ sở dữ liệu PostgreSQL
│   ├── schema.sql                           # DDL: Khởi tạo tất cả các bảng, ràng buộc, index
│   ├── logic.sql                            # DDL: Stored Procedures, Functions, Triggers
│   ├── seed_and_test.sql                    # DML: Dữ liệu mẫu khởi tạo nền tảng
│   └── seed_extra.sql                       # DML: Dữ liệu test mở rộng cho nhiều kho (KHO01, KHO03, KHO04)
├── src/main/java/com/example/backend/
│   ├── config/
│   │   ├── SecurityConfig.java              # Cấu hình Spring Security & phân quyền endpoint
│   │   └── WebConfig.java                   # Cấu hình CORS & Static resource handlers
│   ├── controller/
│   │   ├── DieuPhoiController.java          # REST API cho phân hệ Nhân viên Điều phối (/api/dieu-phoi)
│   │   └── TaiXeController.java             # REST API cho phân hệ Tài xế (/api/tai-xe)
│   ├── dto/                                 # Data Transfer Objects cho Request & Response
│   │   ├── ApiResponse.java                 # Standard Response format {success, message, data, timestamp}
│   │   ├── DieuPhoiDashboardDTO.java        # Chỉ số KPI và biểu đồ dashboard điều phối
│   │   ├── ChuyenGiaoChiTietDTO.java        # Chi tiết chuyến giao + danh sách kiện + % tải trọng
│   │   ├── DonCanDieuPhoiDTO.java           # Kiện hàng chờ điều phối (kèm flag phuHopLoaiChuyen)
│   │   ├── GanKienRequest.java              # Request gán kiện vào chuyến giao
│   │   ├── TaoChuyenGiaoRequest.java        # Request tạo chuyến giao mới
│   │   ├── XuatPhatChuyenRequest.java       # Request xuất phát chuyến
│   │   ├── TaiXeDashboardDTO.java           # Chỉ số và danh sách chuyến cho tài xế
│   │   ├── QuetKienRequest.java             # Request quét barcode kiểm tra kiện
│   │   ├── GiaoThanhCongRequest.java        # Request ghi nhận kết quả phát hàng thành công
│   │   ├── GiaoThatBaiRequest.java          # Request ghi nhận kết quả phát hàng thất bại
│   │   └── ...
│   ├── entity/                              # JPA Entities ánh xạ CSDL
│   │   ├── ChuyenGiao.java, ChiTietChuyenGiao.java, ChiTietChuyenGiaoId.java
│   │   ├── TuyenVanChuyen.java, PhuongTien.java, TaiXe.java, KienHang.java
│   │   └── Kho.java, NhanVien.java
│   ├── repository/                          # Spring Data JPA Repositories
│   │   ├── ChuyenGiaoRepository.java, ChiTietChuyenGiaoRepository.java
│   │   ├── TuyenVanChuyenRepository.java, PhuongTienRepository.java
│   │   ├── TaiXeRepository.java, KienHangRepository.java, KhoRepository.java
│   │   └── ...
│   └── service/                             # Business Logic Layer
│       ├── DieuPhoiService.java             # Nghiệp vụ điều phối, tải trọng, kiểm tra ràng buộc kiện
│       └── TaiXeService.java                # Nghiệp vụ tài xế, gọi SP cập nhật kết quả giao, nhận chuyến
├── src/main/resources/
│   ├── application.properties               # Cấu hình kết nối DB, Server port, JPA/Hibernate
│   ├── static/                              # Tài nguyên tĩnh
│   │   ├── css/
│   │   │   ├── design-system.css            # Biến màu sắc, Typography, Tokens chuẩn
│   │   │   ├── layout.css                   # Header, Sidebar, Grid layouts
│   │   │   └── components.css               # Cards, Tables, Badges, Modals, Toasts
│   │   └── js/
│   │       ├── api-client.js                # Fetch API client chuẩn hóa gọi Backend REST API
│   │       └── components.js                # Helpers: Toasts, Modals, Status Badges, Formatters
│   └── templates/
│       ├── dispatcher/                      # Giao diện Điều phối
│       │   ├── 12-dashboard.html            # Dashboard giám sát mạng lưới
│       │   ├── 13-routes.html               # Quản lý tuyến vận chuyển
│       │   └── 14-trips.html                # Quản lý chuyến giao & phân bổ tải trọng
│       └── driver/                          # Giao diện Tài xế
│           ├── 15-dashboard.html            # Dashboard nhiệm vụ tài xế
│           ├── 16-trip-detail.html          # Chi tiết chuyến & lộ trình di chuyển
│           └── 17-delivery-update.html      # Quét kiện & cập nhật kết quả giao hàng
└── Readme_logictis.md                       # Tài liệu này
```

---

## 3. CƠ SỞ DỮ LIỆU & THỨ TỰ CHẠY SCRIPTS

Hệ thống cơ sở dữ liệu được xây dựng trên **PostgreSQL 18** (tương thích từ PostgreSQL 14+), tận dụng tối đa Stored Procedures và Triggers để đảm bảo tính toàn vẹn dữ liệu.

### 3.1. Thứ tự thực thi bắt buộc
Khi cài đặt mới hoặc thiết lập môi trường CI/CD, chạy các file theo đúng thứ tự sau:

```bash
# 1. Tạo bảng, khóa chính, khóa ngoại, indexes
psql -U postgres -d ute_express -f database/schema.sql

# 2. Tạo Functions, Stored Procedures, Triggers nghiệp vụ
psql -U postgres -d ute_express -f database/logic.sql

# 3. Nạp dữ liệu nền tảng cơ bản (kho, tài khoản, phương tiện, tuyến, kiện mẫu)
psql -U postgres -d ute_express -f database/seed_and_test.sql

# 4. Nạp dữ liệu test mở rộng (đa dạng kho xuất phát KHO01, KHO03, KHO04 để test đầy đủ các ca)
psql -U postgres -d ute_express -f database/seed_extra.sql
```

### 3.2. Các bảng liên quan trực tiếp đến Logistics
| Tên bảng | Ý nghĩa | Các trường quan trọng |
|---|---|---|
| `chuyengiao` | Chuyến xe vận chuyển | `machuyen`, `loai_chuyen` (`LIEN_KHO`/`GIAO_CUOI`), `matuyen`, `maphuongtien`, `mataixe`, `makho_xuatphat`, `makho_dich`, `trangthai`, `taitrong_dadung`, `taitrong_toida` |
| `chitietchuyengiao` | Kiện hàng gán trong chuyến | `machuyen`, `makienhang`, `thutugian`, `trangthaigiao`, `lydo_thatbai` |
| `tuyen_vanchuyen` | Tuyến đường kết nối 2 kho | `matuyen`, `tentuyen`, `makho_di`, `makho_den`, `khoangcach_km`, `thoigian_dukien_gio` |
| `phuongtien` | Phương tiện vận chuyển | `maphuongtien`, `bienso`, `taitrong_kg`, `thetich_m3`, `loai_xe`, `trangthai` (`SAN_SANG`, `DANG_CHAY`, `BAO_TRI`) |
| `taixe` | Tài xế phụ trách | `mataixe`, `manguoidung`, `hang_banglai`, `trangthai` (`SAN_SANG`, `DANG_CHAY`, `NGHI_PHEP`) |
| `kienhang` | Kiện hàng | `makienhang`, `madonhang`, `makho_hientai`, `makho_tieptheo`, `diachi_giao`, `khoiluong_kg`, `thetich_m3`, `trangthai` |
| `lichsu_kienhang` | Nhật ký di chuyển kiện | Tự động ghi nhận qua Trigger khi trạng thái kiện thay đổi |

### 3.3. Stored Procedures & Triggers trọng tâm
- `sp_tao_chuyen_giao(...)`: Tạo chuyến và khóa trạng thái phương tiện/tài xế.
- `sp_gan_kien_vao_chuyen(...)`: Gán kiện hàng vào chuyến, tự động kiểm tra vượt tải trọng phương tiện và cộng dồn `taitrong_dadung`.
- `sp_xuat_phat_chuyen_giao(...)`: Chuyển trạng thái chuyến sang `DANG_DI_CHUYEN`, đồng thời cập nhật toàn bộ kiện trong chuyến sang `DANG_GIAO`.
- `sp_cap_nhat_ket_qua_phat_hang(...)`: Cập nhật trạng thái phát hàng thành công hoặc thất bại. Tự động tăng số lần giao (`so_lan_giao`). Nếu vượt quá ngưỡng (thường là 3 lần), chuyển kiện sang trạng thái hoàn hàng `CHO_XU_LY_HOAN`.

---

## 4. KIẾN TRÚC BACKEND & DANH MỤC REST API

Tất cả API đều trả về định dạng đồng nhất qua lớp `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Thông báo kết quả",
  "data": { ... },
  "timestamp": "2026-10-10T15:30:00"
}
```

### 4.1. Nhóm API Điều phối (`/api/dieu-phoi`)

| Method | Endpoint | Mô tả | Tham số / Body |
|---|---|---|---|
| `GET` | `/api/dieu-phoi/dashboard` | Lấy dữ liệu KPIs và thống kê dashboard | `?maKho=KHO01` (optional) |
| `GET` | `/api/dieu-phoi/tuyen` | Danh sách tất cả các tuyến vận chuyển | - |
| `GET` | `/api/dieu-phoi/tuyen/{maTuyen}` | Xem chi tiết 1 tuyến | - |
| `POST` | `/api/dieu-phoi/tuyen` | Tạo tuyến vận chuyển mới | `{"maTuyen", "tenTuyen", "maKhoDi", "maKhoDen", "khoangCach", "thoiGianDuKienGio"}` |
| `PUT` | `/api/dieu-phoi/tuyen/{maTuyen}` | Cập nhật thông tin tuyến | Thông tin cập nhật |
| `DELETE` | `/api/dieu-phoi/tuyen/{maTuyen}` | Xóa tuyến vận chuyển | - |
| `GET` | `/api/dieu-phoi/chuyen-giao` | Danh sách chuyến giao kèm bộ lọc | `?trangThai=...&maKho=...&loaiChuyen=...` |
| `GET` | `/api/dieu-phoi/chuyen-giao/{maChuyen}` | Chi tiết chuyến, danh sách kiện và % tải trọng | - |
| `POST` | `/api/dieu-phoi/chuyen-giao` | Lập lịch / Tạo chuyến giao mới | `TaoChuyenGiaoRequest` |
| `PUT` | `/api/dieu-phoi/chuyen-giao/{maChuyen}` | Cập nhật thông tin chuyến giao | `CapNhatChuyenGiaoRequest` |
| `POST` | `/api/dieu-phoi/chuyen-giao/{maChuyen}/gan-kien` | Gán kiện hàng vào chuyến | `{"maKienHang": "KH001"}` |
| `DELETE` | `/api/dieu-phoi/chuyen-giao/{maChuyen}/xoa-kien/{maKienHang}` | Gỡ kiện hàng khỏi chuyến | - |
| `POST` | `/api/dieu-phoi/chuyen-giao/{maChuyen}/xuat-phat` | Phát lệnh xuất phát chuyến xe | `{"maNhanVien": "NV003"}` |
| `POST` | `/api/dieu-phoi/chuyen-giao/{maChuyen}/huy` | Hủy chuyến giao (hoàn trả trạng thái kiện & xe) | `?lyDo=...` |
| `GET` | `/api/dieu-phoi/don-can-dieu-phoi` | Lấy danh sách kiện chờ điều phối | `?maKho=KHO01&loaiChuyen=LIEN_KHO` |
| `GET` | `/api/dieu-phoi/phuong-tien-kha-dung` | Danh sách xe sẵn sàng tại kho | `?maKho=KHO01` |
| `GET` | `/api/dieu-phoi/tai-xe-kha-dung` | Danh sách tài xế sẵn sàng tại kho | `?maKho=KHO01` |

### 4.2. Nhóm API Tài xế (`/api/tai-xe`)

| Method | Endpoint | Mô tả | Tham số / Body |
|---|---|---|---|
| `GET` | `/api/tai-xe/dashboard/{maTaiXe}` | Thống kê số chuyến, kiện và COD cho tài xế | - |
| `GET` | `/api/tai-xe/chuyen-giao/{maTaiXe}` | Danh sách tất cả chuyến được phân công cho tài xế | `?trangThai=...` (optional) |
| `GET` | `/api/tai-xe/chuyen-giao/{maTaiXe}/{maChuyen}` | Xem chi tiết chuyến & danh sách kiện của tài xế | - |
| `POST` | `/api/tai-xe/chuyen-giao/{maChuyen}/nhan-chuyen` | Tài xế bấm nhận nhiệm vụ | `?maTaiXe=TX001` |
| `POST` | `/api/tai-xe/chuyen-giao/{maChuyen}/xuat-phat` | Tài xế bấm xuất phát | `?maTaiXe=TX001` |
| `POST` | `/api/tai-xe/chuyen-giao/{maChuyen}/den-kho-dich` | Hoàn thành chuyến liên kho khi đến đích | `?maTaiXe=TX001` |
| `POST` | `/api/tai-xe/quet-kien` | Quét mã barcode kiểm tra kiện có trong chuyến không | `{"maChuyen": "...", "maKienHang": "..."}` |
| `POST` | `/api/tai-xe/giao-thanh-cong` | Cập nhật phát hàng thành công | `{"maChuyen", "maKienHang", "anhXacNhan", "chuKy", "soTienThuCod", "nguoiCapNhat"}` |
| `POST` | `/api/tai-xe/giao-that-bai` | Cập nhật phát hàng thất bại | `{"maChuyen", "maKienHang", "lyDo", "anhXacNhan", "ngayHenGiaoLai", "nguoiCapNhat"}` |

---

## 5. QUY TẮC NGHIỆP VỤ CỐT LÕI (BUSINESS RULES)

### 5.1. Ràng buộc Loại Chuyến giao vs Loại Kiện hàng
Hệ thống phân biệt rõ 2 loại chuyến:
1. **Chuyến Liên kho (`LIEN_KHO`)**:
   - Vận chuyển kiện từ kho xuất phát (`maKhoXuatPhat`) đến kho trung chuyển tiếp theo (`maKhoDich`).
   - Tuyến đường bắt buộc kết nối giữa 2 kho trong bảng `tuyen_vanchuyen`.
   - **Ràng buộc kiện**: Chỉ cho phép gán kiện có `maKhoTiepTheo` trùng với `maKhoDich` của chuyến. Nếu kiện là kiện giao chặng cuối cho khách (đã đến kho đích cuối cùng), hệ thống sẽ chặn không cho gán vào chuyến liên kho.
2. **Chuyến Giao cuối (`GIAO_CUOI`)**:
   - Vận chuyển kiện từ kho địa phương giao tận tay người nhận trong cùng khu vực quận/huyện.
   - Không yêu cầu chọn mã tuyến liên kho (`maTuyen = null`). `maKhoDich` của chuyến được đặt bằng chính kho xuất phát hoặc địa bàn giao hàng.
   - **Ràng buộc kiện**: Chỉ cho phép gán kiện mà `maKhoHienTai` chính là kho xuất phát và kiện đã sẵn sàng để phát hàng cho khách (`maKhoTiepTheo` rỗng hoặc kiện đang ở kho chặng cuối).

> **Frontend Validation & Backend Protection**:
> - API `GET /api/dieu-phoi/don-can-dieu-phoi?maKho=...&loaiChuyen=...` tự động tính toán trường boolean `phuHopLoaiChuyen` để giao diện hiển thị badge "Phù hợp" hoặc "Không phù hợp".
> - Giao diện tự động disable checkbox và hiển thị tooltip cảnh báo nếu người dùng chọn kiện không khớp với loại chuyến.
> - Backend `DieuPhoiService.java` và CSDL kiểm tra nghiêm ngặt trước khi thực thi `sp_gan_kien_vao_chuyen`.

### 5.2. Kiểm soát Tải trọng Phương tiện
- Mỗi phương tiện có `taiTrongKg` và `theTichM3` tối đa.
- Khi gán kiện vào chuyến, `taitrong_dadung` được cộng dồn.
- Nếu `taitrong_dadung + khoiluong_kien > taitrong_toida`, hệ thống từ chối gán và trả về thông báo lỗi chi tiết.
- Giao diện có thanh tiến trình (progress bar) thể hiện trực quan tỷ lệ % tải trọng (Xanh < 70%, Vàng 70–90%, Đỏ > 90%).

### 5.3. Vòng đời Trạng thái Chuyến giao (State Machine)
```
[TAO_MOI] (Tạo mới & Gán kiện)
    │
    ▼
[CHO_XUAT_PHAT] (Tài xế nhận chuyến / Điều phối duyệt)
    │
    ▼
[DANG_DI_CHUYEN] (Xuất phát chuyến xe)
    │
    ├─────────────────────────────┬─────────────────────────────┐
    ▼ (Chuyến Liên kho)           ▼ (Chuyến Giao cuối)          ▼ (Sự cố)
[HOAN_THANH]                  [HOAN_THANH]                  [DA_HUY]
(Xe tới kho đích, kiện        (Tất cả kiện đã được phát     (Hoàn trả kiện về kho,
chuyển trạng thái nhập kho)   hoặc ghi nhận kết quả)        giải phóng tài xế & xe)
```

---

## 6. GIAO DIỆN NGƯỜI DÙNG (UI FRONTEND)

Toàn bộ UI tuân thủ hệ thống **UTE Express Design System**:
- **Tone màu chủ đạo**: Navy Blue (`#0B192C`, `#1E3E62`), Brand Red (`#E53935` / `#DC2626`), Background Clean White/Light Slate (`#F8FAFC`).
- **Font**: Inter / Roboto, hỗ trợ responsive hoàn hảo trên máy tính bàn (Điều phối) và thiết bị di động (Tài xế).
- **Không phụ thuộc thư viện ngoài nặng nề**: Sử dụng Vanilla JS, Vanilla CSS, giao tiếp qua `api-client.js`.

### Danh sách màn hình:
1. `dispatcher/12-dashboard.html`: Màn hình Giám sát Điều phối (KPIs, biểu đồ chuyến xe, danh sách chuyến đang di chuyển).
2. `dispatcher/13-routes.html`: Màn hình Quản lý Tuyến vận chuyển (Bảng tuyến, Modal tạo/sửa tuyến, cảnh báo kết nối kho).
3. `dispatcher/14-trips.html`: Màn hình Quản lý Chuyến giao & Phân bổ tải trọng (Bộ lọc đa năng, Modal tạo chuyến, Modal gán kiện kiểm soát tải trọng theo thời gian thực).
4. `driver/15-dashboard.html`: Dashboard nhiệm vụ Tài xế (Chỉ số cá nhân, các tab chuyến cần thực hiện).
5. `driver/16-trip-detail.html`: Chi tiết chuyến giao dành cho tài xế (Nút nhận chuyến, xuất phát, lộ trình, danh sách kiện cần phát).
6. `driver/17-delivery-update.html`: Quét barcode & Cập nhật kết quả phát hàng (Xem chi tiết kiện, chụp ảnh xác nhận, chữ ký số, nhập tiền COD hoặc chọn lý do thất bại).

---

## 7. HƯỚNG DẪN TÍCH HỢP KHI MERGE VỚI CÁC NHÁNH KHÁC

Để các thành viên phụ trách các nhánh khác dễ dàng merge mà không gặp xung đột, dưới đây là các điểm cần lưu ý:

### 7.1. Tích hợp với Nhánh Xác thực & Phân quyền (Authentication / Security)
- **Hiện trạng trên nhánh logistics**:
  - `SecurityConfig.java` đang cấu hình `requestMatchers("/**").permitAll()` và tắt CSRF để thuận tiện cho việc phát triển và kiểm thử API cục bộ.
  - Các màn hình tài xế đang tạm thời lấy định danh tài xế qua `localStorage.getItem('ute_current_driver') || 'TX001'`.
- **Khi gộp với nhánh Auth**:
  1. Giữ nguyên các định nghĩa API của logistics và áp dụng phân quyền theo Role:
     - `GET/POST/PUT/DELETE /api/dieu-phoi/**` ➔ Yêu cầu quyền `ROLE_DIEU_PHOI` hoặc `ROLE_ADMIN`.
     - `GET/POST /api/tai-xe/**` ➔ Yêu cầu quyền `ROLE_TAI_XE`.
  2. Tại `TaiXeController.java`: Thay thế việc truyền `maTaiXe` từ Path/Param bằng cách trích xuất trực tiếp từ Spring Security:
     ```java
     @AuthenticationPrincipal CustomUserDetails userDetails
     ```
  3. Tại các file HTML (`15-dashboard.html`, `16-trip-detail.html`, `17-delivery-update.html`): Bỏ fallback `TX001`, đọc `user` và `token` từ Auth session / JWT đã lưu trong `localStorage` khi đăng nhập.

### 7.2. Tích hợp với Nhánh Quản lý Đơn hàng (Order Management)
- **Quy trình kết nối**:
  1. Khi nhánh Order duyệt đơn và hoàn tất đóng gói, đơn hàng sẽ tạo ra một hoặc nhiều `KienHang` với trạng thái `CHO_DIEU_PHOI` hoặc `DA_NHAP_KHO`.
  2. Bảng `kienhang` đã có sẵn các trường liên kết: `madonhang`, `makho_hientai`, `makho_tieptheo`, `diachi_giao`, `khoiluong_kg`, `thetich_m3`.
  3. Màn hình Điều phối của Logistics sẽ tự động truy vấn danh sách kiện này qua API `GET /api/dieu-phoi/don-can-dieu-phoi`.

### 7.3. Tích hợp với Nhánh Quản lý Kho (Warehouse)
- **Quy trình luân chuyển**:
  1. Khi chuyến xe liên kho xuất phát (`sp_xuat_phat_chuyen_giao`), kiện hàng chuyển sang `DANG_VAN_CHUYEN`.
  2. Khi chuyến xe đến kho đích (`POST /api/tai-xe/chuyen-giao/{maChuyen}/den-kho-dich`), hệ thống logistics chuyển trạng thái chuyến thành `HOAN_THANH`, và các kiện hàng chuyển thành `DA_DEN_KHO_DICH`.
  3. Phân hệ Quản lý Kho có thể lắng nghe sự kiện này hoặc truy vấn theo `makho_hientai` để nhân viên kho tiến hành thủ tục quét mã nhập kho (Inbound).

### 7.4. Hướng dẫn giải quyết xung đột khi Git Merge
Khi thực hiện lệnh:
```bash
git checkout main
git merge feature/logistics
```
Nếu xuất hiện Conflict, xử lý theo hướng dẫn sau:
1. **`application.properties`**: Giữ cấu hình PostgreSQL của dự án chung, đảm bảo có thông số kết nối đúng tới CSDL `ute_express`.
2. **`SecurityConfig.java`**: Giữ lại các SecurityFilterChain của nhánh Auth nhưng bổ sung matcher cho các đường dẫn `/api/dieu-phoi/**` và `/api/tai-xe/**`.
3. **`database/schema.sql` & `database/logic.sql`**: Hợp nhất các bảng và Stored Procedures mới của logistics, không xóa các bảng `donhang`, `khachhang` của các phân hệ khác.

---

## 8. CHECKLIST KIỂM THỬ SAU KHI MERGE (VERIFICATION CHECKLIST)

Sau khi merge, chạy các bước kiểm tra sau để đảm bảo hệ thống vận hành thông suốt:

- [ ] **1. Compile Project**: Chạy `mvn clean compile` thành công không có lỗi biên dịch.
- [ ] **2. Database Migration**: Chạy đầy đủ 4 script SQL (`schema.sql` ➔ `logic.sql` ➔ `seed_and_test.sql` ➔ `seed_extra.sql`).
- [ ] **3. Chạy Server**: Khởi động Spring Boot Application trên port `8080`.
- [ ] **4. Test Luồng Điều phối**:
  - Truy cập `http://localhost:8080/dispatcher/12-dashboard.html`: Kiểm tra các thẻ KPI hiển thị số liệu thực từ database.
  - Truy cập `http://localhost:8080/dispatcher/13-routes.html`: Kiểm tra thêm/sửa/xóa tuyến đường.
  - Truy cập `http://localhost:8080/dispatcher/14-trips.html`: Thử tạo 1 chuyến liên kho (`KHO01` ➔ `KHO03`) và gán kiện hàng. Xác nhận hệ thống chỉ cho phép gán kiện có đích đến là `KHO03`.
- [ ] **5. Test Luồng Tài xế**:
  - Truy cập `http://localhost:8080/driver/15-dashboard.html`: Kiểm tra danh sách chuyến phân công cho tài xế `TX001`.
  - Truy cập `http://localhost:8080/driver/16-trip-detail.html?id=CG001`: Thử thao tác **Nhận chuyến** và **Xuất phát**.
  - Truy cập `http://localhost:8080/driver/17-delivery-update.html?id=CG002`: Thử quét kiện và ghi nhận **Giao thành công** hoặc **Giao thất bại**, kiểm tra dữ liệu cập nhật chính xác trong database.

---
*Tài liệu được soạn thảo bởi thành viên phụ trách nhánh `feature/logistics`.*
