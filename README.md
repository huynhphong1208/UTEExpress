# 🚀 UTE EXPRESS — HỆ THỐNG QUẢN LÝ VẬN CHUYỂN & GIAO NHẬN LOGISTICS

> **Phiên bản tích hợp**: Spring Boot 4.x + Spring Security + JWT + PostgreSQL + Thymeleaf  
> **Trạng thái**: Đã hợp nhất đầy đủ tất cả các phân hệ: **Xác thực (Auth)**, **Bưu cục (Post Office)**, **Tài chính & Thanh toán**, **Điều phối (Dispatcher)**, **Tài xế (Driver)**, và **Quản trị hệ thống (Admin)**.

---

## 🔑 1. DANH SÁCH TÀI KHOẢN & MẬT KHẨU ĐĂNG NHẬP THEO TỪNG ROLE

> 💡 **Mật khẩu dùng chung cho tất cả các tài khoản thử nghiệm**: `123456`  
> *(Toàn bộ mật khẩu đã được mã hóa bằng chuẩn BCrypt trong cơ sở dữ liệu PostgreSQL).*

### 1.1. Quản trị viên (Admin)
| STT | Vai trò | Tên đăng nhập | Mật khẩu | Họ và tên | Chức vụ / Đơn vị | Trang chuyển hướng |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- |
| **1** | `ADMIN` | `admin` | `123456` | Quản trị viên hệ thống | Ban Quản trị UTE Express | `/admin/dashboard` |

### 1.2. Nhân viên Bưu cục (NVBC) — 10 Bưu cục toàn quốc
| STT | Vai trò | Tên đăng nhập | Mật khẩu | Họ và tên | Bưu cục phụ trách | Đơn mới tạo (`TT01`) chờ tiếp nhận |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- |
| **1** | `NVBC` | **`nvbc01`** | `123456` | Phạm Hoàng Nam | Bưu cục Thủ Đức (`KHO01`) | **7 đơn mới** (`DH000001`, `DH000022` ➔ `DH000027`)<br>• *Chuyển khoản (Đã thanh toán)*: DH01, DH23, DH24, DH26<br>• *Tiền mặt/COD (Chưa thanh toán)*: DH22, DH25, DH27 |
| **2** | `NVBC` | **`nvbc02`** | `123456` | Nguyễn Văn Cường | Bưu cục Cầu Giấy (`KHO02`) | **7 đơn mới** (`DH000018`, `DH000028` ➔ `DH000033`)<br>• *Chuyển khoản (Đã thanh toán)*: DH18, DH28, DH30, DH31<br>• *Tiền mặt/COD (Chưa thanh toán)*: DH29, DH32, DH33 |
| **3** | `NVBC` | **`nvbc03`** | `123456` | Võ Thị Lan | Bưu cục Đà Nẵng (`KHO03`) | **7 đơn mới** (`DH000019`, `DH000034` ➔ `DH000039`)<br>• *Chuyển khoản (Đã thanh toán)*: DH19, DH34, DH36, DH38<br>• *Tiền mặt/COD (Chưa thanh toán)*: DH35, DH37, DH39 |
| **4** | `NVBC` | `nvbc04` | `123456` | Trần Văn Hùng | Bưu cục Cần Thơ (`KHO04`) | Sẵn sàng tiếp nhận (`DH000020` - Đã thanh toán CK) |
| **5** | `NVBC` | `nvbc05` | `123456` | Lê Thị Mai | Bưu cục Hải Phòng (`KHO05`) | Sẵn sàng tiếp nhận (`DH000021` - Chưa thanh toán COD) |
| **6** | `NVBC` | `nvbc06` | `123456` | Phan Thanh Bình | Bưu cục Biên Hòa (`KHO06`) | Sẵn sàng hoạt động |
| **7** | `NVBC` | `nvbc07` | `123456` | Đỗ Hoàng Yến | Bưu cục Nha Trang (`KHO07`) | Sẵn sàng hoạt động |
| **8** | `NVBC` | `nvbc08` | `123456` | Bùi Quang Sáng | Bưu cục Buôn Ma Thuột (`KHO08`) | Sẵn sàng hoạt động |
| **9** | `NVBC` | `nvbc09` | `123456` | Ngô Bảo Châu | Bưu cục Huế (`KHO09`) | Sẵn sàng hoạt động |
| **10** | `NVBC` | `nvbc10` | `123456` | Trịnh Kim Chi | Bưu cục Vũng Tàu (`KHO10`) | Sẵn sàng hoạt động |

> 📌 *Trang Dashboard cho toàn bộ NVBC*: `/post-office/dashboard`. Khi đăng nhập bằng tài khoản của bưu cục nào, hệ thống sẽ lọc chính xác đơn hàng và kiện hàng thuộc riêng bưu cục đó. Các đơn hàng có phương thức **Chuyển khoản** hiển thị huy hiệu xanh **Đã thanh toán**, trong khi các đơn **Tiền mặt / COD** hiển thị **Chưa thanh toán (Thu tiền mặt khi giao)**.

