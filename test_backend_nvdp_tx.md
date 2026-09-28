# 🧪 Test Backend API — NV Điều phối & Tài xế

> **Yêu cầu**: App đang chạy tại `http://localhost:8080`, database `ute_express` đã chạy `seed_and_test.sql` (có dữ liệu mẫu).

## Dữ liệu mẫu có sẵn

| Bảng | Mã | Ghi chú |
|------|----|---------|
| Kho | `KHO01` (Thủ Đức), `KHO02` (Cầu Giấy), `KHO03` (Đà Nẵng), `KHO04` (Cần Thơ) | |
| Tuyến | `TU01` (KHO01→KHO03, 960km/18h), `TU04` (KHO01→KHO04, 170km/4h) | |
| NV Điều phối | `NV003` (Đặng Quốc Huy), tài khoản `ND004` | |
| Tài xế | `TX001` (Huỳnh Văn Tài, ND005), `TX002` (Ngô Minh Đức, ND006) | |
| Phương tiện | `PT001` (1500kg), `PT002` (5000kg), `PT003` (30kg xe máy) | |

---

## Cách chạy

Mở **Postman** hoặc dùng **PowerShell**. Dưới đây dùng `curl` (nếu Windows không có curl, thay bằng lệnh PowerShell ở cuối file).

---

## 📋 PHẦN 1: NV ĐIỀU PHỐI (`/api/dieu-phoi`)

### 1.1 — Lấy danh sách tuyến vận chuyển
```bash
curl -s http://localhost:8080/api/dieu-phoi/tuyen
```
✅ **Mong đợi**: `success: true`, `data` chứa 5 tuyến (TU01–TU05)

---

### 1.2 — Lấy chi tiết 1 tuyến
```bash
curl -s http://localhost:8080/api/dieu-phoi/tuyen/TU01
```
✅ **Mong đợi**: `maKhoDi: "KHO01"`, `maKhoDen: "KHO03"`, `khoangCach: 960`

---

### 1.3 — Tạo tuyến mới
```bash
curl -s -X POST http://localhost:8080/api/dieu-phoi/tuyen ^
  -H "Content-Type: application/json" ^
  -d "{\"maTuyen\":\"TU_TEST\",\"maKhoDi\":\"KHO02\",\"maKhoDen\":\"KHO04\",\"khoangCach\":500,\"thoiGianDuKienGio\":10}"
```
✅ **Mong đợi**: `success: true`, message "Tạo tuyến vận chuyển thành công"

---

### 1.4 — Cập nhật tuyến
```bash
curl -s -X PUT http://localhost:8080/api/dieu-phoi/tuyen/TU_TEST ^
  -H "Content-Type: application/json" ^
  -d "{\"maKhoDi\":\"KHO02\",\"maKhoDen\":\"KHO04\",\"khoangCach\":550,\"thoiGianDuKienGio\":11}"
```
✅ **Mong đợi**: `khoangCach` cập nhật thành `550`

---

### 1.5 — Xóa tuyến
```bash
curl -s -X DELETE http://localhost:8080/api/dieu-phoi/tuyen/TU_TEST
```
✅ **Mong đợi**: `success: true`, message "Xóa tuyến vận chuyển thành công"

---

### 1.6 — Tạo chuyến giao LIÊN KHO (HCM → Đà Nẵng)
```bash
curl -s -X POST http://localhost:8080/api/dieu-phoi/chuyen-giao ^
  -H "Content-Type: application/json" ^
  -d "{\"loai\":\"LIEN_KHO\",\"maTuyen\":\"TU01\",\"maKhoGiao\":null,\"maTx\":\"TX001\",\"maPt\":\"PT001\",\"ngayXuatPhat\":\"2026-10-01T08:00:00\",\"maNvDp\":\"NV003\",\"maNd\":\"ND004\",\"ngayDenDuKien\":null}"
```
✅ **Mong đợi**: `success: true`, message "Tạo chuyến giao thành công"

> **Lưu ý**: `ngayDenDuKien` = null → DB tự tính = xuất phát + 18h (theo tuyến TU01)

---

