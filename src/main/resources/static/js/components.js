/**
 * UTE EXPRESS LOGISTICS - INTERACTIVE COMPONENTS & PROTOTYPE LOGIC
 */

// 1. MODAL CONTROLS
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }
}

// Close when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('show');
        document.body.style.overflow = '';
    }
});

// 2. TOAST NOTIFICATION SYSTEM
function showToast(title, message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div style="flex: 1;">
            <div class="toast-title">${title}</div>
            <div class="toast-msg">${message}</div>
        </div>
        <button style="background:none;border:none;cursor:pointer;color:#94A3B8;font-size:16px;" onclick="this.parentElement.remove()">✕</button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// 3. TÍNH CƯỚC PHÍ VẬN CHUYỂN TỰ ĐỘNG
function calculateShippingFee(weightKg, lengthCm = 10, widthCm = 10, heightCm = 10, distanceKm = 10) {
    const volWeight = (lengthCm * widthCm * heightCm) / 5000;
    const chargeWeight = Math.max(parseFloat(weightKg) || 0, volWeight);
    
    let basePrice = 15000;
    if (chargeWeight > 1) {
        basePrice += Math.ceil(chargeWeight - 1) * 5000;
    }
    if (distanceKm > 10) {
        basePrice += Math.ceil(distanceKm - 10) * 1000;
    }
    return basePrice;
}

// 4. RENDER STATUS BADGE HELPER
function getStatusBadge(status) {
    switch (status) {
        case 'Giao thành công':
        case 'Hoạt động':
        case 'Đã thanh toán':
        case 'Sẵn sàng':
            return `<span class="badge badge-success"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Đang vận chuyển':
        case 'Đang xử lý':
        case 'Đang chạy':
            return `<span class="badge badge-info"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Mới tạo':
        case 'Đã tiếp nhận':
        case 'Đến kho':
        case 'Đang giao':
        case 'Chờ tiếp nhận':
        case 'Chưa thanh toán':
            return `<span class="badge badge-warning"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Hủy':
        case 'Trả hàng':
        case 'Bị khóa':
        case 'Thất bại':
        case 'Bảo trì':
            return `<span class="badge badge-danger"><span class="badge-dot"></span>${status}</span>`;
            
        default:
            return `<span class="badge badge-gray"><span class="badge-dot"></span>${status}</span>`;
    }
}

// 5. PROTOTYPE TOOLBAR INJECTOR (Tự động thích ứng cả chạy qua Server localhost và mở file trực tiếp)
document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('prototype-toolbar')) {
        const isHttp = window.location.protocol.startsWith('http');
        
        // Helper chuyển route linh hoạt
        window.navProto = function(serverRoute, fileRelativeRoute) {
            if (isHttp) {
                window.location.href = serverRoute;
            } else {
                window.location.href = fileRelativeRoute;
            }
        };

        const tb = document.createElement('div');
        tb.id = 'prototype-toolbar';
        tb.className = 'prototype-toolbar';
        tb.innerHTML = `
            <div class="proto-label">UTE Express Prototype</div>
            <select class="proto-select" id="roleNavigator" onchange="if(this.value) window.location.href=this.value">
                <option value="">-- Chọn Frame / Vai trò xem nhanh --</option>
                <optgroup label="Hệ thống & Design System">
                    <option value="${isHttp ? '/design-system' : '../design-system.html'}">00. Design System & Component Showcase</option>
                </optgroup>
                <optgroup label="1. Guest (Khách chưa đăng nhập)">
                    <option value="${isHttp ? '/' : '../guest/03-index.html'}">03. Trang chủ Public</option>
                    <option value="${isHttp ? '/tracking' : '../guest/04-tracking.html'}">04. Tra cứu đơn hàng</option>
                    <option value="${isHttp ? '/register' : '../guest/01-register.html'}">01. Đăng ký tài khoản</option>
                    <option value="${isHttp ? '/login' : '../guest/02-login.html'}">02. Đăng nhập hệ thống</option>
                </optgroup>
                <optgroup label="2. Khách hàng">
                    <option value="${isHttp ? '/customer/dashboard' : '../customer/05-dashboard.html'}">05. Dashboard Khách hàng</option>
                    <option value="${isHttp ? '/customer/create-order' : '../customer/06-create-order.html'}">06. Tạo đơn hàng (Tính cước tự động)</option>
                    <option value="${isHttp ? '/customer/order-detail' : '../customer/07-order-detail.html'}">07. Chi tiết đơn & Timeline</option>
                    <option value="${isHttp ? '/customer/payment' : '../customer/08-payment.html'}">08. Thanh toán</option>
                </optgroup>
                <optgroup label="3. Nhân viên Bưu cục">
                    <option value="${isHttp ? '/post-office/dashboard' : '../post-office/09-dashboard.html'}">09. Dashboard NV Bưu cục</option>
                    <option value="${isHttp ? '/post-office/packages' : '../post-office/10-packages.html'}">10. Quản lý kiện hàng tại kho</option>
                    <option value="${isHttp ? '/post-office/update-status' : '../post-office/11-update-status.html'}">11. Cập nhật trạng thái chuẩn luồng</option>
                </optgroup>
                <optgroup label="4. Nhân viên Điều phối">
                    <option value="${isHttp ? '/dispatcher/dashboard' : '../dispatcher/12-dashboard.html'}">12. Dashboard Điều phối</option>
                    <option value="${isHttp ? '/dispatcher/routes' : '../dispatcher/13-routes.html'}">13. Quản lý tuyến vận chuyển</option>
                    <option value="${isHttp ? '/dispatcher/trips' : '../dispatcher/14-trips.html'}">14. Quản lý chuyến giao & tải trọng</option>
                </optgroup>
                <optgroup label="5. Tài xế">
                    <option value="${isHttp ? '/driver/dashboard' : '../driver/15-dashboard.html'}">15. Dashboard Tài xế</option>
                    <option value="${isHttp ? '/driver/trip-detail' : '../driver/16-trip-detail.html'}">16. Chi tiết chuyến giao</option>
                    <option value="${isHttp ? '/driver/delivery-update' : '../driver/17-delivery-update.html'}">17. Quét kiện & Cập nhật giao</option>
                </optgroup>
                <optgroup label="6. Quản trị viên (Admin)">
                    <option value="${isHttp ? '/admin/dashboard' : '../admin/18-dashboard.html'}">18. Admin Dashboard & Chỉ số</option>
                    <option value="${isHttp ? '/admin/users' : '../admin/19-users.html'}">19. Quản lý người dùng</option>
                    <option value="${isHttp ? '/admin/warehouses' : '../admin/24-warehouses.html'}">24. Quản lý kho / bưu cục</option>
                    <option value="${isHttp ? '/admin/vehicles' : '../admin/25-vehicles.html'}">25. Quản lý phương tiện</option>
                    <option value="${isHttp ? '/admin/orders' : '../admin/27-orders.html'}">27. Giám sát đơn hàng</option>
                    <option value="${isHttp ? '/admin/payments' : '../admin/28-payments.html'}">28. Doanh thu & COD</option>
                    <option value="${isHttp ? '/admin/reports' : '../admin/29-reports.html'}">29. Báo cáo & Thống kê</option>
                    <option value="${isHttp ? '/admin/settings' : '../admin/30-settings.html'}">30. Quản trị hệ thống & Cấu hình</option>
                </optgroup>
            </select>
            <a href="${isHttp ? '/design-system' : '../design-system.html'}" class="proto-btn">Design System</a>
        `;
        document.body.appendChild(tb);
    }
});