### 1.3. Nhân viên Điều phối (NVDP)
| STT | Vai trò | Tên đăng nhập | Mật khẩu | Họ và tên | Chức vụ / Đơn vị | Trang chuyển hướng |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- |
| **1** | `NVDP` | `nvdp01` | `123456` | Đặng Quốc Huy | Trung tâm Điều phối Miền Nam (`KHO01`) | `/dispatcher/dashboard` |
| **2** | `NVDP` | `nvdp02` | `123456` | Hoàng Minh Quân | Trung tâm Điều phối Miền Bắc (`KHO02`) | `/dispatcher/dashboard` |

### 1.4. Tài xế Giao nhận (TAI_XE)
| STT | Vai trò | Tên đăng nhập | Mật khẩu | Họ và tên | Bằng lái | Phụ trách phương tiện | Trang chuyển hướng |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| **1** | `TAI_XE` | `tx01` | `123456` | Huỳnh Văn Tài | Hạng C | Xe tải 51C-123.45 (`PT001`, tải 5 tấn) | `/driver/dashboard` |
| **2** | `TAI_XE` | `tx02` | `123456` | Ngô Minh Đức | Hạng E | Xe tải 29B-678.90 (`PT002`, tải 8 tấn) | `/driver/dashboard` |
| **3** | `TAI_XE` | `tx03` | `123456` | Lê Hoàng Long | Hạng C | Xe tải 65C-345.67 (`PT005`, tải 5 tấn) | `/driver/dashboard` |
| **4** | `TAI_XE` | `tx04` | `123456` | Trần Quốc Toản | Hạng A2 | Xe máy giao hàng (`PT007`, tải 40 kg) | `/driver/dashboard` |
| **5** | `TAI_XE` | `tx05` | `123456` | Nguyễn Văn Dũng | Hạng C | Xe tải 43C-890.12 (`PT004`, tải 6 tấn) | `/driver/dashboard` |

### 1.5. Khách hàng (KHACH_HANG)
| STT | Vai trò | Tên đăng nhập | Mật khẩu | Họ và tên | Khu vực | Trang chuyển hướng |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- |
| **1** | `KHACH_HANG` | `0901000001` | `123456` | Nguyễn Văn An | Khách hàng cá nhân (TP.HCM) | `/customer/dashboard` |
| **2** | `KHACH_HANG` | `0901000002` | `123456` | Trần Thị Bích | Khách hàng cá nhân (Hà Nội) | `/customer/dashboard` |

---

## 🛡️ 2. KIẾN TRÚC BẢO MẬT & XÁC THỰC (SECURITY)

Hệ thống được bảo vệ bằng mô hình **Stateless JWT Authentication** kết hợp **Role-Based Access Control (RBAC)** của Spring Security:

1. **Cơ chế Đăng nhập (`POST /api/auth/login`)**:
   - Client gửi `tenDangNhap` và `matKhau`.
   - `CustomUserDetailsService` tải thông tin người dùng từ bảng `nguoi_dung`, xác thực mật khẩu qua `BCryptPasswordEncoder`.
   - Nếu tài khoản có trạng thái `"Bị khóa"`, hệ thống từ chối đăng nhập (`LockedException`).
   - Nếu xác thực thành công, hệ thống phát sinh chuỗi **JWT Token** (thuật toán HS256/HS512) chứa Subject, Roles và thời hạn hiệu lực.
   - Frontend lưu trữ token vào `localStorage` (`accessToken` & `token`) và tự động chuyển hướng người dùng đến đúng trang Dashboard tương ứng với vai trò của họ.

2. **Bảo vệ REST API theo Role**:
   - `JwtAuthFilter` tự động trích xuất token từ Header `Authorization: Bearer <token>`, giải mã và gán quyền (`SecurityContextHolder`) cho từng request.
   - Các API được phân quyền nghiêm ngặt:
     - `/api/admin/**`, `/api/v1/admin/**`: Chỉ dành cho `ADMIN`.
     - `/api/v1/post-office/**`, `/api/v1/payments/**`: Dành cho `NVBC` và `ADMIN`.
     - `/api/dieu-phoi/**`: Dành cho `NVDP` và `ADMIN`.
     - `/api/tai-xe/**`: Dành cho `TAI_XE` và `ADMIN`.
     - `/api/khach-hang/**`, `/api/don-hang/**`: Dành cho `KHACH_HANG` và `ADMIN`.
     - `/api/auth/**`, `/api/tra-cuu/**`: Công khai (`permitAll`).

---

## 🗄️ 3. HƯỚNG DẪN KHỞI TẠO & CHẠY LẠI CƠ SỞ DỮ LIỆU POSTGRESQL

Cơ sở dữ liệu: `ute_express` trên cổng `5432`.  
Toàn bộ mã nguồn database đã được gộp gọn thành **3 file duy nhất** theo đúng thứ tự luồng phụ thuộc:

```
database/
├── 1. schema.sql          # Khởi tạo bảng, khóa chính, khóa ngoại, sequence, ràng buộc
├── 2. logic.sql           # Stored Procedures, Functions tính cước, Trigger nghiệp vụ & View
└── 3. seed_and_test.sql   # Dữ liệu danh mục, 10 Bưu cục, 20 Tuyến, 12 Nhân viên, 5 Tài xế, 
                           # 7 Phương tiện, Bộ Test Suite tự động (38 test cases) 
                           # và Dữ liệu mẫu toàn diện (39 Đơn hàng, 41 Kiện hàng, 8 Chuyến giao,
                           # 5 Giao dịch thanh toán, 37 Lịch sử trạng thái truy vết).
```

### Lệnh chạy nhanh toàn bộ Database qua terminal PowerShell:

```powershell
$env:PGPASSWORD='123456'
$PSQL='C:\Program Files\PostgreSQL\18\bin\psql.exe'

# Xóa và tạo mới schema sạch sẽ (tùy chọn khi cần reset hoàn toàn)
& $PSQL -h localhost -U postgres -d ute_express -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Thực thi lần lượt 3 file theo thứ tự chuẩn:
& $PSQL -h localhost -U postgres -d ute_express -f database/schema.sql
& $PSQL -h localhost -U postgres -d ute_express -f database/logic.sql
& $PSQL -h localhost -U postgres -d ute_express -f database/seed_and_test.sql
```

> ✅ Sau khi nạp xong:
> - **NVBC 1** (`KHO01` - Bưu cục Thủ Đức): Có sẵn **7 đơn hàng mới tạo** (`TT01`) chờ tiếp nhận.
> - **NVBC 2** (`KHO02` - Bưu cục Cầu Giấy): Có sẵn **7 đơn hàng mới tạo** (`TT01`) chờ tiếp nhận.
> - **NVBC 3** (`KHO03` - Bưu cục Đà Nẵng): Có sẵn **7 đơn hàng mới tạo** (`TT01`) chờ tiếp nhận.
> - Nhân viên điều phối có sẵn chuyến liên kho và chuyến giao cuối để theo dõi & lọc theo kho nhận.

---

## 💻 4. HƯỚNG DẪN KHỞI CHẠY ỨNG DỤNG BACKEND

### Yêu cầu môi trường:
- **Java**: OpenJDK 17 trở lên (đã kiểm thử tương thích Java 17, 21, 25).
- **Maven**: Tích hợp sẵn qua Maven Wrapper (`./mvnw`).
- **PostgreSQL**: Đang chạy tại `localhost:5432` với Database `ute_express`.

### Kiểm tra Unit Test & Build:
```powershell
./mvnw test
```

### Khởi động ứng dụng:
```powershell
./mvnw spring-boot:run
```
Ứng dụng sẽ khởi chạy tại cổng: `http://localhost:8080`.

---

## 🌐 5. DANH MỤC TRANG WEB & TÀI LIỆU API

### 5.1. Trang Giao diện Web (Thymeleaf Views):
- **Trang chủ công khai**: [http://localhost:8080/](http://localhost:8080/)
- **Đăng nhập**: [http://localhost:8080/login](http://localhost:8080/login)
- **Đăng ký tài khoản**: [http://localhost:8080/register](http://localhost:8080/register)
- **Tra cứu vận đơn**: [http://localhost:8080/tracking](http://localhost:8080/tracking)
- **Khách hàng**:
  - Dashboard: `/customer/dashboard`
  - Tạo đơn hàng: `/customer/create-order`
  - Chi tiết đơn: `/customer/order-detail`
  - Thanh toán: `/customer/payment`
- **Nhân viên Bưu cục**:
  - Dashboard bưu cục: `/post-office/dashboard`
  - Quản lý kiện hàng tại kho: `/post-office/packages`
  - Cập nhật trạng thái: `/post-office/update-status`
- **Nhân viên Điều phối**:
  - Dashboard điều phối: `/dispatcher/dashboard`
  - Quản lý tuyến vận chuyển: `/dispatcher/13-routes.html`
  - Quản lý chuyến giao & tải trọng: `/dispatcher/14-trips.html`
- **Tài xế**:
  - Dashboard tài xế: `/driver/dashboard`
  - Chi tiết chuyến giao: `/driver/16-trip-detail.html`
  - Quét kiện & Kết quả giao hàng: `/driver/17-delivery-update.html`
- **Quản trị viên (Admin)**:
  - Dashboard tổng quan: `/admin/dashboard`
  - Quản lý người dùng: `/admin/users`
  - Quản lý kho / bưu cục: `/admin/warehouses`
  - Quản lý phương tiện: `/admin/vehicles`
  - Giám sát đơn hàng: `/admin/orders`
  - Doanh thu & COD: `/admin/payments`
  - Báo cáo & Thống kê: `/admin/reports`
  - Cấu hình hệ thống: `/admin/settings`

### 5.2. Tài liệu API Swagger:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