### 1.7 — Tạo chuyến giao GIAO CUỐI (từ kho Đà Nẵng)
```bash
curl -s -X POST http://localhost:8080/api/dieu-phoi/chuyen-giao ^
  -H "Content-Type: application/json" ^
  -d "{\"loai\":\"GIAO_CUOI\",\"maTuyen\":null,\"maKhoGiao\":\"KHO03\",\"maTx\":\"TX002\",\"maPt\":\"PT002\",\"ngayXuatPhat\":\"2026-10-02T08:00:00\",\"maNvDp\":\"NV003\",\"maNd\":\"ND004\",\"ngayDenDuKien\":\"2026-10-02T12:00:00\"}"
```
✅ **Mong đợi**: `success: true`

---

### 1.8 — Gán kiện vào chuyến *(cần có mã chuyến + mã kiện)*

> Trước tiên, query DB lấy `ma_chuyen` và `ma_kien` vừa tạo, hoặc dùng mã có sẵn từ seed test.

```bash
curl -s -X POST http://localhost:8080/api/dieu-phoi/chuyen-giao/gan-kien ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000001\",\"maKien\":\"KI0000001\",\"maNd\":\"ND004\"}"
```
✅ **Mong đợi**: `success: true` hoặc lỗi từ DB (kiện không ở đúng kho, vượt tải trọng...)

---

### 1.9 — Tra cứu chuyến giao của NV điều phối
```bash
curl -s "http://localhost:8080/api/dieu-phoi/chuyen-giao?maNvDieuPhoi=NV003"
```
✅ **Mong đợi**: danh sách các chuyến do NV003 tạo

---

### 1.10 — Xem chi tiết kiện trong chuyến
```bash
curl -s http://localhost:8080/api/dieu-phoi/chuyen-giao/CG000001/chi-tiet
```
✅ **Mong đợi**: danh sách `ChiTietChuyenGiao` (các kiện đã gán)

---

## 🚛 PHẦN 2: TÀI XẾ (`/api/tai-xe`)

### 2.1 — Xem danh sách chuyến giao của tài xế
```bash
curl -s "http://localhost:8080/api/tai-xe/chuyen-giao?maTx=TX001"
```
✅ **Mong đợi**: danh sách chuyến của TX001

---

### 2.2 — Lọc chuyến theo trạng thái
```bash
curl -s "http://localhost:8080/api/tai-xe/chuyen-giao?maTx=TX001&trangThai=Chưa đi"
```
✅ **Mong đợi**: chỉ chuyến có `trangThai = "Chưa đi"`

---

### 2.3 — Xem chi tiết kiện trong chuyến
```bash
curl -s http://localhost:8080/api/tai-xe/chuyen-giao/CG000001/chi-tiet
```
✅ **Mong đợi**: danh sách kiện với trạng thái `Đã gán` / `Đã quét`

---

### 2.4 — Quét kiện lên xe
```bash
curl -s -X POST http://localhost:8080/api/tai-xe/quet-kien ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000001\",\"maKien\":\"KI0000001\",\"maNd\":\"ND005\",\"ghiChu\":\"Kiện nguyên vẹn\"}"
```
✅ **Mong đợi**: `success: true`, hoặc lỗi nếu tài xế không phải của chuyến này

---

### 2.5 — Xuất phát chuyến
```bash
curl -s -X POST http://localhost:8080/api/tai-xe/xuat-phat ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000001\",\"maNd\":\"ND005\"}"
```
✅ **Mong đợi**: `success: true`
❌ **Lỗi nếu**: còn kiện chưa quét → `"Còn X kiện chưa được quét lên xe"`

---

### 2.6 — Giao thành công
```bash
curl -s -X POST http://localhost:8080/api/tai-xe/giao-thanh-cong ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000002\",\"maKien\":\"KI0000001\",\"maNd\":\"ND006\",\"ghiChu\":\"Khách đã nhận\",\"nguoiThu\":\"Lê Văn Cường\"}"
```
✅ **Mong đợi**: `success: true` — trigger tự thu COD và đổi trạng thái đơn nếu giao đủ kiện

---

### 2.7 — Giao thất bại
```bash
curl -s -X POST http://localhost:8080/api/tai-xe/giao-that-bai ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000002\",\"maKien\":\"KI0000002\",\"maNd\":\"ND006\",\"lyDo\":\"Khách không nghe máy, hẹn giao lại\"}"
```
✅ **Mong đợi**: `success: true`
❌ **Lỗi nếu**: `lyDo` rỗng → `"Phải nhập lý do giao không thành công"`

---

## ❌ PHẦN 3: TEST ERROR HANDLING

### 3.1 — Tuyến không tồn tại
```bash
curl -s http://localhost:8080/api/dieu-phoi/tuyen/KHONG_CO
```
✅ **Mong đợi**: `400`, `{"success": false, "message": "Không tìm thấy tuyến vận chuyển: KHONG_CO"}`

---

### 3.2 — Tài xế trùng lịch (tạo 2 chuyến cùng tài xế cùng giờ)
Tạo chuyến thứ 2 cho TX001 trùng giờ với chuyến ở bước 1.6:
```bash
curl -s -X POST http://localhost:8080/api/dieu-phoi/chuyen-giao ^
  -H "Content-Type: application/json" ^
  -d "{\"loai\":\"LIEN_KHO\",\"maTuyen\":\"TU04\",\"maTx\":\"TX001\",\"maPt\":\"PT002\",\"ngayXuatPhat\":\"2026-10-01T09:00:00\",\"maNvDp\":\"NV003\",\"maNd\":\"ND004\"}"
```
✅ **Mong đợi**: `400`, message `"Tài xế đã được phân công chuyến khác trùng thời gian"`

---

### 3.3 — Giao thất bại không có lý do
```bash
curl -s -X POST http://localhost:8080/api/tai-xe/giao-that-bai ^
  -H "Content-Type: application/json" ^
  -d "{\"maChuyen\":\"CG000002\",\"maKien\":\"KI0000001\",\"maNd\":\"ND006\",\"lyDo\":\"\"}"
```
✅ **Mong đợi**: `400`, message `"Phải nhập lý do giao không thành công"`

---

## 💡 Lệnh PowerShell tương đương (nếu không có curl)

Thay mỗi lệnh `curl` bằng:

```powershell
# GET
Invoke-WebRequest -Uri 'http://localhost:8080/api/dieu-phoi/tuyen' -UseBasicParsing | Select-Object -ExpandProperty Content

# POST
Invoke-WebRequest -Uri 'http://localhost:8080/api/dieu-phoi/chuyen-giao' `
  -Method POST `
  -ContentType 'application/json' `
  -Body '{"loai":"LIEN_KHO","maTuyen":"TU01","maTx":"TX001","maPt":"PT001","ngayXuatPhat":"2026-10-01T08:00:00","maNvDp":"NV003","maNd":"ND004"}' `
  -UseBasicParsing | Select-Object -ExpandProperty Content

# DELETE
Invoke-WebRequest -Uri 'http://localhost:8080/api/dieu-phoi/tuyen/TU_TEST' `
  -Method DELETE -UseBasicParsing | Select-Object -ExpandProperty Content
```

---

## 📌 Thứ tự test khuyến nghị

> [!TIP]
> Nên chạy theo flow nghiệp vụ để dữ liệu liên kết đúng:
>
> **1.1** → **1.2** → **1.3** → **1.4** → **1.5** (CRUD tuyến) →
> **1.6** (tạo chuyến liên kho) → **1.9** (tra cứu thấy chuyến vừa tạo) →
> **1.8** (gán kiện vào chuyến) → **1.10** (xem kiện đã gán) →
> **2.1** (TX xem chuyến) → **2.4** (quét kiện) → **2.5** (xuất phát) →
> **2.6** / **2.7** (giao hàng) →
> **3.1** → **3.2** → **3.3** (test lỗi)

> [!IMPORTANT]
> Mã chuyến (`CG000001`, `CG000002`...) và mã kiện (`KI0000001`...) phụ thuộc vào thứ tự tạo trong DB. Hãy kiểm tra mã thực tế bằng cách gọi API tra cứu trước (bước 1.9, 2.1) hoặc query trực tiếp trong DBeaver.
